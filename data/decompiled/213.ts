function script_213(int0: int, int1: int, int2: int, int3: int): void {
	switch (varplayerbit_278) {
		case 1: {
			IF_SETTRANS(200, int2);
			break;
		};
		case 2: {
			IF_SETTRANS(150, int2);
			break;
		};
		case 3: {
			IF_SETTRANS(50, int2);
			break;
		};
		default: {
			IF_SETTRANS(255, int2);
			break;
		};
	};
	switch (varclient_97) {
		case 1: {
			script2766(0, 20, int3);
			break;
		};
		case 2: {
			IF_SETHIDE(0, int0);
			IF_SETMODELANIM(10708, int1);
			IF_SETHIDE(1, int2);
			script2768(10, int3);
			break;
		};
		case 3: {
			IF_SETHIDE(1, int0);
			IF_SETHIDE(1, int2);
			script2768(50, int3);
			break;
		};
		case 4: {
			IF_SETHIDE(0, int0);
			IF_SETMODELANIM(10712, int1);
			IF_SETHIDE(1, int2);
			script2768(115, int3);
			break;
		};
		case 5: {
			IF_SETHIDE(1, int0);
			IF_SETHIDE(0, int2);
			script2768(50, int3);
			break;
		};
		default: {
			IF_SETHIDE(1, int0);
			IF_SETHIDE(0, int2);
			break;
		};
	};
	return;
}