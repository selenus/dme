var SvgViewer = function(container, content) {
	this.container = container;
	this.content = content;
	
	this.svgid = Math.floor(Math.random() * 100000);
	
	this.svgContainerSelector = ".inner-svg-container";
	
	this.zoomInButton = ".btn-svg-zoomin";
	this.zoomOutButton = ".btn-svg-zoomout";
	this.resetButton = ".btn-svg-reset";
	this.maximizeButton = ".btn-svg-newwindow";
	
	this.maximizeSvgPath = __util.getBaseUrl() + "common/forms/maximizeSvg";
	
	this.maximizedHeight = $(window).height() - 300;
	this.maximizedSvgContainerSelector = ".maximized-svg-container";

	this.init();
};

SvgViewer.prototype.init = function() {
	var _this = this;
	
	var svg = $(this.content);
	svg.prop("id", "svg_" + this.svgid);
	
	$(this.container).find(this.svgContainerSelector).html(svg);
	
	var panZoom = svgPanZoom("#svg_" + this.svgid, {
		fit: false,
		center: false
	});
	
	$(this.container).find(this.zoomInButton).click(function() {
		panZoom.zoomIn(); return false;
	});
	$(this.container).find(this.zoomOutButton).click(function() {
		panZoom.zoomOut(); return false;
	});
	$(this.container).find(this.resetButton).click(function() {
		panZoom.reset(); return false;
	});
	$(this.container).find(this.maximizeButton).click(function() {
		_this.maximizeView(); return false;
	});
};

SvgViewer.prototype.destroy = function(selector, svgid) {
	if ($("#svg_" + this.svgid).length) {
		svgPanZoom("#svg_" + this.svgid).destroy();
		
		$(this.container).find(this.svgContainerSelector).text("");
		$(this.container).find("button").off();
	}
};

SvgViewer.prototype.maximizeView = function() {
	var _this = this;
	var form_identifier = "max-parsed-input";	
	var content = $(this.container).find(this.svgContainerSelector).html();
	
	modalFormHandler = new ModalFormHandler({
		formFullUrl: _this.maximizeSvgPath,
		identifier: form_identifier,
		translations: [{placeholder: "~*servererror.head", key: "~eu.dariah.de.minfba.common.view.forms.servererror.head"},
		                {placeholder: "~*servererror.body", key: "~eu.dariah.de.minfba.common.view.forms.servererror.body"}
		                ],
		setupCallback: function(modal) {
			$(modal).find(_this.maximizedSvgContainerSelector).height(_this.maximizedHeight + "px");
		},
		displayCallback: function() { 
			new SvgViewer(_this.maximizedSvgContainerSelector, content);
		},
		additionalModalClasses: "max-modal",
	});
	modalFormHandler.show(form_identifier);
}



