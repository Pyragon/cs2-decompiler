function script_144(int0: int, int1: int, int2: int, int3: int): void {
	int4 = 0;
	if((CLIENT_CLOCK() % 40) < 20) {
		int4 = (CLIENT_CLOCK() - int2);
		int4 = (int4 * 255);
		int4 = (int4 / (int3 - int2));
		CC_SETTRANS(int4);
	} else {
		CC_SETTRANS(255);
		return;
	};
}