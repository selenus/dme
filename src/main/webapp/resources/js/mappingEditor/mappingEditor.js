var mappingEditor;
$(document).ready(function() {
	mappingEditor = new MappingEditor();
	mappingEditor.initLayout();
	mappingEditor.initGraphs();
});
$(window).resize(function() {
	mappingEditor.resizeLayout();
	mappingEditor.resizeContent();
});

var MappingEditor = function() {
	var _this = this;
	
	this.mappingId = $("#mapping-id").val();
	this.sourceId = $("#source-id").val();
	this.targetId = $("#target-id").val();
	this.mappingOwn = $("#mapping-own").val()==="true";
	this.mappingWrite = $("#mapping-write").val()==="true";
	
	this.mappingPath = __util.getBaseUrl() + "mapping/editor/" + this.mappingId + "/";
	this.sourcePath = __util.getBaseUrl() + "schema/editor/" + this.sourceId + "/";
	this.targetPath = __util.getBaseUrl() + "schema/editor/" + this.targetId + "/";
	
	this.layoutContainer = $("#mapping-editor-layout-container");
	this.editorContainer = $("#mapping-editor-container");
	this.context = document.getElementById("mapping-editor-canvas").getContext("2d");
	this.layout = null;
	
	this.footerOffset = 70;
};

MappingEditor.prototype.initLayout = function() {
	var _this = this;
	this.layoutContainer.addClass("fade");
	this.layoutContainer.removeClass("hide");
	
	var initEastClosed = true;
	var initWestClosed = true;
	if (this.layoutContainer.width()>800) {
		initEastClosed = false;
	}
	if (this.layoutContainer.width()>1100) {
		initWestClosed = false;
	}
	
	this.layout = this.layoutContainer.layout({
		defaults : {
			fxName : "slide",
			fxSpeed : "slow",
			spacing_closed : 14,
			minWidth : 200,
		}, 
		east : {
			size : "25%",
			minWidth : 200,
			minHeight : 400,
			initClosed : initEastClosed,
			paneSelector : ".outer-east"
		},
		center : {
			size : "50%",
			paneSelector : ".outer-center",
			minHeight : 200
		}, 
		south : {
			size : 100,
			paneSelector : ".outer-south"
		},
		west : {
			size : "25%",
			minWidth : 200,
			minHeight : 400,
			initClosed : initWestClosed,
			paneSelector : ".outer-west"
		},
		onresize: function () {
			_this.resizeContent();
	        return false;
	    }
	});
	this.layoutContainer.removeClass("fade");
};

MappingEditor.prototype.initGraphs = function() {
	
	var _this = this;
	$.ajax({
	    url: this.sourcePath + "async/getRendered",
	    type: "GET",
	    //dataType: "json",
	    success: function(data) {
	    	if (data===null || data===undefined || data.length==0) {
	    		return;
	    	}
	    	
	    	//_this.processElementHierarchy(data);
	    	//_this.graph.update();
	    }/*,
	    error: __util.processServerError*/
	});
	
	
	this.resizeLayout();
 	this.resizeContent();
};
 	
MappingEditor.prototype.resizeLayout = function() {
	var height = Math.floor($(window).height() - this.layoutContainer.offset().top - this.footerOffset);
	
	this.layoutContainer.height(height);
	this.layout.resizeAll();
}

MappingEditor.prototype.resizeContent = function() {};