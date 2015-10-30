var MappingConnection = function(owner, from, to, id, score, isVerified) {
	this.graph = owner;
	this.id = id;
	if (score < 0) {
		score = 0;
	} else if (score > 1) {
		score = 1;
	}
	this.score = score;
	this.isVerified = isVerified == null ? false : isVerified;
	
	var red = 255 - Math.round(this.score * 255);
	var color = "Rgb(" + red + ", 230, 0)";

	this.base = Connection;
	this.base(from, to, color, true);
		
	this.hover = false;
	
	this.isInherited = false;
	this.isInteractive = true;
	
	this.selected = false;
	this.isDeselected = false;
	
	this.conceptConnector = new Connector(this, { 	
	  		name: "mappings",
   			type: "Mapping [out] [array]",
   			description: "Mappings out",
   			position: function(from, to) {
   				var startElement = from.owner.findVisible();
   				var endElement = (to !== null) ? to.owner.findVisible() : null;
   				
   				var start = startElement.getConnectorPosition(from);
   				var end = endElement.getConnectorPosition(to);
   				   				
   				return { 
   					x: start.x + (end.x - start.x)/2, 
   					y: start.y + (end.y - start.y)/2 }; 
   			},
   			isInteractive: true,
			isMappable: true });
};

MappingConnection.prototype = new Connection;

MappingConnection.prototype.hitTest = function(rectangle) {
	if (this.isInherited || this.isDeselected) {
		return false;
	} else {
		//return this.base.hitTest(rectangle);
		return Connection.prototype.hitTest.call(this, rectangle);
	}
};

MappingConnection.prototype.findVisible = function() {
	return this;
};

MappingConnection.prototype.getMappingConnector = function() {
	return this.conceptConnector;
};


MappingConnection.prototype.getConnectorPosition = function(connector) {
	var startElement = this.from.owner.findVisible();
	var endElement = (this.to !== null) ? this.to.owner.findVisible() : null;
	
	var start = startElement.getConnectorPosition(this.from);
	var end = (endElement !== null) ? endElement.getConnectorPosition(this.to) : this.toPoint;
	
	var x = start.x + (end.x - start.x)/2;
	var y = start.y + (end.y - start.y)/2;
	
	var point = {};
	point.x = x;
	point.y = y;
	return point;
};

MappingConnection.prototype.paint = function(context, pointerPosition)
{
	this.isInherited = false;
	this.isInteractive = true;
	
	if (!this.from.owner.isVisible || !this.to.owner.isVisible) {
		
		this.isInherited = true;
		this.isInteractive = false;
	}
	
	context.lineWidth = 1;
	if (this.selected) {
		context.lineWidth = 3;
	}
	
	if (this.isInherited || this.isDeselected) {
		context.strokeStyle = "#D9D9D9";
	} else if (this.isVerified) {
		context.strokeStyle = "rgb(70, 155, 231)";
	} else {
		context.strokeStyle = this.color;
	}
	
	this.paintLine(context, this.isInherited || this.isDeselected);
	//this.paintBox(context, this.isInherited || this.isDeselected);
	
	if (this.hover)
	{
		var percentage = Math.round(this.score * 10000) / 100;
		var text = percentage + "%";
		context.textBaseline = "bottom";
		context.font = "8.25pt Tahoma";
		var size = context.measureText(text);
		size.height = 14;
		var a = new Rectangle(pointerPosition.x - Math.floor(size.width / 2), pointerPosition.y + size.height + 6, size.width, size.height);
		var b = new Rectangle(a.x, a.y, a.width, a.height);
		a.inflate(4, 1);
		context.fillStyle = context.strokeStyle;
		context.fillRect(a.x - 0.5, a.y - 0.5, a.width, a.height);
		context.strokeStyle = "#000";
		context.lineWidth = 1;
		context.strokeRect(a.x - 0.5, a.y - 0.5, a.width, a.height);
		context.fillStyle = "#000";
		context.fillText(text, b.x, b.y + 13);
	}
	if (!this.isInherited && !this.isDeselected) {
		this.conceptConnector.paint(context, null);
	}
};

MappingConnection.prototype.paintBox = function(context, dashed) {
	var startElement = this.from.owner.findVisible();
	var endElement = (this.to !== null) ? this.to.owner.findVisible() : null;
	
	var start = startElement.getConnectorPosition(this.from);
	var end = (endElement !== null) ? endElement.getConnectorPosition(this.to) : this.toPoint;
	
	var x = start.x + (end.x - start.x)/2;
	var y = start.y + (end.y - start.y)/2;
	
	context.strokeRect(x-8, y-8, 16, 16);
	
	context.fillStyle = "#FFF";
	context.fillRect(x-7, y-7, 14, 14);
};

MappingConnection.prototype.paintLine = function(context, dashed)
{
	if (this.from !== null)
	{
		var startElement = this.from.owner.findVisible();
		var endElement = (this.to !== null) ? this.to.owner.findVisible() : null;
		
		var start = startElement.getConnectorPosition(this.from);
		var end = (endElement !== null) ? endElement.getConnectorPosition(this.to) : this.toPoint;

		if (this.isInherited && startElement !== null && endElement !== null) {
			if (startElement.getMappingConnector().connections.length > 0 || 
					endElement.getMappingConnector().connections.length > 0) {
				//return;
			}
		}
		
		if ((start.x != end.x) || (start.y != end.y))
		{
			context.beginPath();
			if (dashed)
			{
				context.dashedLine(start.x, start.y, end.x, end.y);
			}
			else
			{
				context.moveTo(start.x - 0.5, start.y - 0.5);
				context.lineTo(end.x - 0.5, end.y - 0.5);
			}
			context.closePath();
			context.stroke();
		}
	}
};