var Element = function(owner, template, point, id, parent, label, icon)
{
	this.id = id;
	this.template = template;
	this.content = label;
	this.owner = owner;
	this.graph = this.owner.owner;
	this.hover = false;
	this.selected = false;
	this.tracker = null;
	this.parent = parent;
	this.children = [];
	this.isExpanded = false;
	this.isVisible = false;
	this.expander = null;
	this.connectors = [];
	this.maxChildScore = 0;
	this.icon = icon;
	
	context = this.owner.owner.context;
	context.font = this.template.font;	
	width = context.measureText(this.content).width + 45
	if (icon != null) {
		width += 20
	}
	
	this.rectangle = new Rectangle(point.x, point.y, width, template.defaultHeight);
	
	for (var i = 0; i < template.connectorTemplates.length; i++)
	{
		var connectorTemplate = template.connectorTemplates[i];
		this.connectors.push(new Connector(this, connectorTemplate));
	}	
};

Element.prototype.addChild = function(child) {
	if (!this.children.contains(child)) {
		this.children.push(child);
		if (this.expander == null) {
			this.expander = new Expander(this);
		}
	}
	
	for (var i=0; i<this.children.length; i++) {
		for (var j=0; j<this.children.length - 1; j++) {
			var doSwitch = false;
			if (this.children[j+1].typeInfo === "Nonterminal" && (this.children[j].typeInfo === "fDesc" ||
					this.children[j].typeInfo === "fOut" || this.children[j].typeInfo === "Label") ) {
				doSwitch = true;
			} else if (this.children[j+1].typeInfo === "Label" && (this.children[j].typeInfo === "fDesc" ||
					this.children[j].typeInfo === "fOut") ) {
				doSwitch = true;
			} else if (this.children[j+1].typeInfo === "fDesc" && this.children[j].typeInfo === "fOut") {
				doSwitch = true;
			} else if ((this.children[j+1].typeInfo === this.children[j].typeInfo) && 
					(this.children[j+1].content.toLowerCase() <  this.children[j].content.toLowerCase())) {
				doSwitch = true;
			}
			
			if (doSwitch) {
				var tmp = this.children[j+1];
				this.children[j+1] = this.children[j];
				this.children[j] = tmp;
			}
		}
	}
	
};

/** Behavior hide/show all subordinate elements depending on the expander states 
 * 	 expander states of subordinate elements stay as they are, only the visibility is toggled */
Element.prototype.setExpanded = function(expand) {
	this.isExpanded = expand;
	
	// Reflect Expander state to all children
	if (this.children.length > 0) {
		for (var i=0; i<this.children.length; i++) {
			// Recursively set the visibility of the child elements
			this.children[i].setVisibility(expand);
		}
	}
};

Element.prototype.addScore = function(score) {
	if (this.parent !== null && this.parent.maxChildScore < score) {
		this.parent.maxChildScore = score;
		this.parent.addScore(score);
	}
};

Element.prototype.setVisibility = function(isVisible) {
	this.isVisible = isVisible;
	
	// No changes to children necessary if the expander is closed
	if (!this.isExpanded) {
		return;
	}
	if (this.children.length > 0) {
		for (var i=0; i<this.children.length; i++) {
			this.children[i].setVisibility(isVisible);
		}
	}
};


Element.prototype.findVisible = function() {
	if (this.isVisible) {
		return this;
	}
	if (this.parent == null) {
		return null;
	}
	return this.parent.findVisible();
};

Element.prototype.select = function()
{
	this.selected = true;
	this.tracker = new Tracker(this.rectangle, 
			("resizable" in this.template) ? this.template.resizable : false, 
			("moveable" in this.template) ? this.template.moveable : false);
	this.invalidate();
};

Element.prototype.deselect = function()
{
	this.selected = false;
	this.invalidate();
	this.tracker = null;
};

Element.prototype.getRectangle = function()
{
	return ((this.tracker !== null) && (this.tracker.track)) ? this.tracker.rectangle : this.rectangle;
};

Element.prototype.getPageRectangle = function()
{
	var rectangle = this.getRectangle();
	rectangle = new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	var canvas = this.owner.canvas;
	rectangle.x += canvas.offsetLeft;
	rectangle.y += canvas.offsetTop;
	return rectangle;
};

Element.prototype.setRectangle = function(rectangle)
{
	this.invalidate();
	this.rectangle = rectangle;
	if (this.tracker !== null)
	{
		this.tracker.rectangle = new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}
	this.invalidate();
};

Element.prototype.paint = function(context)
{
	this.template.paint(this, context);
	
	if (this.selected)
	{
		this.tracker.paint(context);
	}

	if (this.expander != null) {
		this.expander.paint(context);
	}
		
};

Element.prototype.getExpanderPosition = function() {
	// Expander for showing/hiding child elements
	if (this.template.isTarget) {
		this.expanderPosition = { x: this.rectangle.width-10, y: Math.floor(this.rectangle.height / 2) };
	} else {
		this.expanderPosition = { x: 10, y: Math.floor(this.rectangle.height / 2) };
	}
	
	return new Point(this.rectangle.x + this.expanderPosition.x, this.rectangle.y + this.expanderPosition.y);
};

Element.prototype.invalidate = function() {
	
};

Element.prototype.insertInto = function(owner)
{
	this.owner = owner;
	this.owner.elements.push(this);
};

Element.prototype.remove = function()
{
	this.invalidate();

	for (var i = 0; i < this.connectors.length; i++)
	{
		var connections = this.connectors[i].connections;
		for (var j = 0; j < connections.length; j++)
		{
			connections[j].remove();
		}
	}
	
	if (this.parent!=null) {
		this.parent.children.remove(this);
		if (this.parent.children.length==0) {
			this.parent.expander = null;
		}
	}
	
	if ((this.owner !== null) && (this.owner.elements.contains(this)))
	{
		this.owner.elements.remove(this);
	}

	this.owner = null;
};

Element.prototype.hitTest = function(rectangle)
{
	if ((rectangle.width === 0) && (rectangle.height === 0))
	{
		if (this.rectangle.contains(rectangle.topLeft()))
		{
			return true;
		}

		if ((this.tracker !== null) && (this.tracker.track))
		{
			var h = this.tracker.hitTest(rectangle.topLeft());
			if ((h.x >= -1) && (h.x <= +1) && (h.y >= -1) && (h.y <= +1))
			{
				return true;
			}
		}

		for (var i = 0; i < this.connectors.length; i++)
		{
			if (this.connectors[i].hitTest(rectangle))
			{
				return true;
			}
		}

		return false;
	}

	return rectangle.contains(this.rectangle);
};

Element.prototype.getCursor = function(point)
{
	/*if (this.tracker !== null)
	{
		var cursor = this.tracker.getCursor(point);
		if (cursor !== null)
		{
			return cursor;
		}
	}

	if (window.event.shiftKey)
	{
		return Cursors.add;
	}

	return Cursors.select; */
	
	return Cursors.arrow;
};

Element.prototype.getConnector = function(name)
{
	for (var i = 0; i < this.connectors.length; i++)
	{
		var connector = this.connectors[i];
		if (connector.template.name == name)
		{
			return connector;
		}
	}
	return null;
};

Element.prototype.getMappingConnector = function()
{
	for (var i = 0; i < this.connectors.length; i++)
	{
		var connector = this.connectors[i];
		if (connector.template.isMappable == true)
		{
			return connector;
		}
	}
	return null;
};


Element.prototype.getConnectorPosition = function(connector)
{
	var rectangle = this.getRectangle();
	var point = connector.template.position(this);
	point.x += rectangle.x;
	point.y += rectangle.y;
	return point;
};

Element.prototype.setContent = function(content)
{
	this.owner.setElementContent(this, content);
};

Element.prototype.getContent = function()
{
	return this.content;
};
