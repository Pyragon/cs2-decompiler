const CS2Script = require('./cs2script');
const fs = require('fs');

const decodeScript = (id, data) => {
    let script = new CS2Script();
    let decoded;
    if(Array.isArray(data))
        decoded = script.decode(id, data);
    else {
        let file = fs.readFileSync(data);
        decoded = script.decode(id, file);
    }
    let results = {};
    results.error = !decoded;
    if(decoded) {
        results.script = script;
        results.decoded = decoded;
    }
    return results;
};

module.exports = decodeScript;