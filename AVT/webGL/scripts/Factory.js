function Factory(scene) {
	this.scene = scene;
	this.trunks = [];
	this.turtles = [];

	this.ticks = 640;

	var velocity = 20;
	var increment = 5;

	var index = 0;

	this.canAddToScene = false;

	this.startCreatingElements = function(){
		this.canCreate = true;
		setInterval(this.incrementVelocity, 20000);
	}

	this.incrementVelocity = function(){
		velocity += increment;
		this.player.velocity = velocity;
	}

	this.createElements = function(){
        this.createVehicles();
        this.createTrunks();
		this.createTurtles();
		this.timer2 = setInterval(this.addToScene, 2000, this);
	}

	this.addToScene = function(factory){
		if(factory.trunks && factory.turtles && factory.vehicles){
			if(index<LIMIT){
				factory.trunks[index].canMove = true;
				factory.turtles[index].canMove = true;
				factory.vehicles[index].canMove = true;
				factory.scene.add(factory.trunks[index].mesh);
				factory.scene.add(factory.turtles[index]);
				factory.scene.add(factory.vehicles[index]);
				index++
			}

			if(index == LIMIT){
				clearInterval(factory.timer2);
			}
		}
	}




	//create the player cube
	this.createBox = function(size, pos, color, receiveShadow, castShadow) {
		var geometry = new THREE.BoxGeometry(size.x, size.y, size.z);
		var material = new THREE.MeshPhongMaterial({color: color});
		var mesh = new THREE.Mesh(geometry, material);
		mesh.position.set(pos.x+100, pos.y, pos.z);
		mesh.castShadow = castShadow;
		mesh.receiveShadow = receiveShadow;
		return mesh;
	}

	//create textured box
	this.createTextureBox = function(size, pos, color, receiveShadow, castShadow, type, transparency) {
        
        if(type=="grass") {
            var geometry = new THREE.BoxGeometry(size.x, 0, size.z);
        }else {
            var geometry = new THREE.BoxGeometry(size.x, size.y, size.z);
        }
        
		var loader = new THREE.TextureLoader();
		var url;
		
		switch(type) {
			case "grass" : url = "images/pavement.png"; break;
			case "water" : url = "images/water.jpg"; break;
			case "road" : url = "images/road3.png"; break;
		}

		loader.load(url, function(texture) {
			texture.wrapS = THREE.RepeatWrapping;
			texture.wrapT = THREE.RepeatWrapping;
            texture.repeat.set(1, 3);
            var props = {
                opacity : transparency,
                transparent : true,
                map: texture
            }
            if(type == "grass") {
                var mapHeight = new THREE.TextureLoader().load( "images/PavementBump.png" );
                mapHeight.wrapS = THREE.RepeatWrapping;
                mapHeight.wrapT = THREE.RepeatWrapping;
                mapHeight.repeat.set(4, 4);
                props.bumpMap = mapHeight;
                props.bumpScale = 5;
            }
            
            var materials = [
                new THREE.MeshPhongMaterial(props),
                new THREE.MeshPhongMaterial(props),
                new THREE.MeshPhongMaterial(props),
                new THREE.MeshPhongMaterial(props),
                new THREE.MeshPhongMaterial(props),
                new THREE.MeshPhongMaterial(props),
            ]    
			
            var mesh = new THREE.Mesh(geometry, materials);
            if(type == "grass") {
                mesh.position.set(pos.x+100, 2, pos.z);    
            } else {
                mesh.position.set(pos.x+100, pos.y, pos.z);
            }
			
		    mesh.castShadow = castShadow;
			mesh.receiveShadow = receiveShadow;
			
			this.scene.add(mesh);
		});
	}

	//create semi-cylinders for logs
	this.createCylinder = function(radius, height, pos, color, receiveShadow, castShadow) {
		var geometry = new THREE.CylinderGeometry(radius, radius, height, 8, 8, false, 0, Math.PI);
		var loader = new THREE.TextureLoader();

		var texture = new THREE.TextureLoader().load("images/log.jpg");
		texture.wrapS = THREE.RepeatWrapping;
		texture.wrapT = THREE.RepeatWrapping;
		texture.repeat.set(4, 4);

		var material = new THREE.MeshPhongMaterial({map: texture});
		
		var mesh = new THREE.Mesh(geometry, material);
		mesh.position.set(pos.x+100, pos.y, pos.z);
		mesh.rotation.x = Math.PI/2;
		mesh.rotation.y = Math.PI/2;
		mesh.castShadow = castShadow;
		mesh.receiveShadow = receiveShadow;
		
		return mesh;
	}

	//animate car obj
	var velocity= 20;
	this.moveCar = function(deltaTime) {
	
		if(objects.length > 0) {
			for(var i=0;i<objects.length;i++){
				if(objects[i].canMove){
					if(objects[i].velocity > 0){
						objects[i].position.z += velocity*deltaTime;
					}
					else{
						objects[i].position.z -= velocity*deltaTime;
					}

					if(objects[i].position.z > 50) {
						objects[i].position.z = -50;
					} else if(objects[i].position.z < -50) {
						objects[i].position.z = 50;
					}
				}
			}
		}
	}

	this.moveTrunks = function(deltaTime){
		if(this.trunks.length > 0) {
			for(var i=0;i<this.trunks.length;i++){
				if(this.trunks[i].canMove){
					this.trunks[i].mesh.position.z -= velocity*deltaTime;
					this.trunks[i].movePace = velocity*deltaTime;
					if(this.trunks[i].mesh.position.z < -50) {
						this.trunks[i].mesh.position.z = 50;
					}
				}
			}
		}
	}

	this.moveTurtles = function(deltaTime){
		if(turtles.length > 0) {
			for(var i=0;i<turtles.length;i++){
				if(turtles[i].canMove){
					turtles[i].position.z += velocity*deltaTime;
					turtles[i].movePace = velocity*deltaTime;
					if(turtles[i].position.z > 50) {
						turtles[i].position.z = -50;
					}
				}
			}
		}
	}

	//car collision detector
	this.carCollisionDetector = function(collisionDetector) {
		if(object != undefined) {
			collisionDetector.setCarObject(object);
			var collision = collisionDetector.checkCarCollision();

			if(collision.collided) {
				var lives = document.getElementById("rem").innerHTML;
				lives--;
				document.getElementById("rem").innerHTML = (lives < 0) ? 0: lives;
				
				if(lives > 0) {
					reset();
				}
				else {
					window.setTimeout(function() {
						cancelAnimationFrame(id);
						document.getElementById("gameOver").style.display = "block";
						console.warn("GameOver");

					}, 150);
				}
			}
		}
	}

	var objects = [];
	onLoadCarOBJ = function(obj) {
		this.object = obj;
		this.object.traverse(function(node) { 
			if(node instanceof THREE.Mesh) { 
				node.castShadow = true; 
				node.receiveShadow = false;
			} 
		});
		var index3 = Math.floor(Math.random() * 4);
		if(index3 % 2 == 0){
			this.object.position.set(road[index3], 3, 50);
			this.object.velocity = -1;
			this.object.rotation.y = Math.PI/2;
			this.object.scale.set(2.3,2.3, 2.3);
		}
		else{
			this.object.position.set(road[index3], 3, -50);
			this.object.velocity = 1;
			this.object.rotation.y = -Math.PI/2;
			this.object.scale.set(2.3, 2.3, 2.3);
		}

		this.object.canMove = false;
		objects.push(this.object);
	}

	onLoadFrogOBJ = function(obj) {
		this.object3 = obj;
		this.object3.name = "frog";
		this.object3.traverse(function(node) { 
			if(node instanceof THREE.Mesh) { 
				node.castShadow = true; 
				node.receiveShadow = false;
			} 
		});
		this.object3.position.set(148, 1, 0);
		this.object3.initialPosition = [148, 1, 0];
		this.object3.rotation.x = -90*Math.PI/180;
		this.object3.rotation.z = Math.PI;
		this.object3.scale.set(0.5, 0.5, 0.5);
		this.scene.add(this.object3);

		this.frogLoaded = true;
	}

	 var turtles = [];
	onLoadTurtleOBJ = function(obj) {
		this.object4 = obj;
		this.object4.traverse(function(node) { 
			if(node instanceof THREE.Mesh) { 
				node.castShadow = true; 
				node.receiveShadow = false;
			} 
		});
		var index2 = Math.floor(Math.random() * 2);
		this.object4.position.set(riverTurtle[index2], 2, 0);
		this.object4.rotation.x = -90*Math.PI/180;
		this.object4.rotation.z = -Math.PI/2;
		turtles.push(this.object4);
		this.object4.scale.set(0.08, 0.08, 0.08);
		this.object4.canMove = false;
	}


	//https://sketchfab.com/3d-models/turtle-6e53ddefba2e45dda8e4f1c73fb89f67

	this.onLoadCarMTL = function(materials) {
		this.materials = materials;
		this.materials.preload();

		var path = "models/car3/";
		var objLoader = new THREE.OBJLoader();
		objLoader.setMaterials(materials);
		objLoader.setPath(path);
		objLoader.load('car.obj', onLoadCarOBJ);
	}

	this.onLoadFrogMTL = function(materials) {
		this.materials = materials;
		this.materials.preload();

		var path = "models/frog/";
		var objLoader = new THREE.OBJLoader();
		objLoader.setMaterials(materials);
		objLoader.setPath(path);
		objLoader.load('frog.obj', onLoadFrogOBJ);
	}

	this.onLoadTurtleMTL = function(materials) {
		this.materials = materials;
		this.materials.preload();

		var path = "models/turtle/";
		var objLoader = new THREE.OBJLoader();
		objLoader.setMaterials(materials);
		objLoader.setPath(path);
		objLoader.load('turtle.obj', onLoadTurtleOBJ);
	}

	this.createCar = function() {
		var path = "models/car3/";

		var mtlLoader = new THREE.MTLLoader();		
		mtlLoader.setCrossOrigin('');
		mtlLoader.load(path+'car.mtl', this.onLoadCarMTL);
	}

	this.createFrog = function() {
		var path = "models/frog/";

		var mtlLoader = new THREE.MTLLoader();		
		mtlLoader.setCrossOrigin('');
		mtlLoader.load(path+'frog.mtl', this.onLoadFrogMTL);
	}

	this.createPlayer = function() {
		this.createFrog();
	}

	this.createBase = function() {
		this.createTextureBox({x: 5, y: 5, z: 150}, {x: 48.5, y: 0, z: 0}, 0x00ff00, true, false, "grass", 1);	//grass
        
        
        this.createTextureBox({x: 41, y: 5, z: 150}, {x: 25.5, y: 0, z: 0}, 0x778899, true, false, "road",1);	//road
        
		this.createTextureBox({x: 5, y: 5, z: 150}, {x: 2.5, y: 0, z: 0}, 0x00ff00, true, false, "grass",1);	//grass
		//this.createTextureBox({x: 45, y: 5, z: 150}, {x: -22.5, y: 0, z: 0}, 0x0000ff, true, false, "water",0.5);	//water
		this.createTextureBox({x: 5, y: 5, z: 150}, {x: -47.5, y: 0, z: 0}, 0x00ff00, true, false, "grass",1);	//grass
	}

	var LIMIT = 2;

	var riverTrunk = [-40, -23, -5];
	var riverTurtle = [68, 86];
	var road = [140, 130, 121, 113];

	this.createVehicles = function() {
		for(var i=0; i<LIMIT; i++){
			this.createCar();			
		}	
		
		this.vehicles = objects;
	}

	this.createTrunks = function() {
		for(var i=0; i< LIMIT; i++){
			var index1 = Math.floor(Math.random() * 3);
			this.trunks.push({ mesh: this.createCylinder(3, 20, {x: riverTrunk[index1], y: 1, z: -50}, 0x663300, false, true), canMove: false });
		}
	}

	this.createTurtles = function() {
		for(var i=0; i<LIMIT;i++){
			this.createTurtle();
		}
		this.turtles = turtles;		
	}

	this.createTurtle = function(){
		var path = "models/turtle/";

		var mtlLoader = new THREE.MTLLoader();		
		mtlLoader.setCrossOrigin('');
		mtlLoader.load(path+'turtle.mtl', this.onLoadTurtleMTL);
	}

    var skyBox;
	this.createSkyBox = function(){
		/*var cube = new THREE.CubeGeometry(500,500,500);
		var cubeMaterials =
		[
			new THREE.MeshBasicMaterial({ map: new THREE.TextureLoader().load("skybox/sky_front.png"), side: THREE.DoubleSide}),

			new THREE.MeshBasicMaterial({ map: new THREE.TextureLoader().load("skybox/sky_front.png"), side: THREE.DoubleSide}),

			new THREE.MeshBasicMaterial({ map: new THREE.TextureLoader().load("skybox/sky_up.png"), side: THREE.DoubleSide}),

			new THREE.MeshBasicMaterial({ map: new THREE.TextureLoader().load("skybox/sky_down.png"), side: THREE.DoubleSide}),


			new THREE.MeshBasicMaterial({ map: new THREE.TextureLoader().load("skybox/sky_right.png"), side: THREE.DoubleSide}),

			new THREE.MeshBasicMaterial({ map: new THREE.TextureLoader().load("skybox/sky_left.png"), side: THREE.DoubleSide})	

		];

		var cubeMaterial = new THREE.MeshFaceMaterial( cubeMaterials);
		var skybox = new THREE.Mesh(cube, cubeMaterial);
		skybox.position.set(70,0,0);
		this.scene.add(skybox);*/
		var sphereGeometry = new THREE.SphereGeometry(500, 24, 24);
		var sphereMaterial = new THREE.ShaderMaterial({
			uniforms: { 
				texture: { type: 't', value: THREE.ImageUtils.loadTexture( 'skybox/skydome.jpg' ) },
			},
			vertexShader: document.getElementById( 'sky-vertexs' ).textContent,
			fragmentShader: document.getElementById( 'sky-frags' ).textContent,
			shading: THREE.SmoothShading,
			side: THREE.BackSide
		});
		var sphere = new THREE.Mesh( sphereGeometry, sphereMaterial );
        sphere.scale.set(-1,-1,-1); // flip normals
        skyBox = sphere;
		this.scene.add(sphere);

	}

	var lightPos = [-60, -20, 60, 20];
	var lightPosX = [148.5, 102.5];

    var lights = [];
    var sphereMaterial;
	this.createLights = function(){
		for(var i=0; i<4; i++) {
			for(var j=0; j<2; j++) {
				var light = new THREE.PointLight(0xffffff, 5, 30, 2);
				light.position.set(lightPosX[j], 15, lightPos[i]);

				var cylinderGeometry = new THREE.CylinderGeometry(0.30, 0.30, 1, 8, 1, false,0, 6.3);
				var cylinderMaterial = new THREE.MeshPhongMaterial( {color: 0x000000} );
				var cylinder = new THREE.Mesh( cylinderGeometry, cylinderMaterial );
				cylinder.scale.set(1,10,1);
				cylinder.position.set(lightPosX[j], 7, lightPos[i]);

				var sphereGeometry = new THREE.SphereGeometry(2, 8, 8, 0, 6.3, 0, 3.1);
				var sphereMaterial = new THREE.ShaderMaterial({
					uniforms: { 
						tOpacity: { type: "f", value: 0.8 },
						tMatCap: { type: 't', value: THREE.ImageUtils.loadTexture( 'images/sphere.jpg' ) },
					},
					vertexShader: document.getElementById( 'vertexs' ).textContent,
					fragmentShader: document.getElementById( 'frags' ).textContent,
					shading: THREE.SmoothShading,
					transparent: true,
				});

				var sphere = new THREE.Mesh( sphereGeometry, sphereMaterial );
				sphere.position.set(lightPosX[j], 13, lightPos[i]);

				this.scene.add(cylinder);
				this.scene.add(sphere);
                this.scene.add(light);
                
                var sphereSize = 5;

				lights.push(light);
			}
		}
	}

	var billboards = [];

	this.createBillboards = function(){
		var noFrogGeometry = new THREE.PlaneGeometry( 10, 20, 8  );
		var texture = new THREE.TextureLoader().load( 'images/stop2.png' );
		var noFrogMaterial = new THREE.MeshBasicMaterial({ map: texture });
		noFrogMaterial.transparent = true; 
		noFrogMaterial.depthWrite = false;
		noFrog = new THREE.Mesh(noFrogGeometry, noFrogMaterial);
		noFrog.scale.set(0.6,0.6,0.6);
		noFrog.position.set(100.5,8, 15);
		scene.add(noFrog);
		billboards.push(noFrog);

		var stopGeometry = new THREE.PlaneGeometry( 10, 20, 8  );
		var texture = new THREE.TextureLoader().load( 'images/stop.png' );
		   var stopMaterial = new THREE.MeshBasicMaterial({ map: texture });
		   stopMaterial.transparent = true; 
		   stopMaterial.depthWrite = false;

		stop = new THREE.Mesh(stopGeometry, stopMaterial);
		stop.scale.set(0.6,0.6,0.6);
		stop.position.set(100.5,8,-15)
		scene.add(stop);
		billboards.push(stop);		
	}

	this.getBillboards = function(){
		return billboards;
    }

    this.getSkyBox = function() {
        return skyBox;
    }
    
    this.getLightMaterials = function() {
        return sphereMaterial;
    }

	this.getStreetLights = function(){
		return lights;
	}

	this.getPlayer = function() {
		return this.player;
	}

	this.getFrog = function() {
		return this.object3;
	}

	this.getVehicles = function() {
		return objects;
	}

	this.isFrogLoaded = function() {
		return this.frogLoaded;
	}

	this.getTrunks = function() {
		return this.trunks;
	}

	this.getTurtles = function() {
		return turtles;
	}
}


