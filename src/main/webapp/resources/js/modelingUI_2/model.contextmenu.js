/**
 * 	Functionality for showing a context-sensitive menu for convenient model interaction
 * 	
 * 	 - the active object must implement getContextMenuItems() to enable context menus
 *   - getContextMenuItems() must return an array of item objects
 *   	- {key: ..., label: ..., glyphicon: ..., e: ..., callback: function(itemKey, e)} for items or
 *   	- {key: "-"} for a separator
 *   - clicks on any context menu item will also lead to a "contextMenuClickEvent" that exposes 
 *   	an Event object of the form { itemKey: ...(the presented key), e: ...(the presented e) } 
 */
Model.prototype.initContextMenu = function() {
	var _this = this;
	this.contextMenu = $("#" + this.canvas.id).nuContextMenu({
        items: "#" + _this.canvas.id,
        callback: _this.raiseContextMenuItemClickedEvent,
        menu: function(e) { return _this.handleContextMenu(e); }
    });
};

Model.prototype.handleContextMenu = function(e) {
	this.updateMousePosition(e);
	this.updateActiveObject();
	if (this.activeObject==undefined || this.activeObject==null) {
		return false;
	}
	if (this.activeObject.selected!==undefined && !this.activeObject.selected) {
		this.deselectAll();
		this.select(this.activeObject);
	}
	var items = null;
	if (this.activeObject.getContextMenuItems!==undefined) {
		items = this.activeObject.getContextMenuItems();
	}
	return this.createMenuItems(items);
};

Model.prototype.createMenuItems = function (items) {
	if (items!==undefined && items!==null && items.length>0) { 
		var _this = this;
		var oItems = {};
		var sepCount = 0;
		for (var i=0; i<items.length; i++) {
			if (items[i].key=="-") {
				oItems["sep"+sepCount++] = "---------";
			} else {
				oItems[items[i].key] = {
						title: items[i].label,
						id : items[i].id,
						type : items[i].type,
						glyphicon : items[i].glyphicon,
						icon: function(itemKey, item) {
							return '<span class="glyphicon glyphicon-' + item.glyphicon + '" aria-hidden="true"></span> ';
						}
				}
			}
		}
		return oItems;
	}
};

Model.prototype.createContextMenuItem = function(key, code, glyphicon, elementId, elementType) {
	return {
		key: key, 
		label: __translator.translate(code), 
		glyphicon: glyphicon, 
		id: elementId, 
		type: elementType
	};
};

Model.prototype.createContextMenuSeparator = function() {
	return { key: "-" };
};

Model.prototype.raiseContextMenuItemClickedEvent = function(element, key, id, type) {
	var menuItemClickedEvent = document.createEvent("Event");
	menuItemClickedEvent.initEvent("contextMenuClickEvent", true, true);
	menuItemClickedEvent.key = key;
	menuItemClickedEvent.id = id;
	menuItemClickedEvent.nodeType = type;
	document.dispatchEvent(menuItemClickedEvent);
};
