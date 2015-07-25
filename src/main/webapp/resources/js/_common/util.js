var _csrf = $('meta[name=_csrf]').attr("content");
var _csrfHeader = $('meta[name=_csrf_header]').attr("content");

$.ajaxSetup({
    beforeSend: function(xhr, settings) {
        if (settings.type == 'POST' || settings.type == 'PUT' || settings.type == 'DELETE') {
        	xhr.setRequestHeader(_csrfHeader, _csrf);
        }
    }
});

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
                                      "~eu.dariah.de.minfba.common.link.no"])
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
                                label : __translator.translate("~eu.dariah.de.minfba.common.view.common.no"),
                                className : "btn-default"
                        },
                        yes : {
                                label : __translator.translate("~eu.dariah.de.minfba.common.view.common.yes"),
                                className : "btn-primary",
                                callback : function() {
                                        window.location = $("#login a").prop("href");
                                }
                        }
                }
        });
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