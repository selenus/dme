var Connection = function(template, from, to, id) {
	this.template = template;
	this.active = false;
	this.id = id;
		
	if (from!==undefined && from!=null) {
		if (from instanceof Array) {
			this.from = from;
		} else {
			this.from = [];
			this.from.push(from);
		}
		for (var i=0; i<this.from.length; i++) {
			this.from[i].addConnection(this);
		}
	} else {
		this.from = [];
	}
	
	if (to!==undefined && to!=null) {
		if (to instanceof Array) {
			this.to = to;
		} else {
			this.to = [];
			this.to.push(to);
		}
		for (var i=0; i<this.to.length; i++) {
			this.to[i].addConnection(this);
		}
	} else {
		this.to = [];
	}
	
	this.template.init(this);
}

Connection.prototype.clearMovedForkPoint = function() {
	if (this.movedForkPoint!==undefined) {
		this.movedForkPoint=null;
	}
};

Connection.prototype.getId = function() {
	return this.id;
};

Connection.prototype.isSelected = function() {
	return this.selected;
};

Connection.prototype.getContextMenuItems = function() {
	return this.template.getContextMenuItems(this);
};

Connection.prototype.setSelected = function(selected) {
	this.selected = selected;
};

Connection.prototype.setActive = function(active) {
	this.active = active;
};

Connection.prototype.getCursor = function() {
	return Cursors.arrow;
};

Connection.prototype.hitTest = function(point) {
	return this.template.hitTest(this, point);
};

Connection.prototype.getRectangle = function() {
	return this.template.getRectangle(this);
};

Connection.prototype.addFrom = function(from) {
	if (from!==undefined && from!=null) {
		if (from instanceof Array) {
			for (var i=0; i<from.length; i++) {
				this.from.push(from[i]);
				from[i].addConnection(this);
			}
		} else {
			this.from.push(from);
			from.addConnection(this);
		}
	}
};

Connection.prototype.addTo = function(to) {
	if (to!==undefined && to!=null) {
		if (to instanceof Array) {
			for (var i=0; i<to.length; i++) {
				this.to.push(to[i]);
				to[i].addConnection(this);
			}
		} else {
			this.to.push(to);
			to.addConnection(this);
		}
	}
};

Connection.prototype.paint = function(context) {
	this.template.paint(this, context);
};