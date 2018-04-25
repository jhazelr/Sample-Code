
var player; 
var cursors; 
var clientID;
var positionInfo = [];
var players = {};
var collisions = {};
var lock = 1;
var modelList = [];
var scale = 0.9;
var game = new Phaser.Game(800, 700, Phaser.AUTO, '', { preload: preload, create: create, update: update, render: render });
var newPosition = [-1000, -1000];

function setNewPostion(x, y, pClass) {
	player.x = x;
	player.y = y;
	player.class = pClass;
}
var socket;

function preload() {
	game.stage.backgroundColor = '#85b5e1'; 
	//game.load.baseURL = 'http://examples.phaser.io/assets/'; 
	game.load.crossOrigin = 'anonymous'; 

	game.load.image('food', 'Images/food.png')
	game.load.image('player', 'Images/minnow.png');
	game.load.image('player2', 'Images/shark.png');
	//game.load.image('background', 'http://examples.phaser.io/assets/sprites/splat.png'); 

	game.load.image('background', 'Images/background.png'); 

	playerModel = 'player';
	sharkModel = 'player2'; 
	foodModel = 'food';
	modelList.push(foodModel);
	modelList.push(playerModel); 
	modelList.push(sharkModel);
}

function initHandlers(s) {
	s.on('clientconnected', function( data ) {
		clientID = data.id;
		newPosition[0] = data.x;
		newPosition[1] = data.y;
		lock = 0
		console.log( 'Connected successfully to the socket.io server. My server side ID is ' + data.id );
	});
	s.on('update', function( data) {
		positionInfo = data.msg; 
	})
	s.on('playerDisconnect', function(data) {
		players[data.userid].destroy();
		delete players[data.userid];
	});
	s.on('movePlayer', function(data) {
		console.log("moving player to new location")
		setNewPostion(data.x, data.y, data.class);
	})

	s.on("classChange", function(data){
		player.class = data.class;
	})
}

function create() {
	socket = io.connect('/');
	initHandlers(socket);
	
	game.add.tileSprite(0, 0, 1920, 1920, 'background');

	game.world.setBounds(0, 0, 1920, 1920);

	player = game.add.sprite(newPosition[0], newPosition[1], 'player');
	game.physics.arcade.enable(player);
	player.scale.setTo(scale,scale);

	player.anchor.setTo(0.5, 0.5)
	player.body.collideWorldBounds = false;
	game.physics.startSystem(Phaser.Physics.ARCADE);
	game.camera.follow(player); 
  
	player.body.allowRotation = false;
	
	game.physics.enable(player, Phaser.Physics.ARCADE);
	 
}

function setModel(xVal, yVal, modelName) {
	if (modelName == playerModel) {
		return;
	}
	player.destroy();
	playerModel = modelName;
	player = game.add.sprite(player.x, player.y, playerModel);
	game.physics.arcade.enable(player);
	player.body.collideWorldBounds = false;
	game.camera.follow(player);
	player.body.allowRotation = false;
	game.physics.enable(player, Phaser.Physics.ARCADE);
}

function update() {
	player.rotation = game.physics.arcade.moveToPointer(player, 200, game.input.activePointer); 
	socket.emit("translate", {rotation: player.rotation, x: player.x, y: player.y});
	for (i = 0; i < positionInfo.length; i++) {
		info = positionInfo[i]
		if (info["userid"] == clientID) {
			player.loadTexture(modelList[player.class])
			player.foodCount = info["foodCount"];
			player.timer = info["timeout"];
			player.playersEaten = info["playersEaten"];
		}
		else {
			if (!(info["userid"] in players)) {
				console.log("adding player sprite")
				players[info["userid"]] = game.add.sprite(-1000, -1000, playerModel);
				players[info["userid"]].scale.setTo(scale,scale);
				players[info["userid"]].anchor.setTo(0.5, 0.5);
				players[info["userid"]].isColliding = 0
			}
			players[info["userid"]].rotation= info["rotation"]; 
			players[info["userid"]].x = info["x"];
			players[info["userid"]].y = info["y"];
			players[info["userid"]].loadTexture(modelList[info["class"]]);
			if(checkOverlap(player, players[info["userid"]])) {
				//send message to server regarding collision
				if (!(players[info["userid"]].isColliding)) {
					players[info["userid"]].isColliding = 1;
  					socket.emit("collision", {object: info}); 
  					setTimeout(allowCollision, 1000, info["userid"])
  				}
			}
		}
	}
}


function allowCollision(userid) {
	players[userid].isColliding = 0
}


function checkOverlap(spriteA, spriteB) {
	var boundsA = spriteA.getBounds(); 
	console.log(spriteA);
	var boundsB = spriteB.getBounds(); 
	return Phaser.Rectangle.intersects(boundsA, boundsB); 
}

function render() {
	game.debug.text("Food Count: "+ player.foodCount, 20, 20);
	if(player.class == 2) {
		game.debug.text("Time-to-Live: " + player.timer/1000, 20, 60);
		game.debug.text("Minnows Eaten: " + player.playersEaten, 20, 40)
	}
}


