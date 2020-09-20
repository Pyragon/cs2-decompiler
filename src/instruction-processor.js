const Printer = require('./util/printer');
const _instructions = require('./util/instructions');

const process = function(script) {

    let printer = new Printer();

    printer.printScriptData(script);
    printer.newLine();

    script.variables.filter(e => e.vType === 'var').forEach(e => {
        printer.print(e.type+' '+e.name);
        printer.newLine();
    });

	let scope = [];

    let iStack = [];

    let sStack = [];

    let lStack = [];

    const processInstruction = function(instruction, i) {
        let results;
        let name;
        let type;
        let value;
        let variable;
        let variableIndex;
        let goto;
		let gotoSize;
		let scope;
        switch(instruction.name) {
            case 'PUSH_INT':
            case 'PUSH_STRING':
            case 'PUSH_LONG':
                name = instruction.name;
                type = name.split('_')[1].toLowerCase();
                if(type === 'int') value = instruction.iValue;
                else if(type === 'string') {
                    value = instruction.sValue;
                    if(!value) value = '';
                } else if(type === 'long') value = instruction.lValue;
                results = this.asType('LITERAL')({
                    name,
                    type,
                    value
                });
                if(type === 'int') iStack.push(results);
                else if(type === 'string') sStack.push(results);
                else if(type === 'long') lStack.push(results);
                break;
            case 'LOAD_INT':
            case 'LOAD_STRING':
            case 'LOAD_LONG':
                variableIndex = script.iValues[i];
                variable = script.variables.find(e => e.index === variableIndex);
                if(!variable) throw new Error('Unable to find variable with index: '+variableIndex);
                name = instruction.name;
                type = name.split('_')[1].toLowerCase();
                results = this.asType('VARIABLE_LOAD')({
                    name,
                    type,
                    variableIndex,
                    variable
                });
                if(type === 'int') iStack.push(results);
                else if(type === 'string') sStack.push(results);
                else if(type === 'long') lStack.push(results);
                break;
            case 'MERGE_STRINGS':
                let strings = [];
                for(let i = 0; i < instruction.iValue; i++)
                    strings.push(sStack.pop());
                results = this.asType('MERGE_STRINGS')({
                    strings
                });
                sStack.push(results);
                break;
            case 'ARRAY_LOAD':
                let values = [ instruction.iValue, iStack.pop() ];
                results = this.asType('ARRAY_LOAD')({
                    values
                });
                iStack.push(results);
                break;
            case 'ARRAY_STORE':
                let globalIndex = instruction.iValue;
                value = iStack.pop();
                let arrayIndex = iStack.pop();
                results = this.asType('ARRAY_STORE')({
                    globalIndex,
                    arrayIndex,
                    value
                });
                break;
            case 'POP_STRING':
                sStack.pop();
                break;
            case 'POP_INT':
                iStack.pop();
                break;
            case 'LOAD_VARP':
            case 'LOAD_VARPBIT':
            case 'LOAD_VARC':
                iStack.push(this.asType(instruction.name)({
                    value: instruction.iValue
                }));
                break;
            case 'STORE_VARC':
                results = this.asType('STORE_VARC')({
                    id: script.iValues[i],
                    value: iStack.pop()
                });
                break;
            case 'STORE_INT':
            case 'STORE_STRING':
            case 'STORE_LONG':
                variableIndex = script.iValues[i];
                variable = script.variables.find(e => e.index === variableIndex);
                if(!variable) throw new Error('Unable to find variable with index: '+variableIndex);
                name = instruction.name;
                type = name.split('_')[1].toLowerCase();
                if(type === 'int') value = iStack.pop();
                else if(type === 'string') value = sStack.pop();
                else if(type === 'long') value = lStack.pop();
                results = this.asType('VARIABLE_ASSIGNATION')({
                    name,
                    type,
                    variableIndex,
                    variable,
                    value
                });
                break;
            case 'INT_LT':
				let expr = [];
				expr.push(this.asType('EXPRESSION')({
					right: iStack.pop(),
					left: iStack.pop(),
					type: instruction.name
				}));
                let tillGoto = script.iValues[i];
                for(let k = 0; k < tillGoto-1; k++) {
                    let instr = script.instructions[++i];
					if(instr.name === 'INT_LT') {
						expr.push(this.asType('EXPRESSION')({
                    		right: iStack.pop(),
                    		left: iStack.pop(),
                    		type: instruction.name
                		}));
                    } else
                        [ i ] = processInstruction(instr, i);
                }
                results = this.asType('STATEMENT')({
					expr,
					scope: [],
					type: 'if'
				});
               	gotoSize = script.iValues[i];
                let s = results.value.scope;
                let end = i+gotoSize;
				while(i < end) {
                    let instr = script.instructions[++i];
					if(instr.name === 'GOTO' && i == end) {
						let size = script.iValues[i];
						if(size < 0) {
							results.value.type = 'while';
							break;
						}
						results.value.hasElse = true;
						results.value.else = {};
						results.value.else.scope = [];
						s = results.value.else.scope;
						end += size;
					}
					let result;
					[i, result] = processInstruction(instr, i);
					if(result) s.push(result);
                }
                break;
            case 'RETURN':
                let size = iStack.length + sStack.length + lStack.length;
                if(!script.returnType) {
                    if(size == 1)
                        script.returnType = iStack.length > 0 ? 'int' : sStack.length > 0 ? 'string': 'long';
                    else {
                        let returnTypes = [];
                        for(let i = 0; i < iStack.length; i++)
                            returnTypes.push('int');
                        for(let i = 0; i < sStack.length; i++)
                            returnTypes.push('string');
                        for(let i = 0; i < lStack.length; i++)
                            returnTypes.push('long');
                        script.returnType = 'script_'+script.id+'_struct('+returnTypes.join(', ')+')';
                    }    
                }
                if(size > 1 && !script.returnType.includes('struct')) {
                    console.log('Printing remaining info');
                    while(iStack.length > 0)
                        console.log(iStack.pop());
                    while(sStack.length > 0)
                        console.log(sStack.pop());
                    while(lStack.length > 0)
                        console.log(lStack.pop());
                    throw new Error('Only 1 value can be returned');
                }
                if(script.returnType.includes('struct')) {
                    let returnTypes = script.returnType.substring(script.returnType.indexOf('(') + 1, script.returnType.indexOf(')')).split(';');
                    params = [];
                    for(let i = 0; i < returnTypes.length; i++) {
                        let type = returnTypes[i];
                        if(type === 'int') params.push(iStack.pop());
                        else if(type === 'string') params.push(sStack.pop());
                        else if(type === 'long') params.push(lStack.pop());
                    }
                    value = this.asType('STRUCT')({
                        id: script.id,
                        params
                    });
                    results = this.asType('RETURN_STATEMENT')({
                        value
                    });
                    break;
                }
                if(iStack.length > 0)
                    value = iStack.pop();
                else if(sStack.length > 0)
                    value = sStack.pop();
                else if(lStack.length > 0)
                    value = lStack.pop();
                else value = undefined;
                results = this.asType('RETURN_STATEMENT')({
                    value
                });
                break;
            case 'SWITCH':
            	let switchIndex = script.iValues[i];
				let switchMap = script.switchMap[switchIndex];
				let cases = [];
				variable = iStack.pop();
				gotoSize = script.iValues[++i]+1;
				let sscope;
				cases.push(this.asType('CASE')({
					value: switchMap[1],
					scope: []
				}));
                sscope = cases[cases.length-1].value.scope;
				for(let k = 1; k < gotoSize; k++) {
                    nextInstr = script.instructions[++i];
                    if(!nextInstr) {
                        console.log('next instr is null... '+(gotoSize-k));
                        break typeS;
                    }
					if(nextInstr.name === 'GOTO') {
                        let value = switchMap[k+1];
                        if(!value) {
                            value = 'default';
                            gotoSize += nextInstr.iValue;
                        }
						cases.push(this.asType('CASE')({
							value,
							scope: []
						}));
						sscope = cases[cases.length-1].value.scope;
					} else {
						let result;
                        let startI = i;
						[ i, result ] = processInstruction(nextInstr, i);
                        k += (i-startI);
						if(result)
							sscope.push(result);
						}
					}
				results = this.asType('SWITCH_STATEMENT')({
					variable,
					cases
				});
                break;
			case 'ADD':
			case 'DIVIDE':
			case 'SUBTRACT':
			case 'MULTIPLY':
				//todo - add others like mod, xor, or, and
				results = this.asType('CALC_FUNCTION')({
					left: iStack.pop(),
					right: iStack.pop()
				});
				let operator;
				if(instruction.name === 'ADD')
					operator = '+';
				else if(instruction.name === 'SUBTRACT')
					operator = '-';
				else if(instruction.name === 'DIVIDE')
					operator = '/';
				else if(instruction.name === 'MULTIPLY')
					operator = '*';
				results.value.operator = operator;
				iStack.push(results);
				break;
            default:
                let params = [];
                if(!instruction.popOrder) break;
                for(let s of instruction.popOrder) {
                    let result;
                    if(s == 's') result = sStack.pop();
                    else if(s == 'i') result = iStack.pop();
                    else if(s == 'l') result = lStack.pop();
                    else if(s == 'ic') {
                        let interfaceHash = iStack.pop().value.value;
                        let interfaceId = interfaceHash >> 16;
                        let componentId = interfaceHash - (interfaceId << 16);
                        result = this.asType('LITERAL')({
                            type: 'int',
                            value: `if_gethash(${interfaceId}, ${componentId})`
                        });
                    }
                    params.push(result);
                }
                results = this.asType('FUNCTION_CALL')({
                    name: instruction.name.toLowerCase(),
                    params,
                    returnType: instruction.returnType
                });
                if(instruction.returnType === 'void') break;
                else if(instruction.returnType === 'int') iStack.push(results);
                else if(instruction.returnType === 'string') sStack.push(results);
                else if(instruction.returnType === 'long') lStack.push(results);
                break;
        }
        if(iStack.length == 0 && sStack.length == 0 && lStack.length == 0 && results) {
            return [i, results];
        }
        return [i, undefined];
    }
    for(let i = 0; i < script.instructions.length; i++) {
        [i, results] = processInstruction(script.instructions[i], i);
    	if(results) scope.push(results);
	}

    for(let result of scope)
		printer.printInstruction(result);

	return printer.getData();
}

asType = (type) => (value) => ({
    type,
    value
});

module.exports = process;
