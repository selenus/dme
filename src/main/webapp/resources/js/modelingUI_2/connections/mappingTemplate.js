var MappingTemplate = function(model) {
	this.model = model;
	
	this.functionTemplate = new FunctionTemplate(this.model, { 
		primaryColor: "#FFE173", 
		secondaryColor: "#6d5603", 
		radius: 5
	});
}

MappingTemplate.prototype.init = function(connection) {
	connection.func = new Function(connection, this.functionTemplate);
};

MappingTemplate.prototype.hitTest = function(connection, point) { 
	var result = connection.func.hitTest(point);
	if (result!=null) {
		return result;
	}
	
	var p1 = connection.from.getPosition();
	var p2 = connection.to[0].getPosition();
	
	// TODO Also apply rectangle here
	
	var yShould = (p2.y - p1.y) / (p2.x - p1.x) * (point.x - p1.x) + p1.y;
		
	if (yShould-point.y < 10 && point.y - yShould < 10) {
		return connection;
	} 
	return null;
};

MappingTemplate.prototype.paint = function(connection, context) {
	var fromPosition = connection.from.getPosition();
	var toPosition;
	var height = fromPosition.y;
	
	if (connection.active) {
		context.lineWidth = 2;
	} else {
		context.lineWidth = 1;
	}
	context.strokeStyle = this.model.theme.mappingConnectionDefault;
	
	if (connection.to!==undefined && connection.to!==null && connection.to.length > 0) {
		for (var i=0; i<connection.to.length; i++) {
			toPosition = connection.to[i].getPosition();
			context.simpleLine(fromPosition.x, fromPosition.y, toPosition.x, toPosition.y);
		}
	} else {
		// New connection being dragged
		toPosition = this.model.mousePosition;
		context.dashedLine(fromPosition.x, fromPosition.y, toPosition.x, toPosition.y);
	}
	
	context.simpleLine(fromPosition.x, fromPosition.y, fromPosition.x, height+0.5);
	
	if (connection.to.length>0 && connection.func!==undefined && connection.func!==null) {
		connection.func.paint(context);
	}
};

MappingTemplate.prototype.getRectangle = function(connection) {
	var p1 = connection.from.getPosition();
	
	if (connection.to.length>0) {
		var p2 = connection.to[0].getPosition();
		return new Rectangle(p1.x, p1.y, p2.x-p1.x, p2.y-p1.y)
	} else {
		// Just to stay compatible with 'new' connections
		return new Rectangle(p1.x, p1.y, p1.x, p1.y);
	}
};