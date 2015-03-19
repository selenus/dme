/**
 * 	BaseEditor: superclass for all editors handling dataTables
 * ==========================================================================================
 * 	Notes:
 * 		- All filter options that are indended to trigger a reload of the table
 * 		  need to hold a class 'editor-option'
 * 		- Defaults for bProcessing, sAjaxSource, bAutoWidth and the fnDrawCallback
 * 		  are set here but can be overridden when initializing the actual table
 * 		- Tooltips in the data are identified by the class 'hint-tooltip' and
 * 		  loaded after every refresh of the editor
 */
function BaseEditor() {
	// Explicit accessor for base properties in the editors 
	this._base = this;
	this.table = null;
	this.error = false;
	
	this.options = {
			refreshInterval: __properties.refreshIntervalMs,
			cyclicRefresh: __properties.refreshViews
	};
	
	this.baseTranslations = ["~eu.dariah.de.minfba.common.view.notifications.async_general_error",
	                         "~eu.dariah.de.minfba.common.view.notifications.async_timeout",
	                         "~eu.dariah.de.minfba.common.view.notifications.session_expired_reload"];
		
	var _this = this;
	// Setting some defaults for the datatables as used in the project
	$.extend(true, $.fn.dataTable.defaults, {
		"bProcessing": true,
		"sAjaxSource": window.location.pathname + "/async/getData",
		"bAutoWidth": false,
		"fnDrawCallback": function (oSettings) {
			_this.handleRefresh(oSettings);
	    },
	    "fnServerData": function ( sSource, aoData, fnCallback ) {
	        $.ajax( {
	            "dataType": 'json',
	            "type": "GET",
	            "url": sSource,
	            "data": aoData,
	            "success": fnCallback,
	            "timeout": 15000,
	            "error": function(xhr, textStatus, error) {_this.handleAjaxError(xhr, textStatus, error); }
	            });
	    }
	});
	$(".editor-option").change(function() { _this.refresh(); });
	
	this.cycleRefresh();
}

BaseEditor.prototype.handleAjaxError = function(xhr, textStatus, error) {
    // Reload because the session has expired
	if (xhr.status===403) {
    	bootbox.alert(__translator.translate("~eu.dariah.de.minfba.common.view.notifications.session_expired_reload"), function(result) {
    		window.location.reload();
    	});
	} else if (textStatus==='timeout') {
        alert(__translator.translate("~eu.dariah.de.minfba.common.view.notifications.async_timeout"));
    } else {
        alert(__translator.translate("~eu.dariah.de.minfba.common.view.notifications.async_general_error"));
    }
	this.error = true;
    this.table.fnProcessingIndicator(false);
};

BaseEditor.prototype.cycleRefresh = function() {
	var _this = this;
	if (this.options.cyclicRefresh) {
		setTimeout(function() { _this.refresh(); _this.cycleRefresh(); }, _this.options.refreshInterval);
	};
};

BaseEditor.prototype.refresh = function() {
	if (!this.error && this.table!=null) {
		this.table.fnReloadAjax(null, null, true);
	}
};

BaseEditor.prototype.handleRefresh = function(oSettings) {
	// This is the case for the pre-load callback
	if (oSettings.aoData===null || oSettings.aoData===undefined || 
			(oSettings.aoData instanceof Array && oSettings.aoData.length==0)) {
		return;
	}

	// We arrive here after data has been loaded into the table
	$(this.table).find(".hint-tooltip").tooltip({
	     'delay': { show: 1000, hide: 0 }
	});
};

BaseEditor.prototype.prepareTranslations = function(translations) {
	if (translations!=null || (translations instanceof Array && translations.length>0)) {
		__translator.addTranslations(translations);
		__translator.addTranslations(this.baseTranslations);
		__translator.getTranslations();
	}
};

jQuery.fn.dataTableExt.oApi.fnProcessingIndicator = function ( oSettings, onoff ) {
    if ( typeof( onoff ) == 'undefined' ) {
        onoff = true;
    }
    this.oApi._fnProcessingDisplay( oSettings, onoff );
};