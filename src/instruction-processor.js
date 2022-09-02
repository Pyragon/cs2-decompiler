const Printer = require('./util/printer');
const _instructions = require('./util/instructions');
const _scripts = require('./util/scripts.js');
const _params = require('./../data/params.json');
const fs = require('fs');

class InstructionProcessor {

    constructor(script) {
        this.script = script;
    }

    process() {
        this.script.returnType = _scripts[this.script.id].returnType;
        this.printer = new Printer(this.script.id);

        this.printer.printScriptData(this.script);
        this.printer.newLine();

        this.script.variables.filter(e => e.vType === 'var').forEach(e => {
            this.printer.print(e.type + ' ' + e.name);
            this.printer.newLine();
        });

        this.scope = [];
        this.iStack = [];
        this.sStack = [];
        this.lStack = [];

        let results;

        for (let i = 0; i < this.script.instructions.length; i++) {
            [i, results] = this.processInstruction(this.script.instructions[i], i);
            if (results) this.scope.push(results);
            // console.log(this.toString(this.script.instructions[i]));
        }

        for (let result of this.scope)
            this.printer.printInstruction(result);

        return this.printer.getData();
    }

    processInstruction(instruction, i) {
        let results;
        let name;
        let type;
        let value;
        let variable;
        let variableIndex;
        let goto;
        let gotoSize;
        let scope;
        let params;
        let str;
        let stack;
        // console.log(this.iStack.length, this.sStack.length, this.lStack.length);
        // console.log(instruction.name, i);
        typeS: switch (instruction.name) {
            case 'PUSH_INT':
            case 'PUSH_STRING':
            case 'PUSH_LONG':
                name = instruction.name;
                type = name.split('_')[1].toLowerCase();
                if (type === 'int') value = instruction.iValue;
                else if (type === 'string') {
                    value = instruction.sValue;
                    if (!value) value = '';
                } else if (type === 'long') value = instruction.lValue;
                results = this.asType('LITERAL')({
                    name,
                    type,
                    value,
                    i
                });
                if (type === 'int') this.iStack.push(results);
                else if (type === 'string') this.sStack.push(results);
                else if (type === 'long') this.lStack.push(results);
                break;
            case 'LOAD_INT':
            case 'LOAD_STRING':
            case 'LOAD_LONG':
                variableIndex = this.script.iValues[i];
                variable = this.script.variables.find(e => e.index === variableIndex);
                if (!variable) throw new Error('Unable to find variable with index: ' + variableIndex);
                name = instruction.name;
                type = name.split('_')[1].toLowerCase();
                results = this.asType('VARIABLE_LOAD')({
                    name,
                    type,
                    variableIndex,
                    variable
                });
                if (type === 'int') this.iStack.push(results);
                else if (type === 'string') this.sStack.push(results);
                else if (type === 'long') this.lStack.push(results);
                break;
            case 'MERGE_STRINGS':
                let strings = [];
                for (let k = 0; k < instruction.iValue; k++)
                    strings.push(this.sStack.pop());
                results = this.asType('MERGE_STRINGS')({
                    strings
                });
                this.sStack.push(results);
                break;
            case 'ARRAY_LOAD':
                let values = [instruction.iValue, this.iStack.pop()];
                results = this.asType('ARRAY_LOAD')({
                    values
                });
                this.iStack.push(results);
                break;
            case 'ARRAY_STORE':
                let globalIndex = instruction.iValue;
                value = this.iStack.pop();
                let arrayIndex = this.iStack.pop();
                results = this.asType('ARRAY_STORE')({
                    globalIndex,
                    arrayIndex,
                    value
                });
                break;
            case 'ARRAY_NEW':
                results = this.asType('ARRAY_NEW')({
                    arrayIndex: instruction.iValue >> 16,
                    valueIsZero: instruction.iValue & 0xffff,
                    size: this.iStack.pop()
                });
                break;
            case 'POP_STRING':
            case 'POP_INT':
            case 'POP_2_INT':
                stack = instruction.name.includes('STRING') ? this.sStack : this.iStack;
                stack.pop();
                if (instruction.name.includes('2'))
                    stack.pop();
                results = this.asType('POP')({
                    name: instruction.name
                });
                break;
            case 'instr6771':
            case 'ITEM_PARAM':
            case 'STRUCT_PARAM':
            case 'NPC_PARAM':
                let param = this.iStack.pop();
                if (param.type != 'LITERAL') throw new Error('Expect param Id to be a literal');
                let paramId = param.value.value;
                let struct;
                if (instruction.hasExtraValue)
                    struct = this.iStack.pop();
                if (typeof _params[paramId] === 'undefined')
                    throw new Error('Missing param: ' + paramId + ' in script: ' + this.script.id);
                results = this.asType('PARAM')({
                    name: instruction.name,
                    paramId,
                    struct,
                    isString: _params[paramId]
                });
                if (_params[paramId] === true)
                    this.sStack.push(results);
                else
                    this.iStack.push(results);
                break;
            case 'LOAD_VARP':
            case 'LOAD_VARPBIT':
            case 'LOAD_VARC':
            case 'LOAD_VARC_STRING':
            case 'LOAD_CLAN_SETTING_VARBIT':
            case 'LOAD_CLAN_SETTING_VAR':
            case 'LOAD_CLAN_VARBIT':
            case 'LOAD_CLAN_VAR':
            case 'LOAD_CLAN_SETTING_VAR_LONG':
            case 'LOAD_CLAN_SETTING_VAR_STRING':
                stack = instruction.name.includes('STRING') ? this.sStack : instruction.name.includes('LONG') ? this.lStack : this.iStack;
                results = this.asType('LOAD_VAR')({
                    value: instruction.iValue,
                    name: instruction.name.toLowerCase(),
                });
                stack.push(results);
                break;
            case 'STORE_VARC':
            case 'STORE_VARC_STRING':
            case 'STORE_VARPBIT':
            case 'STORE_VARP':
                str = instruction.name.toLowerCase().includes('string');
                results = this.asType('STORE_VAR')({
                    id: this.script.iValues[i],
                    name: instruction.name.toLowerCase(),
                    value: str ? this.sStack.pop() : this.iStack.pop()
                });
                break;
            case 'STORE_INT':
            case 'STORE_STRING':
            case 'STORE_LONG':
                variableIndex = this.script.iValues[i];
                variable = this.script.variables.find(e => e.index === variableIndex);
                if (!variable) throw new Error('Unable to find variable with index: ' + variableIndex);
                name = instruction.name;
                type = name.split('_')[1].toLowerCase();
                if (type === 'int') value = this.iStack.pop();
                else if (type === 'string') value = this.sStack.pop();
                else if (type === 'long') value = this.lStack.pop();
                results = this.asType('VARIABLE_ASSIGNATION')({
                    name,
                    type,
                    variableIndex,
                    variable,
                    value,
                    i
                });
                break;
            case 'INT_LT':
            case 'INT_LE':
            case 'INT_GE':
            case 'INT_EQ':
            case 'INT_NE':
            case 'INT_GT':
            case 'LONG_NE':
            case 'LONG_EQ':
            case 'LONG_LT':
            case 'LONG_GT':
            case 'LONG_LE':
            case 'LONG_GE':
                let expr = [];
                // console.log(instruction.name, i);
                // console.log(iStack.length, sStack.length, lStack.length);
                stack = instruction.name.startsWith('LONG_') ? this.lStack : this.iStack;
                expr.push(this.asType('EXPRESSION')({
                    right: stack.pop(),
                    left: stack.pop(),
                    type: instruction.name
                }));
                let tillGoto = this.script.iValues[i];
                for (let k = 0; k < tillGoto; k++) {
                    let instr = this.script.instructions[++i];
                    if (instr.name.endsWith('_LT') || instr.name.endsWith('_GE') || instr.name.endsWith('_EQ') ||
                        instr.name.endsWith('_NE') || instr.name.endsWith('_GT') || instr.name.endsWith('_LE')) {
                        stack = instruction.name.startsWith('LONG_') ? this.lStack : this.iStack;
                        expr.push(this.asType('EXPRESSION')({
                            right: stack.pop(),
                            left: stack.pop(),
                            type: instr.name
                        }));
                    } else
                        [i] = this.processInstruction(instr, i);
                }
                results = this.asType('STATEMENT')({
                    expr,
                    scope: [],
                    type: 'if'
                });
                gotoSize = this.script.iValues[i];
                let s = results.value.scope;
                let end = i + gotoSize;
                while (i < end) {
                    let instr = this.script.instructions[++i];
                    if (instr.name === 'GOTO' && i == end) {
                        let size = this.script.iValues[i];
                        if (size < 0) {
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
                    [i, result] = this.processInstruction(instr, i);
                    if (result) s.push(result);
                }
                break;
            case 'RETURN':
                let size = this.iStack.length + this.sStack.length + this.lStack.length;
                if (size == 0)
                    this.script.returnType = 'void';
                else if (size == 1)
                    this.script.returnType = this.iStack.length > 0 ? 'int' : this.sStack.length > 0 ? 'string' : 'long';
                else {
                    let returnTypes = [];
                    for (let k = 0; k < this.iStack.length; k++)
                        returnTypes.push('int');
                    for (let k = 0; k < this.sStack.length; k++)
                        returnTypes.push('string');
                    for (let k = 0; k < this.lStack.length; k++)
                        returnTypes.push('long');
                    this.script.returnType = 'script_' + this.script.id + '_struct(' + returnTypes.join(';') + ')';
                }
                if (size > 1 && !this.script.returnType.includes('struct')) {
                    console.log('Printing remaining info');
                    while (this.iStack.length > 0)
                        console.log(this.iStack.pop());
                    while (this.sStack.length > 0)
                        console.log(this.sStack.pop());
                    while (this.lStack.length > 0)
                        console.log(this.lStack.pop());
                    throw new Error('Only 1 value can be returned: ' + this.script.id);
                }
                if (this.script.returnType.includes('struct')) {
                    let returnTypes = this.script.returnType.substring(this.script.returnType.indexOf('(') + 1, this.script.returnType.indexOf(')')).split(';');
                    params = [];
                    for (let k = 0; k < returnTypes.length; k++) {
                        let type = returnTypes[k];
                        if (type === 'int') params.push(this.iStack.pop());
                        else if (type === 'string') params.push(this.sStack.pop());
                        else if (type === 'long') params.push(this.lStack.pop());
                    }
                    value = this.asType('STRUCT')({
                        id: this.script.id,
                        params
                    });
                    results = this.asType('RETURN_STATEMENT')({
                        value
                    });
                    size = this.iStack.length + this.sStack.length + this.lStack.length;
                    if (size > 1) {
                        console.log('Printing remaining info');
                        while (this.iStack.length > 0)
                            console.log(this.iStack.pop());
                        while (this.sStack.length > 0)
                            console.log(this.sStack.pop());
                        while (this.lStack.length > 0)
                            console.log(this.lStack.pop());
                        throw new Error('Too many values returned from script: ' + this.script.id);
                    }
                    let scriptInfo = {
                        name: 'script_' + this.script.id,
                        id: this.script.id,
                        argTypes: this.script.args.map(a => a.type).join(','),
                        argNames: this.script.args.map(a => a.name).join(','),
                        returnType: this.script.returnType
                    };
                    this.writeScript(scriptInfo);
                    break;
                }
                if (this.iStack.length > 0) {
                    value = this.iStack.pop();
                    this.script.returnType = 'int';
                } else if (this.sStack.length > 0) {
                    value = this.sStack.pop();
                    this.script.returnType = 'string';
                } else if (this.lStack.length > 0) {
                    value = this.lStack.pop();
                    this.script.returnType = 'long';
                } else {
                    value = undefined;
                    this.script.returnType = 'void';
                }
                results = this.asType('RETURN_STATEMENT')({
                    value
                });
                let scriptInfo = {
                    name: 'script_' + this.script.id,
                    id: this.script.id,
                    argTypes: this.script.args.map(a => a.type).join(','),
                    argNames: this.script.args.map(a => a.name).join(','),
                    returnType: this.script.returnType
                };
                this.writeScript(scriptInfo);
                break;
            case 'SWITCH':
                value = this.iStack.pop();
                let switchMap = this.script.switchMap[instruction.iValue];
                let cases = [];
                let sscope;
                let keys = Object.keys(switchMap);
                cases.push(this.asType('CASE')({
                    value: 'default',
                    scope: []
                }));
                sscope = cases[cases.length - 1].value.scope;
                let startIndex = i;
                while (i < startIndex + parseInt(keys[0])) {
                    [i, results] = this.processInstruction(this.script.instructions[++i], i);
                    if (results) sscope.push(results);
                }
                for (let k = 0; k < keys.length; k++) {
                    if (i >= this.script.instructions.length) break typeS;
                    cases.push(this.asType('CASE')({
                        value: switchMap[keys[k]],
                        scope: []
                    }));
                    sscope = cases[cases.length - 1].value.scope;
                    let size = k == keys.length - 1 ? this.script.instructions[i].iValue : (keys[k + 1] - keys[k]);
                    size += i;
                    while (i < size) {
                        [i, results] = this.processInstruction(this.script.instructions[++i], i);
                        if (results) sscope.push(results);
                    }
                }
                results = this.asType('SWITCH_STATEMENT')({
                    variable: value,
                    cases
                });
                break;
            case 'ADD':
            case 'DIVIDE':
            case 'SUBTRACT':
            case 'MULTIPLY':
            case 'BIT_OR':
            case 'BIT_AND':
            case 'BIT_NOT':
            case 'MODULO':
            case 'POW':
                //todo - add others like xor
                results = this.asType('CALC_FUNCTION')({
                    left: this.iStack.pop(),
                    right: this.iStack.pop()
                });
                let operator;
                switch (instruction.name) {
                    case 'ADD':
                        operator = '+';
                        break;
                    case 'SUBTRACT':
                        operator = '-';
                        break;
                    case 'DIVIDE':
                        operator = '/';
                        break;
                    case 'MULTIPLY':
                        operator = '*';
                        break;
                    case 'BIT_OR':
                        operator = '|';
                        break;
                    case 'BIT_AND':
                        operator = '&';
                        break;
                    case 'BIT_NOT':
                        operator = '~';
                        break;
                    case 'MODULO':
                        operator = '%';
                        break;
                    case 'POW':
                        operator = '^';
                        break;
                }
                results.value.operator = operator;
                this.iStack.push(results);
                break;
            case 'CALL_CS2':
                let scriptId = this.script.iValues[i];
                if (!_scripts[scriptId]) {
                    console.error('Missing information for script: ' + scriptId);
                    i = 10000000;
                    return [i, undefined];
                }
                let callScript = _scripts[scriptId];
                let argTypes = callScript.argTypes.split(',');
                if (argTypes.length == 1 && !argTypes[0])
                    argTypes = [];
                params = [];
                if (argTypes.length > 0) {
                    for (let i = argTypes.length - 1; i >= 0; i--) {
                        let value;
                        if (argTypes[i] == 'int')
                            value = this.iStack.pop();
                        else if (argTypes[i] == 'string')
                            value = this.sStack.pop();
                        else if (argTypes[i] == 'long')
                            value = this.lStack.pop();
                        params[i] = value;
                    }
                }
                results = this.asType('CALL_CS2')({
                    name: callScript.name == null ? 'script' + callScript.id : callScript.name,
                    params,
                    returnType: callScript.returnType,
                    i
                });
                if (callScript.returnType == 'int') this.iStack.push(results);
                else if (callScript.returnType == 'string') this.sStack.push(results);
                else if (callScript.returnType == 'long') this.lStack.push(results);
                else if (callScript.returnType.includes('struct')) {
                    let returnTypes = callScript.returnType.substring(callScript.returnType.indexOf('(') + 1, callScript.returnType.indexOf(')')).split(';');
                    for (let k = 0; k < returnTypes.length; k++) {
                        let returnType = returnTypes[k];
                        let res = this.asType('CALL_CS2')({
                            name: callScript.name == null ? 'script' + callScript.id : callScript.name,
                            params,
                            returnType: instruction.returnType,
                            valIndex: k
                        });
                        if (returnType == 'int') this.iStack.push(res);
                        else if (returnType == 'string') this.sStack.push(res);
                        else if (returnType == 'long') this.lStack.push(res);
                    }
                }
                break;
            case 'ENUM':
                let valueId = this.iStack.pop();
                let enumId = this.iStack.pop();
                let valueType = this.iStack.pop();
                let keyType = this.iStack.pop();
                results = this.asType('ENUM')({
                    valueId,
                    enumId,
                    valueType,
                    keyType
                });
                if (valueType.value.value == 's'.charCodeAt(0))
                    this.sStack.push(results);
                else
                    this.iStack.push(results);
                break;
            default:
                //Hooks
                if (typeof instruction.hasExtraHook !== 'undefined') {
                    let component;
                    if (instruction.hasExtraHook === true) {
                        component = this.iStack.pop();
                        if (component.type == 'LITERAL') {
                            let interfaceHash = component.value.value;
                            let interfaceId = interfaceHash >> 16;
                            let componentId = interfaceHash - (interfaceId << 16);
                            component = this.asType('LITERAL')({
                                type: 'int',
                                value: `if_gethash(${interfaceId}, ${componentId})`
                            });
                        }
                    }
                    let paramTypes = this.sStack.pop();
                    if (paramTypes.type != 'LITERAL')
                        throw new Error('Expected param types to be a literal string');
                    let paramTypesE = JSON.parse(JSON.stringify(paramTypes));
                    paramTypes = paramTypes.value.value;
                    let intArr;
                    if (paramTypes.length > 0 && paramTypes[paramTypes.length - 1] == 'Y') {
                        let size = this.iStack.pop().value.value;
                        if (size > 0) {
                            intArr = [];
                            while (size-- > 0)
                                intArr[size] = this.iStack.pop();
                            paramTypes = paramTypes.substring(0, paramTypes.length - 1);
                        }
                    }
                    params = [];
                    for (let i = paramTypes.length; i >= 1; --i) {
                        if (paramTypes[i - 1] == 'i')
                            params[i] = this.iStack.pop();
                        else if (paramTypes[i - 1] == 's')
                            params[i] = this.sStack.pop();
                        else if (paramTypes[i - 1] == 'l')
                            params[i] = this.lStack.pop();
                        else if (paramTypes[i - 1] == 'I') {
                            value = this.iStack.pop();
                            if (value.type !== 'LITERAL' || value.value.value == -2147483645) {
                                params[i] = value;
                                continue;
                            }
                            let interfaceHash = value.value.value; //lol
                            let interfaceId = interfaceHash >> 16;
                            let componentId = interfaceHash - (interfaceId << 16);
                            value = this.asType('LITERAL')({
                                type: 'int',
                                value: `if_gethash(${interfaceId}, ${componentId})`
                            });
                            params[i] = value;
                        } else
                            params[i] = this.iStack.pop();
                    }
                    params[0] = this.iStack.pop();
                    results = this.asType('HOOK')({
                        name: instruction.name,
                        paramTypes: paramTypesE,
                        intArr,
                        params,
                        component
                    });
                    break;
                }
                //Regular instruction calls
                params = [];
                if (!instruction.popOrder) break;
                for (let s of instruction.popOrder) {
                    let result;
                    if (s == 's') result = this.sStack.pop();
                    else if (s == 'i') {
                        result = this.iStack.pop();
                        if (instruction.isColour) {
                            //convert to rgb then to hex
                            let r = result.value.value >> 16;
                            let g = (result.value.value >> 8) & 0xFF;
                            let b = result.value.value & 0xFF;
                            //convert rgb to hex
                            let hex = '0x' + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1).toUpperCase();
                            result = this.asType('LITERAL')({
                                type: 'int',
                                value: hex
                            });
                        }
                    } else if (s == 'l') result = this.lStack.pop();
                    else if (s == 'ic') {
                        result = this.iStack.pop();
                        if (!result)
                            throw new Error('Error on ' + instruction.name + ' for script: ' + this.script.id + ' ' + i);
                        if (result.type == 'LITERAL') {
                            let interfaceHash = result.value.value;
                            let interfaceId = interfaceHash >> 16;
                            let componentId = interfaceHash - (interfaceId << 16);
                            result = this.asType('LITERAL')({
                                type: 'int',
                                value: `if_gethash(${interfaceId}, ${componentId})`
                            });
                        }
                    }
                    params.push(result);
                }
                results = this.asType('FUNCTION_CALL')({
                    name: instruction.name.toLowerCase(),
                    params,
                    returnType: instruction.returnType,
                    i
                });
                if (instruction.returnType.includes(',')) {
                    let returnTypes = instruction.returnType.split(',');
                    for (let k = 0; k < returnTypes.length; k++) {
                        let returnType = returnTypes[k];
                        let res = this.asType('FUNCTION_CALL')({
                            name: instruction.name.toLowerCase(),
                            params,
                            returnType: instruction.returnType,
                            valIndex: k,
                            i
                        });
                        if (returnType == 'int') this.iStack.push(res);
                        else if (returnType == 'string') this.sStack.push(res);
                        else if (returnType == 'long') this.lStack.push(res);
                    }
                } else if (instruction.returnType === 'void') break;
                else if (instruction.returnType === 'int') this.iStack.push(results);
                else if (instruction.returnType === 'string') this.sStack.push(results);
                else if (instruction.returnType === 'long') this.lStack.push(results);
                break;
        }
        if (results && this.iStack.length == 0 && this.sStack.length == 0 && this.lStack.length == 0)
            return [i, results];
        // if (this.iStack.length > 0 || this.sStack.length > 0 || this.lStack.length > 0) {
        //     console.log('Values remaining in stacks.');
        //     console.log('iStack:', this.iStack);
        //     console.log('sStack:', this.sStack);
        //     console.log('lStack:', this.lStack);
        // }
        // if((typeof instruction.returnType === 'undefined' || instruction.returnType === 'void') 
        //     && !instruction.name.includes('PUSH') && !instruction.name.includes('LOAD'))
        //         return [i, results];
        return [i, undefined];
    }

    toString = (instruction) => {
        if (typeof instruction.iValue === 'undefined')
            delete instruction['iValue'];
        if (typeof instruction.sValue === 'undefined')
            delete instruction['sValue'];
        if (typeof instruction.lValue === 'undefined')
            delete instruction['lValue'];
        return instruction;
    };

    asType = (type) => (value) => ({
        type,
        value
    });

    writeScript(info) {
        if (_scripts[info.id]) return;
        _scripts[info.id] = info;
        fs.writeFileSync('./data/scripts.json', JSON.stringify(_scripts, null, 4));
    }

}

module.exports = InstructionProcessor;