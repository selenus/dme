var Connection = function(template, from, to) {
	this.template = template;
	this.template.init(this);
	this.from = from;
	this.active = false;
	
	if (to!==undefined && to!=null) {
		if (to instanceof Array) {
			this.to = to;
		} else {
			this.to = [];
			this.to.push(to);
		}
	} else {
		this.to = [];
		this.to.p
	}
}

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

Connection.prototype.addTo = function(to) {
	if (to!==undefined && to!=null) {
		if (to instanceof Array) {
			for (var i=0; i<to.length; i++) {
				this.to.push(to[i]);
			}
		} else {
			this.to.push(to);
		}
	}
};

Connection.prototype.paint = function(context) {
	this.template.paint(this, context);
};