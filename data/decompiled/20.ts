function script_20(arg0: int): string {
	if(arg0 >= 99999999) {
		return "*"
	};
	if(arg0 >= 10000000) {
		return (arg0 / 1000000).toString() + "M"
	};
	if(arg0 >= 10000) {
		return (arg0 / 1000).toString() + "K"
	};
	return arg0.toString();
}