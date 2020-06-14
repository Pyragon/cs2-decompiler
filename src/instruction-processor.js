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

    let index = 0;
    for(let index = 0; index < script.instructions.length; index++) {
        let instruction = script.instructions[index];
        let results;
        let name;
        let type;
        let value;
        let variable;
        let variableIndex;
        console.log(instruction);
        switch(instruction.name) {
            case 'PUSH_INT':
            case 'PUSH_STRING':
            case 'PUSH_LONG':
                name = instruction.name;
                type = name.split('_')[1].toLowerCase();
                value = script.iValues[index];
                results = this.asType('LITERAL')({
                    name,
                    type,
                    value
                });
                if(type === 'int') iStack.unshift(results);
                else if(type === 'string') sStack.unshift(results);
                else if(type === 'long') lStack.unshift(results);
                break;
            case 'STORE_INT':
            case 'STORE_STRING':
            case 'STORE_LONG':
                variableIndex = script.iValues[index];
                variable = script.variables.find(e => e.index === variableIndex);
                if(!variable) throw new Error('Unable to find variable with index: '+variableIndex);
                name = instruction.name;
                type = name.split('_')[1].toLowerCase();
                if(type === 'int') value = iStack.shift();
                else if(type === 'string') value = sStack.shift();
                else if(type === 'long') value = lStack.shift();
                results = this.asType('VARIABLE_ASSIGNATION')({
                    name,
                    type,
                    variableIndex,
                    variable,
                    value
                });
                break;
        }
        if(iStack.length == 0 && sStack.length == 0 && lStack.length == 0 && results) {
            printer.printInstruction(results);
        }
    }

    console.log(printer.getData());

}

asType = (type) => (value) => ({
    type,
    value
});

module.exports = process;