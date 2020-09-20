const CS2Script = require('./cs2script');
const fs = require('fs');
const processor = require('./instruction-processor');
let scripts = {};

// fs.readdir('./data/compiled/', (err, files) => {
// 	if (err) {
// 		console.error(err);
// 		return;
// 	}
// 	files.forEach((file) => {
// 		loadFile(file);
// 	});
// });

const loadFile = file => {

	let script = new CS2Script();
	let data = fs.readFileSync('./data/compiled/' + file);
	let text = script.decode(parseInt(file.replace('.cs2', '')), data);
	if (text == null)
		return;
	let argTypes = script.args.map(e => e.type).join(',');
	let argNames = script.args.map(e => e.name).join(',');
	scripts[script.id] = {
		id: script.id,
		name: 'script_'+script.id,
		argTypes,
		argNames,
		returnType: script.returnType
	  };
}

const loadScript = id => {

	let script = new CS2Script();
	let data = fs.readFileSync('./data/compiled/' + id + '.cs2');
	let text = script.decode(id, data);
	console.log(text);
};

loadScript(103);
