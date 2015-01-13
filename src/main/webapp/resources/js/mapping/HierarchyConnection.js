var HierarchyConnection = function(from, to) {
	this.base = Connection;
	this.base(from, to, "#000", false);
	this.ownerSelected = false;
};

HierarchyConnection.prototype = new Connection;

HierarchyConnection.prototype.getCursor = function(point) {
	return Cursors.arrow;
};

/* Hit testing not required for HierarchyConnectors */
HierarchyConnection.prototype.hitTest = function(rectangle) {};

HierarchyConnection.prototype.paintLine = function(context, dashed)
{
	if (this.from !== null && (this.from.owner.isVisible && this.to.owner.isVisible))
	{
		var start = this.from.owner.getConnectorPosition(this.from);
		var end = (this.to !== null) ? this.to.owner.getConnectorPosition(this.to) : this.toPoint;

		context.beginPath();
		if (!dashed)
		{
			context.moveTo(start.x - 0.5, start.y - 0.5);
			context.lineTo(start.x - 0.5, end.y - 0.5);
			context.moveTo(start.x - 0.5, end.y - 0.5);
			context.lineTo(end.x - 0.5, end.y - 0.5);
			//context.dashedLine(start.x, start.y, end.x, end.y);
			//context.moveTo(start.x - 0.5, start.y - 0.5);
			//context.lineTo(end.x, end.y);
		}
		else
		{
			context.dashedLine(start.x - 0.5, start.y - 0.5, start.x - 0.5, end.y - 0.5);
			context.moveTo(start.x - 0.5, end.y - 0.5);
			context.dashedLine(start.x - 0.5, end.y - 0.5, end.x - 0.5, end.y - 0.5);
			//context.lineTo(end.x - 0.5, end.y - 0.5);
		}
		context.closePath();
		context.stroke();
	}
};