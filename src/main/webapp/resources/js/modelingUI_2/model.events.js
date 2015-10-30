var Graph = function(canvas, theme) {
	
	this.canvas = canvas;
	this.canvas.focus();
	this.context = this.canvas.getContext("2d");
	
	this.theme = ModelingTheme;
	if (theme!=undefined && theme!=null) {
		this.theme = $.extend({}, ModelingTheme, theme);
	}
	
	this.areas = [];
}