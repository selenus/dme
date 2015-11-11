var Function = function(connection, template) {
	this.connection = connection;
	this.connector = null;
	this.template = template;
	
	this.visible = true;
	
	this.moveOffsetX = null;
	this.moveOffsetY = null;
	
	this.template.init(this);
}

Function.prototype.startMove = function(point) {
	this.moveOffsetX = this.connection.forkPoint.x - point.x;
	this.moveOffsetY = this.connection.forkPoint.y - point.y;
};

Function.prototype.move = function(point) {
	if (this.moveOffsetX != null && this.moveOffsetY != null) {
		var movedX = point.x + this.moveOffsetX;
		var movedY = point.y + this.moveOffsetY;

		// Leave function within horizontal bounds of the connection
		if (movedX < this.connection.getRectangle().x || 
				movedX > (this.connection.getRectangle().x+this.connection.getRectangle().width)) {
			movedX = this.connection.movedForkPoint.x;
		}
		this.connection.movedForkPoint = new Point(movedX, movedY);
	}
};

Function.prototype.stopMove = function(point) {
	this.moveOffsetX = null;
	this.moveOffsetY = null;
};

Function.prototype.isSelected = function() {
	return this.connection.isSelected();
};

Function.prototype.setSelected = function(selected) {
	this.connection.setSelected(selected);
};

Function.prototype.isVisible = function() {
	return this.visible;
};

Function.prototype.setVisible = function(visible) {
	this.visible = visible;
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