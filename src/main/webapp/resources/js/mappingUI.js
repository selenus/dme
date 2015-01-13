var mappingUI = null;

$(document).ready(function() {
	mappingUI = new MappingUI();
	mappingUI.initData();
	//mappingUI.resize();
});

$(window).resize(function() {
	mappingUI.resize();
});

var MappingUI = function() {
	this.menuContainer = $("#mapping-dynamic-button-area");
	this.mappingId = $("#mapping_id").attr("value");
	this.pathname = window.location.pathname;
	
	this.context = document.getElementById("canvas").getContext("2d");
	this.graph = new Graph(this.context.canvas);
	this.container = document.getElementById("mapping_canvas_container");
	
	var height;

	if (navigator.appName.indexOf("Microsoft") != -1) {
		height = document.documentElement.clientHeight - 8;
	} else {
		height = window.innerHeight;
	}

	// Adjust the canvas
	if (this.context.canvas) {
		this.context.canvas.width = this.container.offsetWidth - 1;
		this.context.canvas.height = height - 250;
		window.scroll(0, 0);
		$("#mapping_canvas_toolbar").css("width", (canvas.width + 2) + "px");
	}
	
 	this.sourceSchema = new Area(this.graph, new Rectangle(0, 0, Math.floor(this.context.canvas.width / 2), this.context.canvas.height), true);
 	this.targetSchema = new Area(this.graph, new Rectangle(Math.floor(this.context.canvas.width / 2), 0, this.context.canvas.width - Math.floor(this.context.canvas.width / 2), this.context.canvas.height), false);
 	
 	this.graph.addArea(this.sourceSchema);
 	this.graph.addArea(this.targetSchema);
	
	document.addEventListener("selectionEvent", this.selectionHandler, false);
	document.addEventListener("deselectionEvent", this.deselectionHandler, false);
	document.addEventListener("newMappingCellEvent", this.newMappingCellHandler, false);
};

MappingUI.prototype.editSchemaElement = function(id) {
	modalFormHandler = new ModalFormHandler({
		formUrl: "/ajax/getEditSchemaElementForm?id=" + id,
		translationsUrl: "/ajax/getTranslations",
		identifier: "edit-schema-element-" + id
	});
	modalFormHandler.translations = 
		[{placeholder: "~*servererror.head", key: "~crosswalkRegistry.dialogs.new.servererror.head", defaultText: "Problem interacting with server"},
		 {placeholder: "~*servererror.body", key: "~crosswalkRegistry.dialogs.new.servererror.body", defaultText: "Could not interact with server. Please check the internet connectivity of your computer, try again or inform the administrator if this problem pertains."},
		 {placeholder: "~*importsuccessful.head", key: "~crosswalkRegistry.dialogs.new.importsuccessful.head"},
		 {placeholder: "~*importsuccessful.body", key: "~crosswalkRegistry.dialogs.new.importsuccessful.body"},
		 {placeholder: "~*validationerrors.head", key: "~crosswalkRegistry.dialogs.new.validationerrors.head"},
		 {placeholder: "~*validationerrors.body", key: "~crosswalkRegistry.dialogs.new.validationerrors.body"}
		];
	
	modalFormHandler.show("edit-schema-element-" + id);
};

MappingUI.prototype.selectionHandler = function(e) {
	var _this = mappingUI;
	
	if (e.elementType=="MappingConnection") {
		$.ajax({
	        url: _this.pathname + "/ajax/getCells?mapping=" + _this.mappingId + "&input=" + e.input + "&output=" + e.output,
	        type: "GET",
	        dataType: "json",
	        success: function(data) {
	        	// Show the mapping cell menu according to delivered data 
	        	var menu = $(_this.getMappingDropdownMenu(e.elementId, data, e.input, e.output));
	        	$(menu).css("display", "none");        	
	        	$(_this.menuContainer).html(menu);
	        	$(_this.menuContainer).css("width", $(menu).outerWidth(true)+"px");
	        	$(menu).fadeIn();
	        },
	        error: function(textStatus) { 
	        	alert(textStatus);
	        }
		});
	} else if (e.elementType=="Element") {
		var menu = $(_this.getElementDropdownMenu(e.elementId));
    	$(menu).css("display", "none");        	
    	$(_this.menuContainer).html(menu);
    	$(_this.menuContainer).css("width", $(menu).outerWidth(true)+"px");
    	$(menu).fadeIn();
	}
};

MappingUI.prototype.deselectionHandler = function() {
	// Remove any menu
	$(mappingUI.menuContainer).html("");
};

MappingUI.prototype.newMappingCellHandler = function(e) {
	var _this = mappingUI;
	$.ajax({
		url: _this.pathname + '/saveMappingCells',
        type: "POST",
        data: {mappingId: _this.mappingId, sourceCellId: e.input, targetCellId: e.output, score: 1},
        dataType: "text",
        beforeSend: function(x) {
            if (x && x.overrideMimeType) {
              x.overrideMimeType("application/json;charset=UTF-8");
            }
        },
        success: function(data) { $("#save-notice-area").text(data);},
        error: function(textStatus) { modalMessage.showMessage("warning", "Error updating mapping!", "Please refresh."); }
 	});
};

MappingUI.prototype.resize = function() {
	var height;

	if (navigator.appName.indexOf("Microsoft") != -1) {
		height = document.documentElement.clientHeight - 8;
	} else {
		height = window.innerHeight;
	}

	// Adjust the canvas
	if (this.context.canvas) {
		this.context.canvas.width = this.container.offsetWidth - 1;
		this.context.canvas.height = height - 250;
		window.scroll(0, 0);
		$("#mapping_canvas_toolbar").css("width", (canvas.width + 2) + "px");
				
		if (this.graph !== null) {
			if (this.sourceSchema != null) {
				this.sourceSchema.setSize(new Rectangle(0, 0, Math.floor(this.context.canvas.width / 2), this.context.canvas.height));
			}
			if (this.targetSchema != null) {
				this.targetSchema.setSize(new Rectangle(Math.floor(this.context.canvas.width / 2), 0, this.context.canvas.width - Math.floor(this.context.canvas.width / 2), this.context.canvas.height));
			}
			this.graph.update();
		}
	}
};

MappingUI.prototype.initData = function() {
	var _this = this;
	$.ajax({
        url: _this.pathname + '/loadSchemaElements',
        type: "POST",
        data: { mappingId: _this.mappingId },
        dataType: "json",
        beforeSend: function(x) {
            if (x && x.overrideMimeType) {
              x.overrideMimeType("application/json;charset=UTF-8");
            }
        },
        success: function(data) { _this.processElements(data); },
        error: function(textStatus) { /*alert("error");*/ }
 	});
};

MappingUI.prototype.processElements = function(data) {
	var _this = this;
	
	// Generate the schema elements in graph
	jQuery.each(data, function(i, val) {
		var currentArea;
		var top;
		var isSource;
		
		if (val.members["type"]==="source") {
			currentArea = _this.sourceSchema;
			top = 50;
			isSource = true;
		} else {
			currentArea = _this.targetSchema;
			top = _this.context.canvas.width - 250;
			isSource = false;
		}
		
		var parent = currentArea.addRoot(isSource ? sourceTemplate : targetTemplate, { x: top, y: 25 }, val.members["id"], val.members["name"]);
		_this.generateTree(currentArea, parent, val.members["elements"], isSource);
		currentArea.root.setExpanded(true);
	});
	
	this.graph.update();	
	
	// Fetch the mapping cells
	$.ajax({
		url: _this.pathname + '/loadMappingCells',
        type: "POST",
        data: { mappingId: _this.mappingId },
        dataType: "json",
        beforeSend: function(x) {
            if (x && x.overrideMimeType) {
              x.overrideMimeType("application/json;charset=UTF-8");
            }
        },
        success: function(data) { _this.processMappingCells(data); },
        error: function(textStatus) { /*alert("error");*/ }
 	});
};

MappingUI.prototype.processMappingCells = function(data) {
	var _this = this;
	
	jQuery.each(data, function(i, val) {
		var lhs = _this.sourceSchema.getElement(val.members["inputId"]);
		var rhs = _this.targetSchema.getElement(val.members["outputId"]);
		
		if (lhs != null && rhs != null) {			
			_this.graph.addMapping(lhs.getConnector("mappings"), rhs.getConnector("mappings"), val.members["id"], val.members["score"]);
		}		
	});
	
	this.graph.update();
	
	
	$("#mapping-flyer").fadeOut();
};

MappingUI.prototype.generateTree = function(area, parent, data, isSource) {
	var _this = this;
	jQuery.each(data, function(i, val) {
		var e = area.addElement(isSource ? sourceTemplate : targetTemplate, val.id, val.name, parent);
		if (parent != null) {
			parent.addChild(e);
		}
		if (val.children !== null && Object.prototype.toString.call(val.children) === '[object Array]') {
			_this.generateTree(area, e, val.children, isSource);
		}
	});
};


MappingUI.prototype.getMappingDropdownMenu = function(id, data, inputId, outputId) {
	var _this = this;
	
	var menu = "<ul class='nav'><li class='dropdown'>" +
                        "<a data-toggle='dropdown' class='dropdown-toggle' href='#'>Mapping cell options <b class='caret'></b></a>" +
                        "<ul class='dropdown-menu'>" +
                        "<li class='dropdown-submenu'><a tabindex='-1' href='#'><i class='icon-comment icon-black'></i> Comments</a>" +
	                    	"<ul class='dropdown-menu'>"; 
	
	// Include delete option for the displayed mapping cells
	jQuery.each(data, function(i, val) {
		menu += "<li><a href='#' onclick='annotationViewer.getByAnnotatedObject(\""+ val.members["type"] +"\", \"" + val.members["id"] + "\"); return false;'><i class='icon-edit icon-black'></i> " + val.members["name"] + "</a></li>";
	});
	
	menu +=               	"</ul></li>" +
                          "<li class='nav-header'>Transformation&nbsp;Rules</li>" + 
                          "<li class='dropdown-submenu'><a tabindex='-1' href='#'><i class='icon-remove icon-black'></i> Delete..</a>" +
                        	"<ul class='dropdown-menu'>"; 
	
	// Include delete option for the displayed mapping cells
	jQuery.each(data, function(i, val) {
		menu += "<li><a href='#' onclick='mappingUI.deleteMappingCell("+ _this.mappingId +", " + val.members["id"] + "); return false;'><i class='icon-edit icon-black'></i> " + val.members["name"] + "</a></li>";
	});
	
	menu +=               	"</ul></li>" +
                          "<li class='dropdown-submenu'><a tabindex='-1' href='#'><i class='icon-wrench icon-black'></i> Edit existing..</a>" +
                          	"<ul class='dropdown-menu'>"; 
	
	// Include edit option for the displayed mapping cells
	jQuery.each(data, function(i, val) {
		menu += "<li><a href='#' onclick='mappingUI.getMappingCellEditDialog("+ _this.mappingId +", " +  val.members["id"] + ", " + inputId + ", " + outputId + "); return false;'><i class='icon-edit icon-black'></i> " + val.members["name"] + "</a></li>";
	});
	
	menu +=               	"</ul></li>" +
                          "<li><a href='#' onclick='mappingUI.getMappingCellEditDialog("+ _this.mappingId +", -1, " + inputId + ", " + outputId + "); return false;'><i class='icon-plus icon-black'></i> Create new</a></li>" +
                          "</li>" +
                        "</ul>" +
                      "</li></ul>";
	return menu;
};

MappingUI.prototype.getElementDropdownMenu = function(id) {	
	return "<ul class='nav'><li class='dropdown'>" +
                        "<a data-toggle='dropdown' class='dropdown-toggle' href='#'>Element options <b class='caret'></b></a>" +
                        "<ul class='dropdown-menu'>" +
                          "<li><a href='#' onclick='annotationViewer.getByAnnotatedObject(\"de.dariah.schereg.base.model.ReadOnlySchemaElement\", " + id + ")'><i class='icon-comment icon-black'></i> Comments</a></li>" +
                          "<li><a href='#' onclick='mappingUI.editSchemaElement(" + id + ")'><i class='icon-wrench icon-black'></i> Configure...</a></li>" +
                        "</ul>" +
                      "</li></ul>";
};

MappingUI.prototype.deleteMappingCell = function(mappingId, cellId) {
	var _this = this;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/ajax/deleteForm?mappingId=" + mappingId + "&id=" + cellId,
		translationsUrl: "/ajax/getTranslations",
		identifier: "mapping-cell-delete-" + mappingId + "-" + cellId,
		additionalModalClasses: "narrow-modal",
		/*setupCallback: this.setScoreSlider,*/
		completeCallback: function(data) {
			if (data.success==true) { 
        		// If delete succeeded remove displayed mapping without refreshing the whole graph
        		_this.graph.removeMappingConnection(cellId);
        		_this.graph.update();
        		
        		//modalMessage.showMessage(data.message_type, data.message_head, data.message_body);
        	} else {
        		//modalMessage.showMessage(data.message_type, data.message_head, data.message_body);
        	}
		}
	});
	
	modalFormHandler.show("mapping-cell-delete-" + mappingId + "-" + cellId);
};

MappingUI.prototype.getMappingCellEditDialog = function(mappingId, cellId, inputId, outputId) {

	modalFormHandler = new ModalFormHandler({
		formUrl: "/ajax/detailsForm?mappingId=" + mappingId + "&id=" + cellId + "&inputId=" + inputId  + "&outputId=" + outputId,
		translationsUrl: "/ajax/getTranslations",
		identifier: "mapping-cell-details-" + mappingId + "-" + cellId,
		additionalModalClasses: "wide-modal",
		setupCallback: this.setScoreSlider,
		completeCallback: this.askForComment
	});
	
	modalFormHandler.show("mapping-cell-details-" + mappingId + "-" + cellId);
};

MappingUI.prototype.askForComment = function(data) {
	var _this = mappingUI;
	
	_this.updateGraph(data);
	
	if (data.userActionLogEntry!==undefined) {
		modalFormHandler = new ModalFormHandler({
			formUrl: "/ajax/getCommentForm?actionId=" + data.userActionLogEntry,
			translationsUrl: "/ajax/getTranslations",
			identifier: "mapping-cell-comment-for" + data.userActionLogEntry,
			additionalModalClasses: "narrow-modal",
			/*setupCallback: this.setScoreSlider,*/
			/*completeCallback: function(data) {
				if (data.success==true) { 
	        		// If delete succeeded remove displayed mapping without refreshing the whole graph
	        		_this.graph.removeMappingConnection(cellId);
	        		_this.graph.update();
	        		
	        		//modalMessage.showMessage(data.message_type, data.message_head, data.message_body);
	        	} else {
	        		//modalMessage.showMessage(data.message_type, data.message_head, data.message_body);
	        	}
			}*/
		});
		
		modalFormHandler.show("mapping-cell-comment-for" + data.userActionLogEntry);
	}
};

MappingUI.prototype.updateGraph = function(data) {
	
	var removeCells = [];
	var _this = mappingUI;
	
	jQuery.each(data.updatedMappingCells, function(i, val) {
		if (!removeCells.contains(val.id)) {
			removeCells.push(val.id);
		}
	});
	
	for (var i=0; i<removeCells.length; i++) {
		_this.graph.removeMappingConnection(removeCells[i]);
	}
	
	jQuery.each(data.updatedMappingCells, function(i, val) {
		
		var lhs = _this.sourceSchema.getElement(val.inputId);
		var rhs = _this.targetSchema.getElement(val.outputId);
		
		if (lhs != null && rhs != null) {			
			_this.graph.addMapping(lhs.getConnector("mappings"), rhs.getConnector("mappings"), val.id, val.score);
		}		
	});
	_this.graph.update();
};

MappingUI.prototype.setScoreSlider = function() {
	$(modalFormHandler.form).find("#score-slider").slider({
		range: false,
        min: 0,
        max: 100,
        values: [ $(modalFormHandler.form).find("#mappingCell_score").first().attr("value")*100 ],
        slide: function( event, ui ) {  
        	$(modalFormHandler.form).find("#mappingCell_score").first().val( ui.values[ 0 ] / 100 );
        	$(modalFormHandler.form).find("#mappingCell_score_readable").text( ui.values[ 0 ] + "%");
    		
        	var red = 255 - Math.round(ui.values[ 0 ] / 100 * 255);
        	var color = "Rgb(" + red + ", 230, 0)";
        	
        	$(modalFormHandler.form).find("#score-slider").css("background-color", color);
        }
	});
	
	
	$(modalFormHandler.form).find("#mappingCell_score_readable").text(
		$(modalFormHandler.form).find("#mappingCell_score").first().attr("value")*100 + "%"
	);
	
	
	var red = 255 - Math.round($(modalFormHandler.form).find("#mappingCell_score").first().attr("value") * 255);
	var color = "Rgb(" + red + ", 230, 0)";
	$(modalFormHandler.form).find("#score-slider").css("background-color", color);
	
	
	$(modalFormHandler.form).find("#mappingCell_score").change(function() {
		 var value = this.value.substring(1);
		 $(modalFormHandler.form).find("#score-slider").slider("value", parseInt(value)*100);
	 });
};