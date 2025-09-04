function script_669(int0: int, int1: int, int2: int): int {
	int0 = 0;
	int1 = 0;
	if(int1 == 0) {
		return 0;
	};
	if(int0 == 0 || int2 == 0) {
		return 0;
	};
	if((int0 / int1) >= (2147483647 / int2)) {
		return 2147483647;
	};
	if((((int0 % int1) * (int2 % int1)) % int1) > (int1 / 2)) {
		return (SCALE(int0, int1, int2) + 1);
	};
	return SCALE(int0, int1, int2);
}