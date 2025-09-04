function script_2091(int0: int, int1: int): void {
	string0 = "";
	if(varplayerbit_5835 == 1) {
		return;
	};
	switch (int0) {
		case 54788105:
		case 54788109:
		case 54788124: {
			string0 = "The number of times that the avatar has been killed.";
			break;
		};
		case 54788106:
		case 54788110:
		case 54788121: {
			string0 = "The Slayer level required to attack each avatar.";
			break;
		};
		case 54788107:
		case 54788111:
		case 54788120: {
			string0 = "The remaining health of each avatar.";
			break;
		};
		case 54788122:
		case 54788123: {
			string0 = "How much time the game has remaining.";
			break;
		};
		case 54788104: {
			string0 = "This column displays the blue team's statistics.";
			break;
		};
		case 54788108: {
			string0 = "This column displays the red team's statistics.";
			break;
		};
		case 54788125: {
			string0 = "Shows which team controls the soul obelisk.";
			break;
		};
		case 54788126: {
			string0 = "Shows which team controls the western graveyard.";
			break;
		};
		case 54788127: {
			string0 = "Shows which team controls the eastern graveyard.";
			break;
		};
		case 54788102: {
			string0 = "Shows how much control a team has over the soul obelisk.";
			break;
		};
		case 54788165: {
			string0 = "Shows how much control a team has over the eastern graveyard.";
			break;
		};
		case 54788158: {
			string0 = "Shows how much control a team has over the western graveyard.";
			break;
		};
		case 54788152: {
			string0 = "Shows how active you have been during the game.";
			return;
			break;
		};
	};
	script39(string0, int0, int1, 25, IF_GETWIDTH(IF_GETLAYER(int1)));
	return;
}