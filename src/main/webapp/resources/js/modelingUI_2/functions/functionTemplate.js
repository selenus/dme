var FunctionTemplate = function(model, options) {
	this.model = model;
	this.count = 1;
	
	this.connectorTemplates = [];
	
	this.connectorTemplates.push(new ConnectorTemplate(this.model, { 
		name: "mappings", 
		type: "Mapping [out] [array]",
		isOut: true,
		isArray: true,
		description: "Mappings out", 
		isInteractive: true, 
		isMappable: true, 
		position: function(func) {
			return { x: func.getRectangle().width, y: Math.floor(func.getRectangle().height / 2) };
		},
		addConnection: function(connection) {
			var cOwning = this.element.connection; // Connection that holds the function
			if (connection.to!==undefined && connection.to!==null && connection.to.length>0) {
				for (var i=0; i<connection.to.length; i++) {
					if (!cOwning.to.contains(connection.to[i])) {
						cOwning.addTo(connection.to[i]);
					}
				}
			}
			return cOwning;
		}
	}));
	
	this.connectorTemplates.push(new ConnectorTemplate(this.model, { 
		name: "mappings", 
		type: "Mapping [in] [array]", 
		description: "Mappings in", 
		isInteractive: true, 
		isMappable: true, 
		isOut: false,
		isArray: true,
		position: function(func) {
			return { x: 0, y: Math.floor(func.getRectangle().height / 2) };
		},
		addConnection: function(connection) {
			var cOwning = this.element.connection; // Connection that holds the function
			if (connection.from!==undefined && connection.from!==null && connection.from.length>0) {
				for (var i=0; i<connection.from.length; i++) {
					if (!cOwning.from.contains(connection.from[i])) {
						cOwning.addFrom(connection.from[i]);
					}
				}
			}
			return cOwning;
		}
	}));
	
	this.options = $.extend(true, {
		lineWidth: 2,
		radius: 0,
		primaryColor: "#e6f1ff",
		secondaryColor: "#0049a6",
		font: "10px Glyphicons Halflings",
		text: "\uE075", //Forward
		//text: "\uE019", // Cog
		getContextMenuItems: undefined
	}, options);
}

FunctionTemplate.prototype.init = function(func) {
	func.connectors = [];
	
	var c = null;
	for (var i=0; i<this.connectorTemplates.length; i++) {
		c = new Connector(func, this.connectorTemplates[i]);
		c.addConnection = this.connectorTemplates[i].options.addConnection;
		func.connectors.push(c);
	}
};

FunctionTemplate.prototype.getAnchor = function(connection) {
	return connection.forkPoint;
};

FunctionTemplate.prototype.getRectangle = function(func) {
	var anchor = this.getAnchor(func.connection);
	return new Rectangle(anchor.x, anchor.y, 0, 0).inflate(this.calculateWidth(func)/2 + 7, 7); 
};

FunctionTemplate.prototype.hitTest = function(func, point) {
	if(this.getRectangle(func).contains(point)) {
		return func;
	}
	return null;
};

FunctionTemplate.prototype.calculateWidth = function(func) {
	var context = this.model.context;
	context.font = this.options.font;	
	return context.measureText(this.getText(func)).width;
};

FunctionTemplate.prototype.paint = function(func, context, faded) {
	if (faded) {
		return this.paintFaded(func, context);
	}
	
	var rectangle = this.getRectangle(func);
	
	if (func.getActive()) {
		context.lineWidth = this.options.lineWidth+1;
	} else {
		context.lineWidth = this.options.lineWidth;
	}
	
	context.fillStyle = func.getActive() || func.isSelected() ? this.options.secondaryColor : this.options.primaryColor;
	context.strokeStyle = func.getActive() || func.isSelected() ? this.options.primaryColor : this.options.secondaryColor;
	
	/*if (parentsConnected) {
		context.fillStyle = this.model.theme.mappingConnectionInvisible;
	}*/
	
	if (this.options.radius==0) {
		context.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, true, true)
	} else {
		context.drawRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, this.options.radius, true, true)
	}
	
	context.textAlign = "center";
	context.font = this.options.font;
	context.fillStyle = context.strokeStyle;
	context.textBaseline = "middle";
	context.fillText(this.getText(func), rectangle.x + rectangle.width/2, rectangle.y + rectangle.height/2 + 2);
	return true;
};

FunctionTemplate.prototype.paintFaded = function(func, context) {
	var anchor = this.getAnchor(func.connection);
	var rectangle = new Rectangle(anchor.x, anchor.y, 0, 0).inflate(4, 4);
	
	context.lineWidth = this.options.lineWidth;
	context.fillStyle = func.getActive() || func.isSelected() ? this.model.theme.functionFaded.secondary : this.model.theme.functionFaded.primary;
	context.strokeStyle = func.getActive() || func.isSelected() ? this.model.theme.functionFaded.primary : this.model.theme.functionFaded.secondary;
	
	if (this.options.radius==0) {
		context.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, true, true);
	} else {
		context.drawRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, this.options.radius, true, true);
	}
	return true;
};

FunctionTemplate.prototype.getText = function(func) {
	return this.options.text;
};