var Function = function(connection, template) {
	this.connection = connection;
	this.connectors = null;
	this.template = template;
	
	this.visible = true;
	
	this.moveOffsetX = null;
	this.moveOffsetY = null;
	
	this.template.init(this);
}

Function.prototype.getId = function() {
	return this.connection.getId();
};

Function.prototype.startMove = function(point) {
	this.moveOffsetX = this.connection.forkPoint.x - point.x;
	this.moveOffsetY = this.connection.forkPoint.y - point.y;
};

Function.prototype.move = function(point) {
	if (this.moveOffsetX != null && this.moveOffsetY != null) {
		var movedX = point.x + this.moveOffsetX;
		var movedY = point.y + this.moveOffsetY;

		// Leave function within horizontal bounds of the connection
		if (this.connection.movedForkPoint!= null && (
				movedX < this.template.model.getRectangle().x + this.template.model.theme.paddingArea.x || 
				movedX > this.template.model.getRectangle().x + this.template.model.getRectangle().width - this.template.model.theme.paddingArea.x ) ) {
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
	return this.connection.getContextMenuItems();
};

Function.prototype.setActive = function(active) {
	this.connection.setActive(active);
};

Function.prototype.getActive = function() {
	return this.connection.active;
};

Function.prototype.paint = function(context, faded) {
	if (this.template.paint(this, context, faded) && !faded) {
		if (this.connectors!=null) {
			var newConnection = this.connection.template.model.newConnection;
			var isNewConnection = newConnection!==undefined && newConnection!==null
			var activeConnector = this.connection.template.model.activeConnector;
			
			for (var i=0; i<this.connectors.length; i++) {
				this.connectors[i].setActiveTarget(isNewConnection && this.connectors[i].isValid(activeConnector));
				this.connectors[i].paint(context);
			}
		}
	}
};

Function.prototype.getCursor = function() {
	return Cursors.select;
};

Function.prototype.hitTest = function(point) {
	for (var i=0; i<this.connectors.length; i++) {
		if (this.connectors[i].hitTest(point)!=null) {
			return this.connectors[i];
		}
	}
	return this.template.hitTest(this, point);
};

Function.prototype.getRectangle = function() {
	return this.template.getRectangle(this);
};