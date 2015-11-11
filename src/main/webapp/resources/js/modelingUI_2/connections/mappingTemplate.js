var MappingTemplate = function(model, options) {
	this.model = model;
	
	this.functionTemplate = new FunctionTemplate(this.model, { 
		primaryColor: "#FFE173", 
		secondaryColor: "#6d5603", 
		radius: 5
	});
	
	this.options = $.extend(true, {
		relativeControlPointX : 4,
		connectionHoverTolerance : 5
	}, options);
}

MappingTemplate.prototype.init = function(connection) {
	connection.func = new Function(connection, this.functionTemplate);
	connection.forkPoint = new Point(0, 0);
	connection.movedForkPoint = null; 
};

MappingTemplate.prototype.hitTest = function(connection, point) { 
	var result = connection.func.hitTest(point);
	if (result!=null) {
		return result;
	}
	
	// TODO: Evaluate rectangles of the segments separately
	if (!this.getRectangle(connection).contains(point)) {
		return null;
	}
	
	// TODO: Requires implementation
	var yShould = this.getCurvePositionForX(point.x, this.getCubicCurve(connection.from, connection.forkPoint));
		
	if (yShould-point.y < this.options.connectionHoverTolerance && point.y - yShould < this.options.connectionHoverTolerance) {
		return connection;
	} 
	return null;
};

MappingTemplate.prototype.getCubicCurve = function(from, to) {
	var split = (to.x - from.x) / this.options.relativeControlPointX;
	var p1 = new Point(split+from.x, from.y); 
	var p2 = new Point(split*(this.options.relativeControlPointX-1)+from.x, to.y);
	return { p0: from, p1: p1, p2: p2, p3: to };
};

MappingTemplate.prototype.getCurvePositionForX = function(x, curve) {
	// 1. Find t
	//t = f(x); 	
	
	// 2. Find y
	//var x = (1-t)*(1-t)*(1-t) * curve.p0.x + 3*(1-t)*(1-t)*t * curve.p1.x + 3*(1-t)*t*t * curve.p2.x + t*t*t * curve.p3.x;
	//var y = (1-t)*(1-t)*(1-t) * curve.p0.y + 3*(1-t)*(1-t)*t * curve.p1.y + 3*(1-t)*t*t * curve.p2.y + t*t*t * curve.p3.y;
    
	return -100;
};

/**
 * This should be rethought and refactored
 */
MappingTemplate.prototype.paint = function(connection, context) {
	if (connection.from.element.isVisible()) {
		var from = connection.from.getPosition();
		var fromParent = false;
	} else {
		var from = connection.from.element.findVisibleParent().getConnector("mappings").getPosition();
		var fromParent = true;
	}

	var height = from.y;
	var strokeStyle;
	
	if (connection.active || connection.isSelected()) {
		context.lineWidth = 2;
		strokeStyle = this.model.theme.mappingConnectionSelected;
	} else {
		context.lineWidth = 1;
		strokeStyle = this.model.theme.mappingConnectionDefault;
	}
	
	var connectedParents = [];
	var renderedTo = [];
	
	if (connection.to!==undefined && connection.to!==null && connection.to.length > 0) {
		var pFork = new Point(null, from.y);
		var count = 1; // Including the from point	
		for (var i=0; i<connection.to.length; i++) {
			if (!connection.to[i].element.isVisible()) {
				var visibleParent = connection.to[i].element.findVisibleParent();
				var to = visibleParent.getConnector("mappings").getPosition();
				if (!connectedParents.contains(to)) {
					connectedParents.push(to)
					renderedTo.push(to)
				} else {
					continue;
				}
			} else {
				var to = connection.to[i].getPosition();
				renderedTo.push(connection.to[i].getPosition());
			}
						
			pFork.y += to.y;
			if (pFork.x===null || pFork.x>to.x) {
				pFork.x = to.x;
			}
			count++;
		}
		
		// One line to the fork point
		if (connection.movedForkPoint!=null) {
			connection.forkPoint = connection.movedForkPoint;
		} else {
			// Calculated fork point
			pFork.x = (pFork.x - from.x)/2 + from.x;
			pFork.y = pFork.y / count;
			
			connection.forkPoint = pFork;
		}

		var split = (connection.forkPoint.x - from.x) / this.options.relativeControlPointX;
		if (fromParent) {
			context.strokeStyle = this.model.theme.mappingConnectionInvisible;
		} else {
			context.strokeStyle = strokeStyle;
		}
		context.beginPath();
		context.moveTo(from.x, from.y);
		context.bezierCurveTo(split+from.x, from.y, split*(this.options.relativeControlPointX-1)+from.x, connection.forkPoint.y, connection.forkPoint.x, connection.forkPoint.y);
		context.stroke();

		// [to.length] lines from fork point
		for (var i=0; i<renderedTo.length; i++) {
			var to = renderedTo[i];
			var toParent = connectedParents.contains(to);
			
			if (toParent) {
				context.strokeStyle = this.model.theme.mappingConnectionInvisible;
			} else {
				context.strokeStyle = strokeStyle;
			}
			
			var split = (to.x - connection.forkPoint.x) / this.options.relativeControlPointX;
			context.beginPath();
			context.moveTo(connection.forkPoint.x, connection.forkPoint.y);
			
			var toY = to.y;
			if (toParent) { 
				if (connection.forkPoint.y < to.y) {
					toY -= 4;
				} else {
					toY += 4;
				} 			
			} 
			
			context.bezierCurveTo(split+connection.forkPoint.x, connection.forkPoint.y, split*(this.options.relativeControlPointX-1)+connection.forkPoint.x, toY, to.x, toY);
			context.stroke();
		}
	} else {
		// New connection being dragged
		var to = this.model.mousePosition;
		context.dashedLine(from.x, from.y, to.x, to.y);
	}
	
	// For debugging...
	//var rectangle = this.getRectangle(connection);
	//context.strokeRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	
	if (connection.to.length>0 && connection.func!==undefined && connection.func!==null && (
			!fromParent || renderedTo.length!=connectedParents.length)) {
		connection.func.paint(context);
	}
};

MappingTemplate.prototype.getRectangle = function(connection) {
	var from = connection.from.getPosition();
	var pMin = new Point(from.x, from.y);
	var pMax = new Point(from.x, from.y);
	
	if (connection.to.length>0) {
		for (var i=0; i<connection.to.length; i++) {
			var to = connection.to[i].getPosition(); 
			
			if (to.y < pMin.y) {
				pMin.y = to.y;
			}
			// left: to.x < pMin.x is not possible and thus not checked 
			
			if (to.x > pMax.x) {
				pMax.x = to.x;
			}
			if (to.y > pMax.y) {
				pMax.y = to.y;
			}
		}
	}
	return new Rectangle(pMin.x, pMin.y, pMax.x-pMin.x, pMax.y-pMin.y);
};