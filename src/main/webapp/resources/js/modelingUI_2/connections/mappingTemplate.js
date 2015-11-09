var MappingTemplate = function(model) {
	this.model = model;
	
	this.conceptConnector = new ConnectorTemplate(this.model, {
		name: "mappings", type: "Mapping [out] [array]", description: "Mappings out", isInteractive: true, isMappable: true,
		position: function(connection) {
			var rect = connection.template.getRectangle(connection);
			return { 
				x: rect.width/2, 
				y: rect.height/2 
			};
		}
	});
}

MappingTemplate.prototype.init = function(connection) {
	connection.connector = new Connector(connection, this.conceptConnector);
};

MappingTemplate.prototype.hitTest = function(connection, point) { 
	
	var p1 = connection.from.getPosition();
	var p2 = connection.to[0].getPosition();
	
	// TODO Also apply rectangle here
	
	var yShould = (p2.y - p1.y) / (p2.x - p1.x) * (point.x - p1.x) + p1.y;
		
	return (yShould-point.y < 5 && point.y - yShould < 5); 
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
	
	if (connection.connector!==undefined && connection.connector!==null) {
		connection.connector.paint(context);
	}
};

MappingTemplate.prototype.getRectangle = function(connection) {
	var p1 = connection.from.getPosition();
	
	if (connection.to.length>0) {
		var p2 = connection.to[0].getPosition();
		return new Rectangle(p1.x, p1.y, p2.x-p1.x, p2.y-p1.y)
	} else {
		// Just to stay compatible with 'new' connections
		return new Rectangle(p1.x, p1.y, p1.x, p2.y);
	}
};