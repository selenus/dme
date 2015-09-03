var Toolbar = function(owner, rectangle) {
	this.rectangle = rectangle;
	this.owner = owner;	
	this.style = {
		fillColor : "#FF0000",
		strokeColor : "#A0A0A0",
		innerFillColor : "#13304B",
		hPadding : 5,
		vPadding : 5,
		topTolerance : 25
	};
};

Toolbar.prototype.paint = function(context) {

	context.fillStyle = this.style.fillColor;
	context.fillRect(this.rectangle.x, this.rectangle.y, this.rectangle.width, this.rectangle.height);
	context.strokeStyle = this.style.strokeColor;
	context.lineWidth = 1;
	context.strokeRect(this.rectangle.x, this.rectangle.y, this.rectangle.width, this.rectangle.height);
};