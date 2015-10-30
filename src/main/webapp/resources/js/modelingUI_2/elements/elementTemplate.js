var ElementTemplate = function(area, options) {
	this.area = area;
	this.options = $.extend(true, {
		height: 25,
		font: "bold 10px Verdana",
		levelIndent: 25,
		lineWidth: 2,
		primaryColor: "#e6f1ff",
		secondaryColor: "#0049a6"
	}, options);
}

ElementTemplate.prototype.paint = function(element, context) {
	var rectangle = element.rectangle;
	
	context.lineWidth = this.options.lineWidth;
	context.fillStyle = element.selected ? this.options.secondaryColor : this.options.primaryColor;
	context.strokeStyle = element.selected ? this.options.primaryColor : this.options.secondaryColor;
	
	this.drawRect(context, rectangle.x, rectangle.y, rectangle.width, rectangle.height, true, true)
	
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
	
	// Draw children if visible
	if (element.children.length > 0) {
		for (var i=0; i<element.children.length; i++) {
			element.children[i].template.paint(element.children[i], context);
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