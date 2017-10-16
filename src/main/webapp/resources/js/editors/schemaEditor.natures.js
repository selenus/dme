/*
 * Controls (#model-natures-controls)
 *  - #select-model-natures
 *  - #edit-model-nature
 *  - #add-model-nature
 *  - #remove-model-nature
 */

SchemaEditor.prototype.initNatures = function() {
	this.currentNature = $('#select-model-natures').val();
	this.setNature();
	
	var _this = this;
	$("#select-model-natures").change(function() {
		_this.currentNature = $(this).val();
		_this.setNature();
		_this.reloadElementHierarchy();
	});
	
	$("#edit-model-nature").click(function() { _this.triggerEditNature(); });
	$("#add-model-nature").click(function() { _this.triggerAddNature(); });
	$("#remove-model-nature").click(function() { _this.triggerRemoveNature(); });
}

SchemaEditor.prototype.setNature = function() {
	if(this.currentNature==="logical_model") {
		$("#edit-model-nature").hide();
		$("#remove-model-nature").hide();
	} else {
		// Currently only the XML nature has properties at nature level (namespaces)
		if (this.currentNature==="de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature") {
			$("#edit-model-nature").show();
		}
		$("#remove-model-nature").show();
	}
	$("#current-model-nature").val(this.currentNature);
}


SchemaEditor.prototype.triggerAddNature = function() {
	var _this = this;
	var form_identifier = "add-nature";
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "natures/form/add",
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],          
        completeCallback: function(data) {
        	if (data.success) {
	        	var label = "~" + data.pojo + ".display_label";
	        	
	        	__translator.addTranslation(label);
	        	__translator.getTranslations();
	        	
	        	$('#select-model-natures').append($('<option>', {
	        	    value: data.pojo,
	        	    text: __translator.translate(label)
	        	}));
	        	$('#select-model-natures').val(data.pojo);
	        	$('#select-model-natures').trigger("change");
        	}
		}
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.triggerEditNature = function() {
	var _this = this;
	var form_identifier = "edit-nature-" + this.currentNature;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "natures/form/edit/",
		data: { n: this.currentNature },
		identifier: form_identifier,
		additionalModalClasses: "wide-modal",
		translations: [{placeholder: "~*servererror.head", key: "~de.unibamberg.minf.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~de.unibamberg.minf.common.view.forms.servererror.body"}
		                ],
        submitCallback: function(data, container) {
        	var i=0;
        	$("#edit-nature-namespaces tr").each(function() {
        		$($(this).find("input")[0]).prop("name", "namespaces[" + i + "].prefix");
        		$($(this).find("input")[1]).prop("name", "namespaces[" + i + "].url");
        		i++
        	});
        },          
        completeCallback: function(data) {
        	_this.reloadElementHierarchy();
		}
	});
		
	modalFormHandler.show(form_identifier);
};

SchemaEditor.prototype.triggerRemoveNature = function() {
	var _this = this;
	bootbox.confirm(__translator.translate("~de.unibamberg.minf.dme.dialog.confirm_delete_nature"), function(result) {
		if(result) {
			$.ajax({
			    url: _this.pathname + "/natures/async/remove",
			    data: { n: _this.currentNature },
			    type: "GET",
			    dataType: "json",
			    success: function(data) {
			    	if (data.success) {
				    	$("#select-model-natures option[value='" + data.pojo + "']").remove();
			        	$('#select-model-natures').val("logical_model");
			        	$('#select-model-natures').trigger("change");
			    	}
			    },
			    error: __util.processServerError
			});
		}
	});
};