var ElementTemplate = function(area, options) {
	this.area = area;
	this.options = $.extend(true, {
		key: "",
		height: 25,
		font: "bold 10px Verdana",
		levelIndent: 25,
		lineWidth: 2,
		radius: 0,
		primaryColor: "#e6f1ff",
		secondaryColor: "#0049a6"
	}, options);
	
	this.connectorTemplates = [];
	
	this.renderConnectorTemplates();
}

ElementTemplate.prototype.getExpanderPosition = function(element) {
	return new Point(element.rectangle.x + 10, element.rectangle.y + element.rectangle.height/2);
};

ElementTemplate.prototype.renderConnectorTemplates = function() {
	this.connectorTemplates.push(new ConnectorTemplate(this.area, {
		name: "parent", type: "Element [in]", description: "Father", isInteractive: false, isMappable: false,
		position: function(element) {
			return { x: 0, y: Math.floor(element.rectangle.height / 2) };
		}
	}));
	
	this.connectorTemplates.push(new ConnectorTemplate(this.area, {
		name: "children", type: "Element [out] [array]", description: "Child", isInteractive: false, isMappable: false,
		position: function(element) {
			return { x: 10, y: element.rectangle.height };
		}
	}));
};

ElementTemplate.prototype.paint = function(element, context) {
	var rectangle = element.rectangle;
	
	if (!this.area.rectangle.contains(new Point(rectangle.x, rectangle.y)) && 
			!this.area.rectangle.contains(new Point(rectangle.x + rectangle.width, rectangle.y + rectangle.height))) {
		return;
	}
	
	context.lineWidth = this.options.lineWidth;
	context.fillStyle = element.selected ? this.options.secondaryColor : this.options.primaryColor;
	context.strokeStyle = element.selected ? this.options.primaryColor : this.options.secondaryColor;
	
	if (this.options.radius==0) {
		context.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, true, true)
	} else {
		context.drawRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, this.options.radius, true, true)
	}
	
	context.font = this.options.font;
	context.fillStyle = context.strokeStyle;
	context.textBaseline = "middle";
	context.textAlign = "left";
	context.fillText(element.label, rectangle.x + 22, rectangle.y + rectangle.height/2 + 2);
	
	if (element.icons != null) {
		for (var i=0; i<element.icons.length; i++) {
			var icon = new Image();
			icon.src = element.icons[i];
			if (this.isTarget) {
				context.drawImage(icon, rectangle.x + 3, rectangle.y + 4); 
			} else {
				context.drawImage(icon, rectangle.x + rectangle.width - 23, rectangle.y + 4); 
			}
		}
	}
};