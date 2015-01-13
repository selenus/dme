var ModalMessage = function() {
	this.container = null;
};


ModalMessage.prototype.showMessage = function(type, header, message) {
	if (this.container == null) {
		$("#message-display-container").remove();
		this.container = $("<div id='message-display-container' style='z-index: 1050px;'>");
	}
	
	var msgContainer = $("<div class='alert alert-block alert-" + type + " fade in'>");
	msgContainer.append("<button data-dismiss='alert' class='close' type='button'>×</button>");
	if (header!=="") { 
		msgContainer.append($("<h4>").text(header)); 
	}
	if (message!=="") {
		msgContainer.append($("<p>").text(message));
	}
	
	setTimeout(function() {$(msgContainer.fadeOut(400, function() {$(this).remove();}));}, 5500);
	
	$(this.container).append(msgContainer);
	
	$("body").append(this.container);
	$(this.container).show();
	
	var _this = this;
	
	setTimeout(function() { $(_this.container).fadeOut(400, function() {$(this).remove();}); }, 5000);	
};

ModalMessage.prototype.close = function() {
	if (this.container !== null) {
		$(this.container).modal('hide');
		$(this.container).remove();
		this.container = null;
	}
};

ModalMessage.prototype.showLoading = function() {
	if (this.container == null) {
		this.container = $("<div class='modal overlay' style='left: 150px; top: 300px;'>");
	}

	$("body").append(this.container);
	
	
	$(this.container).spin({
		  lines: 11, // The number of lines to draw
		  length: 12, // The length of each line
		  width: 7, // The line thickness
		  radius: 17, // The radius of the inner circle
		  corners: 1, // Corner roundness (0..1)
		  rotate: 0, // The rotation offset
		  color: '#000', // #rgb or #rrggbb
		  speed: 1.2, // Rounds per second
		  trail: 80, // Afterglow percentage
		  shadow: false, // Whether to render a shadow
		  hwaccel: false, // Whether to use hardware acceleration
		  className: 'spinner', // The CSS class to assign to the spinner
		  zIndex: 2e9, // The z-index (defaults to 2000000000)
		  top: 'auto', // Top position relative to parent in px
		  left: 'auto' // Left position relative to parent in px
		});
		
	$(this.container).show();
	
	
};

var modalMessage = new ModalMessage();