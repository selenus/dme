var Function = function(connection, template) {
	this.connection = connection;
	this.connector = null;
	this.template = template;
	
	this.visible = true;
	
	this.template.init(this);
}

Function.prototype.isSelected = function() {
	return this.connection.isSelected();
};

Function.prototype.setSelected = function(selected) {
	this.connection.setSelected(selected);
};

Function.prototype.getContextMenuItems = function() {
	return this.template.getContextMenuItems(this);
};

Function.prototype.setActive = function(active) {
	this.connection.setActive(active);
};

Function.prototype.getActive = function() {
	return this.connection.active;
};

Function.prototype.paint = function(context) {
	if (this.template.paint(this, context)) {
		this.connector.paint(context);
	}
};

Function.prototype.getCursor = function() {
	return Cursors.select;
};

Function.prototype.hitTest = function(point) {
	if (this.connector.hitTest(point)!=null) {
		return this.connector;
	}
	return this.template.hitTest(this, point);
};

Function.prototype.getRectangle = function() {
	return this.template.getRectangle(this);
};