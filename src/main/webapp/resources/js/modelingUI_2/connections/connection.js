var Connection = function(template, from, to) {
	this.template = template;
	this.from = from;
	
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