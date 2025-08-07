package com.cryo.cache.store;

import com.cryo.Settings;
import com.cryo.cache.IndexType;
import com.cryo.cache.store.util.CacheUtil;
import com.cryo.cache.store.util.Whirlpool;
import com.cryo.io.OutputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public final class Store {

	private Index[] indexes;
	private MainFile index255;
	private String path;
	private RandomAccessFile data;

	public Store(String path) throws IOException {
		this.path = path;
		data = new RandomAccessFile(path + "main_file_cache.dat2", "rw");
		index255 = new MainFile(255, data, new RandomAccessFile(path + "main_file_cache.idx255", "rw"));
		int idxsCount = index255.getArchivesCount();
		indexes = new Index[idxsCount];
		for (int id = 0; id < idxsCount; id++) {
			Index index = new Index(index255, new MainFile(id, data, new RandomAccessFile(path + "main_file_cache.idx" + id, "rw")));
			if (index.getTable() == null)
				continue;
			indexes[id] = index;
		}
	}
	
	public final byte[] getChecksumContainer() {
		byte[] checksumTable = getChecksumTable();
		OutputStream out = new OutputStream();
		out.writeByte(255);
		out.writeInt(255);
		out.writeByte(0);
		out.writeInt(checksumTable.length);
		int offset = 10;
		for (int index = 0; index < checksumTable.length; index++) {
			if (offset == 512) {
				out.writeByte(255);
				offset = 1;
			}
			out.writeByte(checksumTable[index]);
			offset++;
		}
		return out.toByteArray();
	}

	public final byte[] getChecksumTable() {
		OutputStream os = new OutputStream();
		os.writeByte(indexes.length);
		for (int i = 0;i < indexes.length;i++) {
			os.writeInt(indexes[i].getCRC());
			os.writeInt(indexes[i].getTable().getRevision());
			os.writeBytes(indexes[i].getWhirlpool());
		}
		
		byte[] archive = os.toByteArray();

		OutputStream temp = new OutputStream(65);
		temp.writeByte(0);
		temp.writeBytes(Whirlpool.getWhirlpool(archive, 0, archive.length));
		os.writeBytes(CacheUtil.cryptRSA(temp.toByteArray(), Settings.RSA_PRIVATE_EXPONENT, Settings.RSA_PRIVATE_MODULUS));
		return os.toByteArray();
	}
	
	public Index getIndex(IndexType index) {
		return indexes[index.ordinal()];
	}
	
	public Index[] getIndices() {
		return indexes;
	}

	public MainFile getIndex255() {
		return index255;
	}

	/*
	 * returns index
	 */
	public int addIndex(boolean named, boolean usesWhirpool, int tableCompression) throws IOException {
		int id = indexes.length;
		Index[] newIndexes = Arrays.copyOf(indexes, indexes.length + 1);
		resetIndex(id, newIndexes, named, usesWhirpool, tableCompression);
		indexes = newIndexes;
		return id;
	}

	public void resetIndex(int id, boolean named, boolean usesWhirpool, int tableCompression) throws FileNotFoundException, IOException {
		resetIndex(id, indexes, named, usesWhirpool, tableCompression);
	}

	public void resetIndex(int id, Index[] indexes, boolean named, boolean usesWhirpool, int tableCompression) throws FileNotFoundException, IOException {
		OutputStream stream = new OutputStream(4);
		stream.writeByte(5);
		stream.writeByte((named ? 0x1 : 0) | (usesWhirpool ? 0x2 : 0));
		stream.writeShort(0);
		byte[] archiveData = new byte[stream.getOffset()];
		stream.setOffset(0);
		stream.getBytes(archiveData, 0, archiveData.length);
		Archive archive = new Archive(id, tableCompression, -1, archiveData);
		index255.putArchiveData(id, archive.compress());
		indexes[id] = new Index(index255, new MainFile(id, data, new RandomAccessFile(path + "main_file_cache.idx" + id, "rw")));
	}

}
