var _csrf = $('meta[name=_csrf]').attr("content");
var _csrfHeader = $('meta[name=_csrf_header]').attr("content");

/*$.ajaxSetup({
    beforeSend: function(xhr, settings) {
        if (settings.type == 'POST' || settings.type == 'PUT' || settings.type == 'DELETE') {
        	xhr.setRequestHeader(_csrfHeader, _csrf);
        }
    }
});*/

/* 
 * Mainly for Safari support
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Function/bind
 */
if (!Function.prototype.bind) {
	  Function.prototype.bind = function (oThis) {
	    if (typeof this !== "function") {
	      // closest thing possible to the ECMAScript 5 internal IsCallable function
	      throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable");
	    }

	    var aArgs = Array.prototype.slice.call(arguments, 1),
	        fToBind = this,
	        fNOP = function () {},
	        fBound = function () {
	          return fToBind.apply(this instanceof fNOP && oThis
	                                 ? this
	                                 : oThis,
	                               aArgs.concat(Array.prototype.slice.call(arguments)));
	        };

	    fNOP.prototype = this.prototype;
	    fBound.prototype = new fNOP();

	    return fBound;
	  };
}


if (!String.format) {
        String.format = function(format) {
                var args = Array.prototype.slice.call(arguments, 1);
                if (format!==null && format!==undefined) {
	                return format.replace(/{(\d+)}/g, function(match, number) {
	                        return typeof args[number] != 'undefined' ? args[number] : match;
	                });
                } else {
                	return format;
                }
        };
}

String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

String.prototype.startsWith = function(start) {
        return this.length >= start.length && this.substr(0,start.length)==start;
};

var Util = function() {
        __translator.addTranslations(["~eu.dariah.de.minfba.common.view.notifications.login_required.head",
                                      "~eu.dariah.de.minfba.common.view.notifications.login_required.body",
                                      "~eu.dariah.de.minfba.common.link.yes",
                                      "~eu.dariah.de.minfba.common.link.no",
                                      "~eu.dariah.de.minfba.common.view.forms.servererror.head",
                                      "~eu.dariah.de.minfba.common.view.forms.servererror.body",
                                      "~eu.dariah.de.minfba.common.error.insufficient_rights.head",
                                      "~eu.dariah.de.minfba.common.error.insufficient_rights.body",
                                      "~eu.dariah.de.minfba.common.error.page_reload_required.head",
                                      "~eu.dariah.de.minfba.common.error.page_reload_required.body"])
        // We depend on the view's main js for this call
    //__translator.getTranslations();
        this.entityMap = {
                "&" : "&amp;",
                "<" : "&lt;",
                ">" : "&gt;",
                '"' : '&quot;',
                "'" : '&#39;',
                "/" : '&#x2F;'
        };
};
var __util = new Util();

Util.prototype.escapeHtml = function(string) {
        var _this = this;
        return String(string).replace(/[&<>"'\/]/g, function(s) {
                return _this.entityMap[s];
        });
};

Util.prototype.showLoginNote = function() {
	var _this = this;

    bootbox.dialog({
            message : __translator.translate("~eu.dariah.de.minfba.common.view.notifications.login_required.body"),
            title : __translator.translate("~eu.dariah.de.minfba.common.view.notifications.login_required.head"),
            buttons : {
                    no : {
                            label : __translator.translate("~eu.dariah.de.minfba.common.link.no"),
                            className : "btn-default"
                    },
                    yes : {
                            label : __translator.translate("~eu.dariah.de.minfba.common.link.yes"),
                            className : "btn-primary",
                            callback : function() {
                                    window.location = $("#login a").prop("href");
                            }
                    }
            }
    });
};

Util.prototype.processServerError = function(jqXHR, textStatus) {
	var errorContainer = $("<div>");
	var _this = __util;
	
	if (jqXHR!=null && jqXHR.status!=null) {
		if (jqXHR.status===200) {
			// Just a unnecessary call to this function;
			return;
		}
		if (jqXHR.status===403) {
			if (_this.isLoggedIn()) {
				_this.showErrorAlert("~eu.dariah.de.minfba.common.error.insufficient_rights.head",
						"~eu.dariah.de.minfba.common.error.insufficient_rights.body", $(errorContainer).html());
			} else {
				_this.showLoginNote();
			}
			return;
		}
		if (jqXHR.status===205) {
			_this.showErrorAlert("~eu.dariah.de.minfba.common.error.page_reload_required.head",
					"~eu.dariah.de.minfba.common.error.page_reload_required.body", $(errorContainer).html(),
					function() { location.reload(true); });
			return;
		}
	}
	
	// Generic server error
	if (jqXHR.responseJSON!==null && jqXHR.responseJSON!==undefined && jqXHR.responseJSON.success===false) {
		var error = $('<div class="server-error-container">').append(_this.showErrors(jqXHR.responseJSON)).get();
		$(errorContainer).append(error);
	} else if (jqXHR.responseText!==null && jqXHR.responseText!==undefined) {
		 try {
			 // Happens e.g. when we call for HTML forms asynchronously - i.e. JSON is not the expected answer type
			 var jsonError = JSON.parse(jqXHR.responseText);
			 var error = $('<div class="server-error-container">').append(_this.showErrors(jsonError)).get();
			 $(errorContainer).append(error);
		 } catch (e) {
			 var error = $('<div class="server-error-container">').append(jqXHR.responseText).get();
			 $(errorContainer).append(error);
		 }
	}
	
	//$(errorContainer).append(jqXHR.responseText);
	
	_this.showErrorAlert("~eu.dariah.de.minfba.common.view.forms.servererror.head",
			"~eu.dariah.de.minfba.common.view.forms.servererror.body", $(errorContainer).html());
};

Util.prototype.showErrors = function(modelActionPojo) {	
	var result = $("<ul>");
	
	if (modelActionPojo.objectErrors!=null && Array.isArray(modelActionPojo.objectErrors)) {
		for (var i=0; i<modelActionPojo.objectErrors.length; i++) {
			var li = $("<li>");
			li.append(modelActionPojo.objectErrors[i]);
			result.append(li);
		}
	}
	if (modelActionPojo.objectWarnings!=null && Array.isArray(modelActionPojo.objectWarnings)) {
		for (var i=0; i<modelActionPojo.objectWarnings.length; i++) {
			var li = $("<li>");
			li.append(modelActionPojo.objectWarnings[i]);
			result.append(li);
		}
	}
	return result;
}

Util.prototype.showErrorAlert = function(titleCode, messageCode, payload, callback) {	
	bootbox.alert(
			"<h3>" + __translator.translate(titleCode) + "</h3>" +
			"<p>" + __translator.translate(messageCode) + "</p>" + ( payload!==undefined ? payload : "")
		, callback);
};

Util.prototype.isLoggedIn = function() {
    var loggedIn = false;
    $.ajax({
    	url: __util.getBaseUrl() + "async/isAuthenticated",
    	type: "GET",
    	async: false,
    	encoding: "UTF-8",
    	dataType: "text",
    	success: function(data) {
    		loggedIn = (data=="true");
    	}
    });
   
    if (loggedIn) {
            $("#login").css("display", "none");
            $("#logout").css("display", "block");
    } else {
            $("#login").css("display", "block");
            $("#logout").css("display", "none");
    }
    return loggedIn;       
};

Util.prototype.getBaseUrl = function() {
        return $("#baseUrl").val();
};

Util.prototype.composeUrl = function(target) {
    return $("#baseUrl2").val().replace("{}", target);
};


Util.prototype.renderActivities = function(container, id, data) {
	$(container).html("");
	
	if (data!=null) {
		for (var i=0; i<data.length; i++) {
			$(container).append(
					"<div class=\"alert alert-sm alert-info activity-history-element\">" + 
						"<em>" + (data[i].timestamp==null ? "?" : data[i].timestampString) +"</em><br />" + 
						"<h4>" +
							" " + data[i].user + 
						"</h4>" +
						"<span class=\"glyphicon glyphicon-asterisk\" aria-hidden=\"true\"></span> " + 
							data[i].news + "&nbsp;&nbsp;" +
						"<span class=\"glyphicon glyphicon-pencil\" aria-hidden=\"true\"></span> " + 
							data[i].edits + "&nbsp;&nbsp;" +
						"<span class=\"glyphicon glyphicon-trash\" aria-hidden=\"true\"></span> " + 
							data[i].deletes +
					"</div>");
			
		}
	}
}