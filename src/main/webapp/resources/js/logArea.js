var LogArea = function(config) {
	this.options = {
			maxLogEntries: 50,
			autoRefresh: 0,				// in ms; 0 to disable auto-refresh
			logList: $("ul#schema-editor-log"),
			pathPrefix : null				// must be set when used
	};
	$.extend(true, this.options, config);
	this.tsMax = null;
	this.initLogging();
};

LogArea.prototype.initLogging = function() {
	if (this.options.pathPrefix===null || this.options.pathPrefix===undefined) {
		throw "pathPrefix setting must be provided in options";
	}
	this.refresh();
	this.autoRefresh();
};

LogArea.prototype.autoRefresh = function() {
	var _this = this;
	if (this.options.autoRefresh > 0) {
		setTimeout(function() { _this.refresh(); _this.autoRefresh(); }, _this.options.autoRefresh);
	};
};

LogArea.prototype.refresh = function() {
	var _this = this;
	$.ajax({
	    url: _this.options.pathPrefix + "/async/getLog",
	    type: "GET",
	    data: { 
	    	maxEntries : _this.options.maxLogEntries,
	    	tsMin: _this.tsMax
	    },
	    dataType: "json",
	    success: function(data) {
	    	if (data!=null && data!=undefined && Array.isArray(data) && data.length>0) {
	    		_this.showLog(data);
	    	}
	    }, error: function(jqXHR, textStatus, errorThrown ) {
	    	alert("error2");
	    }
	});
};

LogArea.prototype.showLog = function(data) {
	if (this.tsMax!=null && data.length>0 && data[0].numericTimestamp<=this.tsMax) {
		return;
	}
	$(this.options.logList).html("");
	for (var i=0; i<data.length; i++) {
		if (i==0) {
			this.tsMax = data[i].numericTimestamp;
		} else if (i>=this.options.maxLogEntries-1) {
			break;
		}
		$(this.options.logList).append("<li class=\"log-" + data[i].logType + "\">" + data[i].displayTimestamp + " " + data[i].logType + " " + data[i].message + "</li>");
	}
};
