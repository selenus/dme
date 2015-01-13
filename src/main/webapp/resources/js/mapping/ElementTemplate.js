var sourceTemplate = new ElementTemplate(false, false);
var sourceRootTemplate = new ElementTemplate(false, true);
var targetTemplate = new ElementTemplate(true, false);
var targetRootTemplate = new ElementTemplate(true, true);

function ElementTemplate(isTarget, isRoot)
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
		
		if (isTarget) {
			this.connectorTemplates.push({
   				name: "children",
   				type: "Person [out] [array]",
   				description: "Children",
   				position: function(element) { return { x: element.getRectangle().width - 10, y: element.getRectangle().height }; },
   				isInteractive: false,
				isMappable: false });
			this.connectorTemplates.push({ 
   				name: "mappings",   
   				type: "Mapping [in] [array]",
   				description: "Mappings in",
   				position: function(element) { return { x: 0, y: Math.floor(element.getRectangle().height / 2) }; },
   				isInteractive: true,
				isMappable: true });
		} else {
			this.connectorTemplates.push({
   				name: "children",
   				type: "Person [out] [array]",
   				description: "Children",
   				position: function(element) { return { x: 10, y: element.getRectangle().height }; },
   				isInteractive: false,
				isMappable: false });
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
	var textColor = "#000";
	context.lineWidth = 2;
	
	if (this.isRoot) {
		context.fillStyle = "#13304B";
		context.strokeStyle = "#000";
		textColor = "#FFF";		
	} else {
		context.fillStyle = element.selected ? "#2C73B2" : "#FFF";
		context.strokeStyle = element.selected ? "#CCC" : "#13304B";
		textColor = context.strokeStyle;
	}
	
	context.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	context.strokeRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	context.font = "bold 10px Verdana";
	context.fillStyle = textColor;
	context.textBaseline = "bottom";
	context.textAlign = "center";
	context.fillText(element.getContent(), rectangle.x + (rectangle.width / 2), rectangle.y + 20);
	
	if (this.isRoot == null) {
		var schemaIcon = new Image();
		schemaIcon.src = "../resources/img/schema.png";
		if (this.isTarget) {
			context.drawImage(schemaIcon, rectangle.x + 3, rectangle.y + 3); 
		} else {
			context.drawImage(schemaIcon, rectangle.x + rectangle.width - 23, rectangle.y + 3); 
		}
	}
};