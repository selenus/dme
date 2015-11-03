var Expander = function(element) {
	this.element = element;
	this.active = false;
	this.expanded = false;
}

Expander.prototype.setActive = function(active) {
	this.active = active;
};

Expander.prototype.mouseDown = function() {
	this.expanded = !this.expanded;
};

Expander.prototype.getCursor = function(point) {
	return Cursors.grip;
};

Expander.prototype.hitTest = function(point) {
	return this.getRectangle().contains(point);
};

Expander.prototype.getRectangle = function() {
	var point = this.element.template.getExpanderPosition(this.element);
	var rectangle = new Rectangle(point.x, point.y, 0, 0);
	rectangle.inflate(5, 5);
	return rectangle;
};

Expander.prototype.paint = function(context) {
	if (this.active) {
		context.lineWidth = 2;
		context.strokeStyle = this.element.template.area.theme.expanderHoverBorder;
	} else {
		context.lineWidth = 1;
		context.strokeStyle = this.element.template.area.theme.expanderBorder;
	}
	var rectangle = this.getRectangle();
	context.fillStyle = this.element.template.area.theme.expander;
	context.fillRect(rectangle.x - 0.5, rectangle.y - 0.5, rectangle.width, rectangle.height);
	context.strokeRect(rectangle.x - 0.5, rectangle.y - 0.5, rectangle.width, rectangle.height);
	context.font = "bold 11px Verdana";
	context.fillStyle = context.strokeStyle;
	context.textBaseline = "bottom";
	context.textAlign = "center";
	context.fillText(this.expanded ? "-" : "+", rectangle.x + (rectangle.width / 2), rectangle.y + 11);
};