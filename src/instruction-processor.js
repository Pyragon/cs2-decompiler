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
        console.log(instruction);
        switch(instruction.name) {
            case 'PUSH_INT':
            case 'PUSH_STRING':
            case 'PUSH_LONG':
                name = instruction.name;
                type = name.split('_')[1].toLowerCase();
                if(type === 'int') value = script.iValues[i];
                else if(type === 'string') value = script.sValues[i];
                else if(type === 'long') value = script.lValues[i];
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
                let right = iStack.pop();
                let left = iStack.pop();
                goto = script.instructions[++i];
                let gotoSize = script.iValues[i];
                printer.print('if(');
                printer.printInstruction(left);
                printer.print(' < ');
                printer.printInstruction(right);
                printer.print(') {');
                printer.newLine();
                printer.tab();
                for(let k = 0; k < gotoSize; k++) {
                    let instr = script.instructions[++i];
                    i = processInstruction(instr, i);
                }
                printer.untab();
                printer.print('}');
                printer.newLine();
                break;
            case 'RETURN':
                let size = iStack.length + sStack.length + lStack.length;
                if(size > 1) throw new Error('Only 1 value can be returned');
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
                //TODO
                //get case addresses from switchMap
                //count down, include all instructions in scope
                break;
            default:
                let params = [];
                if(!instruction.popOrder) break;
                for(let s of instruction.popOrder) {
                    let result;
                    if(s == 's') result = sStack.pop();
                    else if(s == 'i') result = iStack.pop();
                    else if(s == 'l') result = lStack.pop();
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
            printer.printInstruction(results);
        }
        return i;
    }
    for(let i = 0; i < script.instructions.length; i++) {
        i = processInstruction(script.instructions[i], i);
    }

    console.log(printer.getData());

}

asType = (type) => (value) => ({
    type,
    value
});

module.exports = process;