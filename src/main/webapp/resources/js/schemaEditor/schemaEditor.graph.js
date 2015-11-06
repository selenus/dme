/**   - getContextMenuItems() must return an array of item objects
*   	- {key: ..., label: ..., glyphicon: ..., e: ..., callback: function(itemKey, e)} for items or
*   	- {key: "-"} for a separator
*/
SchemaEditor.prototype.initGraph = function() {
	var _this = this;
	this.graph = new Model(this.context.canvas, 
			[{
				key: "Nonterminal",
				primaryColor: "#e6f1ff", secondaryColor: "#0049a6",
				getContextMenuItems: function(element) { 
					var items = [
						_this.graph.createContextMenuItem("addNonterminal", "~eu.dariah.de.minfba.schereg.button.add_nonterminal", "plus", element),
						_this.graph.createContextMenuItem("addDescription", "~eu.dariah.de.minfba.schereg.button.add_desc_function", "plus", element),
						_this.graph.createContextMenuItem("editElement", "~eu.dariah.de.minfba.common.link.edit", "edit", element),
						_this.graph.createContextMenuSeparator(),
						_this.graph.createContextMenuItem("removeElement", "~eu.dariah.de.minfba.common.link.delete", "trash", element),
					];
					return items; 
				}
			}, {
				key: "Label",
				primaryColor: "#f3e6ff", secondaryColor: "#5700a6"
			}, {
				key: "Function",
				primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 5
			}, {
				key: "Grammar",
				primaryColor: "#FFE173", secondaryColor: "#6d5603", radius: 5
			}]);
	this.area = this.graph.addArea();
		
	this.graph.init();
	
	this.contextMenuClickEventHandler = this.handleContextMenuClicked.bind(this);
	
	document.addEventListener("contextMenuClickEvent", this.contextMenuClickEventHandler, false);
};

SchemaEditor.prototype.handleContextMenuClicked = function(e) {
	this.performTreeAction(e.key, e.id, e.nodeType);
};

SchemaEditor.prototype.updateGraph = function() {
	this.graph.update();
};