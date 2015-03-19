var editor;
$(document).ready(function() {
	editor = new SchemaEditor();
	$("#btn-add-schema").click(function() { editor.triggerAddSchema(); });
});

var SchemaEditor = function() {
	this.prepareTranslations([]);
	this.createTable();
};


SchemaEditor.prototype = new BaseEditor();

SchemaEditor.prototype.createTable = function() {
	this._base.table = $('.table').dataTable({
		"aaSorting": [[ 3, "asc" ]],
		"aoColumnDefs": [{	"aTargets": [0], 
							"mData": "id", 
							/*"mRender": function (data, type, full) { return editor.renderCheckboxColumn(data, type, full);}*/
						 },
		                 {	"aTargets": [1],
		                 	"mData": "label", 
		                 	/*"mRender": function (data, type, full) { return editor.renderBadgeColumn(data, type, full);}*/
						 },
						 {	"aTargets": [2], 
							"mData": "label", 
							"bSortable": false,
							/*"mRender": function (data, type, full) { return editor.renderActionColumn(data, type, full);},*/
							"sClass": "td-no-wrap"
						 },
		                 {	"aTargets": [3], 
							"mData": "label", 
							/*"mRender": function (data, type, full) { return editor.renderCollectionColumn(data, type, full);},*/
						 	"sWidth": "35%"
						 },
		                 {	"aTargets": [4], 
							"mData": "id", 
							/*"mRender": function (data, type, full) { return editor.renderEndpointsColumn(data, type, full);},*/
							"sWidth": "65%"
						 }],
		/*"fnServerParams": function (aoData) { 
			var hideWithoutEndpoint = "true";
			$("#chk-filter-without-endpoint").each(function() { if ($(this).prop("checked")==false) { hideWithoutEndpoint = "false"; } });
			aoData.push( { "name": "hideWithoutEndpoint", "value": hideWithoutEndpoint } ); 
		}*/
	});
};

SchemaEditor.prototype.triggerAddSchema = function () {
	this.triggerEditSchema();
};

SchemaEditor.prototype.triggerEditSchema = function(schemaId) {
	var _this = this;
	var form_identifier = "edit-schema-" + schemaId;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: (schemaId!=undefined ? ("/async/edit/" + schemaId) : "/async/add"),
		identifier: form_identifier,
		//additionalModalClasses: "wider-modal",
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.schereg.view.async.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.schereg.view.async.servererror.body"}
		                ],
		completeCallback: function() {_this.refresh();}
	});
	modalFormHandler.show(form_identifier);
};