var HierarchyTemplate = function(model) {
	this.model = model;
}

HierarchyTemplate.prototype.init = function(connection) {};

HierarchyTemplate.prototype.paint = function(connection, context) {	
	if (!connection.from.element.isExpanded()) {
		return;
	}
	
	var fromPosition = connection.from.getPosition();
	var toPosition;
	var height = fromPosition.y;
	
	context.lineWidth = 1;
	context.strokeStyle = this.model.theme.hierarchyConnection;
	
	if (connection.to!==undefined && connection.to!==null && connection.to.length > 0) {
		for (var i=0; i<connection.to.length; i++) {
			toPosition = connection.to[i].getPosition();
			if (toPosition.y > height) {
				height = toPosition.y; 
			}
			context.simpleLine(fromPosition.x, toPosition.y, toPosition.x, toPosition.y);
		}
	}
	
	context.simpleLine(fromPosition.x, fromPosition.y, fromPosition.x, height+0.5);
};

HierarchyTemplate.prototype.hitTest = function(connection, point) { 
	// Irrelevant for hierarchy connections
	return false; 
};

HierarchyTemplate.prototype.getContextMenuItems = function(connection) {
	return [];
};