var Function = function(connection, template) {
	this.connection = connection;
	this.connector = null;
	this.template = template;
	
	this.active = false;
	this.selected = false;
	this.visible = true;
	
	this.template.init(this);
}

Function.prototype.setActive = function(active) {
	this.active = active;
};

Function.prototype.getActive = function() {
	return this.active || this.connection.active;
};

Function.prototype.paint = function(context) {
	if (this.template.paint(this, context)) {
		this.connector.paint(context);
	}
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