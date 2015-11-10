var FunctionTemplate = function(model, options) {
	this.model = model;
	this.count = 1;
	
	this.connectorTemplate = new ConnectorTemplate(this.model, { 
		name: "mappings", type: "Mapping [out] [array]", description: "Mappings out", isInteractive: true, isMappable: true, 
		position: function(func) {
			return { x: func.getRectangle().width / 2, y: Math.floor(func.getRectangle().height / 2) };
		}
	});
	
	this.options = $.extend(true, {
		lineWidth: 2,
		radius: 0,
		primaryColor: "#e6f1ff",
		secondaryColor: "#0049a6",
		font: "bold 9px Verdana"
	}, options);
}

FunctionTemplate.prototype.init = function(func) {
	func.connector = new Connector(func, this.connectorTemplate);
};

FunctionTemplate.prototype.getAnchor = function(connection) {
	var connRect = connection.getRectangle();
	return new Point(connRect.x + connRect.width/2, connRect.y + connRect.height/2);
};

FunctionTemplate.prototype.getRectangle = function(func) {
	var anchor = this.getAnchor(func.connection);
	return new Rectangle(anchor.x, anchor.y, 0, 0).inflate(this.calculateWidth(func)/2 + 7, 7); 
};

Function.prototype.getCursor = function() {
	return Cursors.cross;
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

FunctionTemplate.prototype.paint = function(func, context) { 
	var rectangle = this.getRectangle(func);
	
	if (func.getActive()) {
		context.lineWidth = this.options.lineWidth+1;
	} else {
		context.lineWidth = this.options.lineWidth;
	}
	
	context.fillStyle = func.selected ? this.options.secondaryColor : this.options.primaryColor;
	context.strokeStyle = func.selected ? this.options.primaryColor : this.options.secondaryColor;
		
	if (this.options.radius==0) {
		context.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, true, true)
	} else {
		context.drawRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, this.options.radius, true, true)
	}
	
	context.textAlign = "center";
	context.font = this.options.font;
	context.fillStyle = context.strokeStyle;
	context.textBaseline = "middle";
	context.fillText(this.getText(func), rectangle.x + rectangle.width/2, rectangle.y + rectangle.height/2);
	return true;
};

FunctionTemplate.prototype.getText = function(func) {
	return this.count;
};