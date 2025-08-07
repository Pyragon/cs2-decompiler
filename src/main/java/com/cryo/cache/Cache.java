package com.cryo.cache;

import com.cryo.Settings;
import com.cryo.cache.store.Store;

import java.io.IOException;

public final class Cache {

	public static Store STORE;

    private static byte[] CHECKSUM_CONTAINER;

	private Cache() {

	}
	
	public static void init(String path) throws IOException {
		STORE = new Store(path);
	}

	public static void init() throws IOException {
		STORE = new Store(Settings.PACKED_PATH);
        CHECKSUM_CONTAINER = STORE.getChecksumContainer();
	}

    public static void regenChecksum() {
        CHECKSUM_CONTAINER = STORE.getChecksumContainer();
    }

    public static byte[] getChecksumContainer() {
        return CHECKSUM_CONTAINER;
    }
}
