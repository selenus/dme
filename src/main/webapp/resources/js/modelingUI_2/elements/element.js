var Element = function(template, parent, id, label, icons) {
	this.template = template;
	this.parent = parent;
	this.id = id;
	this.label = label;
	this.icons = [];
	this.children = [];
	this.expanded = true;
	this.selected = false;
	this.active = false;
	
	this.connectors = [];
	if (this.template.connectorTemplates!=null) {
		for (var i=0; i<this.template.connectorTemplates.length; i++) {
			this.connectors.push(new Connector(this, this.template.connectorTemplates[i]));
		}
	}
	
	if (icons!=undefined && icons!=null) {
		if (icons instanceof Array) {
			this.icons = icons;
		} else {
			this.icons.push(icons);
		}
	}

	this.rectangle = new Rectangle(0, 0, this.calculateWidth(), this.template.options.height);
};

Element.prototype.getCursor = function() {
	return Cursors.arrow;
};

Element.prototype.setActive = function(active) {
	this.active = active;
};

Element.prototype.getHierarchyConnections = function() {
	var result = this.getConnections(this.getConnector("parent"));
	var result2 = this.getConnections(this.getConnector("children"));
	return result.concat(result2);
};

Element.prototype.getConnections = function(connector) {
	var result = [];
	if (connector!==undefined && connector!==null) {
		for (var i=0; i<connector.connections.length; i++) {
			result.push(connector.connections[i]);
		}
	}
	return result;
};

Element.prototype.addChild = function(child) {
	this.children.push(child);
};

Element.prototype.setExpanded = function(expand) {
	this.expanded = expand;
};

Element.prototype.getConnector = function(type) {
	if (this.connectors!=null) {
		for (var i=0; i<this.connectors.length; i++) {
			if (this.connectors[i].template.options.name==type) {
				return this.connectors[i];
			}
		}
	}
	return null;
};

Element.prototype.setRectangle = function(rect) {
	this.rectangle = rect;
};

Element.prototype.paint = function(context) {
	this.template.paint(this, context);
	
	if (this.connectors!=null) {
		for (var i=0; i<this.connectors.length; i++) {
			this.connectors[i].paint(context);
		}
	}
};

Element.prototype.calculateWidth = function() {
	var context = this.template.area.model.context;
	context.font = this.template.font;	
	var width = context.measureText(this.label).width + 45
	if (this.icons != null) {
		width += this.icons.length * 20;
	}
	return width;
};

Element.prototype.hitTest = function(position) {
	if (!this.rectangle.clone().inflate(5, 5).contains(position)) {
		return null;
	}
	if (this.connectors!=null) {
		var hitConnector=null;
		for (var i=0; i<this.connectors.length; i++) {
			hitConnector = this.connectors[i].hitTest(position);
			if (hitConnector!=null) {
				return hitConnector;
			}
		}
	}
	return this;
};