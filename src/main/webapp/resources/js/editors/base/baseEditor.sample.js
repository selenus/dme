BaseEditor.prototype.initSample = function(samplePath, sampleEntityId) {
	var _this = this;
	
	this.samplePath = samplePath;
	this.sampleEntityId = sampleEntityId;
	this.sampleTextbox = $(".sample-textarea");  //schema-editor-outer-east-container, schema-editor-detail-pane
	this.samplePane = $(".editor-sample-pane");
	this.sampleContainer = $(".editor-sample-container");
	this.sampleOutputContainer = $("#sample-output-container");
	
	this.sampleModified = !$("#sample-set").val();
	
	$(this.sampleTextbox).on('change keyup paste', function() {
		_this.sampleModified = true;
	});
		
	if (this.getSampleResourceCount()>0) {
		this.getSampleResource();
	}
	
	$("#sample-input-textarea").focusout(function() {_this.handleLeaveTextarea();});
	
	$('.editor-sample-container a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
		_this.resize();
	})
};

BaseEditor.prototype.handleEnterTextarea = function() {
	
	$("#sample-input-textarea").removeClass("hide");
	$("#sample-input-textarea-placeholder").addClass("hide");
	
	$("#sample-input-textarea").focus();
	
	this.resizeContent();
};

BaseEditor.prototype.handleLeaveTextarea = function() {
	if ($("#sample-input-textarea").val().length==0) {
		$("#sample-input-textarea").addClass("hide");
		$("#sample-input-textarea-placeholder").removeClass("hide");
		
		this.resizeContent();
	}
};

BaseEditor.prototype.applyAndExecuteSample = function() {
	var _this = this;
	this.samplePane.children("div:not(.ui-pane-title)").hide();
	
	if (this.sampleModified) {
		this.applySample(function() {
			_this.executeSample();
		})
	} else {
		this.executeSample();
	}
};

BaseEditor.prototype.uploadAndExecuteSample = function() {
	var _this = this;
	var form_identifier = "edit-root";

	modalFormHandler = new ModalFormHandler({
		formUrl: "forms/uploadSample/",
		additionalModalClasses: "wide-modal",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"},
		                {placeholder: "~*file.uploadcomplete.head", key: "~eu.dariah.de.minfba.schereg.editor.forms.sample_uploaded.head"},
		                {placeholder: "~*file.uploadcomplete.body", key: "~eu.dariah.de.minfba.schereg.editor.forms.sample_uploaded.body"}	
		                ],
		completeCallback: function() { 
			_this.logArea.refresh();
    		_this.sampleModified = false;
			_this.samplePane.children("div:not(.ui-pane-title)").hide();
			_this.executeSample(); 
		}
	});
	
	modalFormHandler.fileUploadElements.push({
		selector: "#upload_source",				// selector for identifying where to put widget
		formSource: "forms/fileupload",		// where is the form
		uploadTarget: "async/uploadSample", 			// where to we upload the file(s) to
		multiFiles: false, 						// one or multiple files
		elementChangeCallback: _this.handleFileValidatedOrFailed
	});	
		
	modalFormHandler.show(form_identifier);
};

BaseEditor.prototype.handleSampleUploaded = function(data) {
	var select = $("#schema_root");
	select.html("");
	if (data==null || data.pojo==null || data.pojo.length==0) {
		select.prop("disabled", "disabled");
		$("#btn-submit-schema-elements").prop("disabled", "disabled");
		return;
	}
	
	var option;
	for (var i=0; i<data.pojo.length; i++) {
		option = "<option value='" + i + "'>" + data.pojo[i].name + " <small>(" + data.pojo[i].namespace + ")</small>" + "</option>";
		select.append(option);
	}
	select.removeProp("disabled");
	$("#btn-submit-schema-elements").removeProp("disabled");
};

BaseEditor.prototype.applySample = function(callback) {
	var _this = this;
	$.ajax({
	    url: this.samplePath + "async/applySample",
	    type: "POST",
	    data: { sample : _this.sampleTextbox.val() },
	    dataType: "json",
	    success: function(data) {
	    	if (data.success) { 
	    		_this.logArea.refresh();
	    		_this.sampleModified = false;
	    		callback();
	    	}
	    }, 
	    error: function(jqXHR, textStatus, errorThrown ) {
	    	$(_this.samplePane).children("div:not(.ui-pane-title)").show();
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    }
	});
};

BaseEditor.prototype.loadSampleInput = function() {
	var _this = this;
	this.samplePane.children("div:not(.ui-pane-title)").hide();
	
	$.ajax({
	    url: _this.pathname + "/async/load_sample",
	    type: "GET",
	    dataType: "json",
	    data: { t: "input" },
	    success: function(data) {
    		
	    	$('#sample-input-textarea').val(data.pojo) , '#load-sample-input-buttonbar'
	    	$('#load-sample-input-buttonbar').addClass("hide");
    		
    		_this.samplePane.children("div:not(.ui-pane-title)").show();
    		_this.resize();
	    },
	    error: __util.processServerError
	});
};

BaseEditor.prototype.downloadSample = function(type) {
	var _this = this;
	if (type==="output" || type==="transformed") {		
	    bootbox.confirm({
	        title: __translator.translate("~eu.dariah.de.minfba.schereg.editor.sample.download.set_or_resource.head"),
	    	message: __translator.translate("~eu.dariah.de.minfba.schereg.editor.sample.download.set_or_resource.body"),
	        buttons: {
	            confirm: {
	                label: __translator.translate("~eu.dariah.de.minfba.schereg.editor.sample.download.resource"),
	            },
	            cancel: {
	                label: __translator.translate("~eu.dariah.de.minfba.schereg.editor.sample.download.set"),
	            }
	        },
	        callback: function (result) {
	            if (result===true) {
	            	_this.downloadSampleFile(type, _this.getCurrentSampleIndex()); 
	            } else {
	            	_this.downloadSampleFile(type);
	            }
	        }
	    });
	} else {
		this.downloadSampleFile(type);
	}
};

BaseEditor.prototype.downloadSampleFile = function(type, index) {
	var _this = this;
	$.ajax({
	    url: _this.pathname + "/async/download_sample",
	    type: "GET",
	    dataType: "json",
	    data: { t: type, i: (index===null||index===undefined ? -1 : index) },
	    success: function(data) {
	    	if (data.content===null || data.content.length==0) {
	    		bootbox.alert(__translator.translate("~eu.dariah.de.minfba.schereg.editor.sample.notice.empty_sample"));
	    	} else {
		    	if (data.extension==="json") {
		    		data.content = JSON.stringify(data.content);
		    	}
		    	blob = new Blob([data.content], {type: data.mime});
		    	saveAs(blob, "sample_" + _this.schema.id + "." + data.extension);
	    	}
	    },
	    error: __util.processServerError
	});
};

BaseEditor.prototype.executeSample = function() {
	var _this = this;
	$.ajax({
	    url: this.samplePath + "async/executeSample",
	    type: "GET",
	    //data: { sample : _this.sampleTextbox.val() },
	    dataType: "json",
	    async: false,
	    success: function(data) {
	    	if (data.success==true && data.pojo>0 && _this.mappingId!==undefined && _this instanceof MappingEditor) {
	    		_this.executeSampleMapping();
	    	}
	    	if (data.success) {
	    		_this.processSampleExecutionResult(data.pojo);
	    	}
	    	//_this.currentSampleIndex = 0;
	    	_this.logArea.refresh();
	    	
	    	$(_this.samplePane).children("div:not(.ui-pane-title)").show();
	    	_this.resize();
	    }, 
	    error: function(jqXHR, textStatus, errorThrown ) {
	    	$(_this.samplePane).children("div:not(.ui-pane-title)").show();
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    }
	});
};

BaseEditor.prototype.setCurrentSampleIndex = function(index) {
	$("#currentSampleIndex").val(index);
};

BaseEditor.prototype.getCurrentSampleIndex = function() {
	return parseInt(Number($("#currentSampleIndex").val()));
};

BaseEditor.prototype.setSampleResourceCount = function(count) {
	$("#currentSampleCount").val(count);
};

BaseEditor.prototype.getSampleResourceCount = function() {
	return parseInt(Number($("#currentSampleCount").val()));
};


BaseEditor.prototype.executeSampleMapping = function() {
	var _this = this;
	$.ajax({
	    url: this.samplePath + "async/executeSampleMapping",
	    type: "GET",
	    //data: { sample : _this.sampleTextbox.val() },
	    dataType: "json",
	    async: false,
	    success: function(data) {
	    	
	    }, 
	    error: function(jqXHR, textStatus, errorThrown ) {
	    	$(_this.samplePane).children("div:not(.ui-pane-title)").show();
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    }
	});
};

BaseEditor.prototype.executeMapping = function() {
	this.isMappingEditor = true;
};

BaseEditor.prototype.processSampleExecutionResult = function(count) {
	var navTab = $(this.sampleContainer).find("a[href='#sample-output-container']");
	var enabled = false;
	
	$(navTab).find(".badge-c").remove();
	$(".sample-output-resource").text("");
		
	var resCount = 0;
	if (!isNaN(count) && parseInt(Number(count))==count && !isNaN(parseInt(count, 10))) {
		resCount = count;
	}
	if (this.getSampleResourceCount()>resCount) {
		this.setCurrentSampleIndex(0);
	}
	this.setSampleResourceCount(resCount);
	
	if (this.getSampleResourceCount() > 0) {
		enabled = true;
	}
	$(navTab).append("<span class=\"badge-c\"> <span class=\"badge\">" + this.getSampleResourceCount() + "</span></span>");
		
	if (enabled) {
		$(navTab).parent().removeClass("disabled");
		$(navTab).attr("data-toggle", "tab");
		$(navTab).tab('show');
	} else {
		$(navTab).parent().addClass("disabled");
		$(navTab).removeAttr("data-toggle");
		$(this.sampleContainer).find("a[href='#sample-input-container']").tab('show');
	}
	this.getSampleResource();
};

BaseEditor.prototype.getSampleResource = function(force) {
	var _this = this;
	this.samplePane.children("div:not(.ui-pane-title)").hide();
	
	$.ajax({
	    url: this.samplePath + "async/getSampleResource",
	    type: "GET",
	    data: { 
	    	index: _this.getCurrentSampleIndex(),
	    	force: (force===true)
	    },
	    dataType: "json",
	    success: function(data) {
	    	if (data!=null && data!=undefined) {
	    		$(".sample-output-resource").html(_this.buildSampleResourceObject(data));
	    	} else {
	    		$(".sample-output-resource").text("");
	    	}
	    	_this.setSampleNavigationBar();
	    	if (_this.mappingId!==undefined && _this instanceof MappingEditor) {
	    		_this.getTransformedResource();
	    	} else {
	    		_this.samplePane.children("div:not(.ui-pane-title)").show();
	    	}
	    },
	    error: function() {
	    	$(".sample-output-resource").text("");
	    	$(".sample-transformed-resource").text("");
	    }
	});
};

BaseEditor.prototype.getTransformedResource = function() {
	var _this = this;
	$.ajax({
	    url: this.samplePath + "async/getTransformedResource",
	    type: "GET",
	    data: { index : _this.getCurrentSampleIndex() },
	    dataType: "json",
	    success: function(data) {
	    	if (data!=null && data!=undefined) {
		    	$(".sample-transformed-resource").html(_this.buildSampleResourceObject(data));
	    	} else {
	    		$(".sample-transformed-resource").text("");
	    	}
	    	_this.samplePane.children("div:not(.ui-pane-title)").show();
	    	_this.setSampleNavigationBar();
	    },
	    error: function() {
	    	$(".sample-transformed-resource").text("");
	    }
	});
};


BaseEditor.prototype.setSampleNavigationBar = function() {
	$(".sample-output-counter").text("" + (this.getCurrentSampleIndex()+1) + " / " + this.getSampleResourceCount());
	
	if (this.getCurrentSampleIndex() > 0) {
		$(".btn-sample-prev-resource").removeClass("disabled");
	} else {
		$(".btn-sample-prev-resource").addClass("disabled");
	}
	if (this.getCurrentSampleIndex() < this.getSampleResourceCount()-1) {
		$(".btn-sample-next-resource").removeClass("disabled");
	} else {
		$(".btn-sample-next-resource").addClass("disabled");
	}	
};

BaseEditor.prototype.getPrevSampleResource = function() {
	var index = this.getCurrentSampleIndex();
	if (index > 0) {
		this.setCurrentSampleIndex(index-1);
		this.getSampleResource();
	}
};

BaseEditor.prototype.getNextSampleResource = function() {
	var index = this.getCurrentSampleIndex();
	if (index < this.getSampleResourceCount()-1) {
		this.setCurrentSampleIndex(index+1);
		this.getSampleResource();
	}
};

BaseEditor.prototype.showSampleResourceTarget = function() {
	$(".sample-output-resource").addClass("hide");
	$(".sample-transformed-resource").removeClass("hide");
	
	$(".btn-sample-source").addClass("btn-default");
	$(".btn-sample-source").removeClass("btn-info");
	$(".btn-sample-target").addClass("btn-info");
	$(".btn-sample-target").removeClass("btn-default");
}

BaseEditor.prototype.showSampleResourceSource = function() {
	$(".sample-output-resource").removeClass("hide");
	$(".sample-transformed-resource").addClass("hide");
	
	$(".btn-sample-source").addClass("btn-info");
	$(".btn-sample-source").removeClass("btn-default");
	$(".btn-sample-target").addClass("btn-default");
	$(".btn-sample-target").removeClass("btn-info");
}

BaseEditor.prototype.buildSampleResourceObject = function(data) {
	if (data.statusInfo.available===true) {
		if (data.statusInfo.oversize===false) {
			
			$(".btn-load-sample-output").addClass("hide");
			
			var result = $("<ul>");
    		result.append(this.buildSampleResource(data.pojo));
			
			return result;
		} else {
			$(".btn-load-sample-output").removeClass("hide");
			var result = $("<p>");
    		result.html("Large output resource. Please <a href=\"#\" onclick=\"editor.getSampleResource(true); return false;\"><span class=\"glyphicon glyphicon-download\" aria-hidden=\"true\"></span> load here</a> " +
    				"explicitly if needed or <a href=\"#\" onclick=\"editor.downloadSample('output', true); return false;\"><span class=\"glyphicon glyphicon-download\" aria-hidden=\"true\"></span> download as file</a>.");
			return result;
		}
	}
};

BaseEditor.prototype.buildSampleResource = function(resource, parentItem) {
	var items = [];

	for (var i=0; i<Object.getOwnPropertyNames(resource).length; i++) {
		var key = Object.getOwnPropertyNames(resource)[i];
		if (key==="#") {
			continue;
		}
		
		if (key==="~") {
			parentItem.append("<button onclick=\"editor.toggleSampleOutputValue(this);\" class=\"btn btn-link btn-xs sample-output-value-expanded\">" +
								"<i class=\"fa fa-minus-square-o\" aria-hidden=\"true\"></i>" +
								"<i class=\"fa fa-plus-square\" aria-hidden=\"true\"></i>" +
							  "</button>");
			parentItem.append("<span class=\"sample-output-value\">" + resource[key] + "</span>");
			parentItem.append("<span class=\"sample-output-value-placeholder\" style=\"display: none;\">...</span>");
			continue;
		}
		
		var value = resource[key];
		if (Array.isArray(value)) {
			for (var j=0; j<value.length; j++) {
				items.push(this.buildSampleResourceItem(key, value[j]));
			}
		} else {
			items.push(this.buildSampleResourceItem(key, value));
		}
	
	}
	return items;
};

BaseEditor.prototype.toggleSampleOutputValue = function(button) {
	if ($(button).hasClass("sample-output-value-expanded")) {
		$(button).addClass("sample-output-value-collapsed");
		$(button).removeClass("sample-output-value-expanded");
		
		$(button).siblings(".sample-output-value").hide();
		$(button).siblings(".sample-output-value-placeholder").show();
	} else {
		$(button).addClass("sample-output-value-expanded");
		$(button).removeClass("sample-output-value-collapsed");
		
		$(button).siblings(".sample-output-value").show();
		$(button).siblings(".sample-output-value-placeholder").hide();
	}
	
	
};

BaseEditor.prototype.buildSampleResourceItem = function(key, resource) {
	
	var item = $("<li>");
	item.append("&#8594; <span class=\"sample-output-key\">" + key + "</span>");
	
	var subItems = this.buildSampleResource(resource, item);
	if (subItems.length > 0) {
		item.append($("<ul>").append(subItems));
	}
	
	return item;
};


BaseEditor.prototype.buildSampleResourceValue = function(resource, parentItem, subItems) {
	var key = Object.getOwnPropertyNames(resource)[0];
	if (key==="") {
		parentItem.append(": <span class=\"sample-output-value\">" + resource[key] + "</span>");
		return 0;
	} else {
		subItems.append(this.buildSampleResource(resource));
		return 1;
	}
};

BaseEditor.prototype.resetSampleSession = function() {
	var _this = this;
	sessions.resetSession(_this.sampleEntityId, function() {
		window.location.reload();
	});
};