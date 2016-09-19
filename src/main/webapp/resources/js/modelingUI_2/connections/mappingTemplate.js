var MappingTemplate = function(model, options) {
	this.model = model;
	this.options = $.extend(true, {
		relativeControlPointX : 4,
		connectionHoverTolerance : 5
	}, options);
	
	this.functionTemplate = new FunctionTemplate(this.model, this.options.functionTemplateOptions);
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

MappingTemplate.prototype.getContextMenuItems = function(connection) {
	if (this.options.getContextMenuItems!==undefined) {
		return this.options.getContextMenuItems(connection);
	}
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


MappingTemplate.prototype.paint = function(connection, context) {
	if (connection.to===undefined || connection.to===null || connection.to.length==0) {
		this.paintNewConnection(connection.from[0], context);
		return;
	}
	
	var strokeStyle = this.model.theme.mappingConnectionDefault;
	context.lineWidth = 1;
	
	var toVisible = [];
	var toParents = [];
	var fromVisible = [];
	var fromParents = [];
	
	
	if (connection.active || connection.isSelected()) {
		context.lineWidth = 2;
		strokeStyle = this.model.theme.mappingConnectionSelected;
	}
	
	/** 
	 * Determine calculated "fork point"
	 */
	
	var pFork = new Point(null, null);
	
	var count = 0;
	var yTotal = 0;
	var xMaxFrom = 0;
	var xMinTo = 0;
	
	for (var i=0; i<connection.from.length; i++) {
		var point = this.addPosOrParent(connection.from[i], fromVisible, fromParents);
		yTotal += point.y;
		if (xMaxFrom==0 || xMaxFrom < point.x) {
			xMaxFrom=point.x;
		}
		count++;
	}
	
	for (var i=0; i<connection.to.length; i++) {
		var point = this.addPosOrParent(connection.to[i], toVisible, toParents);
		yTotal += point.y;
		if (xMinTo==0 || xMinTo > point.x) {
			xMinTo=point.x;
		}
		count++;
	}
	
	/**
	 * Stroke from 'from' to fork point
	 */
	if (connection.movedForkPoint!=null) {
		connection.forkPoint = connection.movedForkPoint;
	} else {
		// Calculated fork point
		pFork.x = (xMinTo - xMaxFrom)/2 + xMaxFrom;
		pFork.y = yTotal / count;
		
		connection.forkPoint = pFork;
	}
	
	
	var renderedFrom = [];
	for (var i=0; i<connection.from.length; i++) {
		renderedFrom.push(connection.from[i].getPosition());
	}
	
	for (var i=0; i<renderedFrom.length; i++) {
		
		
		var split = (connection.forkPoint.x - renderedFrom[i].x) / this.options.relativeControlPointX;
		/*if (fromParent) {
			context.strokeStyle = this.model.theme.mappingConnectionInvisible;
		} else {*/
			context.strokeStyle = strokeStyle;
		//}
		context.beginPath();
		context.moveTo(renderedFrom[i].x, renderedFrom[i].y);
		context.bezierCurveTo(split+renderedFrom[i].x, renderedFrom[i].y, split*(this.options.relativeControlPointX-1)+renderedFrom[i].x, connection.forkPoint.y, connection.forkPoint.x, connection.forkPoint.y);
		context.stroke();
	}
	


	/**
	 * Stroke from fork point to 'to'
	 */
	for (var i=0; i<toVisible.length; i++) {
		var to = toVisible[i];
		var toParent = toParents.contains(to);
		
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
	
	if (connection.to.length>0 && connection.func!==undefined && connection.func!==null) {
		connection.func.paint(context, false);//(fromParent || renderedTo.length==connectedParents.length));
	}
};

MappingTemplate.prototype.addPosOrParent = function(connector, from, fromParents) {
	if (!connector.element.isVisible()) {
		var parentElement = connector.element.findVisibleParent();
		var parentPos = parentElement.getConnector("mappings").getPosition();
		if (!fromParents.contains(parentPos)) {
			fromParents.push(parentPos);
		} 
		return parentPos;
	} else {
		var pos = connector.getPosition();
		if (!from.contains(pos)) {
			from.push(pos);
		}
		return pos;
	}
};

MappingTemplate.prototype.paintNewConnection = function(from, context) {
	var to = this.model.mousePosition;
	context.lineWidth = 1;
	context.strokeStyle = this.model.theme.mappingConnectionDefault;
	context.dashedLine(from.x, from.y, to.x, to.y);
}

MappingTemplate.prototype.getRectangle = function(connection) {
	var from = connection.from[0].getPosition();
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