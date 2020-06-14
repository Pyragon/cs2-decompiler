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

    unTab() {
        this.tabIndex--;
    }

    print(data) {
        this.data += data;
    }

    printInstruction(results) {
        console.log(results);
        switch(results.type) {
            case 'VARIABLE_ASSIGNATION':
                this.print(results.value.variable.name+' = ');
                this.printInstruction(results.value.value);
                break;
            case 'LITERAL':
                let value = results.value.value;
                switch(results.value.type) {
                    case 'int':
                        this.print(value);
                        break;
                    case 'string':
                        this.print(`"${value}"`);
                        break;
                    case 'long':
                        this.print(value+'L');
                        break;
                }
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