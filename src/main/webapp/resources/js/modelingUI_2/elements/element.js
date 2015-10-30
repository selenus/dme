var Element = function(template, parent, id, label, icons) {
	this.template = template;
	this.parent = parent;
	this.id = id;
	this.label = label;
	this.icons = [];
	this.children = [];
	this.expanded = true;
	this.selected = false;
	
	this.connectors = {
		child: null,
		parent: null,
		mappingIn: null,
		mappingOut: null,
	};
	
	if (icons!=undefined && icons!=null) {
		if (icons instanceof Array) {
			this.icons = icons;
		} else {
			this.icons.push(icons);
		}
	}

	this.rectangle = new Rectangle(0, 0, this.calculateWidth(), this.template.options.height);
};

Element.prototype.addChild = function(child) {
	this.children.push(child);
};

Element.prototype.setExpanded = function(expand) {
	this.expanded = expand;
};

Element.prototype.setRectangle = function(rect) {
	this.rectangle = rect;
};

Element.prototype.calculateWidth = function() {
	var context = this.template.area.model.context;
	context.font = this.template.font;	
	var width = context.measureText(this.label).width + 45
	if (this.icons != null) {
		width += this.icons.length * 20;
	}
	return width;
};