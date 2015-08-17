var functionEditor;

var FunctionEditor = function(modal) {
	this.modal = modal;
	this.schemaId = schemaEditor.schemaId;
	this.functionId = schemaEditor.selectedElementId;
	this.pathname = __util.getBaseUrl() + "schema/editor/" + this.schemaId + "/function/" + this.functionId;
	this.svg = null;
	
	this.modified = false;
	this.error = false;
	this.validated = false;
	
	this.init();
};

FunctionEditor.prototype.init = function() {
	/* - load and set status of transformation function
	 * 
	 */
	var _this = this;
	
	$(this.modal).find("#function_function").on('change keyup paste', function() {
		_this.modified = true;
		_this.updateFunctionState();
	});
	
	this.updateFunctionState();
};

FunctionEditor.prototype.updateFunctionState = function() {
	var state = "";
	if (this.modified) {
		state = "<span class=\"glyphicon glyphicon-info-sign glyphicon-color-info\" aria-hidden=\"true\"></span> modified, validation needed";
	} else if (this.error || $(this.modal).find("#error").val()=="true") {
		state = "<span class=\"glyphicon glyphicon-exclamation-sign glyphicon-color-danger\" aria-hidden=\"true\"></span> error";
	} else if ($(this.modal).find("#grammar_error").val()=="true") {
		state = "<span class=\"glyphicon glyphicon-exclamation-sign glyphicon-color-warning\" aria-hidden=\"true\"></span> ~ error in grammar";
	} else if (this.validated) {
		state = "<span class=\"glyphicon glyphicon-ok-sign glyphicon-color-success\" aria-hidden=\"true\"></span> validated";
	} else {
		state = "<span class=\"glyphicon glyphicon-ok-sign\" aria-hidden=\"true\"></span> ok";
	}
	$(this.modal).find("#function_state").html(state);
};

FunctionEditor.prototype.showHelp = function() {
	
};

FunctionEditor.prototype.processFunction = function() {
	var _this = this;	
	var form_identifier = "process-function";
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/function/" + _this.functionId + "/async/process",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		displayCallback: function() { 
			_this.validateFunction($("#function_function").val());
		},
		additionalModalClasses: "wider-modal",
		completeCallback: function() {
			
		}
	});
	modalFormHandler.show(form_identifier);
};

FunctionEditor.prototype.validateFunction = function(f) {
	var _this = this;
	
	if (this.svg!=null) {
		this.svg.destroy();
	}
	$.ajax({
	    url: _this.pathname + "/async/validate",
	    type: "POST",
	    data: { func : f },
	    dataType: "json",
	    success: function(data) {
	    	if (data.success) {
	    		_this.showValidationResult(data);
	    	} else {
	    		alert("error1");
	    	}
	    	_this.updateFunctionState();
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	alert("error2");
	    }
	});
};

FunctionEditor.prototype.showValidationResult = function(data) {
	this.svg = new SvgViewer("#function-svg", data.pojo);
	
	var alert = $("<div class=\"alert\">");
	if (data.objectErrors!=null && data.objectErrors.length > 0) {
		var errorList = $("<ul>");
		for (var i=0; i<data.objectErrors.length; i++) {
			errorList.append("<li>" + data.objectErrors[i] + "</li>");
		}
		alert.html(errorList)
		alert.addClass("alert-danger");
		this.error = true;
		this.validated = false;
		this.modified = false;
	} else {
		alert.text("~ Validation of transformation function succeeded")
		alert.addClass("alert-success");
		this.error = false;
		this.validated = true;
		this.modified = false;
	}
	$("#function-alerts").html(alert);
};

FunctionEditor.prototype.performTransformation = function() {
	var _this = this;
	var f = $("#function_function").val();
	var s = $("#function-sample-input").val();
	
	$.ajax({
	    url: _this.pathname + "/async/parseSample",
	    type: "POST",
	    data: { 
	    	func: f,
	    	sample: s
	    },
	    dataType: "json",
	    success: function(data) {
	    	if (data.success) {
	    		//$("#transformation-result-container").text(JSON.stringify(data.pojo));
	    		_this.showTransformationResults(data.pojo);
	    	} else {
	    		alert("error1");
	    	}
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	alert("error2");
	    }
	});
};

FunctionEditor.prototype.showTransformationResults = function(params) {
	if (params==null || !Array.isArray(params)) {
		alert("no array");
		return;
	}
	var list = $("<ul>");
	this.appendTransformationResults(params, list);
	$("#transformation-result-container").html(list);
};

FunctionEditor.prototype.appendTransformationResults = function(elements, container) {
	for (var i=0; i<elements.length; i++) {
		var elem = $("<li>");
		elem.append("<span class=\"transformation-result-label\">" + elements[i].label + "</span>");
		if (elements[i].children!=null && Array.isArray(elements[i].children) && elements[i].children.length > 0) {
			var subelem = $("<ul>");
			this.appendTransformationResults(elements[i].children, subelem);
			elem.append(subelem);
		} else {
			elem.append(": ");
			elem.append("<span class=\"transformation-result-value\">" + elements[i].value + "</span>");
		}
		container.append(elem);
	}
};