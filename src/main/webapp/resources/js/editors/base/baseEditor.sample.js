BaseEditor.prototype.initSample = function(samplePath, sampleEntityId) {
	var _this = this;
	
	this.samplePath = samplePath;
	this.sampleEntityId = sampleEntityId;
	this.sampleTextbox = $("#schema-sample-textarea");  //schema-editor-outer-east-container, schema-editor-detail-pane
	this.samplePane = $("#schema-editor-sample-pane");
	this.sampleModified = !$("#sample-set").val();
	this.currentSampleIndex = parseInt(Number($("#currentSampleIndex").val()));
	this.sampleResourceCount = parseInt(Number($("#currentSampleCount").val()));
	
	$(this.sampleTextbox).on('change keyup paste', function() {
		_this.sampleModified = true;
	});
		
	if (this.sampleResourceCount>0) {
		this.getSampleResource();
	}
};

BaseEditor.prototype.applyAndExecuteSample = function() {
	var _this = this;
	$("#schema-editor-sample-pane").children("div:not(.ui-pane-title)").hide();
	
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
	    data: { sample : $("#schema-sample-textarea").val() },
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
	    //data: { sample : $("#schema-sample-textarea").val() },
	    dataType: "json",
	    success: function(data) {
	    	if (data.success) {
	    		_this.processSampleExecutionResult(data.pojo);
	    	}
	    	_this.currentSampleIndex = 0;
	    	_this.logArea.refresh();
	    	$("#schema-editor-sample-pane").children("div:not(.ui-pane-title)").show();
	    	
	    	if (data.success==true && data.pojo>0 && _this.mappingId!==undefined && _this instanceof MappingEditor) {
	    		_this.executeSampleMapping();
	    	}
	    }, 
	    error: function(jqXHR, textStatus, errorThrown ) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	$("#schema-editor-sample-pane").children("div:not(.ui-pane-title)").show();
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    }
	});
};

BaseEditor.prototype.executeSampleMapping = function() {
	var _this = this;
	$.ajax({
	    url: this.samplePath + "async/executeSampleMapping",
	    type: "GET",
	    //data: { sample : $("#schema-sample-textarea").val() },
	    dataType: "json",
	    success: function(data) {
	    	console.log("return exec sample")
	    }, 
	    error: function(jqXHR, textStatus, errorThrown ) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	$("#schema-editor-sample-pane").children("div:not(.ui-pane-title)").show();
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    }
	});
};

BaseEditor.prototype.executeMapping = function() {
	this.isMappingEditor = true;
};

BaseEditor.prototype.processSampleExecutionResult = function(count) {
	var navTab = $("#schema-editor-sample-container a[href='#schema-sample-output-container']");
	var enabled = false;
	
	$(navTab).find(".badge-c").remove();
	$("#schema-sample-output-resource").text("");
	
	this.sampleResourceCount = 0;
	if (!isNaN(count) && parseInt(Number(count))==count && !isNaN(parseInt(count, 10))) {
		this.sampleResourceCount = count;
	}
	if (this.sampleResourceCount > 0) {
		enabled = true;
	}
	$(navTab).append("<span class=\"badge-c\"> <span class=\"badge\">" + this.sampleResourceCount + "</span></span>");
		
	if (enabled) {
		$(navTab).parent().removeClass("disabled");
		$(navTab).attr("data-toggle", "tab");
		$(navTab).tab('show');
	} else {
		$(navTab).parent().addClass("disabled");
		$(navTab).removeAttr("data-toggle");
		$("#schema-editor-sample-container a[href='#schema-sample-input-container']").tab('show');
	}
	this.getSampleResource();
};

BaseEditor.prototype.getSampleResource = function() {
	var _this = this;
	$.ajax({
	    url: this.samplePath + "async/getSampleResource",
	    type: "GET",
	    data: { index : _this.currentSampleIndex },
	    dataType: "json",
	    success: function(data) {
	    	var result = $("<ul>");
	    	result.append(_this.buildSampleResource(data));
	    	$("#schema-sample-output-resource").html(result);
	    	_this.setSampleNavigationBar();
	    	if (_this.mappingId!==undefined && _this instanceof MappingEditor) {
	    		_this.getTransformedResource();
	    	}
	    },
	    error: __util.processServerError
	});
};

BaseEditor.prototype.getTransformedResource = function() {
	var _this = this;
	$.ajax({
	    url: this.samplePath + "async/getTransformedResource",
	    type: "GET",
	    data: { index : _this.currentSampleIndex },
	    dataType: "json",
	    success: function(data) {
	    	var result = $("<ul>");
	    	result.append(_this.buildSampleResource(data));
	    	$("#schema-sample-transformed-resource").html(result);
	    	_this.setSampleNavigationBar();
	    },
	    error: __util.processServerError
	});
};


BaseEditor.prototype.setSampleNavigationBar = function() {
	$(".schema-sample-output-counter").text("" + (this.currentSampleIndex+1) + " / " + this.sampleResourceCount);
	
	if (this.currentSampleIndex > 0) {
		$(".btn-sample-prev-resource").removeClass("disabled");
	} else {
		$(".btn-sample-prev-resource").addClass("disabled");
	}
	if (this.currentSampleIndex < this.sampleResourceCount-1) {
		$(".btn-sample-next-resource").removeClass("disabled");
	} else {
		$(".btn-sample-next-resource").addClass("disabled");
	}	
};

BaseEditor.prototype.getPrevSampleResource = function() {
	if (this.currentSampleIndex > 0) {
		this.currentSampleIndex--;
		this.getSampleResource();
	}
};

BaseEditor.prototype.getNextSampleResource = function() {
	if (this.currentSampleIndex < this.sampleResourceCount-1) {
		this.currentSampleIndex++;
		this.getSampleResource();
	}
};

BaseEditor.prototype.showSampleResourceTarget = function() {
	$("#schema-sample-output-resource").addClass("hide");
	$("#schema-sample-transformed-resource").removeClass("hide");
}

BaseEditor.prototype.showSampleResourceSource = function() {
	$("#schema-sample-output-resource").removeClass("hide");
	$("#schema-sample-transformed-resource").addClass("hide");
}

BaseEditor.prototype.buildSampleResource = function(resource) {
	var key = Object.getOwnPropertyNames(resource)[0];
	var value = resource[key];
	
	var item = $("<li>");
	item.append("&#8594; <span class=\"schema-sample-output-key\">" + key + "</span>");
	
	var subItems = $("<ul>");
	var subItemCount = 0;
	
	if (Array.isArray(value)) {
		for (var j=0; j<value.length; j++) {
			subItemCount += this.buildSampleResourceValue(value[j], item, subItems);
		}
	} else {
		subItemCount += this.buildSampleResourceValue(value, item, subItems);
	}
	if (subItemCount>0) {
		item.append(subItems);
	}
	return item;
};

BaseEditor.prototype.buildSampleResourceValue = function(resource, parentItem, subItems) {
	var key = Object.getOwnPropertyNames(resource)[0];
	if (key==="") {
		parentItem.append(": <span class=\"schema-sample-output-value\">" + resource[key] + "</span>");
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