var ElementTemplate = function(area, options) {
	this.area = area;
	this.options = $.extend(true, {
		key: "",
		height: 25,
		font: "11px Georgia, serif",
		levelIndent: 25,
		lineWidth: 2,
		radius: 0,
		primaryColor: "#e6f1ff",
		secondaryColor: "#0049a6",
		
	}, options);
	
	this.connectorTemplates = [];
	this.renderConnectorTemplates();
}

ElementTemplate.prototype.getExpanderPosition = function(element) {
	if (!this.area.isTarget) {
		return new Point(element.rectangle.x + 10, element.rectangle.y + element.rectangle.height/2);
	} else {
		return new Point(element.rectangle.x + element.rectangle.width - 10, element.rectangle.y + element.rectangle.height/2);
	}
};

ElementTemplate.prototype.getContextMenuItems = function(element) {
	if (this.options.getContextMenuItems!==undefined) {
		return this.options.getContextMenuItems(element);
	}
};

ElementTemplate.prototype.renderConnectorTemplates = function() {
	var _area = this.area;
	this.connectorTemplates.push(new ConnectorTemplate(this.area, {
		name: "parent", type: "Element [in]", description: "Father", isInteractive: false, isMappable: false,
		position: function(element) {
			if (!_area.isTarget) {
				return { x: 0, y: Math.floor(element.rectangle.height / 2) };
			} else {
				return { x: element.rectangle.width, y: Math.floor(element.rectangle.height / 2) };
			}
		}
	}));
	
	this.connectorTemplates.push(new ConnectorTemplate(this.area, {
		name: "children", type: "Element [out] [array]", description: "Child", isInteractive: false, isMappable: false,
		position: function(element) {
			if (!_area.isTarget) {
				return { x: 10, y: element.rectangle.height };
			} else {
				return { x: element.rectangle.width-10, y: element.rectangle.height };
			}
		}
	}));
	
	if (this.area.isSource) {
		this.connectorTemplates.push(new ConnectorTemplate(this.area, {
			name: "mappings", type: "Mapping [out] [array]", 
			isOut: true,
			isArray: true,
			description: "Mappings out", isInteractive: true, isMappable: true,
			position: function(element) {
				return { x: element.rectangle.width, y: Math.floor(element.rectangle.height / 2) };
			}
		}));
	}
	if (this.area.isTarget) {
		this.connectorTemplates.push(new ConnectorTemplate(this.area, {
			name: "mappings", 
			isOut: false,
			isArray: true,
			type: "Mapping [in] [array]", description: "Mappings in", isInteractive: true, isMappable: true,
			position: function(element) {
				return { x: 0, y: Math.floor(element.rectangle.height / 2) };
			}
		}));
	}
};

ElementTemplate.prototype.paint = function(element, context) {
	var rectangle = element.rectangle;
	
	if (!this.area.rectangle.contains(new Point(rectangle.x + rectangle.width, rectangle.y)) && 
			!this.area.rectangle.contains(new Point(rectangle.x, rectangle.y)) && 
			!this.area.rectangle.contains(new Point(rectangle.x, rectangle.y + rectangle.height)) && 
			!this.area.rectangle.contains(new Point(rectangle.x + rectangle.width, rectangle.y + rectangle.height))) {
		
		return false;
	}
	
	context.lineWidth = this.options.lineWidth;
	context.fillStyle = element.selected ? this.options.secondaryColor : this.options.primaryColor;
	context.strokeStyle = element.selected ? this.options.primaryColor : this.options.secondaryColor;
	
	if (this.options.radius==0) {
		context.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, true, true)
	} else {
		context.drawRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, this.options.radius, true, true)
	}
	
	var textAnchorX;
	if (!this.area.isTarget) {
		context.textAlign = "left";
		textAnchorX = rectangle.x + 22
	} else {
		context.textAlign = "right";
		textAnchorX = rectangle.x + rectangle.width - 22;
	}
	context.font = this.options.font;
	context.fillStyle = context.strokeStyle;
	context.textBaseline = "middle";
	context.fillText(element.label, textAnchorX, rectangle.y + rectangle.height/2 + 2);
	
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
	return true;
};