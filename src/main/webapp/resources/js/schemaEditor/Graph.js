
var Graph = function(element)
{
	this.canvas = element;
	this.canvas.focus();
	this.context = this.canvas.getContext("2d");
	this.theme = { 
			background: "#FFFFFF", 
			connection: "#000", 
			selection: "#fff", 
			connector: "#FFF", 
			connectorBorder: "#000", 
			connectorHoverBorder: "#fff", 
			connectorHover: "#0c0",
			expander: "#DEDEDE", 
			expanderBorder: "#000", 
			expanderHover: "#B9A667",
			expanderHoverBorder: "#000" };
	
	
	this.pointerPosition = new Point(0, 0);
	this.shiftKey = false;
	this.undoService = new UndoService();
	this.elements = null;
	this.visibleElements = [];
	this.activeTemplate = null;
	this.activeObject = null;
	this.selectedItems = [];
	this.newElement = null;
	this.newConnection = null;
	this.selection = null;
	this.track = false;
	this.areas = [];
	this.isMouseDown = 0;
	
	this.mouseDownHandler = this.mouseDown.bind(this);
	this.mouseUpHandler = this.mouseUp.bind(this);
	this.mouseMoveHandler = this.mouseMove.bind(this);
	this.mouseLeaveHandler = this.mouseLeave.bind(this);
	this.mouseWheelHandler = this.mouseWheel.bind(this);
	this.doubleClickHandler = this.doubleClick.bind(this);
	this.touchStartHandler = this.touchStart.bind(this);
	this.touchEndHandler = this.touchEnd.bind(this);
	this.touchMoveHandler = this.touchMove.bind(this);
	this.keyDownHandler = this.keyDown.bind(this);
	this.keyPressHandler = this.keyPress.bind(this);
	this.keyUpHandler = this.keyUp.bind(this);	

	this.canvas.addEventListener("mousedown", this.mouseDownHandler, false);
	this.canvas.addEventListener("mouseup", this.mouseUpHandler, false);
	this.canvas.addEventListener("mouseleave", this.mouseLeaveHandler, false);
	this.canvas.addEventListener("mousemove", this.mouseMoveHandler, false);	
	this.canvas.addEventListener("touchstart", this.touchStartHandler, false);
	this.canvas.addEventListener("touchend", this.touchEndHandler, false);
	this.canvas.addEventListener("touchmove", this.touchMoveHandler, false);
	this.canvas.addEventListener("dblclick", this.doubleClickHandler, false);
	this.canvas.addEventListener("keydown", this.keyDownHandler, false);
	this.canvas.addEventListener("keypress", this.keyPressHandler, false);
	this.canvas.addEventListener("keyup", this.keyUpHandler, false);
		
	// Not working in FF
	this.canvas.addEventListener("mousewheel", this.mouseWheelHandler, false);
	
	this.isWebKit = typeof navigator.userAgent.split("WebKit/")[1] !== "undefined";
	this.isMozilla = navigator.appVersion.indexOf('Gecko/') >= 0 || ((navigator.userAgent.indexOf("Gecko") >= 0) && !this.isWebKit && (typeof navigator.appVersion !== "undefined"));
};

Graph.prototype.getElements = function() {
	var elements = [];
	for (var i=0; i<this.areas.length; i++) {
		for (var j=0; j<this.areas[i].elements.length; j++) {
			elements.push(this.areas[i].elements[j]);
		}
	}
	return elements;
};

Graph.prototype.dispose = function()
{
	if (this.canvas !== null)
	{
		this.canvas.removeEventListener("mousedown", this.mouseDownHandler);
		this.canvas.removeEventListener("mouseup", this.mouseUpHandler);
		this.canvas.removeEventListener("mousemove", this.mouseMoveHandler);
		this.canvas.removeEventListener("dblclick", this.doubleClickHandler);
		this.canvas.removeEventListener("touchstart", this.touchStartHandler);
		this.canvas.removeEventListener("touchend", this.touchEndHandler);
		this.canvas.removeEventListener("touchmove", this.touchMoveHandler);
		this.canvas.removeEventListener("keydown", this.keyDownHandler);
		this.canvas.removeEventListener("keypress", this.keyPressHandler);
		this.canvas.removeEventListener("keyup", this.keyUpHandler);	
		this.canvas = null;
		this.context = null;
	}
};

Graph.prototype.mouseDown = function(e)
{
	e.preventDefault();
	this.canvas.focus();
	this.updateMousePosition(e);

	if (e.button === 0) // left-click
	{
		// alt+click allows fast creation of element using the active template
		if ((this.newElement === null) && (e.altKey))
		{
			this.createElement(this.activeTemplate);
		}
		this.isMouseDown = 1;
		this.pointerDown();
	}
};

Graph.prototype.mouseUp = function(e)
{
	e.preventDefault();
	this.updateMousePosition(e);	
	if (e.button === 0) // left-click
	{
		this.pointerUp();
		this.isMouseDown = 0;
	}
};

Graph.prototype.mouseMove = function(e)
{
	e.preventDefault();	
	this.updateMousePosition(e);
	this.pointerMove();
};

Graph.prototype.mouseLeave = function(e) {
	e.preventDefault(); 
	this.updateMousePosition(e);
	this.pointerMove();
};

Graph.prototype.mouseWheel = function(e)
{
	e.preventDefault(); 
	this.updateMousePosition(e);

	var point = this.pointerPosition;
		
	for (var i=0; i<this.areas.length; i++) {
		if (this.areas[i].hitTest(point)) {
			this.areas[i].moveY(point, e.wheelDeltaY);
			break;
		}
	}
	
	this.update();
	this.updateMouseCursor();
};

Graph.prototype.doubleClick = function(e)
{
	e.preventDefault();
	this.updateMousePosition(e);

	if (e.button === 0) // left-click
	{
		var point = this.pointerPosition;

		this.updateActiveObject(point);
		if ((this.activeObject !== null) && (this.activeObject instanceof Element) && (this.activeObject.template !== null) && ("edit" in this.activeObject.template))
		{
			this.activeObject.template.edit(this.activeObject, point);
			this.update();
		}
	}
};

Graph.prototype.touchStart = function(e)
{	
	if (e.touches.length == 1)
	{
		e.preventDefault();
		this.updateTouchPosition(e);
		this.pointerDown();
	}	
};

Graph.prototype.touchEnd = function(e)
{	
	e.preventDefault();
	this.pointerUp();
};

Graph.prototype.touchMove = function(e)
{
	if (e.touches.length == 1)
	{
		e.preventDefault();
		this.updateTouchPosition(e);
		this.pointerMove();
	}
};

Graph.prototype.selectElement = function(element) {
	if (element != this.activeObject) {
		if (this.activeObject !== null) {
			this.activeObject.hover = false;
		}
		this.activeObject = element;
		if (this.activeObject !== null) {
			this.activeObject.hover = true;
		}
	}
	
	this.undoService.begin();
	var selectionUndoUnit = new SelectionUndoUnit();
	if (!this.shiftKey)
	{
		this.deselectAll(selectionUndoUnit);
		while (this.selectedItems.length > 0) {
			this.selectedItems.remove(this.selectedItems[0]);
		}
	}
	selectionUndoUnit.select(this.activeObject);
	this.selectedItems.push(this.activeObject);
	this.undoService.add(selectionUndoUnit);
	this.undoService.commit();
	
	element.select();
	
	this.handleElementSelected();
};
	
Graph.prototype.pointerDown = function()
{
	var point = this.pointerPosition;

	if (this.newElement !== null)
	{
		this.undoService.begin();
		this.newElement.invalidate();
		this.newElement.rectangle = new Rectangle(point.x, point.y, this.newElement.rectangle.width, this.newElement.rectangle.height);
		this.newElement.invalidate();
		this.undoService.add(new InsertElementUndoUnit(this.newElement, this));
		this.undoService.commit();
		this.newElement = null;
	}
	else
	{
		this.selection = null;
		this.updateActiveObject(point);
		if (this.activeObject === null)
		{			
			for (var i=0; i<this.areas.length; i++) {
				if (this.areas[i].hitTest(point)) {
					this.areas[i].pointerDown(point);
				}
			}
			
			if (this.selectedItems.length > 0) {
				this.undoService.begin();
				var deselectUndoUnit = new SelectionUndoUnit();
				while (this.selectedItems.length > 0) {
					deselectUndoUnit.deselect(this.selectedItems[0]);
					this.selectedItems.remove(this.selectedItems[0]);
				}
				this.undoService.add(deselectUndoUnit);
				this.undoService.commit();
				
				var deselectionEvent = document.createEvent("Event");
				deselectionEvent.initEvent("deselectionEvent", true, true);
				document.dispatchEvent(deselectionEvent);
			}
		}
		else
		{	
			if (this.activeObject instanceof MappingConnection)
			{
				var selectionEvent = document.createEvent("Event");
				selectionEvent.initEvent("selectionEvent", true, true);
				selectionEvent.elementType = "MappingConnection";
				selectionEvent.elementId = this.activeObject.id;
				selectionEvent.input = this.activeObject.from.owner.id;
				selectionEvent.output = this.activeObject.to.owner.id;
				document.dispatchEvent(selectionEvent);
			} else if (this.activeObject instanceof Element) {
				if (!this.activeObject.selected) {
					this.handleElementSelected(this.activeObject);
				} else {
					this.handleElementDeselected();
				}
			} else if (this.activeObject instanceof Expander) {
				// Irrelevant for selection
			} else {
				this.handleElementDeselected();
			}
			
			// start connection
			if (this.activeObject instanceof Expander) {
				this.activeObject.owner.setExpanded(!this.activeObject.owner.isExpanded);
			}
			else if ((this.activeObject instanceof Connector) && (!this.shiftKey))
			{
				if (this.activeObject.isValid(null))
				{
					this.newConnection = new MappingConnection(this.activeObject, null, -1, 1, true);
					this.newConnection.toPoint = point;
					this.activeObject.invalidate();
				}
			}
			else {				
				// select object
				if (!this.activeObject.selected)
				{
					this.undoService.begin();
					var selectionUndoUnit = new SelectionUndoUnit();
					if (!this.shiftKey)
					{
						this.deselectAll(selectionUndoUnit);
						while (this.selectedItems.length > 0) {
							this.selectedItems.remove(this.selectedItems[0]);
						}
					}
					selectionUndoUnit.select(this.activeObject);
					this.selectedItems.push(this.activeObject);
					this.undoService.add(selectionUndoUnit);
					this.undoService.commit();
				}
				else 
				{					
					this.undoService.begin();
					var deselectUndoUnit = new SelectionUndoUnit();
					deselectUndoUnit.deselect(this.activeObject);
					this.undoService.add(deselectUndoUnit);
					this.undoService.commit();
				} 

				// start tracking
				var hit = new Point(0, 0);
				if (this.activeObject instanceof Element && this.activeObject.tracker != null)
				{
					hit = this.activeObject.tracker.hitTest(point);
				}
				for (var i = 0; i < this.visibleElements.length; i++)
				{
					var element = this.visibleElements[i];
					if (element.tracker !== null)
					{
						element.tracker.start(point, hit);
					}
				}

				this.track = true;
			}
		}
	}

	this.update();
	this.updateMouseCursor();
};

Graph.prototype.handleElementDeselected = function() {
	var deselectionEvent = document.createEvent("Event");
	deselectionEvent.initEvent("deselectionEvent", true, true);
	document.dispatchEvent(deselectionEvent);
};

Graph.prototype.handleElementSelected = function(element) {
	var selectionEvent = document.createEvent("Event");
	selectionEvent.initEvent("selectionEvent", true, true);
	selectionEvent.elementType = "Function";
	if (element.template instanceof FunctionTemplate) {
		if (element.typeInfo === "fDesc") {
			selectionEvent.elementSubtype = "DescriptiveFunction";
		} else {
			selectionEvent.elementSubtype = "OutputFunction";
		}				
	} else {
		selectionEvent.elementType = "Element";
		selectionEvent.elementSubtype = element.typeInfo;				
	}
	selectionEvent.elementId = element.id;
	document.dispatchEvent(selectionEvent);
};

Graph.prototype.pointerUp = function()
{
	var point = this.pointerPosition;

	if (this.newConnection !== null)
	{
		this.updateActiveObject(point);
		this.newConnection.invalidate();
				
		if ((this.activeObject !== null) && ((this.activeObject instanceof Connector) || (this.activeObject instanceof Element)))
		{
			if ((this.activeObject != this.newConnection.from) && (this.activeObject.isValid(this.newConnection.from)))
			{
				this.undoService.begin();
				this.undoService.add(new InsertConnectionUndoUnit(this.newConnection, this.newConnection.from, this.activeObject));
				this.undoService.commit();

				/** TODO: 'tmpControl' needs to be refactored and this next call as well... */
				this.newConnection.to = this.activeObject;
				
				var newConnectionEvent = document.createEvent("Event");
				newConnectionEvent.initEvent("newMappingCellEvent", true, true);
				newConnectionEvent.input = this.newConnection.from.owner.id;
				newConnectionEvent.output = this.newConnection.to.owner.id;
				document.dispatchEvent(newConnectionEvent);
			}
		}

		this.newConnection = null;
	}

	if (this.selection !== null)
	{
		this.undoService.begin();
		var selectionUndoUnit = new SelectionUndoUnit();

		var rectangle = this.selection.getRectangle();
		if ((this.activeObject === null) || (!this.activeObject.selected))
		{
			if (!this.shiftKey)
			{
				this.deselectAll(selectionUndoUnit);
			}
		}

		if ((rectangle.width !== 0) || (rectangle.weight !== 0))
		{
			this.selectAll(selectionUndoUnit, rectangle);
		}

		this.undoService.add(selectionUndoUnit);
		this.undoService.commit();
		this.selection = null;
	}

	for (var i=0; i<this.areas.length; i++) {
		this.areas[i].pointerUp(point);
	}
		
	if (this.track)
	{
		this.undoService.begin();
		for (var i = 0; i < this.visibleElements.length; i++)
		{
			var element = this.visibleElements[i];
			if (element.tracker !== null)
			{
				element.tracker.track = false;
				element.invalidate();
				var r1 = element.getRectangle();
				var r2 = element.tracker.rectangle;
				if ((r1.x != r2.x) || (r1.y != r2.y) || (r1.width != r2.width) || (r1.height != r2.height))
				{
					this.undoService.add(new TransformUndoUnit(element, r1, r2));
				}
			}
		}

		this.undoService.commit();
		this.track = false;
		this.updateActiveObject(point);
	}

	this.update();
	this.updateMouseCursor();
};

Graph.prototype.pointerMove = function()
{
	var point = this.pointerPosition;

	if (this.newElement !== null)
	{
		// placing new element
		this.newElement.invalidate();
		this.newElement.rectangle = new Rectangle(point.x, point.y, this.newElement.rectangle.width, this.newElement.rectangle.height);
		this.newElement.invalidate();
	}
	
	for (var i=0; i<this.areas.length; i++) {
		this.areas[i].pointerMove(point);
	}
		
	if (this.track)
	{
		// moving selected elements
		for (var i = 0; i < this.visibleElements.length; i++)
		{
			var element = this.visibleElements[i];
			if (element.tracker !== null)
			{
				element.invalidate();
				element.tracker.move(point);
				element.invalidate();
			}
		}
	}

	if (this.newConnection !== null)
	{
		// connecting two connectors
		this.newConnection.invalidate();
		this.newConnection.toPoint = point;
		this.newConnection.invalidate();
	}

	if (this.selection !== null)
	{
		this.selection.currentPoint = point;
	}

	this.updateActiveObject(point);
	if (this.isMouseDown>0) {
		this.update();
	}
	this.updateMouseCursor();
};


Graph.prototype.keyDown = function(e)
{
	if (!this.isMozilla)
	{
		this.processKey(e, e.keyCode);
	}
};

Graph.prototype.keyPress = function(e)
{
	if (this.isMozilla)
	{
		if (typeof this.keyCodeTable === "undefined")
		{
			this.keyCodeTable = [];
			var charCodeTable = {
				32: ' ',  48: '0',  49: '1',  50: '2',  51: '3',  52: '4', 53:  '5',  54: '6',  55: '7',  56: '8',  57: '9',  59: ';',  61: '=', 
				65:  'a', 66: 'b',  67: 'c',  68: 'd',  69: 'e',  70: 'f',  71: 'g', 72:  'h',  73: 'i',  74: 'j',  75: 'k',  76: 'l',  77: 'm',  78: 'n', 79:  'o', 80: 'p',  81: 'q',  82: 'r',  83: 's',  84: 't',  85: 'u', 86: 'v', 87: 'w',  88: 'x',  89: 'y',  90: 'z',
				107: '+', 109: '-', 110: '.', 188: ',', 190: '.', 191: '/', 192: '`', 219: '[', 220: '\\', 221: ']', 222: '\"' 
			};

			for (var keyCode in charCodeTable)
			{
				var key = charCodeTable[keyCode];
				this.keyCodeTable[key.charCodeAt(0)] = keyCode;
				if (key.toUpperCase() != key)
				{
					this.keyCodeTable[key.toUpperCase().charCodeAt(0)] = keyCode;
				}
			}
		}
		
		this.processKey(e, (this.keyCodeTable[e.charCode]) ? this.keyCodeTable[e.charCode] : e.keyCode);
	}
};

Graph.prototype.keyUp = function(e)
{
	this.updateMouseCursor();
};

Graph.prototype.processKey = function(e, keyCode)
{
	if ((e.ctrlKey || e.metaKey) && !e.altKey) // ctrl or option
	{
		if (keyCode == 65) // A - select all
		{
			this.undoService.begin();
			var selectionUndoUnit = new SelectionUndoUnit();
			this.selectAll(selectionUndoUnit, null);
			this.undoService.add(selectionUndoUnit);
			this.undoService.commit();
			this.update();
			this.updateActiveObject(this.pointerPosition);
			this.updateMouseCursor();
			this.stopEvent(e);
		}

		if ((keyCode == 90) && (!e.shiftKey)) // Z - undo
		{
			this.undoService.undo();
			this.update();
			this.updateActiveObject(this.pointerPosition);
			this.updateMouseCursor();
			this.stopEvent(e);
		}
		
		if (((keyCode == 90) && (e.shiftKey)) || (keyCode == 89)) // Y - redo
		{
			this.undoService.redo();
			this.update();
			this.updateActiveObject(this.pointerPosition);
			this.updateMouseCursor();
			this.stopEvent(e);
		}
	}

	if ((keyCode == 46) || (keyCode == 8)) // DEL - delete
	{
		this.deleteSelection();
		this.update();
		this.updateActiveObject(this.pointerPosition);
		this.updateMouseCursor();
		this.stopEvent(e);
	}

	if (keyCode == 27) // ESC
	{
		this.newElement = null;
		this.newConnection = null;

		this.track = false;
		
		var elements = this.getElements();
		
		for (var i = 0; i < elements.length; i++)
		{
			var element = elements[i];
			if (element.tracker !== null)
			{
				element.tracker.track = false;
			}
		}
		
		this.update();
		this.updateActiveObject(this.pointerPosition);
		this.updateMouseCursor();
		this.stopEvent(e);
	}
};

Graph.prototype.stopEvent = function(e)
{
	e.preventDefault();
	e.stopPropagation();
};

Graph.prototype.removeMappingConnection = function(id)
{
	var i, j, k;
	
	var elements = this.getElements();
	
	for (i = 0; i < elements.length; i++)
	{
		element = elements[i];
		for (j = 0; j < element.connectors.length; j++)
		{
			var connector = element.connectors[j];
			for (k = 0; k < connector.connections.length; k++)
			{
				var connection = connector.connections[k];
				if ((connection instanceof MappingConnection) && (connection.id==id))
				{
					if (this.activeObject===connection) {
						this.activeObject = null;
					}
					
					var deselectionEvent = document.createEvent("Event");
					deselectionEvent.initEvent("deselectionEvent", true, true);
					document.dispatchEvent(deselectionEvent);
					
					connection.remove();
				}
			}
		}
	}
};

Graph.prototype.deleteSelection = function()
{
	var i, j, k;
	var element;
	
	this.undoService.begin();

	var deletedConnections = [];
	var elements = this.getElements();
	
	for (i = 0; i < elements.length; i++)
	{
		element = elements[i];
		for (j = 0; j < element.connectors.length; j++)
		{
			var connector = element.connectors[j];
			for (k = 0; k < connector.connections.length; k++)
			{
				var connection = connector.connections[k];
				if ((element.selected || connection.selected) && (!deletedConnections.contains(connection)))
				{
					this.undoService.add(new DeleteConnectionUndoUnit(connection));
					deletedConnections.push(connection);
				}
			}
		}
	}
	
	for (i = 0; i < elements.length; i++)
	{
		element = elements[i];
		if (element.selected)
		{
			this.undoService.add(new DeleteElementUndoUnit(element));
		}
	}

	this.undoService.commit();
};

Graph.prototype.selectAll = function(selectionUndoUnit, rectangle)
{
	var elements = this.getElements();
	
	for (var i = 0; i < elements.length; i++)
	{
		var element = elements[i];
		if ((rectangle === null) || (element.hitTest(rectangle)))
		{
			selectionUndoUnit.select(element);
		}

		for (var j = 0; j < element.connectors.length; j++)
		{
			var connector = element.connectors[j];
			for (var k = 0; k < connector.connections.length; k++)
			{
				var connection = connector.connections[k];
				if ((rectangle === null) || (connection.hitTest(rectangle)))
				{
					selectionUndoUnit.select(connection);
				}
			}
		}
	}
};

Graph.prototype.deselectAll = function(selectionUndoUnit)
{
	var elements = this.getElements();
	
	for (var i = 0; i < elements.length; i++)
	{
		var element = elements[i];
		selectionUndoUnit.deselect(element);

		for (var j = 0; j < element.connectors.length; j++)
		{
			var connector = element.connectors[j];
			for (var k = 0; k < connector.connections.length; k++)
			{
				var connection = connector.connections[k];
				selectionUndoUnit.deselect(connection);
			}
		}
	}
};

Graph.prototype.updateActiveObject = function(point)
{
	var hitObject = this.hitTest(point);
	if (hitObject != this.activeObject)
	{
		if (this.activeObject !== null) 
		{
			this.activeObject.hover = false;
		}
		this.activeObject = hitObject;
		if (this.activeObject !== null)
		{
			this.activeObject.hover = true;
		}
	}
};

Graph.prototype.hitTest = function(point)
{
	var i, j, k;
	var element, connector, connection;

	var rectangle = new Rectangle(point.x, point.y, 0, 0);

	for (i = 0; i < this.visibleElements.length; i++)
	{
		element = this.visibleElements[i];
		for (j = 0; j < element.connectors.length; j++)
		{
			if (element.expander != null && element.expander.hitTest(rectangle)) {
				return element.expander;
			}
			connector = element.connectors[j];
			if (connector.hitTest(rectangle))
			{
				return connector;
			}
		}
	}

	for (i = 0; i < this.visibleElements.length; i++)
	{
		element = this.visibleElements[i];
		if (element.hitTest(rectangle))
		{
			return element;
		}
	}

	for (i = 0; i < this.visibleElements.length; i++)
	{
		element = this.visibleElements[i];
		for (j = 0; j < element.connectors.length; j++)
		{
			connector = element.connectors[j];
			for (k = 0; k < connector.connections.length; k++)
			{
				connection = connector.connections[k];
				if (connection.hitTest(rectangle))
				{
					return connection;
				}
			}
		}
	}

	return null;
};

Graph.prototype.updateMouseCursor = function()
{	
	if (this.newConnection !== null)
	{
		this.canvas.style.cursor = ((this.activeObject !== null) && (this.activeObject instanceof Connector)) ? this.activeObject.getCursor(this.pointerPosition) : Cursors.cross;
	}
	else
	{
		/*if (this.areaMoving !== null) {
			this.canvas.style.cursor = this.areaMoving.getCursor();
		} else {*/
			if (this.activeObject !== null) {
				this.canvas.style.cursor = this.activeObject.getCursor(this.pointerPosition);
			} else {
				this.canvas.style.cursor = Cursors.arrow;
			}
		//}
	}
};

Graph.prototype.updateMousePosition = function(e)
{
	this.shiftKey = e.shiftKey;
	this.pointerPosition = new Point(e.pageX, e.pageY);
	var node = this.canvas;
	while (node !== null)
	{
		this.pointerPosition.x -= node.offsetLeft;
		this.pointerPosition.y -= node.offsetTop;
		node = node.offsetParent;
	}
};

Graph.prototype.updateTouchPosition = function(e)
{
	this.shiftKey = false;
	this.pointerPosition = new Point(e.touches[0].pageX, e.touches[0].pageY);
	var node = this.canvas;
	while (node !== null)
	{
		this.pointerPosition.x -= node.offsetLeft;
		this.pointerPosition.y -= node.offsetTop;
		node = node.offsetParent;
	}	
};

Graph.prototype.addArea = function(area)
{
	this.areas.push(area);
};

Graph.prototype.addElement = function(template, point, id, content, parent)
{
	this.activeTemplate = template;
	var element = new Element(template, point, id, parent);
	element.content = content;
	element.insertInto(this);
	element.invalidate();
	return element;
};

Graph.prototype.createElement = function(template)
{
	this.activeTemplate = template;
	this.newElement = new Element(template, this.pointerPosition);
	this.update();
	this.canvas.focus();
};

Graph.prototype.addConnection = function(connector1, connector2, color, isHierarchyConnector)
{
	var connection = new Connection(connector1, connector2, color, isHierarchyConnector);
	connector1.connections.push(connection);
	connector2.connections.push(connection);
	connector1.invalidate();
	connector2.invalidate();
	connection.invalidate();
	return connection;
};

Graph.prototype.addMapping = function(connector1, connector2, id, score)
{
	var connection = new MappingConnection(connector1, connector2, id, score);
	
	if (score == 1) {
		connection.isVerified = true;
	}
	
	connector1.connections.push(connection);
	connector2.connections.push(connection);
	connector1.owner.addScore(score);
	connector2.owner.addScore(score);
	connector1.invalidate();
	connector2.invalidate();
	connection.invalidate();
	return connection;
};

Graph.prototype.setElementContent = function(element, content)
{
	this.undoService.begin();
	this.undoService.add(new ContentChangedUndoUnit(element, content));
	this.undoService.commit();
	this.update();
};

var requiresUpdate = true;

Graph.prototype.update = function()
{
	var i, j, k;
	var element, connector, connection;
	
	this.canvas.style.background = this.theme.background;
	this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
	
	
	this.visibleElements = [];
	for (i = 0; i < this.areas.length; i++)
	{
		var areaElements = this.areas[i].getVisibleElements();
		for (j = 0; j < areaElements.length; j++) {
			this.visibleElements.push(areaElements[j]);
		}
	}
	
	
	/**
	 * Besser: Frage an die beiden Areas, welche Attribute sichtbar sind,
	 * 	anzeigen der connections, die zumindest in einem der beiden sichtbar sind.
	 */
	var connections = [];
	var selectedConnections = [];
	var deselectedConnections = [];
	var hoveredConnection = null;
	
	for (i = 0; i < this.visibleElements.length; i++)
	{
		element = this.visibleElements[i];
		for (j = 0; j < element.connectors.length; j++)
		{
			connector = element.connectors[j];
			for (k = 0; k < connector.connections.length; k++)
			{
				connection = connector.connections[k];
				connection.ownerSelected = false;
				
				if (connection.hover) {
					hoveredConnection = connection;
				}
				
				if (element.selected && connection instanceof MappingConnection && !selectedConnections.contains(connection)) {
					selectedConnections.push(connection);
					
					if (!connections.contains(connection)) {
						connections.push(connection); 
					}
				}
				
				if (!connections.contains(connection))
				{
					if (connection.selected && !selectedConnections.contains(connection)) {
						selectedConnections.push(connection);
					} else if (!deselectedConnections.contains(connection)) {
						deselectedConnections.push(connection);
					}
					connections.push(connection);
				}
			}
		}
	}

	/**
	 * Connectors müssen nicht gezeichnet werden?! (Flaggable)
	 */	
	for (i = 0; i < deselectedConnections.length; i++)
	{
		if (selectedConnections.length > 0) {
			deselectedConnections[i].isDeselected = true;
		} else {
			deselectedConnections[i].isDeselected = false;
		}
		deselectedConnections[i].paint(this.context, this.pointerPosition);
	}
	
	for (i = 0; i < selectedConnections.length; i++)
	{
		if (!selectedConnections[i].selected) {
			selectedConnections[i].ownerSelected = true;
		}
		selectedConnections[i].isDeselected = false;
		selectedConnections[i].paint(this.context, this.pointerPosition);
	}
	
	if (hoveredConnection !== null)
	{
		hoveredConnection.paint(this.context, this.pointerPosition);
	}
	
	for (i = 0; i < this.visibleElements.length; i++)
	{
		this.context.save();
		this.visibleElements[i].paint(this.context);
		this.context.restore();
	}
	
	for (i = 0; i < this.visibleElements.length; i++)
	{
		element = this.visibleElements[i];
		for (j = 0; j < element.connectors.length; j++)
		{
			connector = element.connectors[j];

			var hover = false;
			for (k = 0; k < connector.connections.length; k++)
			{
				if (connector.connections[k].hover) { hover = true; }
			}

			if ((element.hover) || (connector.hover) || hover)
			{
				connector.paint(this.context, (this.newConnection !== null) ? this.newConnection.from : null);
			}
			else if ((this.newConnection !== null) && (connector.isValid(this.newConnection.from)))
			{
				connector.paint(this.context, this.newConnection.from);
			}
		}
	}
	
	for (i = 0; i < this.areas.length; i++)
	{
		this.context.save();
		this.areas[i].paint(this.context, this.pointerPosition);
		this.context.restore();
	}
	
	if (this.newElement !== null)
	{
		this.context.save();
		this.newElement.paint(this.context);
		this.context.restore();
	}
	
	if (this.newConnection !== null)
	{
		this.newConnection.paintTrack(this.context);
	}
	
	if (this.selection !== null)
	{
		this.context.strokeStyle = this.theme.selection;
		this.selection.paint(this.context);
	}
};