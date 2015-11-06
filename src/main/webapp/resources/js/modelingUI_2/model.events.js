Model.prototype.initEvents = function() {
	this.mouseDownHandler = this.handleMouseDown.bind(this);
	this.mouseUpHandler = this.handleMouseUp.bind(this);
	this.mouseMoveHandler = this.handleMouseMove.bind(this);
	this.mouseLeaveHandler = this.handleMouseLeave.bind(this);
	
	this.canvas.addEventListener("mousedown", this.mouseDownHandler, false);
	this.canvas.addEventListener("mouseup", this.mouseUpHandler, false);
	this.canvas.addEventListener("mouseleave", this.mouseLeaveHandler, false);
	this.canvas.addEventListener("mousemove", this.mouseMoveHandler, false);	
	
	/*if (this.handleContextMenu!==undefined) {
		this.contextMenuHandler = this.handleContextMenu.bind(this);
		this.canvas.addEventListener("contextmenu", this.contextMenuHandler, false);
		this.initContextMenu();
	}*/
	this.initContextMenu();
};

Model.prototype.removeEvents = function() {
	this.canvas.removeEventListener("mousedown", this.mouseDownHandler);
	this.canvas.removeEventListener("mouseup", this.mouseUpHandler);
	this.canvas.removeEventListener("mouseleave", this.mouseLeaveHandler);
	this.canvas.removeEventListener("mousemove", this.mouseMoveHandler);
	
	/*if (this.handleContextmenu!==undefined) {
		this.canvas.removeEventListener("contextmenu", this.handleContextmenu, false);
	}*/
};

Model.prototype.handleMouseDown = function(e) {
	e.preventDefault();
	this.updateMousePosition(e);
	this.updateActiveObject();
	this.canvas.focus();
		
	if (e.button === 0) {
		this.isMouseDown = true;
		this.leftMouseDown(e);
	}
};

Model.prototype.leftMouseDown = function() {
	if (this.activeObject==undefined || this.activeObject==null) {
		return;
	}
	
	if (this.activeObject instanceof Area || this.activeObject instanceof VerticalScroll) {
		this.activeObject.startMove(this.mousePosition);
	} else if (this.activeObject instanceof Expander) {
		this.activeObject.mouseDown();
		this.activeObject.element.template.area.invalidate();
	} else if (this.activeObject instanceof Connector) {
		this.newConnection = new Connection(this.mappingConnection, this.activeObject, null);
	} else if (this.activeObject.selected!==undefined) {
		this.select(this.activeObject);
	}
	this.paint();
}

Model.prototype.handleMouseUp = function(e) {
	e.preventDefault();
	this.updateMousePosition(e);
	this.updateActiveObject();
	
	if (e.button === 0 && this.isMouseDown) {
		this.leftMouseUp();
		this.isMouseDown=false;
	}
};

Model.prototype.leftMouseUp = function() {
	if (this.newConnection!=null) {
		this.newConnection=null;
	}
	if (this.activeObject instanceof Area) {
		if (this.activeObject.startMoveHandle!=null) {
			this.deselectAll();
		}
		this.activeObject.stopMove();
	} else if (this.activeObject instanceof VerticalScroll) {
		this.activeObject.stopMove(this.mousePosition);
	}
	this.paint();
};

Model.prototype.handleMouseMove = function(e) {
	e.preventDefault();	
	this.updateMousePosition(e);
	this.updateActiveObject();

	if (this.isMouseDown) {
		if (this.newConnection!=null) {
			this.paint();
		}
		
		if (this.activeObject instanceof Area || this.activeObject instanceof VerticalScroll) {
			this.activeObject.move(this.mousePosition);
		}
	}
};

Model.prototype.handleMouseLeave = function(e) {
	e.preventDefault(); 
	this.updateMousePosition(e);
	this.updateActiveObject();
	this.leftMouseUp(e);
	this.isMouseDown=false;
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

Model.prototype.updateActiveObject = function() {
	var object = null;
	for (var i=0; i<this.areas.length; i++) {
		object = this.areas[i].hitTest(this.mousePosition);
		if (object!=null) {
			break;
		}
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