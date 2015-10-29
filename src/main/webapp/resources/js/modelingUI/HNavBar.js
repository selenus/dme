var HNavBar = function(owner, rectangle) {
	this.rectangle = rectangle;
	this.innerRectangle = null;
	this.displayedMinY = 0;
	this.displayedMaxY = 0;
	this.handle = null;
	this.owner = owner;
	this.sizeFactor = 0;
	
	this.style = {
		fillColor : "#FFFFFF",
		strokeColor : "#DDD",
		innerFillColor : "#56516b",
		hPadding : 5,
		vPadding : 5,
		topTolerance : 25
	};
};

HNavBar.prototype.hitTest = function(point) {
	return this.rectangle.contains(point);
};

HNavBar.prototype.startMove = function(point) {
	// Not in innerBar -> jump
	if (!this.innerRectangle.contains(point)) {
		this.handle = new Point(point.x, this.innerRectangle.y + this.innerRectangle.height/2);
		this.owner.handle = this.handle;
		this.move(point);
		this.handle = null;
		this.owner.handle = null;
	} else {
		this.handle = point;
		this.owner.handle = point;
	}
};

HNavBar.prototype.move = function(point) {
	if (this.handle===null) {
		return;
	}
	
	var deltaY = point.y - this.handle.y;		
	this.owner.move(new Point(this.owner.handle.x, this.owner.handle.y - (deltaY / this.sizeFactor)));
	
	this.handle = new Point(this.handle.x, this.handle.y + deltaY);
};

HNavBar.prototype.calculateInnerBar = function(minY, maxY) {
	
	var innerH = this.rectangle.height - this.rectangle.y;
	var outerH = maxY - minY;
	
	if (innerH > outerH) {
		this.sizeFactor = 1;
	} else {
		this.sizeFactor = innerH / outerH;
	}
	
	var innerBarHeight = Math.floor(this.sizeFactor * (this.rectangle.height - 10));
	var innerBarY = 0;
	
	if (minY < 0 || (outerH >= innerH && maxY > this.rectangle.height)) {
		innerBarY = this.rectangle.height * (minY / outerH) * -1;
	} else if (outerH < innerH && maxY > this.rectangle.height) {
		innerBarY = ((maxY - this.rectangle.height) / outerH) * this.rectangle.height * -1;
	}
	
	this.innerRectangle = new Rectangle(this.rectangle.x + this.style.hPadding, 
										this.rectangle.y + innerBarY + this.style.hPadding, 
										this.rectangle.width - (2 * this.style.vPadding), 
										innerBarHeight);
};

HNavBar.prototype.paint = function(context, minY, maxY, isLeft) {
	if (minY != this.displayedMinY || maxY != this.displayedMaxY) {
		this.calculateInnerBar(minY, maxY);
	}
	
	
	context.strokeStyle = this.style.strokeColor;
	context.lineWidth = 2;
	context.beginPath();
	if (isLeft) {
		context.moveTo(this.rectangle.width, this.rectangle.y);
		context.lineTo(this.rectangle.width, this.rectangle.height);
	} else {
		context.moveTo(this.rectangle.x, this.rectangle.y);
		context.lineTo(this.rectangle.x, this.rectangle.height);
	}
	context.stroke();
	
	
	
	context.fillStyle = this.style.fillColor;
	context.fillRect(this.rectangle.x, this.rectangle.y, this.rectangle.width, this.rectangle.height);
	context.strokeStyle = this.style.strokeColor;
	//context.lineWidth = 1;
	//context.strokeRect(this.rectangle.x, this.rectangle.y, this.rectangle.width, this.rectangle.height);
	
	context.fillStyle = this.style.innerFillColor;
	context.fillRect(this.innerRectangle.x, this.innerRectangle.y, this.innerRectangle.width, this.innerRectangle.height);
};