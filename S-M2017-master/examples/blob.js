function Blob(x, y, size){
	this.pos = createVector(x, y);
	this.r = size;
	
	this.update = function(){
		var mouse = createVector(mouseX - width/2, mouseY - height/2);
		mouse.setMag(3);
		this.pos.add(mouse);
	}
	
	this.show = function(){
		fill(255);
		ellipse(this.pos.x,this.pos.y,this.r*2, this.r*2)
	}
}

