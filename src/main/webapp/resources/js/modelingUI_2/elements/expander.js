var Expander = function(element) {
	this.element = element;
	this.active = false;
}

Expander.prototype.getCursor = function(point) {
	return Cursors.grip;
};

Expander.prototype.hitTest = function(rectangle) {
	return false;
};

Expander.prototype.getRectangle = function() {
	var point = this.element.template.getExpanderPosition(this.element);
	var rectangle = new Rectangle(point.x, point.y, 0, 0);
	rectangle.inflate(5, 5);
	return rectangle;
};

Expander.prototype.paint = function(context)
{
	var rectangle = this.getRectangle();
	var strokeStyle = this.element.template.area.theme.expanderBorder; 
	var fillStyle = this.element.template.area.theme.expander;
	
	/*if (this.owner.maxChildScore == 1) {		
		fillStyle = "rgb(70, 155, 231)";
	} else if (this.owner.maxChildScore > 0) {		
		var red = 255 - Math.round(this.owner.maxChildScore * 255);
		fillStyle = "Rgb(" + red + ", 230, 0)";
	}*/
	
	context.lineWidth = 1;
	
	if (this.active)
	{
		context.lineWidth = 2;
		strokeStyle = this.element.template.area.theme.expanderHoverBorder; 
	} 

	context.strokeStyle = strokeStyle;
	context.lineCap = "butt";
	context.fillStyle = fillStyle;
	context.fillRect(rectangle.x - 0.5, rectangle.y - 0.5, rectangle.width, rectangle.height);
	context.strokeRect(rectangle.x - 0.5, rectangle.y - 0.5, rectangle.width, rectangle.height);
	context.font = "bold 10px Verdana";
	context.fillStyle = context.strokeStyle;
	context.textBaseline = "bottom";
	context.textAlign = "center";
	context.fillText(this.element.expanded ? "-" : "+", rectangle.x + (rectangle.width / 2), rectangle.y + 10);
};