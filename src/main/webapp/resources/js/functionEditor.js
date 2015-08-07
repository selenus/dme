var functionEditor;

var FunctionEditor = function(modal) {
	this.modal = modal;
	this.schemaId = schemaEditor.schemaId;
	this.functionId = schemaEditor.selectedElementId;
	this.pathname = __util.getBaseUrl() + "schema/editor/" + this.schemaId + "/function/" + this.functionId;
	this.init();
};

FunctionEditor.prototype.init = function() {
	
};

FunctionEditor.prototype.showHelp = function() {
	
};