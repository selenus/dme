var messageArea = null;

$(document).ready(function() {
	// Make sure the messages are not visible at first
	$("#top_messages").css("left", $("#top_messages").outerWidth(true) + "px");
	
	// MessageArea will take care of the rest
	messageArea = new MessageArea("#top_messages");
	messageArea.initialize();
});


var MessageArea = function(area) {
	this.area = $(area);
	this.list = $(area + " ul");
	this.messageTimeout = 5000; // in seconds
	this.timeoutSet = false;
	this.isShown = false;
	this.messages = [];
};

// Load messages that came from server-side. Reason: In case this js can't run, 
//  at least the server-side messages are displayed.
MessageArea.prototype.initialize = function() {
	var removeDate = new Date();
	var serverMessages = [];
	
	$(this.area).find("li").each(
			function () {
				serverMessages.push($(this).text());
			});
	
	$(this.list).empty();
	
	for (var i=0; i<serverMessages.length; i++) {
		messageArea.addMessage(serverMessages[i], removeDate.getSeconds());
	}
	
};

MessageArea.prototype.addMessage = function(text) {
	var message = new UserMessage(text, (new Date()).getTime());
	this.messages.push(message);
		
	$(this.list).prepend(message.getHtmlItem());
	
	if (!this.isShown) {
		this.showContainer();
	}
	
	if (!this.timeoutSet) {
		this.timeoutSet = true;
		setTimeout(function() { messageArea.removeObsoleteMessages();}, messageArea.messageTimeout);
	}
};

MessageArea.prototype.removeObsoleteMessages = function() {
	var nowS = (new Date()).getTime();
	var removeMessages = [];
	var keepMessages = [];
	var nextRun = null;
	
	for (var i = 0; i < this.messages.length; i++) {
		var lifespan = nowS - this.messages[i].dateS;
		
		if (lifespan >= this.messageTimeout) {
			removeMessages.push(this.messages[i]);
		} else {
			if (nextRun == null || lifespan < nextRun) {
				nextRun = lifespan;
			}
			keepMessages.push(this.messages[i]);
		}
	}
	
	this.messages = keepMessages;
	
	if (this.messages.length == 0) {
		this.hideContainer(function() {
			$(messageArea.list).empty();
			messageArea.timeoutSet = false;
			messageArea.isShown = false;
		});
	} else {
		for (var i = 0; i < removeMessages.length; i++) {
			var item = removeMessages[i].getHtmlItem();
			$(item).remove();
		}
		this.timeoutSet = true;
		setTimeout(function() { messageArea.removeObsoleteMessages();}, nextRun);
	}
	
};

MessageArea.prototype.showContainer = function() {
	
	//$(this.list).html("<ul></ul>");	
	
	for (var i = 0; i < this.messages.length; i++) {
		$(this.list).append(this.messages[i].getHtmlItem());
	}
	this.isShown = true;
	$(this.area).animate({left: 0}, 400);
};

MessageArea.prototype.hideContainer = function(callback) {
	$(this.area).animate({left: $(this.area).outerWidth(true)+"px"}, 400, callback);
};




var UserMessage = function(message, dateS) {
	this.message = message;
	this.dateS = dateS;
	this.item = null;
};

UserMessage.prototype.getHtmlItem = function() { 
	if (this.item == null) {
		this.item = $("<li>" + this.message + "</li>");
	}
	return this.item;
};















