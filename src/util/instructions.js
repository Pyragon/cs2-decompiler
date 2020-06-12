const _ = require('underscore');
const instructionDB = require('../../data/instructions.json');

const byName = _.indexBy(instructionDB, 'name');
const byOpcode = _.indexBy(instructionDB, 'opcode');

module.exports = {
    byName,
    byOpcode
};
