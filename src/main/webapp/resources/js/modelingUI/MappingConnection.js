var MappingConnection = function(from, to, id, score, isVerified) {
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