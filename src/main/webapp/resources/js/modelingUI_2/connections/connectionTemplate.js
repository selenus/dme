var ConnectionTemplate = function(model) {
	this.model = model;
}

ConnectionTemplate.prototype.paint = function(connection, context) {
	var fromPosition = connection.from.getPosition();
	var toPosition;
	var height = fromPosition.y;
	
	context.lineWidth = 1;
	context.strokeStyle = this.model.theme.hierarchyConnection;
	
	if (connection.to!==undefined && connection.to!==null) {
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