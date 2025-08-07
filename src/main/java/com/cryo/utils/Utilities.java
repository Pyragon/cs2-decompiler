package com.cryo.utils;

public class Utilities {

	public static char[] CP_1252_CHARACTERS = { '\u20ac', '\0', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020', '\u2021', '\u02c6',
			'\u2030', '\u0160', '\u2039', '\u0152', '\0', '\u017d', '\0', '\0', '\u2018', '\u2019', '\u201c',
			'\u201d', '\u2022', '\u2013', '\u2014', '\u02dc', '\u2122', '\u0161', '\u203a', '\u0153',
			'\0', '\u017e', '\u0178' };

	public static String readString(byte[] buffer, int i_1, int i_2) {
		char[] arr_4 = new char[i_2];
		int offset = 0;

		for (int i_6 = 0; i_6 < i_2; i_6++) {
			int i_7 = buffer[i_6 + i_1] & 0xff;
			if (i_7 != 0) {
				if (i_7 >= 128 && i_7 < 160) {
					char var_8 = CP_1252_CHARACTERS[i_7 - 128];
					if (var_8 == 0) {
						var_8 = 63;
					}

					i_7 = var_8;
				}

				arr_4[offset++] = (char) i_7;
			}
		}

		return new String(arr_4, 0, offset);
	}

	public static final int packGJString2(int position, byte[] buffer, String String) {
		int length = String.length();
		int offset = position;
		for (int index = 0; length > index; index++) {
			int character = String.charAt(index);
			if (character > 127) {
				if (character > 2047) {
					buffer[offset++] = (byte) ((character | 919275) >> 12);
					buffer[offset++] = (byte) (128 | ((character >> 6) & 63));
					buffer[offset++] = (byte) (128 | (character & 63));
				} else {
					buffer[offset++] = (byte) ((character | 12309) >> 6);
					buffer[offset++] = (byte) (128 | (character & 63));
				}
			} else buffer[offset++] = (byte) character;
		}
		return offset - position;
	}
}
