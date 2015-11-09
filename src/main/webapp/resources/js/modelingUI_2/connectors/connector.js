var Connector = function(element, template) {
	this.element = element;
	this.template = template;
	this.active = false;
	this.activeTarget = false;
	
	this.connections = [];
};

Connector.prototype.isValid = function(value) {
	if (value===this) {
		return false;
	}
	var t1 = this.template.options.type.split(' ');
	if (!t1.contains("[array]") && (this.connections.length == 1)) {
		return false;
	}
	if (value instanceof Connector) {	
		var t2 = value.template.options.type.split(' ');
		if ((t1[0] != t2[0]) ||
		(this.element == value.element) || 
			(t1.contains("[in]") && !t2.contains("[out]")) || 
			(t1.contains("[out]") && !t2.contains("[in]")) || 
			(!t2.contains("[array]") && (value.connections.length == 1))) {
			return false;
		}
	}
	return true;
};

Connector.prototype.getContextMenuItems = function() {
	return this.element.getContextMenuItems();
};

Connector.prototype.getCursor = function() {
	return Cursors.cross;
};

Connector.prototype.setActive = function(active) {
	this.active = active;
};

Connector.prototype.setActiveTarget = function(activeTarget) {
	this.activeTarget = activeTarget;
};

Connector.prototype.paint = function(context) {
	this.template.paint(this, context);
};

Connector.prototype.getPosition = function() {
	return this.template.getPosition(this);
};

Connector.prototype.hitTest = function(position) {
	if (this.template.options.isInteractive && this.template.getRectangle(this).clone().inflate(2, 2).contains(position)) {
		return this;
	}
	return null;
};

Connector.prototype.registerConnection = function(toConnector, template) {
	// This is only ok for our hierarchical connections now...
	var c;
	if (this.connections.length>0) {
		c = this.connections[0];
	} else {
		c = new Connection(template, this);
		this.addConnection(c);
	}
	toConnector.addConnection(c);
	c.addTo(toConnector);
};

Connector.prototype.addConnection = function(connection) {
	this.connections.push(connection);
};