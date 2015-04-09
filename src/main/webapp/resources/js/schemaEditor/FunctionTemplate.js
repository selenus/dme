var functionTemplate = new FunctionTemplate();

function FunctionTemplate()
{
	this.resizable = false;
	this.moveable = false;
	this.defaultWidth = 70;
	this.defaultHeight = 25;
	this.defaultContent = "f(x)";
	this.font = "bold 10px Verdana";

	// All elements get the child connector, 
	//  the other connectors depend on whether the element is root and/or target
	this.connectorTemplates = [];
	
	this.connectorTemplates.push({ 	
			name: "parent",
	  		type: "Person [in]",
	  		description: "Father",
	  		position: function(element) { return { x: element.getRectangle().width, y: Math.floor(element.getRectangle().height / 2) }; },
			isInteractive: false,
			isMappable: false });
		
		
	this.connectorTemplates.push({
		name: "children",
		type: "Person [out] [array]",
		description: "Children",
		position: function(element) { return { x: 10, y: element.getRectangle().height }; },
		isInteractive: false,
		isMappable: false });
			
	
	
	// Expander for showing/hiding child elements

	this.expanderPosition = { x: 10, y: Math.floor(this.defaultHeight / 2) };
	
}


FunctionTemplate.prototype.paint = function(element, context)
{
	var rectangle = element.getRectangle();
	context.lineWidth = 2;
	
	var lightColor = "#FFE173";
	var darkColor = "#6d5603";
	
	context.fillStyle = element.selected ? darkColor : lightColor;
	context.strokeStyle = element.selected ? lightColor : darkColor;
	this.drawRoundRect(context, rectangle.x, rectangle.y, rectangle.width, rectangle.height, 5, true, true)
	
	context.font = this.font;
	context.fillStyle = context.strokeStyle;
	context.textBaseline = "bottom";
	context.textAlign = "center";
	context.fillText(element.getContent(), rectangle.x + (rectangle.width / 2), rectangle.y + 20);
};

FunctionTemplate.prototype.drawRoundRect = function(ctx, x, y, width, height, radius, fill, stroke) {
	if (typeof stroke == "undefined" ) {
	    stroke = true;
	  }
	  if (typeof radius === "undefined") {
	    radius = 5;
	  }
	  ctx.beginPath();
	  ctx.moveTo(x + radius, y);
	  ctx.lineTo(x + width - radius, y);
	  ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
	  ctx.lineTo(x + width, y + height - radius);
	  ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
	  ctx.lineTo(x + radius, y + height);
	  ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
	  ctx.lineTo(x, y + radius);
	  ctx.quadraticCurveTo(x, y, x + radius, y);
	  ctx.closePath();
	  if (stroke) {
	    ctx.stroke();
	  }
	  if (fill) {
	    ctx.fill();
	  }
};