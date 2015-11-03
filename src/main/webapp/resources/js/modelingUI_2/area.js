var Area = function(model) {
	this.model = model;
	this.theme = this.model.theme;
	this.rectangle = null;
	this.elements = [];
	this.root = null;
	this.isSource = false;
	this.isTarget = false;
	this.recalculationRequired = true;

	this.minX = null;
	this.maxX = null;
	this.minY = null;
	this.maxY = null;
	
	this.elementTemplates = [];
	for (var i=0; i<this.model.elementTemplateOptions.length; i++) {
		this.elementTemplates.push(new ElementTemplate(this, this.model.elementTemplateOptions[i]));
	}
	
	this.moveHandle = null;
	this.startMoveHandle = null;
};

Area.prototype.getCursor = function() {
	if (this.startMoveHandle==null && this.moveHandle!=null) {
		return Cursors.move;
	}
	return Cursors.arrow;
};

Area.prototype.startDrag = function(point) {
	this.moveHandle = point;
	this.startMoveHandle = point;
};

Area.prototype.drag = function(point) {
	if (this.moveHandle==null) {
		return;
	}
	
	if (this.startMoveHandle!=null) {
		var overall = new Rectangle(this.startMoveHandle.x-3, this.startMoveHandle.y-3, 6, 6); 
		if (!overall.contains(point)) {
			this.startMoveHandle = null;
		}
	}
	
	var deltaX = point.x - this.moveHandle.x;
	var deltaY = point.y - this.moveHandle.y;
	
	var tmpRect = this.rectangle.clone().inflate(-this.theme.protectedPaddingArea, -this.theme.protectedPaddingArea);
	
	/*// Make sure that the elements cannot completely leave the canvas
	if (this.isLeft) {
		// Right: No move over the middle line
		if (deltaX + this.maxX + this.innerPadding > this.rectangle.x + this.rectangle.width) {
			deltaX = this.rectangle.x + this.rectangle.width - this.maxX - this.innerPadding;
		}
		// Left: No move out of the canvas
		if ((deltaX + this.minX < this.rectangle.x) && 
				(this.maxX + deltaX < this.rectangle.x + this.innerPadding)) {
			deltaX = (this.rectangle.x + this.innerPadding) - this.maxX;
		}
	} else {
		// Left: No move over the middle line
		if (deltaX + this.minX < this.rectangle.x + this.innerPadding) {
			deltaX = this.rectangle.x - this.minX + this.innerPadding;
		}
		// Right: No move out of the canvas
		if ((deltaX + this.maxX > this.rectangle.x + this.rectangle.width) && 
				(this.minX + deltaX + this.innerPadding > this.rectangle.x + this.rectangle.width)) {
			deltaX = this.rectangle.x + this.rectangle.width - this.innerPadding - this.minX;
		}
	}

	// Bottom
	if ((deltaY + this.maxY > this.rectangle.y + this.rectangle.height) && 
			(this.minY + deltaY + this.innerPadding > this.rectangle.y + this.rectangle.height)) {
		deltaY = this.rectangle.y + this.rectangle.height - this.innerPadding - this.minY;
	}
	// Top
	if ((deltaY + this.minY < this.rectangle.y) && 
			(this.maxY + deltaY < this.rectangle.y + this.innerPadding)) {
		deltaY = (this.rectangle.y + this.innerPadding) - this.maxY;
	}*/
	
		
	this.minX = null;
	this.maxX = null;
	
	var rect = this.root.rectangle;
	rect.x += deltaX;
	rect.y += deltaY;
	
	this.root.rectangle = rect;
	
	this.invalidate();
	
	/*// Actually moving the elements with (adapted) deltas
	for (var i = 0; i< this.elements.length; i++) {
		var elem = this.elements[i];
		
		/*if (!elem.isVisible) {
			continue;
		}
		
		var rect = elem.rectangle;
		if (this.moveableX) rect.x += deltaX;
		if (this.moveableY) rect.y += deltaY;

		//this.setMinMaxX(rect.x, rect.width);
		elem.rectangle = rect;
	}*/
	
	this.moveHandle = point;
	this.model.update();
};

Area.prototype.stopDrag = function() {
	this.moveHandle = null;
	this.startMoveHandle = null;
};

Area.prototype.invalidate = function() {
	this.recalculationRequired = true;
};

Area.prototype.setActive = function(active) {
	if (active==false) {
		this.stopDrag();
	}
};

Area.prototype.setRectangle = function(rectangle) {
	this.rectangle = rectangle;
};

Area.prototype.setSize = function() {}; // Dummy placeholder...remove

Area.prototype.addElement = function(templateKey, parent, id, label, icons) {
	var template = null;
	for (var i=0; i<this.elementTemplates.length; i++) {
		if (this.elementTemplates[i].options.key===templateKey) {
			template = this.elementTemplates[i]; 
			break;
		}
	}
	var e = new Element(template, parent, id, label, icons);
	this.elements.push(e);
	if (parent==null) {
		this.root = e;
		this.root.rectangle = new Rectangle(this.model.theme.rootElementPosition.x, this.model.theme.rootElementPosition.x, 
				this.root.rectangle.width, this.root.rectangle.height);
	} else {
		var cParent = parent.getConnector("children");
		var cChild = e.getConnector("parent");
		
		cParent.registerConnection(cChild, this.model.hierarchicalConnection);
	}
	return e;
};

Area.prototype.recalculateElementPositions = function(element, elementList, x, y) {
	if (x<this.minX) { this.minX = x; }
	if (x>this.maxX) { this.maxX = x; }
	if (y<this.minY) { this.minY = y; }
	if (y>this.maxY) { this.maxY = y; }
	
	element.setRectangle(new Rectangle(x, y, element.rectangle.width, element.rectangle.height));
	if (element.expanded && element.children.length > 0) {
		for (var i=0; i<element.children.length; i++) {
			var deltaX = x + this.model.theme.elementPositioningDelta.x;
			/*if (!this.isLeft) {
				deltaX = element.rectangle.x + element.rectangle.width - 20 - element.children[i].rectangle.width;
			}*/
			y = this.recalculateElementPositions(element.children[i], elementList, deltaX, y + this.model.theme.elementPositioningDelta.y);
		}
	}
	this.recalculationRequired = false;
	return y;
};

Area.prototype.paint = function(context, theme) {
		
	context.strokeStyle = theme.areaBorderColor;
	context.lineWidth = theme.areaBorderWidth;
	context.simpleLine(this.rectangle.x + this.rectangle.width, this.rectangle.y, this.rectangle.x + this.rectangle.width, this.rectangle.height);
	
	if (this.root===null) {
		return;
	}
	
	if (this.recalculationRequired) {
		this.recalculateElementPositions(this.root, this.elements, this.root.rectangle.x, this.root.rectangle.y);
	}

	var visibleElements = this.getVisibleElements(this.root);
	var hierarchyConnections = [];
	
	if (visibleElements.length > 0) {
		// Collect and paint hierarchy connections
		for (var i=0; i<visibleElements.length; i++) {
			var connections = visibleElements[i].getHierarchyConnections();
			for (var j=0; j<connections.length; j++) {
				if (!hierarchyConnections.contains(connections[j])) {
					hierarchyConnections.push(connections[j]);
					connections[j].paint(context);
				}
			}
		}
		
		// Paint the elements now
		for (var i=0; i<visibleElements.length; i++) {
			visibleElements[i].paint(context);
		}
	}
};

Area.prototype.deselectAll = function() {
	var elements = this.getVisibleElements(this.root);

	/* Do not worry about connections here, 
	 * hierarchy connections are not selectable anyway */
	for (var i=0; i<elements.length; i++) {
		if (elements[i].selected) {
			elements[i].selected = false;
			if (this.model.selectedItems.contains(elements[i])) {
				this.model.selectedItems.remove(elements[i])
			}
			this.model.handleElementDeselected(elements[i]);
		}
	}
};



Area.prototype.getVisibleElements = function(element) {
	var result = []
	if (element!=null) {
		result.push(element);
		if (element.children.length > 0) {
			for (var i=0; i<element.children.length; i++) {
				var subResult = this.getVisibleElements(element.children[i]);
				for (var j=0; j<subResult.length; j++) {
					result.push(subResult[j]);
				}
			}
		}
	}
	return result;
};

Area.prototype.hitTest = function(position) {
	if (!this.rectangle.contains(position)) {
		return null;
	}
	
	var hitObject=null;
	var visibleElements = this.getVisibleElements(this.root);
	for (var i=0; i<visibleElements.length; i++) {
		hitObject = visibleElements[i].hitTest(position);
		if (hitObject!=null) {
			return hitObject;
		}
	}
	return this;
};