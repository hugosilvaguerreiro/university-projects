/* GLOBAL THREE */
'use strict'
var gl1;
/* Global variables */
var globalAspectRatio = 16/9;

var pause = false;
var dead = false;

var scene, renderer, customCam;
var customCamManager;
var cam5, cam6, cam7;
var collManager;
var pathRandomizer;
var lives;

var updateList = []; /* contains every object to be updated in the update cycle except customCamera */
var inputList = []; /* contains every object to be updated in the update cycle except customCamera */

var gameTable;
//COLLIDING OBJECT LISTS
var playerCar;
var orangeList = [];
var butterList = [];
var cheerioList = [];
var CheeriosList = [];

var clock = new THREE.Clock();

var VIEWPORT = 8;

/* Event Listeners */

//camera resize
window.addEventListener( 'resize', onWindowResize, false );

//camera zoom
window.addEventListener( 'mousewheel', mouseWheelHandler, false );
window.addEventListener( 'DOMMouseScroll', mouseWheelHandler, false );

//car control
window.addEventListener( 'keydown', onKeyDown, false );
window.addEventListener( 'keyup', onKeyUp, false );


/* Event Listener Functions */
function onWindowResize() {
    renderer.setSize( getRendererWidth(), getRendererHeight() );
    customCamManager.prepareWindowResize();
}

function mouseWheelHandler(e) {
	var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail)));
	if(delta == 1) customCamManager.input("scrollUp");
	else customCamManager.input("scrollDown");
}

function onKeyUp(e) {
	var action = "";
	switch(e.keyCode) {
		case 37:
			action = "leftRelease";
			break;
		case 38:
			action = "upRelease";
			break;
		case 39:
			action = "rightRelease";
			break;
		case 40:
			action = "downRelease";
			break;
	}
	for(var i = 0; i<inputList.length && action != ""; i++) { /* notices each individual object that 'asks' to be noticed */
		if(inputList[i].input != undefined) { inputList[i].input(action); }
	}
}

function onKeyDown(e) {
	var action = "";
	switch(e.keyCode) {
		case 37:
			action = "left";
			e.preventDefault();
			break;
		case 38:
			action = "up";
			e.preventDefault();
			break;
		case 39:
			action = "right";
			e.preventDefault();
			break;
		case 40:
			action = "down";
			e.preventDefault();
			break;
		case 65:
			WIREFRAME = !WIREFRAME;
			scene.traverse(function (node) {
				if (node.isBaseObject)
					node.setWireframe(WIREFRAME);
			});
			e.preventDefault();
			break;
		case 49:
			action = "1";
			e.preventDefault();
			break;
		case 50:
			action = "2";
			e.preventDefault();
			break;
		case 51:
			action = "3";
			e.preventDefault();
			break;
		case 71: //"G"
			toggleShading();
			e.preventDefault();
			break;
		case 67: //"C"
			action = "lightsToggle";
			e.preventDefault();
			break;
		case 78: //"N"
			action = "dayNightToggle";
			e.preventDefault();
			break;
		case 76: //"L"
			action = "toggleLight";
			e.preventDefault();
			break;
		case 83: //"S"
			gamePause();
			e.preventDefault();
			break;
		case 72: //"H"
			action = "carLightsToggle";
			e.preventDefault();
			break;
		case 82: //"R"
			action = "reset";
			e.preventDefault();
			break;
		default:

	}
	for(var i = 0; i<inputList.length && action != ""; i++) { /* notifies each individual object that 'asks' to be notified */
		if(inputList[i].input != undefined) { inputList[i].input(action); }

	}
}


/* INIT */
function init() {
	renderer = new THREE.WebGLRenderer({antialias:true, alpha:true});
	renderer.setClearColor(0x000000, 0);
	renderer.setScissorTest( true );
	renderer.autoClear = false;
	renderer.setSize( getRendererWidth(), getRendererHeight() );
	document.body.appendChild(renderer.domElement);

	

	createScene();

	var cam1 = new customCamera(createOrtographicCamera(450, 0, 200, 0, globalAspectRatio), scene.position);

	var cam2 = new customCamera(createPerspectiveCamera(0, 400, 200, globalAspectRatio), scene.position);

	var cam3 = new customCamera(createPerspectiveCamera(0, 0, 0, globalAspectRatio), scene.position);
	cam3.focusOn(playerCar);
	cam3.follow(playerCar, true);
	cam3.setTransform(50, 0, 0, Math.PI/3, 0);
	cam3.manualControl();

	var temp = createOrtographicCamera(450, -10000, 50, 0, globalAspectRatio);
	temp.up = new THREE.Vector3(1,0,0);
	cam5 = new customCamera(temp, new THREE.Vector3(-10000,0,0));
 	cam5.setTransform(80, 0, 0, 0, Math.PI/2);

	var temp2 = createOrtographicCamera(500, -20000, 50, 0, globalAspectRatio);
	//temp.up = new THREE.Vector3(1,0,0);
	cam6 = new customCamera(temp2, new THREE.Vector3(-20000,0,0));
 	cam6.setTransform(80, 0, 0, 0, Math.PI/2);

 	var temp3 = createOrtographicCamera(500, -30000, 50, 0, globalAspectRatio);
	//temp.up = new THREE.Vector3(1,0,0);
	cam7 = new customCamera(temp3, new THREE.Vector3(-30000,0,0));
 	cam7.setTransform(80, 0, 0, 0, Math.PI/2);

 	customCamManager = new cameraManager(cam1, "1");
 	customCamManager.addCamera(cam2, "2");
 	customCamManager.addCamera(cam3, "3");
	inputList.push(customCamManager);

	

	render(customCamManager.getCurrentCam());
	animate();
}



/* Animation main function and update/render cycle */
function animate() {
	//if (pause) {return}
	requestAnimationFrame(animate);

	//update:
	var delta_t = clock.getDelta();

	collManager.checkAllCollisions();

	for(var i = 0; i<updateList.length; i++) { //updates each individual object
		if(updateList[i].update != undefined) { updateList[i].update(delta_t); }
	}

	customCamManager.update(delta_t);

	//render:
	render(customCamManager.getCurrentCam());
}
/* Render function */
function render(cam) {
	renderer.setClearColor( 0xffffff, 0);

	renderer.clear();
	renderer.setScissor(0 , 0, getRendererWidth(), getRendererHeight());
	renderer.render(scene, cam);
	renderer.setScissor(0 , 0, getRendererWidth(), (getRendererHeight()/VIEWPORT)*2);
	renderer.clearDepth();
	renderer.render(scene, cam5.getCamera());
	

	if(dead) {
		renderer.setScissor(0 , 0, getRendererWidth(), getRendererHeight());
		renderer.clearDepth();
		renderer.render(scene, cam6.getCamera());
	}

	if(pause && !dead) {
		renderer.setScissor(0 , 0, getRendererWidth(), getRendererHeight());
		renderer.clearDepth();
		renderer.render(scene, cam7.getCamera());
	}
}

function getRendererWidth() {
	if(window.innerWidth/window.innerHeight >= globalAspectRatio) {
		return window.innerHeight*globalAspectRatio;
	} else {
		return window.innerWidth;
	}
}

function getRendererHeight() {
	if(window.innerWidth/window.innerHeight >= globalAspectRatio) {
		return window.innerHeight;
	} else {
		return window.innerWidth/globalAspectRatio;
	}
}

function resetCameras() {
	var cam3 = new customCamera(createPerspectiveCamera(0, 0, 0, globalAspectRatio), scene.position);
	cam3.focusOn(playerCar);
	cam3.follow(playerCar, true);
	cam3.setTransform(50, 0, 0, Math.PI/3, 0);
	cam3.manualControl();

	customCamManager.addCamera(cam3, "3");
}

function resetGame() {
	pause = true;
	createScene();
	resetCameras();
	pause = false;
	requestAnimationFrame(animate);
}

function toggleShading() {
	scene.traverse(function (node) {
		console.log(node)
		if (node.isBaseObject && !(node instanceof plane))
			node.toggleMesh();
	});

}

/* Scene related functions and classes */
function createScene() {
	scene = new THREE.Scene();
	//scene.add(new THREE.AxisHelper(15));
  	var road = [];
  	road.push.apply(road, circleLine(.2, new THREE.Vector3(185,2,0), 120, -Math.PI/2, Math.PI/2 , .1));
  	road.push.apply(road, circleLine(.1, new THREE.Vector3(185,2,0), 200, -Math.PI/2, Math.PI/2 , .1));
  	road.push.apply(road, straightLine(22, new THREE.Vector3(-168,2,120), new THREE.Vector3(170,2,120), true))
  	road.push.apply(road, straightLine(22, new THREE.Vector3(-168,2,-120), new THREE.Vector3(170,2,-120), true))
  	road.push.apply(road, circleLine(.2, new THREE.Vector3(-185,2,0), 120, Math.PI/2, 3*Math.PI/2 , .1));
  	road.push.apply(road, circleLine(.1, new THREE.Vector3(-185,2,0), 200, Math.PI/2, 3*Math.PI/2 , .1));

  	road.push.apply(road, straightLine(22, new THREE.Vector3(-180,2,200), new THREE.Vector3(155,2,200), true))
  	road.push.apply(road, straightLine(22, new THREE.Vector3(-155,2,-200), new THREE.Vector3(180,2,-200), true))
  	

	cheerioList = fillPos(road);
	updateList.push.apply(updateList, cheerioList);

  	gameTable = new table(0,0, 0, 800, 450, 35, 20);

  
  	//var butter1 = new butter(-50,20,20);

  	pathRandomizer = new randomizer(400,10,225);
  	orangeList = pathRandomizer.createOranges(5, 15, 10);
  	butterList = pathRandomizer.createButters(5, 10, 20, 15,20);

  	//expoCars();
  	var lives = new lifeBar(new THREE.Vector3(-10000 + 450/2, 5, -800/2), 5, 5);
	inputList.push(lives);
	scene.add(lives);

  	var deadPlane = new  plane(new THREE.Vector3(-20000,5,0),401, 100, 0x55AAFF, deadTexture);
	scene.add(deadPlane);

  	var pausePlane = new  plane(new THREE.Vector3(-30000,5,0),401, 100, 0x55AAFF, pauseTexture);
	scene.add(pausePlane);
  	playerCar = new car(0,5,150,5);
  	playerCar.addLives(lives);
  	playerCar.setInitialRotation(0, Math.PI, 0);
  	scene.add(playerCar);
	updateList.push(pathRandomizer);	
	
	updateList.push(playerCar);
	inputList.push(playerCar);

	collManager = new collisionManager(playerCar, cheerioList, orangeList, butterList);
	inputList.push(collManager);
	make_cheerios_example();


	var globallight = new globalLight(new THREE.Vector3(0,50,0), 1);

	var candle1 = new candle(new THREE.Vector3(0,0,80), 5);
	var candle2 = new candle(new THREE.Vector3(-220,0,80), 5);
	var candle3 = new candle(new THREE.Vector3(220,0,80), 5);
	var candle4 = new candle(new THREE.Vector3(220,0,-80), 5);
	var candle5 = new candle(new THREE.Vector3(-220,0,-80), 5);
	var candle6 = new candle(new THREE.Vector3(0,0,-80), 5);


	scene.add(candle1);
	scene.add(candle2);
	scene.add(candle3);
	scene.add(candle4);
	scene.add(candle5);
	scene.add(candle6);


	inputList.push(candle1);
	inputList.push(candle2);
	inputList.push(candle3);
	inputList.push(candle4);
	inputList.push(candle5);
	inputList.push(candle6);
	inputList.push(globallight);

	//var livesPlane = new plane(new THREE.Vector3(-10000,5,0),140, 100, 0x55AAFF, lifeTexture);
	//scene.add( livesPlane );



}

function make_cheerios_example() {
	/*
	var cheerio1 = new cheerio(new THREE.Vector3(60,2,0), 5, 3, 14, 14, 10);
	scene.add(cheerio1);
	cheerioList.push(cheerio1);
	updateList.push(cheerio1);
	
	var cheerio2 = new cheerio(new THREE.Vector3(30,2,0), 5, 3, 14, 14, 10);
	scene.add(cheerio2);
	cheerioList.push(cheerio2);
	updateList.push(cheerio2);
	
	var cheerio3 = new cheerio(new THREE.Vector3(0,2,0), 5, 3, 14, 14, 10);
	scene.add(cheerio3);
	cheerioList.push(cheerio3);
	updateList.push(cheerio3);
	
	var Wheel = new wheel(0, 2, 0, 5);
	scene.add(Wheel);

	var Dome = new dome(0, 10, 0, 5);
	scene.add(Dome);
	
	var axleAndWheel = new axleAndWheel(0, 10, 0, 5);
	axleAndWheel.setRotation(0,0,0);
	scene.add(axleAndWheel);
	*/
	
}

function expoCars() {
	var expoCar0 = new car(12,5,-46,5);
	var expoCar1 = new car(25,5,-25,5);
	var expoCar2 = new car(30,5,0,5);
	var expoCar3 = new car(25,5, 25,5);
	var expoCar4 = new car(12,5, 46,5);
  	expoCar0.setInitialRotation(0, Math.PI/4, 0);
  	expoCar1.setInitialRotation(0, Math.PI/8, 0);
  	expoCar3.setInitialRotation(0, -Math.PI/8, 0);
  	expoCar4.setInitialRotation(0, -Math.PI/4, 0);
  	scene.add(expoCar0);
  	scene.add(expoCar1);
  	scene.add(expoCar2);
  	scene.add(expoCar3);
  	scene.add(expoCar4);

}
function gamePause() {
	if(!pause) {
		pause = true;
		clock.stop();
	} else {
		pause = false;
		clock.start();
	}
}