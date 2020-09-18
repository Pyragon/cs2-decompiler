const CS2Script = require('./cs2script');
const fs = require('fs');

// fs.readdir('./data/compiled/', (err, files) => {
//   if(err) {
//     console.error(err);
//     return;
//   }
//   files.forEach((file) => {
//     loadFile(file);
//   });
// });

const loadFile = file => {

  let script = new CS2Script();
  let data = fs.readFileSync('./data/compiled/'+file);
  let text = script.decode(parseInt(file.replace('.cs2', '')), data);
  if(text == null)
    return;
}

const loadScript = id => {

  let script = new CS2Script();
  let data = fs.readFileSync('./data/compiled/'+id+'.cs2');
  let text = script.decode(id, data);
  console.log(text);
};

loadScript(15);
