var modalFormHandler = null;
var ModalFormHandler = function(options) {
	this.options = options;

	this.form = null; 				// contains the form-object
	this.container = null;			// styled div-container around form
	this.formResetted = false;		// form has been user-resetted (cancel/save)
	this.fileUploadElements = [];		// elements that contain file-upload
	this.showOnlyLastestAlert = true;	// show only one response to user or pile up
	
	this.translations = [];
	this.setupCallback = null;
	this.completeCallback = null;
};

ModalFormHandler.prototype.show = function(identifier) {
	if (this.container===null) { 
		// new form
		this.init();
	} else if (identifier !== this.options.identifier) { 
		// the identifier changed, form is invalid...
		$(this.container).remove();
    	this.container = null;
    	this.form = null;
    	this.init();
	} else { 
		// show previously hidden form
		$(this.container).modal('show');
	}
	this.formResetted = false;
};

ModalFormHandler.prototype.init = function() {
	var _this = this;
	modalMessage.showLoading();
	
	var step1 = false;
	var step2 = false;
		
	$.ajax({
        url: window.location.pathname + _this.options.translationsUrl,
        type: "POST",
        dataType: "json",
        data: {keys: JSON.stringify(_this.translations) },
        success: function(data) {
        	_this.translations = data;
        	step1 = true;
        	if (step2 == true) {
            	_this.setUpForm();
            	modalMessage.close();
        	}
        },
        error: function(textStatus) { 
        	_this.formResetted==true;
        	$(_this).hide();
        	modalMessage.showMessage("error", _this.translate("~*servererror.head"), 
        			_this.translate("~*servererror.body"));
        }
	});
	
	$.ajax({
        url: window.location.pathname + _this.options.formUrl,
        type: "GET",
        dataType: "html",
        success: function(data) {
        	_this.form = $(data);
        	step2 = true;
        	if (step1 == true) {
            	_this.setUpForm();
            	modalMessage.close();
        	}
        },
        error: function(textStatus) { 
        	_this.formResetted==true;
        	$(_this).hide();
        	modalMessage.showMessage("error", _this.translate("~*servererror.head"), 
        			_this.translate("~*servererror.body"));
        }
	});
};

ModalFormHandler.prototype.setUpForm = function() {

	var _this = this;
	
	this.container = $("<div class='modal hide fade'>");
	
	if (this.options.additionalModalClasses!==null) {
		$(this.container).addClass(this.options.additionalModalClasses);
	}
	
	$(this.container).html($(this.form));
	
	$(this.form).find(".form-header").addClass("modal-header");
	$(this.form).find(".form-header").prepend('<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>');
	$(this.form).find(".form-content").addClass("modal-body");
	$(this.form).find(".form-footer").addClass("modal-footer");
	$(this.form).find(".form-btn-cancel").attr("data-dismiss", "modal");
	$(this.form).find(".form-btn-cancel").attr("aria-hidden", "true");
	
	for (var i=0; i<this.fileUploadElements.length; i++) {
		var inputElement = $(this.form).find(this.fileUploadElements[i].selector);
		var containingElement = $(inputElement).parent();
		
		_this.sourceSelector = new SchemaSourceSelector(_this, containingElement, $(inputElement).attr("id"), this.fileUploadElements[i]);
		$(inputElement).remove();
	}
	
	// Form can be destroyed after it is hidden
	$(this.form).find(".form-btn-cancel").on("click", function() {
		_this.formResetted = true;
	});
	$(this.container).on('hidden', function () {
        if (_this.formResetted==true) {
        	$(_this.container).remove();
        	_this.container = null;
        	_this.form = null;
        	if (_this.options.cancelCallback != undefined && typeof _this.options.cancelCallback == 'function') {
        		_this.options.cancelCallback();
        	}
        }
    });
	
	$(this.form).submit(function(e) { 
		_this.submit(this); 
		e.preventDefault();
		return false;
	});
	
	// Form finally presented to the user
	$(this.container).modal('show');
	
	if (_this.options.setupCallback != undefined && typeof _this.options.setupCallback == 'function') {
		_this.options.setupCallback();
	}
};

ModalFormHandler.prototype.addMessage = function(type, header, message) {
	// Build the new alert
	var msgContainer = $("<div class='alert alert-block alert-" + type + " hide'>");
	msgContainer.append("<button data-dismiss='alert' class='close' type='button'>×</button>");
	if (header!=="") { 
		msgContainer.append($("<h4>").text(header)); 
	}
	if (message!=="") {
		msgContainer.append($("<p>").text(message));
	}
	
	var _this = this;
	
	// if only the latest alert should be displayed, remove existing one(s)
	if (this.showOnlyLastestAlert==true) {
		var existing = $(this.form).find(".form-content").find(".alert");
		if ($(existing).length > 0) {
			
			$(_this.form).find(".form-content").prepend(msgContainer);
			$(existing).delay(2000).fadeOut(400, function() {$(this).remove();});
		} else {
			$(_this.form).find(".form-content").prepend(msgContainer);
		}
	} else {
		$(_this.form).find(".form-content").prepend(msgContainer);
	}
	
	// Show the new alert
	$(msgContainer).fadeIn(200);
};

ModalFormHandler.prototype.submit = function(data) {
	var _this = this;
	
	$.ajax({
        url: $(_this.form).attr("action"),
        data: $(data).serialize(),
        type: $(_this.form).attr("method"),
        dataType: "json",
        success: function(data) { _this.processSubmitResponse(data); },
        error: function(textStatus) { _this.addMessage("error", _this.translate("~*servererror.head"), 
        		_this.translate("~*servererror.body")); }
	});
};

ModalFormHandler.prototype.translate = function(placeholder) {
	for(var i=0; i<this.translations.length; i++) {
		if (this.translations[i].placeholder===placeholder) {
			if(this.translations[i].translation==undefined) {
				return this.translations[i].defaultText;
			}
			return this.translations[i].translation;
		}
	}
};

ModalFormHandler.prototype.processSubmitResponse = function(data) {
	var _this = this;
	
	if (data.success == true) {
		// successfully saved
		this.formResetted = true;
		$(this.container).modal('hide');
		modalMessage.showMessage("success", 
				_this.translate("~*importsuccessful.head"), 
				_this.translate("~*importsuccessful.body"));
		
		if (_this.options.completeCallback != undefined && typeof _this.options.completeCallback == 'function') {
			_this.options.completeCallback(data);
		}
	} else {
		var msg = _this.translate("~*validationerrors.body").format(data.errorCount);
		        		
		if (data.errors.globalErrors !== null) {
			var list = $("</ul>");
			$(data.errors.globalErrors).each(function() {list.append($("<li>").text(this));});
		}
		this.addMessage("error", _this.translate("~*validationerrors.head"), msg);
		
		$(this.form).find(".control-group").removeClass("error");
		$(this.form).find(".help-inline").remove();
		
		if (data.errors.fieldErrors !== null) {
			$(data.errors.fieldErrors).each(function() {
				$(_this.form).find("#" + this.field).closest(".control-group").addClass("error");
				var msgContainer = $(_this.form).find("#" + this.field).closest(".controls");
				$(this.errors).each(function() {
    				msgContainer.append($("<span class='help-inline'>").text(this));            				
    			});
			});
		}
	}
};


var SchemaSourceSelector = function(owner, container, modelId, options) {
	this.content = null;
	this.container = container;
	this.owner = owner;
	this.options = options;
	this.modelId = modelId;
	
	this.tmpButton = null;
	
	var _this = this;
	
	$.ajax({
        url: window.location.pathname + _this.options.formSource,
        type: "GET",
        dataType: "html",
        success: function(data) { _this.displayForm(data); },
        error: function(textStatus) { 
        	_this.owner.formResetted==true;
        	$(_this.owner).hide();
        	_this.owner.addMessage("error", _this.owner.translate("~*file.servererror.head"), 
            		_this.owner.translate("~*file.servererror.body"));
        }
	});
};

SchemaSourceSelector.prototype.displayForm = function(data) {
	this.content = $(data);
	$(this.content).attr("id", this.modelId);
	$(this.container).append(this.content);
	
	var _this = this;
	
	$(this.content).find(".fileinput-button").fileupload({
		url: window.location.pathname + _this.options.uploadTarget,
		dataType: 'json',
        add: function (e, data) { $(_this.owner.form).find(".form-btn-submit").attr("disabled", "true"); _this.handleAdd(e, data); },
        progressall: function (e, data) { _this.handleProgressAll(e, data); },
        done: function (e, data) { _this.handleDone(e, data); },
        fail: function (e, data) { _this.handleFail(e, data); }
    });
};

SchemaSourceSelector.prototype.handleAdd = function(e, data) {
	// Show the progress bar
	$(this.container).find(".fileupload-progress").show(0);
	
	// Hide the upload-file button if only one file allowed
	if (this.options.multiFiles == false) {
		this.tmpButton = $(this.content).find(".fileinput-button");
    	$(this.tmpButton).hide();
	}      	
    data.submit();
};

SchemaSourceSelector.prototype.handleProgressAll = function(e, data) {
	var progress = parseInt(data.loaded / data.total / 2 * 100, 10);
    $(this.container).find('.progress .bar').css('width', progress + '%');
    $(this.container).find('.progress-extended').text(data.loaded + " of " + data.total + "B");
};

SchemaSourceSelector.prototype.handleDone = function(e, data) {
	$(this.container).find('.progress .bar').css('width', '75%');
    $(this.container).find('.progress-extended').text("Now validating...");
	
    /* saved, id, fileType, fileSize, created, delete */
    /*$(this.container).find(".fileupload-progress").hide(0);*/
    
    var _this = this;
    
	$(_this.container).find('.fileupload-files').each(function() {
	   
		var fileTable = $("<table>");
		$(this).append(fileTable);
    
		if (data.result.success == true) {
			_this.owner.addMessage("success", _this.owner.translate("~*file.uploadcomplete.head"), 
					_this.owner.translate("~*file.uploadcomplete.body"));
			
			$.each(data.result.files, function (result, object) {
	    		// Do the validation
	    		$.ajax({
			        url: window.location.pathname + object.validateLink,
			        type: "GET",
			        dataType: "json",
			        success: function(data) { 
			        	$(_this.container).find('.progress .bar').css('width', '75%');
			        	$(_this.container).find(".fileupload-progress").hide(0);
		        		_this.owner.addMessage(data.message_type, data.message_head, data.message_body);
			        	
			        	if (data.success==true) {
			        		$(_this.owner.form).find(".form-btn-submit").removeAttr("disabled");
			        	}
			        	
			        	$(_this.container).find("input#file\\.id").attr("value", object.id);
			        },
			        error: function(textStatus) { 
			        	_this.owner.addMessage("error", _this.owner.translate("~*file.servererror.head"), 
			            		_this.owner.translate("~*file.servererror.body"));
			        }
				});
	    		
	    		// General containers
	    		var contentRow = $("<tr>");
	    		
	    		// Buttons
	    		var btnDelete = $('<span class="btn"> <i class="icon-trash icon-black"></i></button>');
	    		$(btnDelete).on('click', function() { _this.handleDelete(object, fileTable); });
	    		$("<td>").append(btnDelete).appendTo($(contentRow));
	    		
	    		// File informational area
	    		$(contentRow).append("<td>" + object.fileName + "</td>");
	    		$(contentRow).append("<td>(" + object.fileSize + ")</td>");
	    		      		
	    		$(contentRow).appendTo($(fileTable));
	    		
	    	});
			
		} else {
			_this.owner.addMessage("error", _this.owner.translate("~*file.generalerror.head"), 
	        		_this.owner.translate("~*file.generalerror.body").format(object.error));
		}
    });
};

SchemaSourceSelector.prototype.handleFail = function(e, data) {
    $(this.container).find(".fileupload-progress").hide(0);
    if (this.options.multiFiles == false) {
		$(this.tmpButton).show();
	}
    this.owner.addMessage("error", this.owner.translate("~*file.uploaderror.head"), 
    		this.owner.translate("~*file.uploaderror.body").format(data.errorThrown));
};

SchemaSourceSelector.prototype.handleDelete = function(object, fileContainer) {
	var _this = this;
	$.ajax({
        url: window.location.pathname + object.deleteLink,
        type: "GET",
        dataType: "text",
        success: function(data) { 
        	_this.owner.addMessage("error", _this.owner.translate("~*file.deletesucceeded.head"), 
            		_this.owner.translate("~*file.deletesucceeded.body"));
        	
        	$(_this.container).find("input#file\\.id").attr("value", "");
        	fileContainer.remove(); 
        	
        	$(_this.owner.form).find(".form-btn-submit").removeAttr("disabled");
        	
        	if (_this.options.multiFiles == false) {
        		$(_this.tmpButton).show();
        	}
        },
        error: function(textStatus) { 
        	_this.owner.addMessage("error", _this.owner.translate("~*file.servererror.head"), 
            		_this.owner.translate("~*file.servererror.body"));
        }
	});
};