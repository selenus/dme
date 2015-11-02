function ConnectorTemplate(area, options) {
	this.area = area;
	this.options = $.extend({ 	
			name: "parent",
		  	type: "Person [in]",
		  	description: "Father",
		  	position: function(element) { 
		  		return { x: element.getRectangle().width, y: Math.floor(element.getRectangle().height / 2) }; 
		  	},
			isInteractive: false,
			isMappable: false }, options)
};

ConnectorTemplate.prototype.paint = function(connector, context) {
	if (!this.options.isInteractive) {
		return;
	}
	
	var rectangle = this.getRectangle(connector);
	
	var strokeStyle = this.area.model.theme.connectorBorder; 
	var fillStyle = this.area.model.theme.connector;
	
	context.lineWidth = 1;
	context.strokeStyle = strokeStyle;
	context.fillStyle = fillStyle;
	context.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	context.strokeRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
};

ConnectorTemplate.prototype.getPosition = function(connector) {
	var position = this.options.position(connector.element);	// Position within the element
	position.x += connector.element.rectangle.x;				// Horizontal element offset
	position.y += connector.element.rectangle.y;				// Vertical element offset
	
	return position;
};

ConnectorTemplate.prototype.getRectangle = function(connector) {
	var position = this.getPosition(connector);
	var rectangle = new Rectangle(position.x, position.y, 0, 0);
	rectangle.inflate(3, 3);
	return rectangle;
};