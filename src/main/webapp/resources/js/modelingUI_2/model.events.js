Model.prototype.initEvents = function() {
	this.mouseDownHandler = this.mouseDown.bind(this);
	this.mouseUpHandler = this.mouseUp.bind(this);
	this.mouseMoveHandler = this.mouseMove.bind(this);
	this.mouseLeaveHandler = this.mouseLeave.bind(this);
	
	this.canvas.addEventListener("mousedown", this.mouseDownHandler, false);
	this.canvas.addEventListener("mouseup", this.mouseUpHandler, false);
	this.canvas.addEventListener("mouseleave", this.mouseLeaveHandler, false);
	this.canvas.addEventListener("mousemove", this.mouseMoveHandler, false);	
};

Model.prototype.removeEvents = function() {
	this.canvas.removeEventListener("mousedown", this.mouseDownHandler);
	this.canvas.removeEventListener("mouseup", this.mouseUpHandler);
	this.canvas.removeEventListener("mouseleave", this.mouseLeaveHandler);
	this.canvas.removeEventListener("mousemove", this.mouseMoveHandler);
};

Model.prototype.updateMousePosition = function(e) {
	this.mousePosition = new Point(e.pageX, e.pageY);
	var node = this.canvas;
	while (node !== null) {
		this.mousePosition.x -= node.offsetLeft;
		this.mousePosition.y -= node.offsetTop;
		node = node.offsetParent;
	}
};

Model.prototype.mouseDown = function(e) {
	e.preventDefault();
	this.canvas.focus();
	//this.updateMousePosition(e);
};

Model.prototype.mouseUp = function(e) {
	e.preventDefault();
	//this.updateMousePosition(e);	
};

Model.prototype.mouseMove = function(e) {
	e.preventDefault();	
	this.updateMousePosition(e);
	
	this.updateActiveObject();
};

Model.prototype.mouseLeave = function(e) {
	e.preventDefault(); 
	this.updateMousePosition(e);
};

Model.prototype.updateActiveObject = function() {
	var object = null;
	for (var i=0; i<this.areas.length; i++) {
		object = this.areas[i].hitTest(this.mousePosition);
	}
	
	if (object!==this.activeObject) {
		if(this.activeObject!=null) {
			this.activeObject.setActive(false);
		}
		if(object!=null) {
			object.setActive(true);
		}
		this.activeObject = object;
		this.paint();
	}
};