var Area = function(model, options) {
	this.index = 0;
	this.model = model;
	this.options = options;
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
	
	this.moveHandle = null;			// To determinate moving deltas
	this.startMoveHandle = null;	// Being used to determine if we have a click or drag of the area
};

Area.prototype.init = function() {
	this.verticalScroll = new VerticalScroll(this);
	
	this.elementTemplates = [];
	for (var i=0; i<this.model.elementTemplateOptions.length; i++) {
		this.elementTemplates.push(new ElementTemplate(this, this.model.elementTemplateOptions[i]));
	}
};

Area.prototype.clear = function() {
	this.root = null;
	this.elements = [];
	this.recalculationRequired = true;
};

Area.prototype.getContextMenuItems = function() {
	if (this.options.contextMenuItems!==undefined && this.options.contextMenuItems!==null) {
		return this.options.contextMenuItems;
	}
};

Area.prototype.setRectangle = function(rectangle) {	
	var xScroll = this.isTarget ? (rectangle.width-this.theme.verticalScroll.width)+rectangle.x : rectangle.x;
	var xArea = this.isTarget ? rectangle.x : this.theme.verticalScroll.width+rectangle.x;
	
	this.verticalScroll.setRectangle(new Rectangle(xScroll, rectangle.y, this.theme.verticalScroll.width, rectangle.height));
	
	if (this.root!=null) {
		var oldRelativeRectangle = this.calculateRelativeRectangle(this.root.rectangle.x, this.root.rectangle.y,
				this.root.rectangle.width, this.root.rectangle.height);
	}
	
	this.rectangle = new Rectangle(xArea, rectangle.y, rectangle.width-this.theme.verticalScroll.width, rectangle.height);
	
	// Relevant for target areas: Move elements with right anchor 
	if (this.root!=null) {
		this.root.setRectangle(this.calculateAbsoluteRectangle(oldRelativeRectangle.x, oldRelativeRectangle.y,
				this.root.rectangle.width, this.root.rectangle.height));
	}
	
	// Makes sure, that area boundaries are respected
	this.moveByDelta(0, 0);
	
	this.invalidate();
};

Area.prototype.getCursor = function() {
	if (this.startMoveHandle==null && this.moveHandle!=null) {
		return Cursors.move;
	}
	return Cursors.arrow;
};

Area.prototype.expandAll = function(expand) {
	this.expand(this.root, expand);
	this.invalidate();
	this.model.update();
}

Area.prototype.expandFromElement = function(element, expand) {
	if (typeof element !== 'object') {
		element = this.findElementById(this.root, element);
	}
	this.expand(element, expand);
	this.invalidate();
	this.model.update();
};

Area.prototype.expand = function(element, expand) {
	element.setExpanded(expand);
	
	for (var i=0; i<element.children.length; i++) {
		this.expand(element.children[i], expand);
	}
};

Area.prototype.findElementById = function(parent, id) {
	if (parent.id==id) {
		return parent;
	}
	if (parent.children!=null) {
		var result = null;
		for (var i=0; i<parent.children.length; i++) {
			result = this.findElementById(parent.children[i], id);
			if (result!=null) {
				return result;
			}
		}
	}
};

Area.prototype.resetView = function() {
	this.collapseAll(this.root, false);
	this.root.setExpanded(true);
	
	this.root.rectangle.x = this.theme.rootElementPosition.x + this.rectangle.x;
	this.root.rectangle.y = this.theme.rootElementPosition.y + this.rectangle.y;
	this.invalidate();
	
	this.model.deselectAll();
	this.model.update();
};

Area.prototype.startMove = function(point) {
	this.moveHandle = point;
	this.startMoveHandle = point;
};

Area.prototype.move = function(point) {
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
	
	this.moveByDelta(deltaX, deltaY);
	
	this.moveHandle = point;
	this.model.paint();
};

Area.prototype.moveByDelta = function(deltaX, deltaY) {

	if (this.root==null || this.minX == null || this.maxX == null || this.minY == null || this.maxY == null) {
		return;
	}
	
	// Left
	if (this.maxX + deltaX < this.rectangle.x + this.theme.paddingArea.x) {
		deltaX = this.rectangle.x + this.theme.paddingArea.x - this.maxX;
	}
	// Right
	if (this.minX + deltaX + this.theme.paddingArea.x > this.rectangle.x + this.rectangle.width) {
		deltaX = this.rectangle.x + this.rectangle.width - this.theme.paddingArea.x - this.minX;
	}	
	// Bottom
	if (this.minY + deltaY + this.theme.paddingArea.y > this.rectangle.y + this.rectangle.height) {
		deltaY = this.rectangle.y + this.rectangle.height - this.theme.paddingArea.y - this.minY;
	}
	// Top
	if (this.maxY + deltaY < this.rectangle.y + this.theme.paddingArea.y) {
		deltaY = this.rectangle.y + this.theme.paddingArea.y - this.maxY;
	}
	
	// Override if required
	if (this.isTarget) {
		// Left
		if (this.minX + deltaX < this.rectangle.x + this.theme.paddingArea.x/2) {
			deltaX = this.rectangle.x + this.theme.paddingArea.x/2 - this.minX + 0.5;
		}
	} else if (this.isSource) {
		// Right
		if (this.maxX + deltaX + this.theme.paddingArea.x/2 > this.rectangle.x + this.rectangle.width) {
			deltaX = this.rectangle.x + this.rectangle.width - this.theme.paddingArea.x/2 - this.maxX + 0.5;
		}
	}
		
	// Repositioning the root element is enough
	var rect = this.root.rectangle;
	rect.x += deltaX;
	rect.y += deltaY;
	this.root.rectangle = rect;
	
	// Force repositioning of the elements (except root)
	this.invalidate();	
};

Area.prototype.stopMove = function() {
	this.moveHandle = null;
	this.startMoveHandle = null;
};

Area.prototype.invalidate = function() {
	this.recalculationRequired = true;
};

Area.prototype.setActive = function(active) {
	if (active==false) {
		this.stopMove();
	}
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
		this.root.rectangle = this.calculateAbsoluteRectangle(this.model.theme.rootElementPosition.x, this.model.theme.rootElementPosition.y,
				this.root.rectangle.width, this.root.rectangle.height);
		this.root.setVisible(true);
	} else {
		
		var cParent = parent.getConnector("children");
		var cChild = e.getConnector("parent");
		
		var c;
		if (cParent.connections.length>0) {
			c = cParent.connections[0];
		} else {
			c = new Connection(this.model.hierarchicalConnection, cParent);
			cParent.addConnection(c);
		}
		cChild.addConnection(c);
		c.addTo(cChild);

		parent.addChild(e);
	}
	return e;
};

Area.prototype.calculateAbsoluteRectangle = function(x, y, width, height) {
	var point = this.calculateAbsolutePoint(x, y);
	if (!this.isTarget) {
		return new Rectangle(point.x, point.y, width, height);
	} else {
		return new Rectangle(point.x-width, point.y, width, height);
	}
};

Area.prototype.calculateAbsolutePoint = function(x, y) {
	var absY = y+this.rectangle.y;
	if (!this.isTarget) {
		return new Point(x+this.rectangle.x, absY);
	} else {
		return new Point(this.rectangle.x+this.rectangle.width-x, absY);
	}
};

Area.prototype.calculateRelativeRectangle = function(x, y, width, height) {
	var point = this.calculateRelativePoint(x, y);
	if (!this.isTarget) {
		return new Rectangle(point.x, point.y, width, height);
	} else {
		return new Rectangle(point.x-width, point.y, width, height);
	}
};

Area.prototype.calculateRelativePoint = function(x, y) {
	var relY = y-this.rectangle.y;
	if (!this.isTarget) {
		return new Point(x-this.rectangle.x, relY);
	} else {
		return new Point(this.rectangle.x+this.rectangle.width-x, relY);
	}
};

Area.prototype.recalculateAll = function() {	
	if (this.recalculationRequired && this.root!=null) {
		this.minX = null;
		this.maxX = null;
		this.minY = null;
		this.maxY = null;
		this.recalculateElementPositions(this.root, this.elements, this.root.rectangle.x, this.root.rectangle.y);
	}
};

Area.prototype.recalculateElementPositions = function(element, elementList, x, y) {
	var rectangle = new Rectangle(x, y, element.rectangle.width, element.rectangle.height);
	
	this.recalculateMinMaxBoundaries(rectangle);
	element.setRectangle(rectangle);
	if (/*element.isExpanded() && */element.children.length > 0) {
		for (var i=0; i<element.children.length; i++) {
			if (!element.children[i].isVisible()) {
				continue;
			}
			var deltaX;
			if (!this.isTarget) {
				deltaX = x + this.model.theme.elementPositioningDelta.x;
			} else {
				deltaX = x - element.children[i].rectangle.width + element.rectangle.width - this.model.theme.elementPositioningDelta.x;
			}
			y = this.recalculateElementPositions(element.children[i], elementList, deltaX, y + this.model.theme.elementPositioningDelta.y);
		}
	}
	this.recalculationRequired = false;
	return y;
};

Area.prototype.recalculateMinMaxBoundaries = function(rectangle) {
	if (this.minX == null || rectangle.x < this.minX) { 
		this.minX = rectangle.x;
	}
	if (this.maxX == null || rectangle.x+rectangle.width > this.maxX) { 
		this.maxX = rectangle.x+rectangle.width; 
	}
	if (this.minY == null || rectangle.y < this.minY) { 
		this.minY = rectangle.y;
		this.verticalScroll.displayedMinY = this.minY;
	}
	if (this.maxY == null || rectangle.y+rectangle.height > this.maxY) { 
		this.maxY = rectangle.y+rectangle.height;
		this.verticalScroll.displayedMaxY = this.maxY;
	}	
}

Area.prototype.paint = function(context, theme) {
	context.strokeStyle = this.theme.areaBorderColor;
	context.lineWidth = this.theme.areaBorderWidth;
	context.strokeRect(this.rectangle.x, this.rectangle.y, this.rectangle.width, this.rectangle.height);
};

Area.prototype.paintElements = function(context) {
	if (this.root===null) {
		return;
	}
	
	this.recalculateAll();
	
	var visibleElements = this.getElements(this.root, true);
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
		//this.root.paint(context);
	}
	
	this.verticalScroll.paint(context);
};

Area.prototype.deselectAll = function() {
	var elements = this.getElements(this.root);

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


Area.prototype.getElements = function(element, visibleOnly) {
	var result = []
	if (element!=null && (visibleOnly===undefined || visibleOnly===false || element.isVisible())) {
		result.push(element);
		if (element.children.length > 0 && (visibleOnly===undefined || visibleOnly===false || element.isExpanded())) {
			for (var i=0; i<element.children.length; i++) {
				var subResult = this.getElements(element.children[i], visibleOnly);
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
		return this.verticalScroll.hitTest(position);
	}	
	
	var hitObject=null;
	var visibleElements = this.getElements(this.root, true);
	for (var i=0; i<visibleElements.length; i++) {
		hitObject = visibleElements[i].hitTest(position);
		if (hitObject!=null) {
			return hitObject;
			
		}
	}	
	return this;
};

Area.prototype.getElementById = function(id, parent) {
	if (parent===undefined) {
		parent = this.root;
	}
	if (parent.id==id) {
		return parent;
	} else if (parent.children.length>0) {
		for (var i=0; i<parent.children.length; i++) {
			var result = this.getElementById(id, parent.children[i]); 
			if (result!=null) {
				return result;
			}
		}
	} else { 
		return null;
	}
};

Area.prototype.getExpandedElementIds = function(parent) {
	var expandedIds = [];
	if (parent.getExpanded()) {
		expandedIds.push(parent.id);
		if (parent.children!=null) {
			for (var i=0; i<parent.children.length; i++) {
				var expandedSubIds = this.getExpandedElementIds(parent.children[i]); 
				if (expandedSubIds.length > 0) {
					for (var j=0; j<expandedSubIds.length; j++) {
						expandedIds.push(expandedSubIds[j]);
					}
				}
			}
		}
	}
	return expandedIds;
};

Area.prototype.selectElementsByIds = function(parent, ids) {
	if (ids.contains(parent.id)) {
		this.model.select(parent);
	}
	if (parent.children!==null) {
		for (var i=0; i<parent.children.length; i++) {
			this.selectElementsByIds(parent.children[i], ids);
		}
	}
};

Area.prototype.expandElementsByIds = function(parent, ids) {
	if (ids.contains(parent.id)) {
		parent.setExpanded(true);
	}
	if (parent.children!==null) {
		for (var i=0; i<parent.children.length; i++) {
			this.expandElementsByIds(parent.children[i], ids);
		}
	}
};