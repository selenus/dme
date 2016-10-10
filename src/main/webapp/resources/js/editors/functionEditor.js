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
	this.error = ($(this.modal).find("#error").val()=="true");
	this.grammarError = ($(this.modal).find("#grammar_error").val()=="true");
	this.validated = false;
		
	__translator.addTranslations(["~eu.dariah.de.minfba.common.link.ok",
	                              "~eu.dariah.de.minfba.common.link.error",
	                              "~eu.dariah.de.minfba.common.link.validated",
	                              "~eu.dariah.de.minfba.common.link.modified",
	                              "~eu.dariah.de.minfba.common.link.validation_required",
	                              
	                              "~eu.dariah.de.minfba.schereg.model.function.error_in_grammar",
	                              "~eu.dariah.de.minfba.schereg.model.function.validation.validation_succeeded"]);
	__translator.getTranslations();
	
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
		state = "<span class=\"glyphicon glyphicon-info-sign glyphicon-color-info\" aria-hidden=\"true\"></span> " + __translator.translate("~eu.dariah.de.minfba.common.link.modified") + "; " + __translator.translate("~eu.dariah.de.minfba.common.link.validation_required");
	} else if (this.error) {
		state = "<span class=\"glyphicon glyphicon-exclamation-sign glyphicon-color-danger\" aria-hidden=\"true\"></span> " + __translator.translate("~eu.dariah.de.minfba.common.link.error");
	} else if (this.grammarError) {
		state = "<span class=\"glyphicon glyphicon-exclamation-sign glyphicon-color-warning\" aria-hidden=\"true\"></span> " + __translator.translate("~eu.dariah.de.minfba.schereg.model.function.error_in_grammar");
	} else if (this.validated) {
		state = "<span class=\"glyphicon glyphicon-ok-sign glyphicon-color-success\" aria-hidden=\"true\"></span> " + __translator.translate("~eu.dariah.de.minfba.common.link.validated");
	} else {
		state = "<span class=\"glyphicon glyphicon-ok-sign\" aria-hidden=\"true\"></span> " + __translator.translate("~eu.dariah.de.minfba.common.link.ok");
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
			_this.validateFunction($("#function_function").val(), modalFormHandler.container);
		},
		additionalModalClasses: "wider-modal"
	});
	modalFormHandler.show(form_identifier);
};

FunctionEditor.prototype.validateFunction = function(f, modal) {
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
	    	$("#collapse-function-parsing .panel-body").removeClass("hide");
	    	$(".function-loading").addClass("hide");
	    	if (data.success) {
	    		_this.showValidationResult(data);
	    		$(".function-ok").removeClass("hide");
	    	} else {
	    		$(".function-error").removeClass("hide");
	    	}
	    	_this.updateFunctionState();
	    	 $(modal).modal("layout");
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	__util.processServerError(jqXHR, textStatus, errorThrown);
	    	$(".function-loading").addClass("hide");
	    	$(".function-error").removeClass("hide");
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
		alert.text(__translator.translate("~eu.dariah.de.minfba.schereg.model.function.validation.validation_succeeded"));
		alert.addClass("alert-success");
		this.error = false;
		this.validated = true;
		this.modified = false;
	}
	$("#function-alerts").html(alert);
};

FunctionEditor.prototype.performTransformation = function() {
	var _this = this;
	var f = $(this.modal).find("#function_function").val();
	
	var elementIds = [];
	var samples = [];
	
	$(this.modal).find(".sample-input").each(function() {
		elementIds.push($(this).find("input[name='elementId']").val());
		samples.push($(this).find(".form-control").val());
	});
	
	$.ajax({
	    url: _this.pathname + "/async/parseSample",
	    type: "POST",
	    data: { 
	    	func: f,
	    	elementIds : elementIds, 
	    	samples: samples
	    },
	    dataType: "json",
	    success: function(data) {
	    	if (data.success) {
	    		//$("#transformation-result-container").text(JSON.stringify(data.pojo));
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
		alert("no array");
		return;
	}
	var list = $("<ul>");
	this.appendTransformationResults(data.pojo, list);
	$("#transformation-result").removeClass("hide");
	$("#transformation-result").html(list);
	$("#transformation-alerts").html("");
	if (data.objectWarnings!=null && Array.isArray(data.objectWarnings)) {
		for (var i=0; i<data.objectWarnings.length; i++) {
			$("#transformation-alerts").append(
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