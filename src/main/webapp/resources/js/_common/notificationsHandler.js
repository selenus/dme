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

NotificationsHandler.prototype.showTranslatedMessage = function(notificationType, headerKey, messageKey, id, autoquit) {
	return this.showMessage(notificationType, __translator.translate(headerKey), __translator.translate(messageKey), id, autoquit);
};

NotificationsHandler.prototype.showMessage = function(notificationType, header, message, id, autoquit) {
	var _this = this;
	if (id===undefined) {
		id = randomnumber=Math.floor(Math.random()*1001);
	} 
	
	$(this.container).find("#notification-" + id).remove();
	
	var notification = $("<div>", {
		"class": "alert pull-right alert-dismissable " + notificationType.cssClass,
		"id" : "notification-" + id
		});
	
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
	
	if (autoquit!==false) {
		// Autoquit notification
		setTimeout(function() {$(notificationContainer.fadeOut(_this.fadeSpeed, function() {$(this).remove();}));}, _this.timeoutMs);
	}
	$(this.container).append(notificationContainer);
	
	return id;
};

NotificationsHandler.prototype.quitMessage = function(id) {
	if (id!==undefined && id!==null) {
		$(this.container).find("#notification-" + id).remove();
	}
}
;