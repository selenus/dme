var VerticalScroll = function(area) {
	this.rectangle = null;
	this.innerRectangle = null;
	this.handle = null;
	this.area = area;
	this.sizeFactor = 0;
	
	this.lastMinY = null;
	this.lastMaxY = null;
};

VerticalScroll.prototype.setRectangle = function(rectangle) {
	this.rectangle = rectangle;
};

VerticalScroll.prototype.hitTest = function(point) {
	if(this.rectangle.contains(point)) {
		return this;
	}
	return null;
};

VerticalScroll.prototype.startMove = function(point) {
	// Not in innerBar -> jump
	if (!this.innerRectangle.contains(point)) {
		this.handle = new Point(point.x, this.innerRectangle.y + this.innerRectangle.height/2);
		this.area.moveHandle = this.handle;
		this.move(point);
		this.handle = null;
		this.area.moveHandle = null;
	} else {
		this.handle = point;
		this.area.moveHandle = point;
	}
};

VerticalScroll.prototype.move = function(point) {
	if (this.handle===null) {
		return;
	}
	
	var deltaY = point.y - this.handle.y;		
	this.area.move(new Point(this.area.moveHandle.x, this.area.moveHandle.y - (deltaY / this.sizeFactor)));
	
	this.handle = new Point(this.handle.x, this.handle.y + deltaY);
};

VerticalScroll.prototype.stopMove = function() {
	this.handle = null;
	this.area.stopMove();
};

VerticalScroll.prototype.setActive = function(active) {
	if (active==false) {
		this.stopMove();
	}
};

VerticalScroll.prototype.getCursor = function() {
	return Cursors.arrow;
};

VerticalScroll.prototype.calculateInnerBar = function() {
	var innerH = this.rectangle.height - this.rectangle.y;
	var outerH = this.area.maxY - this.area.minY;
	
	if (innerH > outerH) {
		this.sizeFactor = 1;
	} else {
		this.sizeFactor = innerH / outerH;
	}
	
	var innerBarHeight = Math.floor(this.sizeFactor * (this.rectangle.height - 10));
	var innerBarY = 0;
	
	if (this.area.minY < 0 || (outerH >= innerH && this.area.maxY > this.rectangle.height)) {
		innerBarY = this.rectangle.height * (this.area.minY / outerH) * -1;
	} else if (outerH < innerH && this.area.maxY > this.rectangle.height) {
		innerBarY = ((this.area.maxY - this.rectangle.height) / outerH) * this.rectangle.height * -1;
	}
	
	this.innerRectangle = new Rectangle(this.rectangle.x + this.area.theme.verticalScroll.hPadding, 
										this.rectangle.y + innerBarY + this.area.theme.verticalScroll.hPadding, 
										this.rectangle.width - (2 * this.area.theme.verticalScroll.vPadding), 
										innerBarHeight);
	
	this.lastMaxY = this.area.maxY;
	this.lastMinY = this.area.minY;
};

VerticalScroll.prototype.paint = function(context) {
	if (this.lastMaxY!=this.area.maxY || this.lastMinY!=this.area.minY) {
		this.calculateInnerBar();
	}
	context.clearRect(this.rectangle.x, this.rectangle.y, this.rectangle.width, this.rectangle.height);
		
	context.strokeStyle = this.area.theme.areaBorderColor;
	context.lineWidth = this.area.theme.areaBorderWidth;
	context.fillStyle = this.area.theme.verticalScroll.fillColor;
	context.fillRect(this.rectangle.x, this.rectangle.y, this.rectangle.width, this.rectangle.height);
	context.strokeRect(this.rectangle.x, this.rectangle.y, this.rectangle.width, this.rectangle.height);
	
	context.strokeStyle = this.area.theme.verticalScroll.innerStrokeColor;
	context.fillStyle = this.area.theme.verticalScroll.innerFillColor;
	
	if (this.area.theme.verticalScroll.radius > 0) {
		context.drawRoundRect(this.innerRectangle.x, this.innerRectangle.y, this.innerRectangle.width, this.innerRectangle.height, this.area.theme.verticalScroll.radius, true, true);
	} else {
		context.drawRound(this.innerRectangle.x, this.innerRectangle.y, this.innerRectangle.width, this.innerRectangle.height, true, true);
	}
};