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
	    		_this.svg = new SvgViewer("#function-svg", data.pojo)
	    	} else {
	    		alert("error1");
	    	}
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	alert("error2");
	    }
	});
};
