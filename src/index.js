const CS2Script = require('./cs2script');
const fs = require('fs');

const loadScript = id => {

  let script = new CS2Script();

  let data = fs.readFileSync('./data/compiled/'+id+'.cs2');

  script.decode(id, data);

};

loadScript(2080);
