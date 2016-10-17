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
	    }, 
	    error: function(jqXHR, textStatus, errorThrown ) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
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
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
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

BaseEditor.prototype.getSampleResource = function() {
	var _this = this;
	$.ajax({
	    url: this.samplePath + "async/getSampleResource",
	    type: "GET",
	    data: { index : _this.getCurrentSampleIndex() },
	    dataType: "json",
	    success: function(data) {
	    	if (data!=null && data!=undefined) {
	    		var result = $("<ul>");
	    		result.append(_this.buildSampleResource(data));
	    		$(".sample-output-resource").html(result);
	    	} else {
	    		$(".sample-output-resource").text("");
	    	}
	    	_this.setSampleNavigationBar();
	    	if (_this.mappingId!==undefined && _this instanceof MappingEditor) {
	    		_this.getTransformedResource();
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
	    		var result = $("<ul>");
		    	result.append(_this.buildSampleResource(data));
		    	$(".sample-transformed-resource").html(result);
	    	} else {
	    		$(".sample-transformed-resource").text("");
	    	}
	    	
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
}

BaseEditor.prototype.showSampleResourceSource = function() {
	$(".sample-output-resource").removeClass("hide");
	$(".sample-transformed-resource").addClass("hide");
}

BaseEditor.prototype.buildSampleResource = function(resource, parentItem) {
	var items = [];
	for (var i=0; i<Object.getOwnPropertyNames(resource).length; i++) {
		var key = Object.getOwnPropertyNames(resource)[i];
		
		if (key==="") {
			parentItem.append(": <span class=\"sample-output-value\">" + resource[key] + "</span>");
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