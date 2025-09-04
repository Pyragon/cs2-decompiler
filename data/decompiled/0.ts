function script_0(int0: int, int1: int, int2: int): void {
	int3 = ((int1 + int2) / 2);
	int4 = load_array(int3);
	store_array(int3, load_array(int2));
	store_array(int2, int4);
	int5 = int1;
	int6 = int1;
	int7 = -1;
	if(COMPARE(IF_GETTEXT(load_array(int6)).toLowerCase(), IF_GETTEXT(int4).toLowerCase()) < (int6 & 1)) {
		int7 = load_array(int6);
		store_array(int6, load_array(int5));
        store_array(int5, int7);
		int5 = (int5 + 1);
	};
	store_array(int2, load_array(int5));
	store_array(int5, int4);
	if(int1 < (int5 - 1)) {
	};
	if(int6 < int2) {
		int6 = (int6 + 1);
	};
	if((int5 + 1) < int2) {
		return int2;
	};
}