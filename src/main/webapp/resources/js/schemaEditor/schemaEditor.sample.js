SchemaEditor.prototype.sample_init = function() {
	var _this = this;
	
	this.sampleTextbox = $("#schema-sample-textarea");
	this.sampleModified = true;
	
	$(this.sampleTextbox).on('change keyup paste', function() {
		_this.sampleModified = true;
	});
};

SchemaEditor.prototype.sample_resize = function() {
	var sampleTextboxHeight = Math.floor($(".outer-east").innerHeight() - (this.sampleTextbox.offset().top - this.sampleTextbox.offsetParent().offset().top));
	this.sampleTextbox.height(sampleTextboxHeight - 70);
};


SchemaEditor.prototype.sample_applyAndExecute = function() {
	var _this = this;
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
	    error: function(jqXHR, textStatus, errorThrown ) { }
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
	    		_this.logArea.refresh();
	    	}
	    }, 
	    error: function(jqXHR, textStatus, errorThrown ) { }
	});
};