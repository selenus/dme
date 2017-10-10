var functionEditor;

var FunctionEditor = function(modal, options) {
	this.options = $.extend({ 	
		pathPrefix: "",
		entityId : "",
		functionId : ""
	}, options)
	
	this.modal = modal;
	this.schemaId = this.options.entityId;
	this.functionId = this.options.functionId;
	this.pathname = this.options.pathPrefix + "/function/" + this.functionId;
	this.svg = null;
	
	this.modified = false;
	this.error = ($(this.modal).find(".error").val()=="true");
	this.grammarError = ($(this.modal).find(".grammar_error").val()=="true");
	this.validated = false;
		
	__translator.addTranslations(["~de.unibamberg.minf.common.link.ok",
	                              "~de.unibamberg.minf.common.link.error",
	                              "~de.unibamberg.minf.common.link.validated",
	                              "~de.unibamberg.minf.common.link.modified",
	                              "~de.unibamberg.minf.common.link.validation_required",
	                              
	                              "~de.unibamberg.minf.dme.model.function.error_in_grammar",
	                              "~de.unibamberg.minf.dme.model.function.validation.validation_succeeded"]);
	__translator.getTranslations();
	
	this.init();
};

FunctionEditor.prototype.init = function() {
	/* - load and set status of transformation function
	 * 
	 */
	var _this = this;	
	$(this.modal).find(".function_function").on('change keyup paste', function() {
		_this.modified = true;
		_this.updateFunctionState();
	});
	
	this.updateFunctionState();
};

FunctionEditor.prototype.updateFunctionState = function() {
	var state = "";
	if (this.modified) {
		state = "<span class=\"glyphicon glyphicon-info-sign glyphicon-color-info\" aria-hidden=\"true\"></span> " + __translator.translate("~de.unibamberg.minf.common.link.modified") + "; " + __translator.translate("~de.unibamberg.minf.common.link.validation_required");
	} else if (this.error) {
		state = "<span class=\"glyphicon glyphicon-exclamation-sign glyphicon-color-danger\" aria-hidden=\"true\"></span> " + __translator.translate("~de.unibamberg.minf.common.link.error");
	} else if (this.grammarError) {
		state = "<span class=\"glyphicon glyphicon-exclamation-sign glyphicon-color-warning\" aria-hidden=\"true\"></span> " + __translator.translate("~de.unibamberg.minf.dme.model.function.error_in_grammar");
	} else if (this.validated) {
		state = "<span class=\"glyphicon glyphicon-ok-sign glyphicon-color-success\" aria-hidden=\"true\"></span> " + __translator.translate("~de.unibamberg.minf.common.link.validated");
	} else {
		state = "<span class=\"glyphicon glyphicon-ok-sign\" aria-hidden=\"true\"></span> " + __translator.translate("~de.unibamberg.minf.common.link.ok");
	}
	$(this.modal).find(".function_state").html(state);
};

FunctionEditor.prototype.showHelp = function() {
	
};

FunctionEditor.prototype.processFunction = function() {
	var _this = this;	
	var form_identifier = "process-function";
	
	this.processFunctionModal = new ModalFormHandler({
		formUrl: "/function/" + _this.functionId + "/async/process",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],
		displayCallback: function() { 
			_this.validateFunction($(_this.modal).find(".function_function").val());
		},
		additionalModalClasses: "wider-modal"
	});
	this.processFunctionModal.show(form_identifier);
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
	    	var container = $(_this.processFunctionModal.container);
	    	
	    	container.find(".collapse-function-parsing .panel-body").removeClass("hide");
	    	container.find(".function-loading").addClass("hide");
	    	if (data.success) {
	    		_this.showValidationResult(data);
	    		container.find(".function-ok").removeClass("hide");
	    	} else {
	    		container.find(".function-error").removeClass("hide");
	    	}
	    	_this.updateFunctionState();
	    	container.modal("layout");
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	container.find(".function-loading").addClass("hide");
	    	container.find(".function-error").removeClass("hide");
	    }
	});
};

FunctionEditor.prototype.showValidationResult = function(data) {
	this.svg = new SvgViewer(".function-svg", data.pojo);
	
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
		alert.text(__translator.translate("~de.unibamberg.minf.dme.model.function.validation.validation_succeeded"));
		alert.addClass("alert-success");
		this.error = false;
		this.validated = true;
		this.modified = false;
	}
	$(this.processFunctionModal.container).find(".function-alerts").html(alert);
};

FunctionEditor.prototype.performTransformation = function() {
	var _this = this;
	var f = $(this.modal).find(".function_function").val();
	
	$(".transformation-result").text("");
	
	var samples = [];
	$(this.modal).find(".sample-input").each(function() {
		samples.push({
			elementId : $(this).find("input[name='elementId']").val(),
			text: $(this).find(".form-control").val()
		});
	});
	
	$.ajax({
	    url: _this.pathname + "/async/validate",
	    type: "POST",
	    data: { func : f },
	    dataType: "json",
	    success: function(data) {
	    	if (data.success) {
	    		_this.showValidationResult(data);
	    		container.find(".function-ok").removeClass("hide");
	    	} else {
	    		container.find(".function-error").removeClass("hide");
	    	}
	    	
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	
	    }
	});
	
	$.ajax({
	    url: _this.pathname + "/async/parseSample",
	    type: "POST",
	    data: JSON.stringify ({ 
	    	func: f,
	    	samples: samples
	    }),
	    contentType: 'application/json',
        dataType: 'json',
	    success: function(data) {
	    	if (data.success) {
	    		//$(_this.modal).find(".transformation-result-container").text(JSON.stringify(data.pojo));
	    		_this.showTransformationResults(data);
	    	} else {
	    		alert("error1");
	    	}
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    }
	});
};

FunctionEditor.prototype.showTransformationResults = function(data) {
	if (data.pojo==null || !Array.isArray(data.pojo)) {
		$(this.modal).find(".transformation-result").addClass("hide");
		$(this.modal).find(".transformation-result").text("");
		$(this.modal).find(".no-results-alert").removeClass("hide");
		return;
	}
	$(this.modal).find(".no-results-alert").addClass("hide");
	var list = $("<ul>");
	this.appendTransformationResults(data.pojo, list);
	$(this.modal).find(".transformation-result").removeClass("hide");
	$(this.modal).find(".transformation-result").html(list);
	$(this.modal).find(".transformation-alerts").html("");
	if (data.objectWarnings!=null && Array.isArray(data.objectWarnings)) {
		for (var i=0; i<data.objectWarnings.length; i++) {
			$(this.modal).find(".transformation-alerts").append(
					"<div class=\"alert alert-sm alert-warning\">" +
						"<span class=\"glyphicon glyphicon-exclamation-sign\" aria-hidden=\"true\"></span> " 
						+ data.objectWarnings[i] + 
					"</div>");			
		}
	}
	
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