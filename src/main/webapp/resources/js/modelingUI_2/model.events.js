Model.prototype.initEvents = function() {
	this.mouseDownHandler = this.mouseDown.bind(this);
	this.mouseUpHandler = this.mouseUp.bind(this);
	this.mouseMoveHandler = this.mouseMove.bind(this);
	this.mouseLeaveHandler = this.mouseLeave.bind(this);
	//this.handleContextmenu = this.handleContextmenu.bind(this);
	
	this.canvas.addEventListener("mousedown", this.mouseDownHandler, false);
	this.canvas.addEventListener("mouseup", this.mouseUpHandler, false);
	this.canvas.addEventListener("mouseleave", this.mouseLeaveHandler, false);
	this.canvas.addEventListener("mousemove", this.mouseMoveHandler, false);	
	//this.canvas.addEventListener("contextmenu", this.handleContextmenu, false);  
};

Model.prototype.removeEvents = function() {
	this.canvas.removeEventListener("mousedown", this.mouseDownHandler);
	this.canvas.removeEventListener("mouseup", this.mouseUpHandler);
	this.canvas.removeEventListener("mouseleave", this.mouseLeaveHandler);
	this.canvas.removeEventListener("mousemove", this.mouseMoveHandler);
	//this.canvas.removeEventListener("contextmenu", this.handleContextmenu, false);
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

Model.prototype.handleContextmenu = function(e) {
	alert ("No context menu");
	
	$.contextMenu({
       /* selector: '.context-menu-one',*/ 
        build: function($trigger, e) {
            // this callback is executed every time the menu is to be shown
            // its results are destroyed every time the menu is hidden
            // e is the original contextmenu event, containing e.pageX and e.pageY (amongst other data)
            return {
                callback: function(key, options) {
                    var m = "clicked: " + key;
                    window.console && console.log(m) || alert(m); 
                },
                items: {
                    "edit": {name: "Edit", icon: "edit"},
                    "cut": {name: "Cut", icon: "cut"},
                    "copy": {name: "Copy", icon: "copy"},
                    "paste": {name: "Paste", icon: "paste"},
                    "delete": {name: "Delete", icon: "delete"},
                    "sep1": "---------",
                    "quit": {name: "Quit", icon: function($element, key, item){ return 'context-menu-icon context-menu-icon-quit'; }}
                }
            };
        }
    });
	
    // prevents the usual context from popping up
    e.preventDefault()
    return(false); 
};

Model.prototype.mouseDown = function(e) {
	e.preventDefault();
	this.canvas.focus();
	
	if (this.activeObject==undefined || this.activeObject==null) {
		return;
	}

	// left-click
	if (e.button === 0) {
		if (this.activeObject instanceof Area || this.activeObject instanceof VerticalScroll) {
			this.activeObject.startMove(this.mousePosition);
		} else if (this.activeObject instanceof Expander) {
			this.activeObject.mouseDown();
			this.activeObject.element.template.area.invalidate();
		} else if (this.activeObject.selected!==undefined) {
			this.select(this.activeObject);
		}
		this.paint();
	}
};

Model.prototype.select = function(object) {
	if (object!=null && !object.selected) {
		this.deselectAll();
		object.selected=true;
		if (!this.selectedItems.contains(object)) {
			this.selectedItems.push(object)
		}
		this.handleElementSelected(object);
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
			this.activeObject.stopMove();
		} else if (this.activeObject instanceof VerticalScroll) {
			this.activeObject.stopMove(this.mousePosition);
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
		if (this.activeObject instanceof Area || this.activeObject instanceof VerticalScroll) {
			this.activeObject.move(this.mousePosition);
		}
	}
};

Model.prototype.mouseLeave = function(e) {
	e.preventDefault(); 
	this.updateMousePosition(e);
	this.mouseUp(e);
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