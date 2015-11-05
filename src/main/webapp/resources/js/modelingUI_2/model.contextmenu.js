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
	// We need special treatment of layer clicks for using this on a canvas
	$.contextMenu.handle.layerClick = function(e) { _this.contextLayerClick(e, this) };	
};

Model.prototype.handleContextMenu = function(e) {
	this.updateMousePosition(e);
	this.updateActiveObject();
	if (this.activeObject==undefined || this.activeObject==null) {
		e.preventDefault()
		return false;
	}
	if (this.activeObject.selected!==undefined) {
		this.select(this.activeObject);
	}

	console.log (this.activeObject);
	
	var items = null;
	if (this.activeObject.getContextMenuItems!==undefined) {
		items = this.activeObject.getContextMenuItems();
	}
	
	var menuItems = this.createMenuItems(items);
	if (this.createContextMenu(menuItems)) {
		// prevents the usual context from popping up
		e.preventDefault()
		return false;
	}
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
						name: items[i].label,
						element : items[i].e,
						glyphicon : items[i].glyphicon,
						clickCallback : items[i].callback,
						icon: function(itemElement, itemKey, item) {
							$(itemElement).html('<span class="glyphicon glyphicon-' + item.glyphicon + '" aria-hidden="true"></span> ' + item.name);
							return "context-menu-icon-glyphicon";
						},
						callback: function(itemKey, opt) {
							var item = opt.items[itemKey];
							console.log(item.element);
							_this.raiseContextMenuItemClickedEvent(itemKey, item.element);
							if (item.clickCallback!==undefined) {
								item.clickCallback(item.element);
							}
						}
				}
			}
		}
		return oItems;
	}
};



Model.prototype.createContextMenu = function (items) {
	if (items!==undefined && items!==null) {
		if (this.canvas.id===undefined || this.canvas.id===null) {
			throw new Error('Canvas has no id');
		}
		
		var ctx = $.contextMenu({
			selector: "#" + this.canvas.id,
			autoHide: true,
            zIndex: 100,
			items: function() { return items; } 
		});
		
		
		
		/*$.contextMenu({
	        selector: "#" + this.canvas.id, 
	        build: function($trigger, e) {
	            return {
	            	autoHide: true,
	                zIndex: 100,
	                items: items
	            };
	        }
	    });*/
		
		return true;
	}
	return false;
}

Model.prototype.contextLayerClick = function (e, contextmenu) {
	
	var root = $(contextmenu).data('contextMenuRoot');

	// Recreate another context menu
	if ((root.trigger === 'left' && e.button === 0) || (root.trigger === 'right' && e.button === 2)) {
		var _this = this;
		root.$trigger.one('contextmenu:hidden', function () {
			_this.updateMousePosition(e);
			_this.updateActiveObject();
			_this.leftMouseDown();			
			$(this).contextMenu({x: e.pageX, y: e.pageY});
	    });
	}
	root.$menu.trigger('contextmenu:hide');
	//$.contextMenu("destroy");
}

Model.prototype.raiseContextMenuItemClickedEvent = function(itemKey, e) {
	var menuItemClickedEvent = document.createEvent("Event");
	menuItemClickedEvent.initEvent("contextMenuClickEvent", true, true);
	menuItemClickedEvent.itemKey = itemKey;
	menuItemClickedEvent.e = e;
	document.dispatchEvent(menuItemClickedEvent);
};
