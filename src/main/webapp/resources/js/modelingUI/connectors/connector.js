var Connector = function(element, template) {
	this.element = element;
	this.template = template;
	this.active = false;
	this.activeTarget = false;
	
	this.connections = [];
};

Connector.prototype.isValid = function(from) {
	if (from===this) {
		return false;
	}
	
	if (!this.isArray() && this.connections.length > 0) {
		return false;
	}
	
	if (from instanceof Connector) {
		if (!from.isArray() && from.connections.length > 0) {
			return false;
		}
		if (from.isOut()!==this.isOut() && from.element!==this.element) {
			return true;
		}
	}
	return false;
};

Connector.prototype.isOut = function() {
	return this.template.options.isOut===true;
}

Connector.prototype.isArray = function() {
	return this.template.options.isArray===true;
}

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
	if ( (this.template.options.isInteractive && this.template.getRectangle(this).clone().inflate(2, 2).contains(position) &&
			!this.template.model.options.readOnly)) {
		return this;
	}
	return null;
};

Connector.prototype.addConnection = function(connection) {
	this.connections.push(connection);
	return connection;
};