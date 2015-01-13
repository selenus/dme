var ModelViewRefresher = function() {
	this.options = {
		refreshCycle: 5000,
		refreshUrlSuffix: "/ajax/refresh",
		newRowUrlSuffix: "/ajax/loadRow"
	};
	this.container = null;
	
	var _this = this;
	
	this.lock = false;
	
	setTimeout(function() { _this.cycleRefresh(); }, _this.options.refreshCycle);
};

ModelViewRefresher.prototype.cycleRefresh = function() {
	this.refreshModel();
	var _this = this;
	setTimeout(function() { _this.cycleRefresh(); }, _this.options.refreshCycle);
};

ModelViewRefresher.prototype.refreshModel = function() {

	if (this.lock==true) {
		return;
	}
	this.lock = true;
	
	// Currently displayed model items (their id and timestamp only)
	var items = [];
	var _this = this;
	
	this.container = $("table.model-list tbody");
	
	$(this.container).find('tr').each(function() {
		var rowId = this.id;
		var rowTimestamp = "";
		
		if (rowId != null && rowId.indexOf("model_id_") != -1)
		{
			$(this).find('td').each(function() {
				if (this.id === rowId + ".lookupTimestamp") {
					rowTimestamp = $.trim($(this).text());
				}
			});
			items.push({ 
				id: rowId.replace(/model_id_/g, ""), 
				modified: rowTimestamp
			});
		}
	});
	
	$.ajax({
        url: window.location.pathname + _this.options.refreshUrlSuffix,
        type: "POST",
        data: {items: JSON.stringify(items) },
        dataType: "json",
        beforeSend: function(x) {
            if (x && x.overrideMimeType) {
              x.overrideMimeType("application/json;charset=UTF-8");
            }
        },
        success: function(data) { _this.handleRefreshSuccess(data); _this.lock = false; },
        error: function(data) { _this.lock = false; }
  });
};

ModelViewRefresher.prototype.handleRefreshSuccess = function(data) {
	var _this = this;
	
	jQuery.each(data, function(index, object) {
		if (object.value=="nochange") {
			return;
		}
		var id = object.members.id;
		if (object.members.status=="add") {
			_this.handleAddedObject(id);
		} else if (object.members.status=="delete") {
			_this.handleDeletedObject(id);
		} else {
			_this.handleUpdatedObject(id, object);
		}
	});
};

ModelViewRefresher.prototype.handleUpdatedObject = function(id, object) {
	for(var attr in object.members){
        var attrValue = object.members[attr];
        itemId = "#model_id_" + id + "\\." + attr;
       	                    
        // TODO: This is where switches for special treatment of attributes are specified
        if (attr == "actions") {
        	if (attrValue=="TRUE") {
        		$(itemId).attr("class", "actions");
        	} else {
        		$(itemId).attr("class", "actionsLocked");
            }	                    	
        } else if (attr == "state") {
        	
        	if (attrValue==9) {
        		$("tr" + "#model_id_" + id).addClass("error");
        	} else {
        		$("tr" + "#model_id_" + id).removeClass("error");
        	}
        	
        	var compId = "model_id_" + id + "." + attr + "." + attrValue;
        	$(itemId).find("i").each(function() {
        		if ($(this).attr("id")==compId) {
        			$(this).removeClass("hidden");
        		} else {
        			$(this).addClass("hidden");
        		}
        	});               	
        } else if (attr == "lookupTimestamp") {
        	$(itemId).text(attrValue);
        } else {
        	$(itemId).find("a").html(attrValue);
        }   
	}	
};

ModelViewRefresher.prototype.handleAddedObject = function(id) {
	var _this = this;
	$.ajax({
		url : window.location.pathname + _this.options.newRowUrlSuffix,
		type : "GET",
		data : { id : id },
		success : function(data) { $(_this.container).append(data); }
	});
};

ModelViewRefresher.prototype.handleDeletedObject = function(id) {
	$(this.container).find('tr#model_id_' + id).remove();
};