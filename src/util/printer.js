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