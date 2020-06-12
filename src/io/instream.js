const CP_1252_CHARS = [
    '\u20ac', '\0', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020', '\u2021', '\u02c6',
    '\u2030', '\u0160', '\u2039', '\u0152', '\0', '\u017d', '\0', '\0', '\u2018', '\u2019', '\u201c',
    '\u201d', '\u2022', '\u2013', '\u2014', '\u02dc', '\u2122', '\u0161', '\u203a', '\u0153',
    '\0', '\u017e', '\u0178'
];

class InStream {

    constructor(data) {
        this.array = new Int8Array(data.length);
        this.array.set(data);
        this.offset = 0;
    }

    readUnsignedByte() {
        return this.readByte() & 0xFF;
    }

    readByte() {
        return this.array[this.offset++];
    }

    readUnsignedShort() {
        return (this.readUnsignedByte() << 8) + this.readUnsignedByte();
    }

    readInt() {
        return (this.readUnsignedByte() << 24) + (this.readUnsignedByte() << 16) + (this.readUnsignedByte() << 8) + this.readUnsignedByte();
    }

    readString() {
        let start = this.offset;
        while(this.array[++this.offset - 1] != 0) {

        }
        let length = this.offset - start - 1;
        return length == 0 ? "" : this.readStringUtil(start, length);
    }

    readStringUtil(start, length) {
        let charArr = [];
        let offset = 0;

        for(let i = 0; i < length; i++) {
            let value = this.array[start + i] & 0xFF;
            if(value != 0) {
                if(value >= 128 && value < 160) {
                    let cp1252char = CP_1252_CHARS[value - 128].charCodeAt(0);
                    if(cp1252char == 0)
                        cp1252char = 63;
                    value = cp1252char;
                }
                charArr[offset++] = value;
            }
        }
        return String.fromCharCode(...charArr);
    }

    readNullString() {
        if(this.array[this.offset] == 0) {
            this.offset++;
            return null;
        }
        return this.readString();
    }

    readLong() {
        let l = readInt() & 0xffffffff;
		let l1 = readInt() & 0xffffffff;
		return (l << 32) + l1;
    }

    getOffset() {
        return this.offset;
    }

    setOffset(offset) {
        this.offset = offset;
    }

    getLength() {
        return this.array.length;
    }

}

module.exports = InStream;