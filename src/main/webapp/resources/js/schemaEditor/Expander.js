var Expander = function(owner) { 
	this.owner = owner;
	this.hover = false;
};

Expander.prototype.getCursor = function(point) {
	return Cursors.grip;
};

Expander.prototype.hitTest = function(rectangle)
{
	if ((rectangle.width === 0) && (rectangle.height === 0))
	{
		return this.getRectangle().contains(rectangle.topLeft());
	}
	return rectangle.contains(this.getRectangle());
};

Expander.prototype.getRectangle = function()
{
	var point = this.owner.getExpanderPosition();
	var rectangle = new Rectangle(point.x, point.y, 0, 0);
	rectangle.inflate(5, 5);
	return rectangle;
};

Expander.prototype.paint = function(context)
{
	var rectangle = this.getRectangle();
	var strokeStyle = this.owner.owner.theme.expanderBorder; 
	var fillStyle = this.owner.owner.theme.expander;
	
	if (this.owner.maxChildScore == 1) {		
		fillStyle = "rgb(70, 155, 231)";
	} else if (this.owner.maxChildScore > 0) {		
		var red = 255 - Math.round(this.owner.maxChildScore * 255);
		fillStyle = "Rgb(" + red + ", 230, 0)";
	}
	
	context.lineWidth = 1;
	
	if (this.hover)
	{
		context.lineWidth = 2;
		strokeStyle = this.owner.owner.theme.expanderHoverBorder; 
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
	context.fillText(this.owner.isExpanded ? "-" : "+", rectangle.x + (rectangle.width / 2), rectangle.y + 10);
	
	/*if (this.hover)
	{
		// Tooltip
		var text = this.owner.maxChildScore;
		context.textBaseline = "bottom";
		context.font = "8.25pt Tahoma";
		var size = context.measureText(text);
		size.height = 14;
		var a = new Rectangle(rectangle.x - Math.floor(size.width / 2), rectangle.y + size.height + 6, size.width, size.height);
		var b = new Rectangle(a.x, a.y, a.width, a.height);
		a.inflate(4, 1);
		context.fillStyle = "rgb(255, 255, 231)";
		context.fillRect(a.x - 0.5, a.y - 0.5, a.width, a.height);
		context.strokeStyle = "#000";
		context.lineWidth = 1;
		context.strokeRect(a.x - 0.5, a.y - 0.5, a.width, a.height);
		context.fillStyle = "#000";
		context.fillText(text, b.x, b.y + 13);
	}*/
};