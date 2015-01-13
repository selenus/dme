var annotationViewer = null;

$(document).ready(function() {
	annotationViewer = new AnnotationViewer({
		additionalModalClasses: "wider-modal"
	});
});

var AnnotationViewer = function(options) {
	this.pathname = window.location.pathname;
	this.options = options;
};

AnnotationViewer.prototype.getByAnnotatedObject = function(type, id) {
	this.getAsync("/getUserAnnotationsByObject", type, id);
};

AnnotationViewer.prototype.getByAggregatorObject = function(type, id) {
	this.getAsync("/getUserAnnotationsByAggregator", type, id);
};

AnnotationViewer.prototype.getAsync = function(method, type, id) {
	var _this = this;
	
	this.type = type;
	this.id = id;
	
	$.ajax({
		url: _this.pathname + method,
        type: "GET",
        data: {type: type, id: id},
        dataType: "text",
        beforeSend: function(x) {
            if (x && x.overrideMimeType) {
              x.overrideMimeType("application/json;charset=UTF-8");
            }
        },
        success: function(data) {
        	_this.content = $(data);
        	_this.setUp();
        },
        error: function(textStatus) { }
 	});
};

AnnotationViewer.prototype.setUp = function() {
	var _this = this;
	this.container = $("<div class='modal hide fade'>");
		
	$(this.content).find(".user-annotation-entry").each(function() {
			$(this).css("width", "315px");
		}
	);
	
	if (this.options.additionalModalClasses!==null) {
		$(this.container).addClass(this.options.additionalModalClasses);
	}
	
	$(this.container).html($(this.content));
	
	// Form can be destroyed after it is hidden
	$(this.content).find(".form-btn-cancel").on("click", function() {
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
	
	$(this.content).find(".form-btn-submit").click(function(e) { 
		_this.formResetted = true;
		$(_this.container).modal('hide');

		_this.askForComment();
		if (_this.options.completeCallback != undefined && typeof _this.options.completeCallback == 'function') {
			_this.options.completeCallback(data);
		}
	});
	
	$(this.container).modal('show');
};

AnnotationViewer.prototype.askForComment = function() {
	var _this = this;
	
	modalFormHandler = new ModalFormHandler({
		formUrl: "/ajax/getCommentForm?annotatedObjectType=" + _this.type + "&annotatedObjectId=" + _this.id,
		translationsUrl: "/ajax/getTranslations",
		identifier: "comment-for" + _this.type + "-" + _this.id,
		additionalModalClasses: "narrow-modal",
	});
	
	modalFormHandler.show("comment-for" + _this.type + "-" + _this.id);
	
};
