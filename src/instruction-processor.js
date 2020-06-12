const Printer = require('./util/printer');

const process = function(script) {

    let printer = new Printer();

    printer.printScriptData(script);
    printer.newLine();

    script.variables.filter(e => e.vType === 'var').forEach(e => {
        printer.print(e.type+' '+e.name);
        printer.newLine();
    });

    let iStack = [];
    let iStackIndex = 0;

    let sStack = [];
    let sStackIndex = 0;

    let lStack = [];
    let lStackIndex = 0;

    let index = 0;
    for(let index = 0; index < script.instructions.length; index++) {
        let instruction = script.instructions[index];
        
    }

    console.log(printer.getData());

}

module.exports = process;