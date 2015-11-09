function ConnectorTemplate(owner, options) {
	if (owner instanceof Area) {
		this.model = owner.model;
	} else {
		this.model = owner;
	}
	
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
	if (!this.options.isInteractive || (!connector.element.active && !connector.active && !connector.activeTarget) ){
		return;
	}
	
	var rectangle = this.getRectangle(connector);
	
	var strokeStyle; 
	var fillStyle;
	if (connector.active) {
		strokeStyle = this.model.theme.connectorHoverBorder; 
		fillStyle = this.model.theme.connectorHover;
	} else {
		strokeStyle = this.model.theme.connectorBorder; 
		fillStyle = this.model.theme.connector;
	}
	
	context.lineWidth = 1;
	context.strokeStyle = strokeStyle;
	context.fillStyle = fillStyle;
	context.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	context.strokeRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
};

ConnectorTemplate.prototype.getPosition = function(connector) {
	var position = this.options.position(connector.element);	// Position within the element
	position.x += connector.element.getRectangle().x + 0.5;		// Horizontal element offset
	position.y += connector.element.getRectangle().y + 0.5;		// Vertical element offset
	
	return position;
};

ConnectorTemplate.prototype.getRectangle = function(connector) {
	var position = this.getPosition(connector);
	var rectangle = new Rectangle(position.x, position.y, 0, 0);
	rectangle.inflate(3, 3);
	return rectangle;
};