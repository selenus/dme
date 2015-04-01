var Area = function(graph, rectangle, isLeft) {
	this.rectangle = null;
	this.root = null;
	this.elements = [];
	this.owner = graph;
	this.theme = graph.theme;
	this.handle = null;
	this.moveableX = true;
	this.moveableY = true;
	this.minX = null;
	this.maxX = null;
	this.minY = null;
	this.maxY = null;
	this.innerPadding = 25;
	this.isLeft = isLeft;
	this.hNavBar = null;
	this.resetPoint = null;
	
	this.setSize(rectangle);
};

Area.prototype.setSize = function(rectangle) {
	
	var deltaX = 0;
	var oldSpanX = 0;
	
	if (this.rectangle !== null) {
		oldSpanX = this.rectangle.x + this.rectangle.width;
	}
	
	this.rectangle = new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	
	// Make room for the horizontal "scrollbar"
	if (this.isLeft) {
		this.rectangle.x += 30;
	} 
	this.rectangle.width -= 30;
	
	
	this.hNavBar = new HNavBar(this, new Rectangle(this.isLeft ? this.rectangle.x - 30 : this.rectangle.x + this.rectangle.width,
											 this.rectangle.y,  
											 30, this.rectangle.height));
	
	if (this.isLeft == false && this.rectangle !== null && this.root !== null) {
		deltaX = this.rectangle.width + this.rectangle.x - oldSpanX;
		
		this.resetPoint.x = this.resetPoint.x + deltaX;
		
		this.root.rectangle.x = this.resetPoint.x;
	}
	
	
};

Area.prototype.hitTest = function(point) {
	return this.rectangle.contains(point) || this.hNavBar.hitTest(point);
};

Area.prototype.pointerDown = function(point) {
	if (this.rectangle.contains(point)) {
		this.handle = point;
	} else if (this.hNavBar.hitTest(point)) {
		this.hNavBar.startMove(point);
	}
};

Area.prototype.performAction = function(action) {
	if (action==="expandAll") {
		this.expandAll(this.root, true);
	} else if (action==="collapseAll") {
		this.expandAll(this.root, false);
	} else if (action==="resetView") {
		this.resetView();
	}
	
	this.owner.update();
};

Area.prototype.pointerUp = function(point) {
	this.handle = null;
	this.hNavBar.handle = null;
};

Area.prototype.pointerMove = function(point) {
	
	if (this.hitTest(point)) {
		if (this.hNavBar.handle !== null) {
			this.hNavBar.move(point);
		} else {
			this.move(point);
		}
	} else {
		this.handle = null;
		this.hNavBar.handle = null;
	}
};

Area.prototype.getCursor = function()
{
	return (this.handle !== null) ? Cursors.move : Cursors.arrow;
};

Area.prototype.invalidate = function() {
	
};

Area.prototype.expandAll = function(element, expand) {
	element.setExpanded(expand);
	
	for (var i=0; i<element.children.length; i++) {
		this.expandAll(element.children[i], expand);
	}
};

Area.prototype.resetView = function() {
	this.expandAll(this.root, false);
	this.root.setExpanded(true);
	
	this.root.rectangle.x = this.resetPoint.x;
	this.root.rectangle.y = this.resetPoint.y;
};


Area.prototype.moveY = function(point, deltaY) {
	if (this.hitTest(point)) {
		this.handle = point;
		this.move(new Point(point.x, point.y + deltaY));
		this.handle = null;
	}
};

Area.prototype.move = function(point) {
	if (this.handle===null || (!this.moveableX && !this.moveableY)) {
		return;
	}
	
	var deltaX = point.x - this.handle.x;
	var deltaY = point.y - this.handle.y;
	
	// Make sure that the elements cannot completely leave the canvas
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
	}
	
		
	this.minX = null;
	this.maxX = null;
	
	// Actually moving the elements with (adapted) deltas
	for (var i = 0; i< this.elements.length; i++) {
		var elem = this.elements[i];
		
		if (!elem.isVisible) {
			continue;
		}
		
		var rect = elem.getRectangle();
		if (this.moveableX) rect.x += deltaX;
		if (this.moveableY) rect.y += deltaY;

		this.setMinMaxX(rect.x, rect.width);
		elem.setRectangle(rect);
	}
	
	this.handle = point;
};


Area.prototype.getElement = function(id) {
	
	for (var i = 0; i< this.elements.length; i++) {
		var elem = this.elements[i];
		if (elem.id==id) {
			return elem;
		}
	}
};

Area.prototype.addElement = function(template, id, label, parent, typeInfo, icon) {
	var element = new Element(template, {x: 0, y: 0}, id, parent);
	element.content = label;
	element.insertInto(this);
	element.invalidate();
	element.typeInfo = typeInfo;
	element.icon = icon;
	
	if (parent != null) {
		this.addHierarchyConnection(parent.getConnector("children"), element.getConnector("parent"));
	}
	
	return element;
};

Area.prototype.removeElement = function(element) {
	element.remove();
};


Area.prototype.addRoot = function(template, point, id, label, typeInfo, icon) {
	
	this.root = new Element(template, point, id, null);
	this.root.content = label;
	this.root.insertInto(this);
	this.root.isVisible = true;
	this.root.isExpanded = false;
	this.root.typeInfo = typeInfo;
	this.root.icon = icon;
	
	this.root.invalidate();
	
	this.resetPoint = point;
		
	return this.root;
};

Area.prototype.addHierarchyConnection = function(connector1, connector2)
{
	var connection = new HierarchyConnection(connector1, connector2);
	connector1.connections.push(connection);
	connector2.connections.push(connection);
	connector1.invalidate();
	connector2.invalidate();
	connection.invalidate();
	return connection;
};

Area.prototype.setMinMaxX = function(x, width) {
	if (this.minX === null || x < this.minX) {
		this.minX = x;
	}
	x += width;
	if (this.maxX === null || x > this.maxX) {
		this.maxX = x;
	}
};

Area.prototype.getVisibleElements = function() {
	var visibleElements = [];
	
	if (this.root===null) {
		return visibleElements;
	}
	
	this.minY = this.root.getRectangle().y;
	this.maxY = this.buildVisibleArea(this.root, visibleElements, this.root.getRectangle().x, this.root.getRectangle().y) + 25;
	
	return visibleElements;
};

Area.prototype.buildVisibleArea = function(element, elementList, x, y) {
	if (!elementList.contains(element) && element.isVisible) {
		element.setRectangle(new Rectangle(x, y, element.getRectangle().width, element.getRectangle().height));
		
		if (y <= this.rectangle.height + 100) {
			elementList.push(element);
		}
		this.setMinMaxX(x, element.getRectangle().width);
		
		if (element.isExpanded && element.children.length > 0) {
			for (var i=0; i<element.children.length; i++) {
				y = this.buildVisibleArea(element.children[i], elementList, this.isLeft ?  x + 20 : x - 20, y + 40);
			}
		}
	}
	return y;
};


Area.prototype.addParents = function(element, elementList) {
	if (!elementList.contains(element)) {
		elementList.push(element);
		if (element.parent !== null) {
			elementList = this.addParents(element.parent, elementList);
		}
	}
	return elementList;
};

Area.prototype.paint = function(context, pointerPosition) {
	/*context.strokeStyle = "#A0A0A0";
	context.lineWidth = 1;
	context.strokeRect(this.rectangle.x, this.rectangle.y, this.rectangle.width, this.rectangle.height);*/
	
	this.hNavBar.paint(context, this.minY, this.maxY);
};