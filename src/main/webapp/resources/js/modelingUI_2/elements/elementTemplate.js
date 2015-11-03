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
	
	context.lineWidth = this.options.lineWidth;
	context.fillStyle = element.selected ? this.options.secondaryColor : this.options.primaryColor;
	context.strokeStyle = element.selected ? this.options.primaryColor : this.options.secondaryColor;
	
	if (this.options.radius==0) {
		this.drawRect(context, rectangle.x, rectangle.y, rectangle.width, rectangle.height, true, true)
	} else {
		this.drawRoundRect(context, rectangle.x, rectangle.y, rectangle.width, rectangle.height, this.options.radius, true, true)
	}
	
	context.font = this.options.font;
	context.fillStyle = context.strokeStyle;
	context.textBaseline = "middle";
	context.textAlign = "left";
	context.fillText(element.label, rectangle.x + 25, rectangle.y + rectangle.height/2 + 2);
	
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

ElementTemplate.prototype.drawRect = function(context, x, y, width, height, fill, stroke) {
	context.beginPath();
	context.moveTo(x, y);
	context.lineTo(x + width, y);
	context.lineTo(x + width, y + height);
	context.lineTo(x, y + height);
	context.lineTo(x, y);
	context.closePath();
	if (stroke) {
		context.stroke();
	}
	if (fill) {
		context.fill();
	}
};

ElementTemplate.prototype.drawRoundRect = function(ctx, x, y, width, height, radius, fill, stroke) {
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