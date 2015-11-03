var Element = function(template, parent, id, label, icons) {
	this.template = template;
	this.parent = parent;
	this.id = id;
	this.label = label;
	this.icons = [];
	this.children = [];
	this.selected = false;
	this.active = false;
	this.visible = true;
	
	this.expander = null;
	
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
	return Cursors.select;
};

Element.prototype.setExpanded = function(expanded) {
	if (this.expander!=null) {
		this.expander.expanded = expanded;
		this.setChildrenVisible(expanded);
	}
};

Element.prototype.setChildrenVisible = function(visible) {
	if (this.expander!=null && this.children!=null) {
		for (var i=0; i<this.children.length; i++) {
			this.children[i].visible = visible
			this.children[i].setChildrenVisible(visible && this.expander.expanded);
		}
	}
};

Element.prototype.getExpanded = function() {
	if (this.expander!=null) {
		return this.expander.expanded;
	} else {
		return false;
	}
};

Element.prototype.getType = function() {
	return this.template.options.key;
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
	if (this.expander==null) {
		this.expander = new Expander(this);
	}
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
	if (this.expander!=null) {
		this.expander.paint(context);
	}
	
	if (this.expander!=null && this.expander.expanded && this.children!=null) {
		for (var i=0; i<this.children.length; i++) {
			this.children[i].paint(context);
		}
	}
};

Element.prototype.calculateWidth = function() {
	var context = this.template.area.model.context;
	context.font = this.template.font;	
	var width = context.measureText(this.label).width + 40
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
	if (this.expander!=null && this.expander.hitTest(position)) {
		return this.expander;
	}
	return this;
};