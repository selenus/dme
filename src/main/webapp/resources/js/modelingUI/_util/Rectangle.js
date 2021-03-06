var Rectangle = function(x, y, width, height) {
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;
};

Rectangle.prototype.contains = function(point) {
	return ((point.x >= this.x) && (point.x <= (this.x + this.width)) && (point.y >= this.y) && (point.y <= (this.y + this.height)));
};

Rectangle.prototype.inflate = function(dx, dy) {
	this.x -= dx;
	this.y -= dy;
	this.width += dx + dx + 1;
	this.height += dy + dy + 1;
	return this;
};

Rectangle.prototype.clone = function() {
	return new Rectangle(this.x, this.y, this.width, this.height);
};

Rectangle.prototype.overlaps = function(rectangle) {
	if ((rectangle.x >= this.x && rectangle.x <= this.x + this.width) || 
			(rectangle.x + rectangle.width >= this.x && rectangle.x + rectangle.width <= this.x + this.width)) {
		if ((rectangle.y >= this.y && rectangle.y <= this.y + this.height) || 
				(rectangle.y + rectangle.height >= this.y && rectangle.y + rectangle.height <= this.y + this.height)) {
			return true;
		}
	}
	return false;
};

Rectangle.prototype.union = function(rectangle) {
	var x1 = (this.x < rectangle.x) ? this.x : rectangle.x;
	var y1 = (this.y < rectangle.y) ? this.y : rectangle.y;
	var x2 = ((this.x + this.width) < (rectangle.x + rectangle.width)) ? (rectangle.x + rectangle.width) : (this.x + this.width);
	var y2 = ((this.y + this.height) < (rectangle.y + rectangle.height)) ? (rectangle.y + rectangle.height) : (this.y + this.height);
	return new Rectangle(x1, y1, x2 - x1, y2 - y1);
};

Rectangle.prototype.topLeft = function() {
	return new Point(this.x, this.y);
};

Rectangle.prototype.equals = function(rectangle) {
	return this.x==rectangle.x && this.y==rectangle.y && this.width==rectangle.width && this.height==rectangle.height;
};