const _scripts = require('./scripts.js');
class Printer {

    constructor() {
        this.data = '';
        this.tabIndex = 0;
    }

    newLine() {
        this.data += '\n';
    }

    tab() {
        this.tabIndex++;
    }

    untab() {
        this.tabIndex--;
    }

    print(data, tab=true) {
        let s = '';
        if(tab)
            for(let i = 0; i < this.tabIndex; i++)
                s += '\t';
        s += data;
        this.data += s;
    }

    printInstruction(results) {
        // console.log('print: ', results);
        switch(results.type) {
			case 'SWITCH_STATEMENT':
				this.print('switch(');
				this.printInstruction(results.value.variable);
				this.print(') {', false);
				this.tab();
				this.newLine();
				for(let casee of results.value.cases)
					this.printInstruction(casee);
				this.untab();
				this.newLine();
                this.print('}', false);
                this.newLine();
				break;
			case 'CASE':
				this.print('case ');
				this.print(results.value.value, false);
				this.print(':', false);
				this.tab();
				this.newLine();
				for(let scope of results.value.scope)
                    this.printInstruction(scope);
                if(results.value.scope[results.value.scope.length-1].type != 'RETURN_STATEMENT')
				    this.print('break');
				this.untab();
				this.newLine();
				break;
			case 'STATEMENT':
				this.print(results.value.type+'(');
				let expr = results.value.expr;
				for(let i = 0; i < expr.length; i++) {
					this.printInstruction(expr[i]);
					if(i != expr.length-1)
						this.print(' || ', false);
				}
                this.print(')', false);
                if(results.value.scope > 1)
                    this.print(' {', false);
				this.newLine();
				this.tab();
				for(let scope of results.value.scope)
					this.printInstruction(scope);
				this.untab();
				if(results.value.hasElse && results.value.type !== 'while') {
					this.print('} else {');
					this.tab();
					this.newLine();
					for(let scope of results.value.else.scope)
                        this.printInstruction(scope, false);
                    this.untab();
                }
                if(results.value.scope.length > 1) {
				    this.print('}');
                    this.newLine();
                }
				break;
			case 'EXPRESSION':
				let left = results.value.left;
				let right = results.value.right;
				let type = results.value.type;
				this.printInstruction(left);
				this.print(' ', false);
				switch(type) {
					case 'INT_LT':
						this.print('<', false);
                        break;
                    case 'INT_GE':
                        this.print('>=', false);
                        break;
                    case 'INT_EQ':
                        this.print('==', false);
                        break;
                    case 'INT_NE':
                        this.print('!=', false);
                        break;
				}
				this.print(' ', false);
				this.printInstruction(right);
				break;
            case 'VARIABLE_ASSIGNATION':
                this.print(results.value.variable.name+' = ');
                this.printInstruction(results.value.value);
                this.newLine();
                break;
            case 'VARIABLE_LOAD':
                this.print(results.value.variable.name, false);
                break;
            case 'LOAD_VARC':
            case 'LOAD_VARP':
            case 'LOAD_VARPBIT':
                this.print(results.type.toLowerCase()+'('+results.value.value+')', false);
                break;
            case 'STORE_VARC':
                this.print('store_varc('+results.value.id+', ', true);
                this.printInstruction(results.value.value);
                this.print(')', false);
                this.newLine();
                break;
            case 'MERGE_STRINGS':
                this.print('merge_strings(', false);
                for(let i = 0; i < results.value.strings.length; i++) {
                    this.printInstruction(results.value.strings[i]);
                    if(i != results.value.strings.length-1)
                        this.print(', ', false);
                }
                this.print('(', false);
                break;
            case 'ARRAY_LOAD':
                this.print('globalArrays[', false);
                this.print(results.value.values[0], false);
                this.print('][', false);
                this.printInstruction(results.value.values[1]);
                this.print(']', false);
                break;
            case 'ARRAY_STORE':
                this.print('globalArrays[', true);
                this.print(results.value.globalIndex, false);
                this.print('][', false);
                this.printInstruction(results.value.arrayIndex);
                this.print('] = ', false);
                this.printInstruction(results.value.value);
                this.newLine();
                break;
            case 'LITERAL':
                let value = results.value.value;
                switch(results.value.type) {
                    case 'int':
                        this.print(value, false);
                        break;
                    case 'string':
                        this.print(`"${value}"`, false);
                        break;
                    case 'long':
                        this.print(value+'L', false);
                        break;
                }
                break;
            case 'RETURN_STATEMENT':
                this.print('return');
                if(results.value.value) {
                    this.print(' ', false);
                    this.printInstruction(results.value.value);
                }
                this.newLine();
                break;
            case 'STRUCT':
                let values = results.value.params;
                let id = results.value.id;
                for(let i = 0; i < values.length; i++) {
                    this.printInstruction(values[i]);
                    if(i != values.length-1)
                        this.print(', ', false);
                }
                break;
            case 'CALL_CS2':
                this.print(results.value.name+'(', results.value.returnType === 'void');
                for(let i = 0; i < results.value.params.length; i++) {
                    this.printInstruction(results.value.params[i]);
                    if(i != results.value.params.length-1)
                        this.print(', ', false);
                }
                this.print(')', false);
                if(results.value.returnType === 'void')
                    this.newLine();
                break;
            case 'FUNCTION_CALL':
                this.print(results.value.name.toLowerCase()+'(', results.value.returnType === 'void');
                for(let i = results.value.params.length-1; i >= 0; i--) {
                    let param = results.value.params[i];
                    this.printInstruction(param);
                    if(i != 0)
                        this.print(', ', false);
                }
                this.print(')', false);
                if(results.value.returnType === 'void') this.newLine();
                break;
			case 'CALC_FUNCTION':
				this.print('calc(', false);
				this.printInstruction(results.value.left);
				this.print(' '+results.value.operator+' ', false);
				this.printInstruction(results.value.right);
				this.print(')', false);
                break;
            case 'HOOK':
                let hookScriptId = results.value.params[0].value.value;
                this.print(results.value.name.toLowerCase()+'(');
                if(results.value.component) {
                    this.printInstruction(results.value.component);
                    this.print(', ', false);
                }
                if(hookScriptId == -1)
                    this.print('None, ', false);
                else {
                    let script = _scripts[hookScriptId];
                    this.print(script.name, false);
                    this.print(', ', false);
                }
                this.printInstruction(results.value.paramTypes);
                let intArr = results.value.intArr;
                if(intArr && intArr.length > 0) {
                    this.print(`, ${intArr.length}, `, false);
                    for(let i = 0; i < intArr.length; i++) {
                        this.printInstruction(intArr[i]);
                        if(i != intArr.length-1)
                            this.print(', ', false);
                    }
                }
                let params = results.value.params;
                if(params && params.length > 1) {
                    this.print(', ');
                    for(let i = 1; i < params.length; i++) {
                        this.printInstruction(params[i]);
                        if(i != params.length-1)
                            this.print(', ', false);
                    }
                }
                this.print(')', false);
                this.newLine();
                break;
			default: throw new Error('Missing instructions to print '+results.type);
        }
    }

    printScriptData(script) {
        let args = [];
        script.args.forEach(e => {
            args.push(e.type+' '+e.name);
        });
        this.print('//');
        this.print(script.name == null ? 'script'+script.id : script.name);
        this.print(`(${script.id})`);
        this.print('(');
        this.print(args.join(', '));
        this.print(')');
        let returnType = script.returnType || 'tba';
        if(returnType.includes('struct'))
            returnType = returnType.substring(returnType.indexOf('(') + 1, returnType.indexOf(')')).split(';').join(', ');
        this.print(`(${returnType})`);
    }

    getData() {
        return this.data;
    }

}

module.exports = Printer;
