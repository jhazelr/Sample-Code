/**
 * Generated from the Phaser Sandbox
 *
 * //phaser.io/sandbox/lfkeOkMd
 *
 * This source requires Phaser 2.6.2
 */

var game = new Phaser.Game(800, 600, Phaser.AUTO, '', { preload: preload, create: create, update: update, render: render });

function preload() {

    game.stage.backgroundColor = '#85b5e1';

    game.load.baseURL = 'http://examples.phaser.io/assets/';
    game.load.crossOrigin = 'anonymous';

    game.load.image('player', 'sprites/wabbit.png');
    game.load.image('background','tests/debug-grid-1920x1920.png');
    

}

var player;
var platforms;
var cursors;
var jumpButton;

function create() {

    game.add.tileSprite(0, 0, 1920, 1920, 'background');

    game.world.setBounds(0, 0, 1920, 1920);
    
    player = game.add.sprite(game.world.centerX, game.world.centerY, 'player');

    game.physics.arcade.enable(player);
    player.anchor.setTo(0.5, 0.5)

    player.body.collideWorldBounds = false;

    game.physics.startSystem(Phaser.Physics.ARCADE);
    game.camera.follow(player); 
    
    player.body.allowRotation = false;
    
    game.physics.enable(player, Phaser.Physics.ARCADE);
    
    

}

function update () {
    player.rotation = game.physics.arcade.moveToPointer(player, 60, game.input.activePointer, 600, 600);
}

function render () {
   
}
