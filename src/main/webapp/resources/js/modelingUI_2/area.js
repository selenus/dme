var Area = function(model) {
	this.model = model;
	this.rectangle = null;
	this.elements = [];
	this.root = null;
	this.isSource = false;
	this.isTarget = false;
	this.recalculationRequired = true;

	this.elementTemplates = [];
	for (var i=0; i<this.model.elementTemplateOptions.length; i++) {
		this.elementTemplates.push(new ElementTemplate(this, this.model.elementTemplateOptions[i]));
	}
};

Area.prototype.invalidate = function() {
	this.recalculationRequired = true;
};

Area.prototype.setActive = function(active) {};

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