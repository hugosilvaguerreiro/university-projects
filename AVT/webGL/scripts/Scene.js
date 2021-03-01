/**
 *	Frogger.Scene.js
 */

var camera3, camera1, camera, camera2, followCamera, controls, scene, renderer, id, factory, keyboard, player, collisionDetector, directionalLight, globalLight;
var cameraOldPos, playerOldPos, side;
var sound = [];

var clock = new THREE.Clock();

var xLimits = [48, 148];
var yLimits = [2.5, 8];	//jump limits
var yLimitsWater = [6.5, 12];	//jump limits
var zLimits = [-70, 65];

var lookAtScene = true, perspectiveCamera = true;

var effect;
var tmpRenderer;

var allStreetLights;
var globalLightOn;

var spotLight;
var globalLight = true;
var streetLights = true;

var billboards;

//Physics
var onGround = false;
var jumped = false;
var gravity = 0.5;
var speed = { x: 1.0, y: 0.0, z: 0.8, hx: 5.0, hy: 0.0, hz: 1.0 };


//Particles
var clock = new THREE.Clock();
var flakeCount = 2000;
var flakeGeometry = new THREE.TetrahedronGeometry(0.1); // radius
var flakeMaterial = new THREE.MeshPhongMaterial({ color: 0xffffff });
var snow = new THREE.Group();
var flakeArray;
var particlesOn = false;

//FOG
var FOG;
var FOGCOLOR;


//MIRROR

function createMirror() {
    var planeGeo = new THREE.PlaneBufferGeometry( 100.1, 100.1 );
    var geometry = new THREE.PlaneBufferGeometry( 60, 150 );
    var groundMirror = new Reflector( geometry, {
        clipBias: 0.003,
        textureWidth: window.innerWidth * window.devicePixelRatio,
        textureHeight: window.innerHeight * window.devicePixelRatio,
        color: 0x777777,
        recursion: 2
    } );
    groundMirror.position.x = 80;
    groundMirror.position.y = 1.4;
    groundMirror.rotateX( - Math.PI / 2 );
    scene.add( groundMirror );

}

var ISLAND;
var ISLAND_POS = [-300, 10, -40];
function createIsland() {
    
    var path = "models/island1/";
    var objLoader = new THREE.OBJLoader();
    objLoader.setPath(path);
    objLoader.load('island1_design2_c4d.obj', function(obj){
        ISLAND = obj;
        /*ISLAND.traverse(function(node) { 
            if(node instanceof THREE.Mesh) { 
                node.castShadow = true; 
                node.receiveShadow = false;
            } 
        });*/
        ISLAND.position.set(ISLAND_POS[0], ISLAND_POS[1], ISLAND_POS[2]);
        ISLAND.scale.set(0.5, 0.5, 0.5);

        var texture = new THREE.TextureLoader().load( 'models/island1/textureSurface_Color_2.jpg' );
        // immediately use the texture for material creation
        var material = new THREE.MeshPhongMaterial( { map: texture } );

        ISLAND.traverse(function (child) {
            if (child instanceof THREE.Mesh) {
        
                // apply texture
                child.material = material;
                child.material.needsUpdate = true;
            }
        })       
        scene.add(ISLAND); 
    
    
    });
}

var BASE;
function createBase() {
    var path = "models/base/";

    var mtlLoader = new THREE.MTLLoader();
    mtlLoader.setPath(path);
    mtlLoader.load('base.mtl', function(materials) {
        materials.preload();
        var objLoader = new THREE.OBJLoader();
        objLoader.setMaterials(materials);
        objLoader.setPath(path);
        objLoader.load('base.obj', function(obj){
            BASE = obj;
            /*ISLAND.traverse(function(node) { 
                if(node instanceof THREE.Mesh) { 
                    node.castShadow = true; 
                    node.receiveShadow = false;
                } 
            });*/
            BASE.position.set(115, -23, 2);
            BASE.scale.set(32, 20, 32);
            BASE.rotateY(Math.PI/4);
    
            // immediately use the texture for material creation
    
            scene.add(BASE); 
        
        
        });
    });
}

var ISLAND2;
var ISLAND2_POS = [0,0,30];
function createIsland2() {
    var path = "models/island2/";

    var mtlLoader = new THREE.MTLLoader();
    mtlLoader.setPath(path);
    mtlLoader.load('island2.mtl', function(materials) {
        materials.preload();
        var objLoader = new THREE.OBJLoader();
        objLoader.setMaterials(materials);
        objLoader.setPath(path);
        objLoader.load('island2.obj', function(obj){
            ISLAND2 = obj;
            /*ISLAND.traverse(function(node) { 
                if(node instanceof THREE.Mesh) { 
                    node.castShadow = true; 
                    node.receiveShadow = false;
                } 
            });*/
            ISLAND2.position.set(
                ISLAND2_POS[0],
                ISLAND2_POS[1],
                ISLAND2_POS[2]
            );
            ISLAND2.scale.set(1, 1, 1);    
            // immediately use the texture for material creation
    
            scene.add(ISLAND2); 
        
        
        });
    });
}

var ISLAND3;
var ISLAND3_POS = [-300, 0, 0];
function createIsland3() {
    var path = "models/island3/";

    var mtlLoader = new THREE.MTLLoader();
    mtlLoader.setPath(path);
    mtlLoader.load('island3.mtl', function(materials) {
        materials.preload();
        var objLoader = new THREE.OBJLoader();
        objLoader.setMaterials(materials);
        objLoader.setPath(path);
        objLoader.load('island3.obj', function(obj){
            ISLAND3 = obj;
            ISLAND3.position.set(
                ISLAND3_POS[0],
                ISLAND3_POS[1],
                ISLAND3_POS[2]
            );
            ISLAND3.scale.set(13, 13, 13);    
            // immediately use the texture for material creation
    
            scene.add(ISLAND3); 
        
        
        });
    });
}


var CLOUD1;
var CLOUD1_POS = [-500, 30, 80];
function createCloud1() {
    var path = "models/nuvens/";

    var mtlLoader = new THREE.MTLLoader();
    mtlLoader.setPath(path);
    mtlLoader.load('nuvens1.mtl', function(materials) {
        materials.preload();
        for (let key in materials.materials) {
            materials.materials[key].transparent = true;
            materials.materials[key].opacity = 0.5;
          }

        var objLoader = new THREE.OBJLoader();
        objLoader.setMaterials(materials);
        objLoader.setPath(path);
        objLoader.load('nuvens1.obj', function(obj){
            CLOUD1 = obj;
            CLOUD1.position.set(
                CLOUD1_POS[0],
                CLOUD1_POS[1],
                CLOUD1_POS[2]
            );
            CLOUD1.scale.set(4, 4, 4);    
            // immediately use the texture for material creation
    
            scene.add(CLOUD1); 
        
        
        });
    });
}


var CLOUD2;
var CLOUD2_POS = [-500, 30, 50];
function createCloud2() {
    var path = "models/nuvens/";

    var mtlLoader = new THREE.MTLLoader();
    mtlLoader.setPath(path);
    mtlLoader.load('nuvens2.mtl', function(materials) {
        materials.preload();
        for (let key in materials.materials) {
            materials.materials[key].transparent = true;
            materials.materials[key].opacity = 0.5;
          }

        var objLoader = new THREE.OBJLoader();
        objLoader.setMaterials(materials);
        objLoader.setPath(path);
        objLoader.load('nuvens2.obj', function(obj){
            CLOUD2 = obj;
            CLOUD2.position.set(
                CLOUD2_POS[0],
                CLOUD2_POS[1],
                CLOUD2_POS[2]
            );
            CLOUD2.scale.set(4, 4, 4);    
            // immediately use the texture for material creation
    
            scene.add(CLOUD2); 
        
        
        });
    });
}
var Lensflare;
function init() {
    
	// world

	scene = new THREE.Scene();
	factory = new Factory(scene);
	collisionDetector = new CollisionDetector();
	//keyboard = new THREEx.KeyboardState();


	factory.createSkyBox();
	factory.createBase();
	factory.startCreatingElements();
	factory.createPlayer();
	factory.createLights();
	factory.createBillboards();
	billboards = factory.getBillboards();

    streetLights = factory.getStreetLights();
    
    clock.startTime = true;

	// renderer

	renderer = new THREE.WebGLRenderer({ 
        antialias: false,
        alpha:true,
        transparent: true
    });
	renderer.setClearColor(0x000000);
	renderer.setPixelRatio(window.devicePixelRatio);
	renderer.setSize(window.innerWidth, window.innerHeight);
	var threeJSCanvas = document.getElementById("container");
	threeJSCanvas.appendChild( renderer.domElement );

	renderer.shadowMap.enabled = true;
	renderer.shadowMap.type = THREE.PCFShadowMap;

	var container = document.getElementById('container');
    container.appendChild(renderer.domElement);
    
    tmpRenderer = renderer;

	window.addEventListener('resize', onWindowResize, false);

	effect = new THREE.StereoEffect(renderer);


	// STATS
	stats = new Stats();
	stats.domElement.style.position = 'absolute';
	stats.domElement.style.bottom = '0px';
	stats.domElement.style.zIndex = 100;
	//container.appendChild( stats.domElement );

    //PARTICLES   
    for (let i = 0; i < flakeCount; i++) {
      var flakeMesh = new THREE.Mesh(flakeGeometry, flakeMaterial);
      flakeMesh.position.set(
        (Math.random() * 148) + 50,
        (Math.random() *50) ,
        (Math.random() * 300)-100
      );
      snow.add(flakeMesh);
    }
    snow.visible = particlesOn;
    scene.add(snow);
    
    flakeArray = snow.children;

    //FOG
    const near = 5;
    const far = 150;
    const color = 'white';
    //scene.fog = new THREE.Fog(color, near, far);    
    FOG = new THREE.FogExp2(color, 0.015);
    FOGCOLOR =new THREE.Color(color);
    scene.fog = undefined;
    //scene.background = FOGCOLOR;

    
    //MIRROR
    //createMirror();
    factory.createElements();
    //ISLAND
    createIsland();
    //createIsland2();
    createIsland3();
    createBase();
    createCloud1();
    createCloud2();
    //createCars();

    // WATER
    var waterGeometry = new THREE.PlaneBufferGeometry( 60, 150);
    var params = {
        color: '#ffffff',
        scale: 4,
        flowX: 1,
        flowY: 1
    };


    water = new Water( waterGeometry, {
        color: params.color,
        scale: params.scale,
        flowDirection: new THREE.Vector2( params.flowX, params.flowY),
        textureWidth: 1024,
        textureHeight: 1024
    } );
    water.position.x = 80;
    water.position.y = 2;
    water.rotateX( - Math.PI / 2 );

    scene.add( water );

    //LENS FLARE
    var textureLoader = new THREE.TextureLoader();

    var textureFlare0 = textureLoader.load( 'images/lensflare/lensflare0.png' );
    var textureFlare3 = textureLoader.load( 'images/lensflare/lensflare3.png' );

    addLight( 0.6, 0.32, 0.59, 0, 30, 0 );
    //addLight( 0.08, 0.8, 0.5, 0, 0, - 1000 );
    //addLight( 0.995, 0.5, 0.9, 5000, 5000, - 1000 );

    function addLight( h, s, l, x, y, z ) {

        var light = new THREE.PointLight( 0xffffff, 1.5, 2000 );
        light.color.setHSL( h, s, l );
        light.position.set( x, y, z );
        scene.add( light );

        var lensflare = new THREE.Lensflare();
        lensflare.addElement( new THREE.LensflareElement( textureFlare0, 700, 0, light.color ) );
        lensflare.addElement( new THREE.LensflareElement( textureFlare3, 60, 0.6 ) );
        lensflare.addElement( new THREE.LensflareElement( textureFlare3, 70, 0.7 ) );
        lensflare.addElement( new THREE.LensflareElement( textureFlare3, 120, 0.9 ) );
        lensflare.addElement( new THREE.LensflareElement( textureFlare3, 70, 1 ) );
        light.add( lensflare );
        Lensflare = lensflare;

    }

	// lights
	directionalLight = new THREE.DirectionalLight(0xffffff);
	directionalLight.position.set(100, 100, -100);
	directionalLight.target.position.set(0, 0, 0);
	directionalLight.castShadow = true;
	directionalLight.shadow = new THREE.LightShadow(new THREE.PerspectiveCamera(120, 1, 1, 1000));
	directionalLight.shadow.bias = 0.00001;
	directionalLight.shadow.mapSize.width = 2048 * 2;
	directionalLight.shadow.mapSize.height = 2048 * 2;
	scene.add(directionalLight);

	/*light = new THREE.DirectionalLight(0xffffff);//0x002288
	light.position.set(-100, 100, -100);
	light.target.position.set(0, 0, 0);
	scene.add(light);*/

	globalLight = new THREE.AmbientLight(0x222222); 
	scene.add(globalLight);
    document.addEventListener("keydown", onDocumentKeyDown, false);


    spotLight = new THREE.SpotLight( 0xffffff , 1, 140, 0.6, 0.05, 1.6);
    //(color : Integer, intensity : Float, distance : Float, angle : Radians, penumbra : Float, decay : Float)
    spotLight.position.set( 150, 3, 0 );

    spotLight.castShadow = true;

    spotLight.shadow.mapSize.width = 1024;
    spotLight.shadow.mapSize.height = 1024;

    spotLight.shadow.camera.near = 500;
    spotLight.shadow.camera.far = 4000;
    spotLight.shadow.camera.fov = 30;

    scene.add( spotLight );


    setupCamera();
    scene.add(camera1);
    scene.add(camera2);
    scene.add(camera3);
    camera = camera1;
    //ISLAND.visible = false;
    //ISLAND2.visible = false;
    //ISLAND3.visible = false;
    //CLOUD1.visible = false;
    //CLOUD2.visible = false;

    controls = new THREE.OrbitControls( camera, renderer.domElement );
    controls.noZoom = true;
    controls.noPan = true;


	render();
	animate();
}


var ROTATION =0;
function openFullscreen() {
    tmpRenderer = effect;
    camera = camera1;
    controls = new DeviceOrientationControls(camera);
    controls.connect();
    controls.update();
	var elem = document.getElementById("container");
	if (elem.requestFullscreen) {
	  elem.requestFullscreen();
	} else if (elem.mozRequestFullScreen) { /* Firefox */
	  elem.mozRequestFullScreen();
	} else if (elem.webkitRequestFullscreen) { /* Chrome, Safari & Opera */
	  elem.webkitRequestFullscreen();
	} else if (elem.msRequestFullscreen) { /* IE/Edge */
	  elem.msRequestFullscreen();
	}
	elem.style.width = '100%';
	elem.style.height = '100%';
  }

//check window resize options
function onWindowResize() {
    camera1.width = window.innerWidth ;
    camera1.height= window.innerHeight ;
    camera1.aspect = window.innerWidth / window.innerHeight;
    camera1.updateProjectionMatrix();

    camera2.left = -window.innerWidth / 2;
    camera2.right = window.innerWidth / 2;
    camera2.top = window.innerHeight / 2;
    camera2.bottom = -window.innerHeight / 2;
    camera2.updateProjectionMatrix();
    //camera2.setSize(window.innerWidth, window.innerHeight),
    //camera2.updateProjectionMatrix();
    //camera3.setSize(window.innerWidth, window.innerHeight),
    camera3.width = window.innerWidth ;
    camera3.height= window.innerHeight ;
    camera3.aspect = window.innerWidth / window.innerHeight;
    camera3.updateProjectionMatrix();


	//camera.setSize(window.innerWidth, window.innerHeight);
	//camera.updateProjectionMatrix();
	renderer.setSize(window.innerWidth, window.innerHeight);
	effect.setSize(window.innerWidth, window.innerHeight);
	render();
}
var gameover = false;
var won = false;
//starting the request frame animation function here
function animate() {
	id = requestAnimationFrame(animate);
    render();
    var deltaTime = clock.getDelta();
	
	factory.moveCar(deltaTime);
	factory.moveTrunks(deltaTime);
    factory.moveTurtles(deltaTime);

	for(var i=0; i<billboards.length;i++){
		billboards[i].quaternion.y =  camera.quaternion.y;
		billboards[i].quaternion.w =  camera.quaternion.w;
	}

    if(particlesOn) {
        for (i = 0; i < flakeArray.length / 2; i++) {
            flakeArray[i].rotation.y += 0.01;
            flakeArray[i].rotation.x += 0.02;
            flakeArray[i].rotation.z += 0.03;
            flakeArray[i].position.y -= 0.018;
            if (flakeArray[i].position.y < -4) {
              flakeArray[i].position.y += 10;
            }
          }
          for (i = flakeArray.length / 2; i < flakeArray.length; i++) {
            flakeArray[i].rotation.y -= 0.03;
            flakeArray[i].rotation.x -= 0.03;
            flakeArray[i].rotation.z -= 0.02;
            flakeArray[i].position.y -= 0.016;
            if (flakeArray[i].position.y < -4) {
              flakeArray[i].position.y += 9.5;
            }
        
            snow.rotation.y -= 0.0000002;
          }
    
    
    }
    var orbitRadius1 = 80;
    var delta = Date.now() * 0.0001;
    if(ISLAND3 != undefined) {
        ISLAND3.position.set(
            ISLAND3_POS[0] + Math.cos(delta) * orbitRadius1,
            ISLAND3_POS[1] +Math.cos(delta) * orbitRadius1,
            ISLAND3_POS[2] +Math.sin(delta) * orbitRadius1
        )
        ISLAND3.rotateY(0.01)
    }
    if(ISLAND2 != undefined) {
        ISLAND3.position.set(
            ISLAND3_POS[0] + Math.cos(delta) * 10,
            ISLAND3_POS[1] +Math.cos(delta) * 10,
            ISLAND3_POS[2] +Math.sin(delta) * 10
        )
        ISLAND3.rotateY(0.01)
    }
    
    var orbitRadius = 10;
    if(ISLAND != undefined) {
        
        ISLAND.position.set(
            ISLAND_POS[0] + Math.cos(-delta) * orbitRadius,
            ISLAND_POS[1] ,
            ISLAND_POS[2] +Math.sin(-delta) * orbitRadius
        )
        ISLAND.rotateY(0.0003);
    }

    if(CLOUD1 != undefined) {
        CLOUD1.position.set(
            CLOUD1_POS[0] ,
            CLOUD1_POS[1] ,
            CLOUD1_POS[2] + Math.sin(delta) * 40
        )
    }

    if(CLOUD2 != undefined) {
        CLOUD2.position.set(
            CLOUD2_POS[0] ,
            CLOUD2_POS[1] ,
            CLOUD2_POS[2] + Math.cos(delta) * 10
        )
    }

    stats.update();

}

//renderer
function render() {
	player = scene.getObjectByName("frog");
	if(player != undefined) {
        speed.y -= gravity;
        player.position.y += speed.y;
        
        if(player.position.y < yLimits[0]) {
            player.position.y = yLimits[0];	
            speed.y = 0.0;
            onGround = true;
            detectCollision();
            factory.carCollisionDetector(collisionDetector);
            jumped = false;
        }

        if(player.position.y >= yLimits[1]) {
            if(jumped) {
                endJump();
            }
        }

        //jump speed for water area
        if(player.position.x <= 102.5) {
            speed.x = 10.0;
        } else {
            speed.x = 5.0;
            yLimits[0] = 2.5;
            yLimits[1] = 8;
        }

        if(player.position.x <= 52.5) {
            //you win
            won = true;
            window.setTimeout(function() {
                cancelAnimationFrame(id);
                document.getElementById("gameOver").innerHTML = "YOU WIN...";
                document.getElementById("gameOver").style.display = "block";
                console.warn("You Win");

            }, 800);
        }
        
        if(lookAtScene && camera != camera2)
            camera.lookAt(scene.position);
        
        //effect.render(scene, camera);
        controls.update();
        tmpRenderer.render(scene, camera);
    }
}

//check collision and display result accordingly on scoreboard
function detectCollision() {
	var woodenLogs = factory.getTrunks();
    var vehicles = factory.getVehicles();
    var turtles = factory.getTurtles();

	collisionDetector.setObjects(vehicles, woodenLogs, turtles, player);
 	var collision = collisionDetector.checkCollision(perspectiveCamera);
	if(collision.collided) {
        switch(collision.object){
            case "logs":
                break;
            default:
                    var lives = document.getElementById("rem").innerHTML;
                    lives--;
                    document.getElementById("rem").innerHTML = (lives < 0) ? 0: lives;
                    if(lives > 0) {
                        reset();
                    }
                    else {
                        window.setTimeout(function() {
                            gameover = true;
                            cancelAnimationFrame(id);
                            document.getElementById("gameOver").innerHTML = "GAME OVER!";
                            document.getElementById("gameOver").style.display = "block";
                        }, 150);
                    }
                    break;            
        }
	}

	else {
		if(jumped && (side == "up" || side == "down")) {
			var score = document.getElementById("scoreno").innerHTML;
			score = parseInt(score);
			score += 10;
			document.getElementById("scoreno").innerHTML = score;
		}
	}
}

// check for keyboard inputs
//left, right, up, down keys to move the player and camera along

var shouldPause = false;
function onDocumentKeyDown(event) {
    var keyCode = event.which;
    console.log(keyCode);

    switch(keyCode) {
        case 67 : // c
            for(var i=0; i<streetLights.length; i++){
                streetLights[i].visible = !streetLights[i].visible;
            }
            break;
        case 80: // p
            //player.rotation.z = Math.PI/2;
            //spotLight.rotation.z = Math.PI/2;
            
            if(player.position.z > zLimits[0]) {
                if(onGround) {
                    player.position.z -= speed.hx;
                    spotLight.position.z -= speed.hx;
                }
                
                if(perspectiveCamera && onGround) {
                    camera.position.z -= speed.hx;
                    chaseLookAt[2] -= speed.hx;
                    
                    camera.lookAt(chaseLookAt[0], chaseLookAt[1], chaseLookAt[2])
                }
                startJump("right");
                 
            }
            break;
        case 79: // o 
            //player.rotation.z = -Math.PI/2;
            //spotLight.rotation.z = -Math.PI/2;
            if(player.position.z <= zLimits[1]) {
                
                if(onGround) {
                    spotLight.position.z += speed.hx;
                    player.position.z += speed.hx;
                } 
                
                if(perspectiveCamera && onGround) {
                    camera.position.z += speed.hx;
                    chaseLookAt[2] += speed.hx;
                    camera.lookAt(chaseLookAt[0], chaseLookAt[1], chaseLookAt[2])
                    
                }
                startJump("left");
                    
            }

            break;
        case 81: // q
            //player.rotation.z = Math.PI;
            //spotLight.rotation.z = Math.PI;
            if(player.position.x > xLimits[0]) {
                
                if(onGround) {
                    player.position.x -= speed.x;
                    spotLight.position.x -= speed.x;
                }
                    
                
                if(perspectiveCamera && onGround) {
                    camera.position.x -= speed.x;
                    chaseLookAt[0] -= speed.x;
                    camera.lookAt(chaseLookAt[0], chaseLookAt[1], chaseLookAt[2])
                }
                startJump("up");
                   
            }
            break;                        
        case 65: // a
            //player.rotation.z = 0;
            if(player.position.x < xLimits[1]) {
                
                if(onGround) {
                    player.position.x += speed.x;
                    spotLight.position.x += speed.x;
                }
                
                if(perspectiveCamera && onGround) {
                    camera.position.x += speed.x;
                    chaseLookAt[0] += speed.x;
                    camera.lookAt(chaseLookAt[0], chaseLookAt[1], chaseLookAt[2])
                }
                startJump("down");
                    
            }
            break;
        case 70: // f  - toggle fog
            if(scene.fog === undefined) {
                scene.fog = FOG;
                scene.background = FOGCOLOR;
                factory.getSkyBox().visible = false;

            }else {
                scene.fog = undefined
                scene.background = new THREE.Color('black');
                factory.getSkyBox().visible = true;
            }
            break;
        case 75: // k  - toggle particles
            particlesOn = !particlesOn
            snow.visible = particlesOn;
            break;
        case 76: // k  - toggle particles
            Lensflare.visible = !Lensflare.visible;
            break;
        case 49: // 1
            //to get the orthographic top view
            //setupOrthographicCamera();
            perspectiveCamera = false;
            camera = camera2;
            break;
        case 50: //2
            //to get the perspective view
            //setupPerspectiveCamera();
            perspectiveCamera = true;
            camera = camera1;
            break;
        case 51: // 3
            //to get the perspective view
            perspectiveCamera = false;
		    camera = camera3;
            break;
        case 52: // 4
            //to get the perspective view
		    setupTopDownCamera();
            break;
        case 83: // s
            //to get the perspective view
            shouldPause = !shouldPause;
            if(shouldPause) {
                cancelAnimationFrame(id);
                document.getElementById("gameOver").innerHTML = "PAUSED";
                document.getElementById("gameOver").style.display = "block";
            } else {
                animate();
                document.getElementById("gameOver").style.display = "none";
            }
		    //pause();
			break;
		case 88: // x
			openFullscreen()
            break;
        case 82: // r
            if(gameover || won) {
                won = false;
                gameover = false;
                lives = document.getElementById("rem").innerHTML = "3";
			    document.getElementById("scoreno").innerHTML = "0";

                document.getElementById("gameOver").style.display = "none";
                reset2();
                animate();
                //to get the perspective view
		        //restart();
            }
            break;
        case 72 : // h
		    spotLight.visible = !spotLight.visible;
            break;
        case 78: //n
            globalLight.visible = !globalLight.visible
            directionalLight.visible = !directionalLight.visible;
            break;
    }
};

function reset2() {
    reset();
}

function setupTopDownCamera() { 
    scene.activeCamera = orthoCamera;
}

function setupPerspectiveCamera() {
	perspectiveCamera = true;
	camera.toPerspective();
	camera.setFov(45);

	camera.position.set(
		cameraOldPos.x + (player.position.x - playerOldPos.x), 
		cameraOldPos.y + (player.position.y - playerOldPos.y), 
		cameraOldPos.z + (player.position.z - playerOldPos.z)
	);
	scene.rotation.y = 0;

	lookAtScene = true;
}

var chaseLookAt = [0,0,0];
function setupCamera() {
    //camera = new THREE.CombinedCamera(window.innerWidth, window.innerHeight, 45, 1, 1000, 1, 1000);
    if(camera1 == undefined) {
        camera1 = new THREE.PerspectiveCamera(45, window.innerWidth/window.innerHeight, 1, 1000);
    }
    camera1.position.set(200, 15, 0);
    chaseLookAt = [0,0,0];
    camera1.lookAt(
        0,0,0
    )
    if(camera2 == undefined) {
        camera2 = new THREE.OrthographicCamera(window.innerWidth/-2, window.innerWidth/2, window.innerHeight/2, window.innerHeight/-2, 1, 1000 );
    }
    
    camera2.position.set(100,10,0);
    
    camera2.lookAt(100, 0, 0);
    camera2.zoom = 9;
    camera2.updateProjectionMatrix();
    if(camera3 == undefined) { 
        camera3 = new THREE.PerspectiveCamera(45, window.innerWidth/window.innerHeight, 1, 1000);    
    }
    
    camera3.position.set(230, 40, 50);
    

    
}

function setupOrthographicCamera() {
	if(perspectiveCamera) {
		cameraOldPos = {x: camera.position.x, y: camera.position.y, z: camera.position.z};
		playerOldPos = {x: player.position.x, y: player.position.y, z: player.position.z};
	}
    perspectiveCamera = false;
	camera.toOrthographic();
    camera.toTopView();
    
    camera.setFov(15);
    cameara.lookAt(new THREE.Vector3(-1,0,0));
    camera.position.set(0, 10, 0);
	scene.rotation.y = 270 * Math.PI/180;
	
	lookAtScene = false;
}

function startJump(name) {
    if(onGround && !jumped) {
        speed.y = 2.0;
        side = name;
        onGround = false;
        jumped = true;
    }
}

function endJump() {
    if(speed.y < 1.0) {
        speed.y = 1.0;
    }
}

function reset() {
	setupCamera();
    player.position.set(player.initialPosition[0],player.initialPosition[1],player.initialPosition[2] );
    spotLight.position.set(150, 3, 0 );
	if(perspectiveCamera) {
        cameraOldPos = {x: camera1.position.x, y: camera1.position.y, z: camera1.position.z};
        playerOldPos = {x: player.position.x, y: player.position.y, z: player.position.z};
        
	}
	else {
		//setupOrthographicCamera();
    }
    
    //controls = new THREE.OrbitControls( camera, renderer.domElement );
}