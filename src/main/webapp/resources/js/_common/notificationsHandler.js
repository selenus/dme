var __notifications;

var NOTIFICATION_TYPES = {
	SUCCESS : { cssClass: "alert-success" },
	ERROR : { cssClass: "alert-danger" },
	WARNING : { cssClass: "alert-warning" },
	INFO : { cssClass: "alert-info" }
};

$(document).ready(function() {
	__notifications = new NotificationsHandler();
});

var NotificationsHandler = function() {
	this.container = __properties.notificationsArea;
	this.timeoutMs = __properties.notificationsTimeoutMs;
	this.fadeSpeed = __properties.notificationsFadeSpeed;
};

NotificationsHandler.prototype.showTranslatedMessage = function(notificationType, headerKey, messageKey) {
	this.showMessage(notificationType, __translator.translate(headerKey), __translator.translate(messageKey));
};

NotificationsHandler.prototype.showMessage = function(notificationType, header, message) {
	var _this = this;
	var notification = $("<div>", {"class": "alert pull-right alert-dismissable " + notificationType.cssClass});
	notification.append($("<button>", {	"type": "button", 
										"class": "close", 
										"data-dismiss": "alert", 
										"aria-hidden": "true"}).append("&times;"));
	if (header!==null && header!==undefined && header!=="") {
		notification.append($("<strong>").text(header)).append(" ");
	}
	notification.append(message);
	
	var notificationContainer = $("<div>", {"class": "clearfix"});
	notificationContainer.append(notification);
	
	// Autoquit notification
	setTimeout(function() {$(notificationContainer.fadeOut(_this.fadeSpeed, function() {$(this).remove();}));}, _this.timeoutMs);
	
	$(this.container).append(notificationContainer);
};