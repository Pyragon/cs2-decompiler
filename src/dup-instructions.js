let _ = require('underscore');
const fs = require('fs');

let oldInstructions = require('./../instructions-db.json');

let newInstructions = require('./../data/instructions.json');

newInstructions = newInstructions.filter(i => !i.name.includes('instr'));

let indexed = _.indexBy(newInstructions, i => i.opcode);

const toFullType = returnType => {
    if(returnType == 'v') return 'void';
    if(returnType == 's') return 'string';
    if(returnType == 'i') return 'int';
    if(returnType == 'l') return 'long';
    console.log('Missing return type: '+returnType);
    return 'void';
}

for(let old of Object.values(oldInstructions)) {
    if(indexed[''+old.opcode]) continue;
    let popOrder = old.popOrder.split(",");
    popOrder = popOrder.length == 1 && popOrder[0] == '' ? [] : popOrder;
    let returnType = toFullType(old.pushType);
    newInstructions.push({
        "name": old.name,
        popOrder,
        returnType,
        "opcode": old.opcode,
        "hasExtra": false
    });
}

fs.writeFileSync('./data/instructions.json', JSON.stringify(newInstructions, null, 1));



/*
"name": "send_message",
		"popOrder": [ "s" ],
		"returnType": "void",
		"opcode": 471,
        "hasExtra": false
        */

        /*"4": {
    "opcode": 4,
    "name": "RESUME_STRINGDIALOG",
    "popOrder": "s",
    "argNames": "value",
    "pushType": "v"
  },*/