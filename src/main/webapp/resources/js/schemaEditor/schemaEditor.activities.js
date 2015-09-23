SchemaEditor.prototype.activities_init = function() {
	this.schemaActivitiesContainer = $("#schema-context-activities");
	this.elementActivitiesContainer = $("#schema-element-context-activities");
};

SchemaEditor.prototype.activities_loadForSchema = function() {
	var _this = this;
	$.ajax({
        url: window.location.pathname + "/async/getChangesForEntity/" + _this.schemaId,
        type: "GET",
        dataType: "json",
        success: function(data) { __util.renderActivities(_this.schemaActivitiesContainer, null, data); },
        error: function(textStatus) {
        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.head"), 
        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.body"));
        }
	});
};

SchemaEditor.prototype.activities_loadForElement = function(id) {
	var _this = this;
	$.ajax({
        url: window.location.pathname + "/async/getChangesForElement/" + id,
        type: "GET",
        dataType: "json",
        success: function(data) { __util.renderActivities(_this.elementActivitiesContainer, id, data); },
        error: function(textStatus) {
        	__notifications.showMessage(NOTIFICATION_TYPES.ERROR, 
        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.head"), 
        			__translator.translate("~eu.dariah.de.minfba.common.view.forms.servererror.body"));
        }
	});
};