Model.prototype.initEvents = function() {
	this.mouseDownHandler = this.handleMouseDown.bind(this);
	this.mouseUpHandler = this.handleMouseUp.bind(this);
	this.mouseMoveHandler = this.handleMouseMove.bind(this);
	this.mouseLeaveHandler = this.handleMouseLeave.bind(this);
	this.mouseWheelHandler = this.mouseWheel.bind(this);
	
	this.canvas.addEventListener("mousedown", this.mouseDownHandler, false);
	this.canvas.addEventListener("mouseup", this.mouseUpHandler, false);
	this.canvas.addEventListener("mouseleave", this.mouseLeaveHandler, false);
	this.canvas.addEventListener("mousemove", this.mouseMoveHandler, false);	
	
	if (this.isMozilla) {
		this.canvas.addEventListener("DOMMouseScroll", this.mouseWheelHandler, false);
	} else {
		this.canvas.addEventListener("mousewheel", this.mouseWheelHandler, false);
	}
	this.initContextMenu();
};

Model.prototype.removeEvents = function() {
	this.canvas.removeEventListener("mousedown", this.mouseDownHandler);
	this.canvas.removeEventListener("mouseup", this.mouseUpHandler);
	this.canvas.removeEventListener("mouseleave", this.mouseLeaveHandler);
	this.canvas.removeEventListener("mousemove", this.mouseMoveHandler);
	
	if (this.isMozilla) {
		this.canvas.removeEventListener("DOMMouseScroll", this.mouseWheelHandler);
	} else {
		this.canvas.removeEventListener("mousewheel", this.mouseWheelHandler);
	}
};

Model.prototype.keyDown = function(e) {
	console.log(e);
};

Model.prototype.keyUp= function(e) {
	console.log(e);
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
	
	if (this.activeObject instanceof Area || this.activeObject instanceof VerticalScroll || 
			this.activeObject instanceof Function) {
		this.activeObject.startMove(this.mousePosition);
	} else if (this.activeObject instanceof Expander) {
		this.activeObject.mouseDown();
		this.activeObject.element.template.area.invalidate();
	} else if (this.activeObject instanceof Connector && !this.options.readOnly) {
		if (this.activeObject.isOut()) {
			this.newConnection = new Connection(this.mappingConnection, this.activeObject, null);
		} else {
			this.newConnection = new Connection(this.mappingConnection, null, this.activeObject);
		}		
		this.activeConnector = this.activeObject;
	}
	
	if (this.activeObject.isSelected!==undefined && !this.activeObject.isSelected()) {
		this.deselectAll();
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

Model.prototype.addMappingConnection = function (from, to, id, silent) {
	var c = new Connection(this.mappingConnection, from, to, id);
	
	var connectionEvent = document.createEvent("Event");
	if (!this.mappings.contains(c)) {
		this.mappings.push(c);
		connectionEvent.initEvent("newConceptMappingEvent" + this.options.eventId, true, true);
	} else {
		connectionEvent.initEvent("changeConceptMappingEvent" + this.options.eventId, true, true);
	}
	
	if (silent) {
		return;
	}
	
	
	connectionEvent.connection = c;
	document.dispatchEvent(connectionEvent);
};

Model.prototype.addOrComplementMappingConnection = function (cDrawn, to, id) {
	var fromConnector = cDrawn.from.length>0 ? cDrawn.from[0] : to;
	var toConnector = cDrawn.from.length>0 ? to : cDrawn.to[0];
	
	if (fromConnector.element instanceof Function || toConnector.element instanceof Function) {
		var connectionEvent = document.createEvent("Event");
		connectionEvent.initEvent("changeConceptMappingEvent" + this.options.eventId, true, true);
		
		if (fromConnector.element instanceof Function) {
			var c = fromConnector.addConnection(toConnector);
		} else {
			var c = toConnector.addConnection(fromConnector);
		}

		connectionEvent.connection = c;
		document.dispatchEvent(connectionEvent);
	} else {
		this.addMappingConnection(fromConnector, toConnector, id);
	}	
};

Model.prototype.leftMouseUp = function() {
	if (this.newConnection!=null && !this.options.readOnly) {
		if (this.activeObject instanceof Connector) {
			if (this.activeObject.isValid(this.activeConnector)) {
				this.addOrComplementMappingConnection(this.newConnection, this.activeObject);
			}
		} else if (this.activeObject instanceof Element) {
			for (var i=0; i<this.activeObject.connectors.length; i++) {
				if (this.activeObject.connectors[i].isValid(this.activeConnector)) {
					this.addOrComplementMappingConnection(this.newConnection, this.activeObject.connectors[i]);
					break;
				}
			}
		}
		this.newConnection=null;
		this.activeConnector=null;
	}
	if (this.activeObject instanceof Area) {
		if (this.activeObject.startMoveHandle!=null) { // It was not a move, but a 'deselect click'
			this.deselectAll();
		}
		this.activeObject.stopMove();
	} else if (this.activeObject instanceof VerticalScroll || this.activeObject instanceof Function) {
		this.activeObject.stopMove(this.mousePosition);
	}
	this.paint();
};

Model.prototype.handleMouseMove = function(e) {
	e.preventDefault();	
	this.updateMousePosition(e);

	if (this.isMouseDown) {
		if (this.activeObject instanceof Area || this.activeObject instanceof VerticalScroll || 
				this.activeObject instanceof Function) {
			this.activeObject.move(this.mousePosition);
		}
		this.paint();
	} else {
		this.updateActiveObject();
	}
};

Model.prototype.mouseWheel = function(e) {
	e.preventDefault(); 
	this.updateMousePosition(e);

	var deltaX = (!this.isMozilla) ? e.wheelDeltaX : 0;
	var deltaY = (!this.isMozilla) ? e.wheelDeltaY : e.detail*-40;
	
	for (var i=0; i<this.areas.length; i++) {
		if (this.areas[i].hitTest(this.mousePosition)) {
			this.areas[i].startMove(this.mousePosition);
			this.areas[i].move(new Point(this.mousePosition.x + deltaX, this.mousePosition.y + deltaY));
			this.areas[i].stopMove();
			break;
		}
	}
	this.update();
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
	
	for (var i=0; i<this.mappings.length; i++) {
		object = this.mappings[i].hitTest(this.mousePosition) 
		if (object!=null) {
			break; 
		}
	}
	
	if (object==null) {
		for (var i=0; i<this.areas.length; i++) {
			object = this.areas[i].hitTest(this.mousePosition);
			if (object!=null) {
				break;
			}
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
	deselectionEvent.initEvent("deselectionEvent" + this.options.eventId, true, true);
	deselectionEvent.element = element;
	document.dispatchEvent(deselectionEvent);
};

Model.prototype.handleElementSelected = function(element) {
	var selectionEvent = document.createEvent("Event");
	selectionEvent.initEvent("selectionEvent" + this.options.eventId, true, true);	
	selectionEvent.element = element;
	document.dispatchEvent(selectionEvent);
};