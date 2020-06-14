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
            case 'VARIABLE_ASSIGNATION':
                this.print(results.value.variable.name+' = ');
                this.printInstruction(results.value.value);
                this.newLine();
                break;
            case 'VARIABLE_LOAD':
                this.print(results.value.variable.name, false);
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
                this.print(results.value.name+'(', results.value.returnType === 'void');
                for(let i = 0; i < results.value.params.length; i++) {
                    let param = results.value.params[i];
                    this.printInstruction(param);
                    if(i != results.value.params.length-1)
                        this.print(', ', false);
                }
                this.print(')', false);
                if(results.value.returnType === 'void') this.newLine();
                break;
        }
    }

    printScriptData(script) {
        this.print('//');
        this.print(script.name);
        this.print(`(${script.id})`);
        this.print('(');
        this.print(script.variables.filter(e => e.vType === 'arg').join(','));
        this.print(')');
        this.print(`(${script.returnType})`);
    }

    getData() {
        return this.data;
    }

}

module.exports = Printer;