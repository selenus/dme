var Model = function(canvas, elementTemplateOptions, theme) {
	this.canvas = canvas;
	this.canvas.focus();
	this.context = this.canvas.getContext("2d");
	
	this.isMouseDown = false;
	this.mousePosition = new Point(-1, -1);
	this.activeObject = null;
	this.selectedItems = [];
	
	this.theme = ModelingTheme;
	if (theme!=undefined && theme!=null) {
		this.theme = $.extend({}, ModelingTheme, theme);
	}
	
	this.areas = [];
	
	// Only options defined here, actual element templates are with the areas
	/**
	 * key: "...", primaryColor: "#...", secondaryColor: "#...", radius: 5 (optional)
	 * getContextMenuItems: function(element) {} to produce items[] based on actual element (see model.contextmenu.js) 
	 */
	this.elementTemplateOptions = elementTemplateOptions;
	
	this.mappingConnection = new ConnectionTemplate(this);
	this.hierarchicalConnection = new ConnectionTemplate(this);
	this.initEvents();
}

Model.prototype.dispose = function() {
	if (this.canvas !== null) {
		this.removeEvents();
		this.canvas = null;
		this.context = null;
	}
};

Model.prototype.addArea = function() {
	var area = new Area(this);
	if (this.areas.length>0) {
		area.isTarget = true;
		this.areas[this.areas.length-1].isSource=true;
	}
	this.areas.push(area);
	return area;
};

Model.prototype.init = function() {
	for (var i=0; i<this.areas.length; i++) {
		this.areas[i].init();
	}
};

Model.prototype.update = function() {
	this.resizeAreas();
	this.paint();
};

Model.prototype.paint = function() {
	this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
	this.canvas.style.background = this.theme.background;
	
	for (var i=0; i<this.areas.length; i++) {
		this.areas[i].paint(this.context, this.theme);
	}
	if (this.activeObject !== null) {
		this.canvas.style.cursor = this.activeObject.getCursor(this.mousePosition);
	} else {
		this.canvas.style.cursor = Cursors.arrow;
	}
};

Model.prototype.resizeAreas = function() {
	var x = 0;
	var width = Math.floor(this.canvas.width / this.areas.length);
	var height = this.canvas.height;
	
	for (var i=0; i<this.areas.length;i++) {
		if (i==length-1) {
			width = this.canvas.width-x; // Fill up remaining horizontal space
		}		
		this.areas[i].setRectangle(new Rectangle(x, 0, width, height));
		x = x + width;
	}
};

Model.prototype.select = function(object) {
	if (object!=null && !object.selected) {
		this.selectedItems = [];
		this.deselectAll();
		object.selected=true;
		if (!this.selectedItems.contains(object)) {
			this.selectedItems.push(object)
		}
		this.handleElementSelected(object);
		this.paint();
	}
};

Model.prototype.deselectAll = function() {
	for (var i=0; i<this.areas.length;i++) {
		this.areas[i].deselectAll();
	}
};