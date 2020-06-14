const _instByName = require('./util/instructions.js').byName;
const _instByOpcode = require('./util/instructions').byOpcode;
const _scripts = require('./util/scripts.js');
const _ = require('underscore');
const InStream = require('./io/instream.js');
const processor = require('./instruction-processor');

class CS2Script {

	constructor(id, name, args, variables, returnType, instructionData) {
		if(!id) return;
		this.id = id;
		this.name = name;
		this.args = Object.values(args);
		this.variables = Object.values(variables);
		this.returnType = returnType;
	}

	decode(id, data) {
		let script = _scripts[id];
		if(!script)
			throw new Error('Currently not auto getting script data. Please add manually');
		this.returnType = script.returnType;
		this.args = [];
		for(let i = 0; i < script.argTypes.length; i++) {
			this.args.push({
				name: script.argNames[i],
				type: script.argTypes[i]
			});
		}

		let stream = new InStream(data);

		stream.setOffset(stream.getLength() - 2);

		let switchBlockSize = stream.readUnsignedShort();
		let instructionLength = stream.getLength() - 2 - switchBlockSize - 16;

		stream.setOffset(instructionLength);

		let codeSize = stream.readInt();

		let vIndex = 0;

		//variables
		let intLocalsCount = stream.readUnsignedShort();
		let stringLocalsCount = stream.readUnsignedShort();
		let longLocalsCount = stream.readUnsignedShort();

		//args (they go before or after variables...?)
		let intArgs = stream.readUnsignedShort();
		let stringArgs = stream.readUnsignedShort();
		let longArgs = stream.readUnsignedShort();

		this.variables = [];

		for(let i = 0; i < intArgs; i++) {
			let index = vIndex++;
			this.variables.push({ type: 'int', vType: 'arg', name: argNames[index], index })
		}
		for(let i = 0; i < stringArgs; i++) {
			let index = vIndex++;
			this.variables.push({ type: 'string', vType: 'arg', name: argNames[index], index })
		}
		for(let i = 0; i < longArgs; i++) {
			let index = vIndex++;
			this.variables.push({ type: 'long', vType: 'arg', name: argNames[index], index })
		}
		for(let i = 0; i < intLocalsCount; i++) {
			let index = vIndex++;
			this.variables.push({ type: 'int', vType: 'var', name: 'var'+index, index })
		}
		for(let i = 0; i < stringLocalsCount; i++) {
			let index = vIndex++;
			this.variables.push({ type: 'string', vType: 'var', name: 'var'+index, index })
		}
		for(let i = 0; i < longLocalsCount; i++) {
			let index = vIndex++;
			this.variables.push({ type: 'long', vType: 'var', name: 'var'+index, index })
		}

		let iValues = [];
		let sValues = [];
		let lValues = [];

		let switchCount = stream.readUnsignedByte();

		if(switchCount > 0) {
			this.switchMap = [];
			for(let i = 0; i < switchCount; i++) {
				this.switchMap[i] = {};
				let size = stream.readUnsignedShort();
				while(size-- > 0) {
					let casee = stream.readInt();
					let addr = stream.readInt();
					this.switchMap[i][casee] = addr;
				}
			}
		}

		stream.setOffset(0);

		let name = stream.readNullString();

		let instructions = [];

		let opCount = 0;

		while(stream.getOffset() < instructionLength) {
			let opcode = stream.readUnsignedShort();
			if(opcode < 0) throw new Error('Invalid instruction opcode: '+opcode);
			let instruction = _instByOpcode[opcode];
			if(!instruction) throw new Error('Missing instruction: '+opcode);
			let opIndex = opCount++;
			if(instruction == _instByName['PUSH_STRING'])
				sValues[opIndex] = stream.readString();
			else if(instruction == _instByName['PUSH_LONG'])
				lValues[opIndex] = stream.readLong();
			else {
				if(instruction.hasExtra)
					iValues[opIndex] = stream.readInt();
				else
					iValues[opIndex] = stream.readUnsignedByte();
			}
			instructions[opIndex] = instruction;
		}

		this.id = id;
		this.name = name;
		this.instructions = instructions;
		this.iValues = iValues;
		this.sValues = sValues;
		this.lValues = lValues;

		processor(this);
		
	}

	encode() {

		let stream = new Stream();

		if(!this.name)
			stream.writeByte(0);
		else
			stream.writeString(this.name);
		
		for(let i = 0; i < this.instructions.length; i++) {
			let instruction = this.instructions[i];
			if(!instruction) continue;
			stream.writeShort(instruction.opcode);
			if(instruction == _instByName['PUSH_STRING'])
				stream.writeString(this.sValues[i]);
			else if(instruction == _instByName['PUSH_LONG'])
				stream.writeLong(this.lValues[i]);
			else if(instruction.hasExtra)
				stream.writeInt(this.iValues[i]);
			else
				stream.writeByte(this.iValues[i]);
		}

		stream.writeInt(this.instructions.length);

		stream.writeShort(this.variables.filter(e => e.type === 'int').length);
		stream.writeShort(this.variables.filter(e => e.type === 'string').length);
		stream.writeShort(this.variables.filter(e => e.type === 'long').length);

		stream.writeShort(this.args.filter(e => e.type === 'int').length);
		stream.writeShort(this.args.filter(e => e.type === 'string').length);
		stream.writeShort(this.args.filter(e => e.type === 'long').length);

		let switchBlock = new Stream();

		let switchMap = this.switchMap;

		if(switchMap.length == 0)
			switchBlock.writeByte(0);
		else {

			switchBlock.writeByte(switchMap.length);

			for(let map of switchMap) {
				switchBlock.writeShort(Object.values(map).length);

				for(let key of Object.keys(map)) {
					switchBlock.writeInt(key);
					switchBlock.writeInt(map[key]);
				}

			}

		}

		let block = switchBlock.toArray();

		stream.writeBytes(block);

		stream.writeShort(block.length)

		return stream.toArray();

	}

}
module.exports = CS2Script;