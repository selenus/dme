var functionEditor;

var FunctionEditor = function(modal) {
	this.modal = modal;
	this.schemaId = schemaEditor.schemaId;
	this.functionId = schemaEditor.selectedElementId;
	this.pathname = __util.getBaseUrl() + "schema/editor/" + this.schemaId + "/function/" + this.functionId;
	this.svg = null;
	
	this.init();
};

FunctionEditor.prototype.init = function() {
	/* - load and set status of transformation function
	 * 
	 */
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
	    		_this.svg = new SvgViewer("#function-svg", data.pojo);
	    		//function-alerts
	    		
	    		if (data.objectErrors!=null && data.objectErrors.length > 0) {
	    			var errorList = $("<ul>");
	    			for (var i=0; i<data.objectErrors.length; i++) {
	    				errorList.append("<li>" + data.objectErrors[i] + "</li>");
	    			}
	    			$("#function-alerts").html(errorList);
	    		}
	    		
	    	} else {
	    		alert("error1");
	    	}
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	alert("error2");
	    }
	});
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