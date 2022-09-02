const CS2Script = require('./cs2script');
const fs = require('fs');
const processor = require('./instruction-processor');

let scripts = require('../data/scripts.json');

// fs.readdir('./data/compiled/', (err, files) => {
//     if (err) {
//         console.error(err);
//         return;
//     }
//     files.forEach((file) => {
//         try {
//             if (file == '.metadata') return;
//             let script = new CS2Script();
//             let id = parseInt(file.replace('.cs2', ''));
//             let data = fs.readFileSync('./data/compiled/' + file);
//             let text = script.decode(id, data);
//             fs.writeFileSync('./data/decompiled/' + file.replace('.cs2', '.txt'), text);

//             let returnType = script.returnType;
//             if (!scripts[id]) return;
//             scripts[id].returnType = script.returnType;
//         } catch (e) {
//             console.error(e);
//         }
//     });

//     fs.writeFileSync('./data/scripts.json', JSON.stringify(scripts, null, 4));
// });

const loadFile = file => {

    let script = new CS2Script();
    let data = fs.readFileSync('./data/compiled/' + file);
    let text = script.decode(parseInt(file.replace('.cs2', '')), data);
    return text;
}

const loadScript = id => {

    let script = new CS2Script();
    let data = fs.readFileSync('./data/compiled/' + id + '.cs2');
    let text = script.decode(id, data);
    if (text != '')
        console.log(text);
    // console.log(script.instructions);
};
loadScript(3171);