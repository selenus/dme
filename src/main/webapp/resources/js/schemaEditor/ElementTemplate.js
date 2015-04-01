// Mapping Editor
var sourceTemplate = new ElementTemplate(false, false, false);
var sourceRootTemplate = new ElementTemplate(false, true, false);
var targetTemplate = new ElementTemplate(true, false, false);
var targetRootTemplate = new ElementTemplate(true, true, false);

// Schema Editor
var editorTemplate = new ElementTemplate(false, false, true);
var editorRootTemplate = new ElementTemplate(false, true, true);

function ElementTemplate(isTarget, isRoot, isAlone)
{
	this.resizable = false;
	this.moveable = false;
	this.defaultWidth = 200;
	this.defaultHeight = 25;
	this.defaultContent = "";
	this.isTarget = isTarget;
	this.isRoot = isRoot;
	
	// All elements get the child connector, 
	//  the other connectors depend on whether the element is root and/or target
	this.connectorTemplates = [];
	
	
	if (!isRoot) {
		this.connectorTemplates.push({ 	
   				name: "parent",
   		  		type: "Person [in]",
   		  		description: "Father",
   		  		position: function(element) { return { x: element.getRectangle().width, y: Math.floor(element.getRectangle().height / 2) }; },
				isInteractive: false,
				isMappable: false });
	}
	
	if (isTarget) {
		this.connectorTemplates.push({
			name: "children",
			type: "Person [out] [array]",
			description: "Children",
			position: function(element) { return { x: element.getRectangle().width - 10, y: element.getRectangle().height }; },
			isInteractive: false,
			isMappable: false });
		if (!isAlone) {
			this.connectorTemplates.push({ 
   				name: "mappings",   
   				type: "Mapping [in] [array]",
   				description: "Mappings in",
   				position: function(element) { return { x: 0, y: Math.floor(element.getRectangle().height / 2) }; },
   				isInteractive: true,
				isMappable: true });
		}
	} else {
		this.connectorTemplates.push({
			name: "children",
			type: "Person [out] [array]",
			description: "Children",
			position: function(element) { return { x: 10, y: element.getRectangle().height }; },
			isInteractive: false,
			isMappable: false });
		if (!isAlone) {
			this.connectorTemplates.push({ 	
       	  		name: "mappings",
       			type: "Mapping [out] [array]",
       			description: "Mappings out",
       			position: function(element) { return { x: element.getRectangle().width, y: Math.floor(element.getRectangle().height / 2) }; },
       			isInteractive: true,
				isMappable: true });
		}
	}
	
	
	// Expander for showing/hiding child elements
	if (isTarget) {
		this.expanderPosition = { x: this.defaultWidth-10, y: Math.floor(this.defaultHeight / 2) };
	} else {
		this.expanderPosition = { x: 10, y: Math.floor(this.defaultHeight / 2) };
	}
}


ElementTemplate.prototype.paint = function(element, context)
{
	var rectangle = element.getRectangle();
	
	context.lineWidth = 2;
	
	var lightColor = "";
	var darkColor = "";
	
	
	if (this.isRoot) {
		lightColor = "#e6f1ff";
		darkColor = "#0049a6";
	} else {
		if (element.typeInfo==="Label") {
			lightColor = "#f3e6ff";
			darkColor = "#5700a6";
		} else {
			lightColor = "#e6f1ff";
			darkColor = "#0049a6";
		}
	}
	
	context.fillStyle = element.selected ? darkColor : lightColor;
	context.strokeStyle = element.selected ? lightColor : darkColor;
	//context.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	//context.strokeRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	this.drawRect(context, rectangle.x, rectangle.y, rectangle.width, rectangle.height, true, true)
	
	context.font = "bold 10px Verdana";
	context.fillStyle = context.strokeStyle;
	context.textBaseline = "bottom";
	context.textAlign = "center";
	context.fillText(element.getContent(), rectangle.x + (rectangle.width / 2), rectangle.y + 20);
	
	if (element.icon != null) {
		var icon = new Image();
		icon.src = element.icon;
		if (this.isTarget) {
			context.drawImage(icon, rectangle.x + 3, rectangle.y + 4); 
		} else {
			context.drawImage(icon, rectangle.x + rectangle.width - 23, rectangle.y + 4); 
		}
	}
};

ElementTemplate.prototype.drawRect = function(ctx, x, y, width, height, fill, stroke) {
	if (typeof stroke == "undefined" ) {
	    stroke = true;
	  }
	  ctx.beginPath();
	  ctx.moveTo(x, y);
	  ctx.lineTo(x + width, y);
	  ctx.lineTo(x + width, y + height);
	  ctx.lineTo(x, y + height);
	  ctx.lineTo(x, y);
	  ctx.closePath();
	  if (stroke) {
	    ctx.stroke();
	  }
	  if (fill) {
	    ctx.fill();
	  }
};