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
	
	if (this.activeObject==undefined || this.activeObject==null) {
		return;
	}

	// left-click
	if (e.button === 0) {
		if (this.activeObject instanceof Area) {
			this.activeObject.startDrag(this.mousePosition);
		} else if (this.activeObject.selected!==undefined) {
			var select = !this.activeObject.selected;
			this.deselectAll();
			if (select) {
				this.activeObject.selected=true;
				if (!this.selectedItems.contains(this.activeObject)) {
					this.selectedItems.push(this.activeObject)
				}
				this.handleElementSelected(this.activeObject);
			}
		}
		this.paint();
	}
};

Model.prototype.deselectAll = function() {
	for (var i=0; i<this.areas.length;i++) {
		this.areas[i].deselectAll();
	}
};

Model.prototype.mouseUp = function(e) {
	e.preventDefault();
	
	// left-click
	if (e.button === 0) {
		if (this.activeObject instanceof Area) {
			if (this.activeObject.startMoveHandle!=null) {
				this.deselectAll();
			}
			this.activeObject.stopDrag();
		}
		this.paint();
	}
};

Model.prototype.mouseMove = function(e) {
	e.preventDefault();	
	this.updateMousePosition(e);
	
	this.updateActiveObject();
	
	// left-click
	if (e.button === 0) {
		if (this.activeObject instanceof Area) {
			this.activeObject.drag(this.mousePosition);
		}
	}
};

Model.prototype.mouseLeave = function(e) {
	e.preventDefault(); 
	this.updateMousePosition(e);
	
	if (this.activeObject instanceof Area) {
		this.activeObject.stopDrag();
	}
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

Model.prototype.handleElementDeselected = function(element) {
	var deselectionEvent = document.createEvent("Event");
	deselectionEvent.initEvent("deselectionEvent", true, true);
	deselectionEvent.element = element;
	document.dispatchEvent(deselectionEvent);
};

Model.prototype.handleElementSelected = function(element) {
	var selectionEvent = document.createEvent("Event");
	selectionEvent.initEvent("selectionEvent", true, true);	
	selectionEvent.element = element;
	document.dispatchEvent(selectionEvent);
};