var blob;
var blobs = [];


function setup() {
	createCanvas(600, 600);
	blob = new Blob(0, 0, 64);
	for(var i = 0; i < 50; i++){
		var x = random(-width, width)
		var y = random(-height, height)
		blobs[i] = new Blob(x, y, 16);
	}
}

function draw() {
	background(0);
	translate(width/2-blob.pos.x, height/2-blob.pos.y)
	blob.show();
	blob.update();
	for(var i = 0; i < blobs.length; i++){
		blobs[i].show();
	}
}
