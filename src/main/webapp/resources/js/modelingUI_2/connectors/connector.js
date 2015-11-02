var Connector = function(element, template) {
	this.element = element;
	this.template = template;
	
	this.connections = [];
};

Connector.prototype.paint = function(context) {
	this.template.paint(this, context);
};

Connector.prototype.getPosition = function() {
	return this.template.getPosition(this);
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