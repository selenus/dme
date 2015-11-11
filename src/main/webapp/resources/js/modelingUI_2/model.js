var Model = function(canvas, options, theme) {
	this.canvas = canvas;
	this.canvas.focus();
	this.context = this.canvas.getContext("2d");
	
	this.options = $.extend(true, {
		elementTemplateOptions : null,
		mappingTemplateOptions : null
	}, options);
	
	//elementTemplates
	
	this.isMouseDown = false;
	this.mousePosition = new Point(-1, -1);
	this.activeObject = null;
	this.selectedItems = [];
	
	this.theme = ModelingTheme;
	if (theme!=undefined && theme!=null) {
		this.theme = $.extend({}, ModelingTheme, theme);
	}
	
	this.areas = [];
	
	this.mappings = [];
	this.newConnection = null;
	
	// Only options defined here, actual element templates are with the areas
	/**
	 * key: "...", primaryColor: "#...", secondaryColor: "#...", radius: 5 (optional)
	 * getContextMenuItems: function(element) {} to produce items[] based on actual element (see model.contextmenu.js) 
	 */
	
	this.mappingConnection = new MappingTemplate(this, this.options.mappingTemplateOption);
	this.hierarchicalConnection = new HierarchyTemplate(this);
	
	this.isWebKit = typeof navigator.userAgent.split("WebKit/")[1] !== "undefined";
	this.isMozilla = navigator.appVersion.indexOf('Gecko/') >= 0 || ((navigator.userAgent.indexOf("Gecko") >= 0) && !this.isWebKit && (typeof navigator.appVersion !== "undefined"));
	this.initEvents();
}

Model.prototype.clearMappings = function() {
	this.mappings = [];
};

Model.prototype.selectMappingsByIds = function(ids) {
	for (var i=0; i<this.mappings.length; i++) {
		if (ids.contains(this.mappings[i].id)) {
			this.select(this.mappings[i]);
		}
	}
};


Model.prototype.dispose = function() {
	if (this.canvas !== null) {
		this.removeEvents();
		this.canvas = null;
		this.context = null;
	}
};

Model.prototype.addArea = function(areaOptions) {
	var area = new Area(this, areaOptions);
	if (this.areas.length>0) {
		area.isTarget = true;
		this.areas[this.areas.length-1].isSource=true;
	}
	this.areas.push(area);
	return area;
};

Model.prototype.init = function() {
	for (var i=0; i<this.areas.length; i++) {
		this.areas[i].index = i;
		this.areas[i].init();
	}
};

Model.prototype.update = function() {
	this.resizeAreas();
	this.paint();
};

Model.prototype.getVisibleMappings = function() {
	var visibleMappings = []
	for (var i=0; i<this.mappings.length; i++) {
		/*if (!this.mappings[i].from.element.isVisible()) {
			continue;
		}*/
		visibleMappings.push(this.mappings[i]);
	}
	return visibleMappings;
};

Model.prototype.paint = function() {
	this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
	this.canvas.style.background = this.theme.background;
	
	// Area background, border, scrollbar
	for (var i=0; i<this.areas.length; i++) {
		this.areas[i].paint(this.context, this.theme);	
		// To have the connections instantly at the right place;
		this.areas[i].recalculateAll();
	}
	
	// All visible mappings (below the elements)
	var visibleMappings = this.getVisibleMappings();
	for (var i=0; i<visibleMappings.length; i++) {
		visibleMappings[i].paint(this.context, this.theme);
	}
	
	// Elements in the areas
	for (var i=0; i<this.areas.length; i++) {
		this.areas[i].paintElements(this.context, this.theme);
	}
	
	if (this.activeObject !== null) {
		this.canvas.style.cursor = this.activeObject.getCursor(this.mousePosition);
	} else {
		this.canvas.style.cursor = Cursors.arrow;
	}
	
	if (this.newConnection !== null) {
		this.newConnection.paint(this.context);
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
		this.areas[i].recalculateAll();
		x = x + width;
	}
};

Model.prototype.select = function(object) {
	if (object!=null && !object.isSelected()) {
		object.setSelected(true);
		if (!this.selectedItems.contains(object)) {
			this.selectedItems.push(object)
		}
		this.handleElementSelected(object);
		this.paint();
	}
};

Model.prototype.deselectAll = function() {
	this.selectedItems = [];
	for (var i=0; i<this.areas.length;i++) {
		this.areas[i].deselectAll();
	}
	for (var i=0; i<this.mappings.length;i++) {
		this.mappings[i].setSelected(false);
	}
};