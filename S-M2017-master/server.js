var io = require('socket.io');
var express = require('express');
var UUID = require('node-uuid');
var http = require('http');


var game = express();
var server = http.createServer(game);
var verbose = false;

var port = process.env.PORT || 3000;
var host = "137.112.235.178" // change to local ip when running server
server.listen(port, host);
var sio = io.listen(server)


game.get( '/', function( req, res ){
	console.log('trying to load %s', __dirname + '/Index.html');
	res.sendFile( '/Index.html' , { root:__dirname });
});

game.get( '/*' , function( req, res, next ) {

    //This is the current file they have requested
	var file = req.params[0];

	//For debugging, we can track what files are requested.
	if(verbose) console.log('\t :: Express :: file requested : ' + file);

	//Send the requesting client the file.
	res.sendFile( __dirname + '/' + file );

});

clients = []
collisions = {}
var MAXFOOD = 50;
var currentFood = 0; 


function handleCollision(client, other) { 
	var position = generatePosition();
	
	if (client.info["class"] == 1 &&  other["class"] == 2) {
		client.info["timeout"] = 0;
		console.log(other["userid"] + " has eaten " +client.info["userid"]);
		resetPlayer(client);
	}
	else if (client.info["class"] == 2 && other["class"] == 1 ) {
		client.info["timeout"] = client.info["timeout"] + 5000; 
		client.info["playersEaten"]++;
		console.log(client.info["userid"] + " gets more time to live for eating player " + other["userid"])
	}

	else if (other["class"] == 0) {
		//food eaten
		for (i =clients.length -1; i >=0; i--) {
			if (clients[i]["userid"] == other["userid"]) {
				setTimeout(makeFood, 5000, i); 
				clients[i].x = -500;
				clients[i].y = -500;

				client.info["foodCount"]++;
				break
			}
		}
		//minnow eats carrot
		if (client.info["class"] == 1) {
			console.log(client.info["userid"] + " a minnow, has eaten a carrot")
		}
		//minnow becomes shark
		if(client.info["foodCount"] == 20) {
			client.info["class"] = 2;
			console.log("player has become a shark")
			client.emit('classChange', {class: 2});
			client.info["timeout"] = 30000; 
			client.info["playersEaten"] = 0;
			setTimeout(resetPlayer, 100, client);
			
		}
		//shark eats carrot
		if (client.info["class"] == 2) {
			client.info["timeout"] = client.info["timeout"] + 500
			console.log(client.info["userid"] + " a shark, has eaten a carrot")
		}

	}
	client.emit("collisionHandled", {userid: other["userid"]});
}

function makeFood(index) {
	var position = generatePosition()
	clients[index].x = position[0]
	clients[index].y = position[1]
}

function resetPlayer(client) {
	if (client.info["timeout"] !=0) {
		client.info["timeout"] -= 100;
		setTimeout(resetPlayer, 100, client)
	} else {
		var position = generatePosition();
		
		client.info["foodCount"] = 0;
		client.info["class"] = 1;
		client.emit('movePlayer', {x: position[0], y: position[1], class:1});
	}
}

function generateFood() { 
	while (currentFood < MAXFOOD) {
		info = {}; 
		info["userid"] = UUID(); 
		info["class"] = 0; 
		var position = generatePosition();
		info["x"] = position[0]; 
		info["y"] = position[1]; 
		info["rotation"] = 0;
		clients.push(info);
		currentFood++;
	}
}

function broadcastPostions() {
	sio.sockets.emit('update', {msg: clients}); 
}

function generatePosition() {
	var x = Math.floor(Math.random()*1920); 
	var y = Math.floor(Math.random()*1920); 
	return [x, y];
}

sio.sockets.on('connection', function(client) {
	console.log(clients.length)
	client.info = {};
	client.info["userid"] = UUID();
	client.info["class"] = 1;
	client.info["foodCount"] = 0;
	client.info["timeout"] = 0;
	client.info["playersEaten"] = 0;
	var position = generatePosition();
	client.emit('clientconnected', { id: client.info["userid"]});
	//each client starts as a fish
	client.emit('movePlayer', {x: position[0], y: position[1], class: client.info["class"]});
	console.log('\t socket.io:: player ' + client.info["userid"] + ' connected');
	clients.push(client.info);
	client.on('translate', function(data) {
		client.info["rotation"] = data.rotation;
 		client.info["x"] = data.x
		client.info["y"] = data.y 

	})	
	client.on('disconnect', function() {
		for (i =clients.length -1; i >= 0; i--) {
			if (clients[i]["userid"] == client.info["userid"]) {
				sio.sockets.emit("playerDisconnect", {userid: clients[i]["userid"]});
				clients.splice(i, 1);
			}
		}
		console.log('\t socket.io:: player ' + client.info["userid"] + ' disconnected');
	});
	client.on('collision', function(data) {
		handleCollision(client, data.object);
	})

});
generateFood()
setInterval(broadcastPostions, 10);