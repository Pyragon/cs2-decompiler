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
				break;
			case 'CASE':
				this.print('case ');
				this.print(results.value.value, false);
				this.print(':', false);
				this.tab();
				this.newLine();
				for(let scope of results.value.scope)
					this.printInstruction(scope);
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
				this.print(') {', false);
				this.newLine();
				this.tab();
				for(let scope of results.value.scope)
					this.printInstruction(scope);
				this.untab();
				if(results.value.hasElse && results.value.type !== 'while') {
					this.print('} else {', false);
					this.tab();
					this.newLine();
					for(let scope of results.value.else.scope)
						this.printInstruction(scope, false);
				}
				this.untab();
				this.print('}');
				this.newLine();
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
                this.print('load_varc('+results.value.value+')', false);
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
			default: throw new Error('Missing instructions to print '+results.type);
        }
    }

    printScriptData(script) {
        this.print('//');
        this.print(script.name == null ? 'script'+script.id : script.name);
        this.print(`(${script.id})`);
        this.print('(');
        this.print(script.variables.filter(e => e.vType === 'arg').map(e => e.type+' '+e.name).join(','));
        this.print(')');
        this.print(`(${script.returnType})`);
    }

    getData() {
        return this.data;
    }

}

module.exports = Printer;
