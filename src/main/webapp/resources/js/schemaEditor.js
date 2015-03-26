var editor;
$(document).ready(function() {
	editor = new SchemaEditor();
});

var SchemaEditor = function() {
	this.schemaId = $("#schema-id").val();
	
	__translator.addTranslations([]);
		
	this.loadHierarchy();
}

SchemaEditor.prototype.loadHierarchy = function() {
	$.ajax({
	    url: __util.getBaseUrl() + "schema/editor/" + this.schemaId + "/async/getHierarchy",
	    type: "GET",
	    dataType: "json",
	    success: function(data) {
	    	alert(data);
	    }
	});
};