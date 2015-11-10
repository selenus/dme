var Connection = function(template, from, to, id) {
	this.template = template;
	this.from = from;
	this.active = false;
	this.id = id;
		
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