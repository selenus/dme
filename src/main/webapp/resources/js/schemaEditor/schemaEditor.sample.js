SchemaEditor.prototype.sample_init = function() {
	var _this = this;
	
	this.sampleTextbox = $("#schema-sample-textarea");  //schema-editor-outer-east-container, schema-editor-detail-pane
	this.samplePane = $("#schema-editor-sample-pane");
	this.sampleModified = !$("#sample-set").val();
	this.currentSampleIndex = parseInt(Number($("#currentSampleIndex").val()));
	this.sampleResourceCount = parseInt(Number($("#currentSampleCount").val()));
	
	$(this.sampleTextbox).on('change keyup paste', function() {
		_this.sampleModified = true;
	});
		
	if (this.sampleResourceCount>0) {
		this.sample_getSampleResource();
	}
};

SchemaEditor.prototype.sample_resize = function() {
	// Always-visible counterparts are used because sample pane is often loaded in hidden state, 
	var helperHeight = $(".schema-editor-buttons").offsetParent().innerHeight();
	var helperTopOffset = $(".schema-editor-buttons").offset().top - $(".schema-editor-buttons").offsetParent().offset().top;
	
	var containerHeight = Math.floor(helperHeight - helperTopOffset - 130);
	
	$("#schema-sample-output-resource").height(containerHeight - 51);
	
	this.sampleTextbox.height(containerHeight);
};

SchemaEditor.prototype.sample_onPaneOpenStart = function() {
	var containerInnerWidth = $("#schema-editor-outer-east-container").innerWidth();
	var containerWidth = $("#schema-editor-outer-east-container").width();
	
	if(containerInnerWidth < 500) {
		this.outerLayout.sizePane("east", Math.floor(500 + containerWidth - containerInnerWidth));
		// Just to 'notify' the inner center to resize
		this.innerLayout.sizePane("east", 250);
	}
};


SchemaEditor.prototype.sample_applyAndExecute = function() {
	var _this = this;
	$("#schema-editor-sample-pane").children("div:not(.ui-pane-title)").hide();
	
	if (this.sampleModified) {
		this.sample_apply(function() {
			_this.sample_execute();
		})
	} else {
		this.sample_execute();
	}
};

SchemaEditor.prototype.sample_apply = function(callback) {
	var _this = this;
	$.ajax({
	    url: _this.pathname + "/async/applySample",
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

SchemaEditor.prototype.sample_execute = function() {
	var _this = this;
	$.ajax({
	    url: _this.pathname + "/async/executeSample",
	    type: "GET",
	    //data: { sample : $("#schema-sample-textarea").val() },
	    dataType: "json",
	    success: function(data) {
	    	if (data.success) {
	    		_this.sample_processExecutionResult(data.pojo);
	    	}
	    	_this.currentSampleIndex = 0;
	    	_this.logArea.refresh();
	    	$("#schema-editor-sample-pane").children("div:not(.ui-pane-title)").show();
	    }, 
	    error: function(jqXHR, textStatus, errorThrown ) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	$("#schema-editor-sample-pane").children("div:not(.ui-pane-title)").show();
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    }
	});
};

SchemaEditor.prototype.sample_processExecutionResult = function(count) {
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
	this.sample_getSampleResource();
};

SchemaEditor.prototype.sample_getSampleResource = function() {
	var _this = this;
	$.ajax({
	    url: _this.pathname + "/async/getSampleResource",
	    type: "GET",
	    data: { index : _this.currentSampleIndex },
	    dataType: "json",
	    success: function(data) {
	    	var result = $("<ul>");
	    	result.append(_this.sample_buildSampleResource(data));
	    	$("#schema-sample-output-resource").html(result);
	    	_this.sample_setNavigationBar();
	    },
	    error: __util.processServerError
	});
};


SchemaEditor.prototype.sample_setNavigationBar = function() {
	$(".schema-sample-output-counter").text("" + (this.currentSampleIndex+1) + " / " + this.sampleResourceCount);
	
	if (this.currentSampleIndex > 0) {
		$("#btn-sample-prev-resource").removeClass("disabled");
	} else {
		$("#btn-sample-prev-resource").addClass("disabled");
	}
	if (this.currentSampleIndex < this.sampleResourceCount-1) {
		$("#btn-sample-next-resource").removeClass("disabled");
	} else {
		$("#btn-sample-next-resource").addClass("disabled");
	}	
};

SchemaEditor.prototype.sample_getPrevResource = function() {
	if (this.currentSampleIndex > 0) {
		this.currentSampleIndex--;
		this.sample_getSampleResource();
	}
};

SchemaEditor.prototype.sample_getNextResource = function() {
	if (this.currentSampleIndex < this.sampleResourceCount-1) {
		this.currentSampleIndex++;
		this.sample_getSampleResource();
	}
};


SchemaEditor.prototype.sample_buildSampleResource = function(resource) {
	var key = Object.getOwnPropertyNames(resource)[0];
	var value = resource[key];
	
	var item = $("<li>");
	item.append("&#8594; <span class=\"schema-sample-output-key\">" + key + "</span>");
	
	var subItems = $("<ul>");
	var subItemCount = 0;
	
	if (Array.isArray(value)) {
		for (var j=0; j<value.length; j++) {
			subItemCount += this.sample_buildSampleResourceValue(value[j], item, subItems);
		}
	} else {
		subItemCount += this.sample_buildSampleResourceValue(value, item, subItems);
	}
	if (subItemCount>0) {
		item.append(subItems);
	}
	return item;
};

SchemaEditor.prototype.sample_buildSampleResourceValue = function(resource, parentItem, subItems) {
	var key = Object.getOwnPropertyNames(resource)[0];
	if (key==="") {
		parentItem.append(": <span class=\"schema-sample-output-value\">" + resource[key] + "</span>");
		return 0;
	} else {
		subItems.append(this.sample_buildSampleResource(resource));
		return 1;
	}
};


SchemaEditor.prototype.sample_resetSession = function() {
	var _this = this;
	$.ajax({
	    url: _this.pathname + "/async/createSession",
	    type: "GET",
	    //data: { sample : $("#schema-sample-textarea").val() },
	    //dataType: "json",
	    success: function(data) {
	    	if (data.success) { 
	    		_this.logArea.refresh();
	    	}
	    }, 
	    error: __util.processServerError
	});
};