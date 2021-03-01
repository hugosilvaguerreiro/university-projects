var DEFAULT_SPEED = 20
var SPACING = 5
var SPEED_FACTOR = 1
var RESPAWN_TIME = 100
var MAX_SPEED = 500

class randomizableObject extends collidableObject {
	constructor(position, boundingRadius) {
		super(position, boundingRadius);
		this.startingPosition = position;
		this.randomizerInputs = new THREE.Vector3(0,0,0); //use this to get info
		this.currentSpeed = Math.floor(DEFAULT_SPEED + Math.random()* DEFAULT_SPEED);
		this.clock = new THREE.Clock(false);
		this.elapsedTime = 0;
		this.outOfBounds = false;
	}
	randomizerUpdater(delta_t) {
		//IMPLEMENT THIS METHOD IN THE SUBCLASSES
	}

	getCurrentSpeed() {
		return this.currentSpeed;
	}
}

class path {
	constructor(object, path, randomizer) {
		this.object = object;
		this.path = path;
		this.randomizer = randomizer
		this.startingPath = path
	}

	notifyRandomizer() {
		this.object.clock.stop();
		this.object.outOfBounds = false;
		this.object.elapsedTime = 0;
		this.randomizer.notifications.push(this);
	}

	update(delta_t, speed, limitx, limitY, limitZ) {
		if(this.object.outOfBounds) {
			var time = this.object.clock.getElapsedTime()
			this.object.elapsedTime += time;
			if(this.object.elapsedTime > RESPAWN_TIME) {
				this.notifyRandomizer()
			}
			return
		}
		if( this.object.getPosition().x  <= limitx + SPACING  && this.object.getPosition().z <= limitZ + SPACING && 
			this.object.getPosition().x  >= -(limitx + SPACING) && this.object.getPosition().z  >= -(limitZ + SPACING)) {

			//this.object.currentSpeed +=  SPEED_FACTOR/this.object.currentSpeed;
			
			
			
			this.object.randomizerInputs.set(this.path.x*this.object.currentSpeed*delta_t, this.path.y*this.object.currentSpeed*delta_t, this.path.z*this.object.currentSpeed*delta_t);
			this.object.update(delta_t);
		}
		else {
			this.object.outOfBounds = true;
			this.object.clock.start();
			this.object.setPosition(0,0,10000); //Notifies the randomizer that an object is out of bounds
		}		
	}
}

class randomizer {
	constructor(limitX, limitY, limitZ, speed) {
		this.limitX = limitX;
		this.limitY = limitY;
		this.limitZ = limitZ;

		this.speed = speed || DEFAULT_SPEED; 
		this.notifications = []
		this.paths = [];
	}

	random(min, max) {
		return Math.random() * (max - min) + min;
	}

	createOranges(minOranges, maxOranges, radius) {
		orangeList = [];
		for(var i=0; i < this.random(minOranges, maxOranges); i++) {

			var pos = new THREE.Vector3(this.random(-this.limitX, this.limitX), this.limitY, this.random(-this.limitZ, this.limitZ));

			var object = new orange(pos, radius);
			var direction = new THREE.Vector3(this.random(-1,1), 0, this.random(-1,1)).normalize();
			var objPath = new path(object, direction ,this);

			object.rotationAxis = direction.clone().cross(new THREE.Vector3(0,1,0));
			
			this.paths.push(objPath);
			orangeList.push(object);
		}
		return orangeList;
	}

	createButters(minButters, maxButters, width, height, depth) {
		butterList = [];
		for(var i=0; i < this.random(minButters, maxButters); i++) {
			var pos = new THREE.Vector3(this.random(-this.limitX, this.limitX), this.limitY/2, this.random(-this.limitZ, this.limitZ));
			var object = new butter(pos, width, height, depth);
			object.setRotation(0,Math.random()*2*Math.PI,0);
			butterList.push(object);
		}
		return butterList;
	}

	replyToNotifications() {
		while(this.notifications.length > 0) {
			var path = this.notifications.pop();

			var positionsFixed = Math.floor(this.random(0,2));
			var posX = 0 
			var posZ = 0;
			var pos = [];
			if(positionsFixed == 0) {
				pos = [-this.limitX, this.limitX];
				posX = pos[Math.floor(this.random(0,2))];
				posZ = this.random(-this.limitZ, this.limitZ);

				path.path = new THREE.Vector3(-Math.sign(posX)*this.random(0, 1), 0, this.random(-1,1)).normalize();

			}
			else {
				pos = [-this.limitZ, this.limitZ];
				posZ = pos[Math.floor(this.random(0,2))];
				posX = this.random(-this.limitX, this.limitX);
				path.path = new THREE.Vector3(this.random(-1,1), 0, -Math.sign(posZ)*this.random(0,1)).normalize();
			}
			path.object.orange.rotation.set(0,0,0);
			path.object.rotationAxis = path.path.clone().cross(new THREE.Vector3(0,1,0));
			path.object.setPosition(posX, this.limitY, posZ);
			if(path.object.currentSpeed < MAX_SPEED) {
				path.object.currentSpeed += this.speed;
			}
			
		}
	}

	changeSpeed(newSpeed) {
		this.speed = newSpeed;
	}

	reset() {
		this.changeSpeed(DEFAULT_SPEED);
		for(var i=0; i < this.paths.length; i++ ) {
			this.paths[i].object.currentSpeed = Math.floor(DEFAULT_SPEED + Math.random()* DEFAULT_SPEED);
			if(this.paths[i].object instanceof orange) {
				var pos = this.paths[i].object.startingPosition;
				this.paths[i].object.position.set(pos.x, pos.y, pos.z);
				this.paths[i].object.orange.rotation.set(0,0,0);
				this.paths[i].object.rotationAxis = this.paths[i].startingPath.clone().cross(new THREE.Vector3(0,1,0));
				this.paths[i].path = this.paths[i].startingPath;
				this.paths[i].object.elapsedTime = 0;
				this.paths[i].object.outOfBounds = false;
				this.notifications = []
			}
			
		}
	}

	update(delta_t) {
		if(!pause) {
			this.speed += SPEED_FACTOR/this.speed;
			for(var i=0; i < this.paths.length; i++ ) {
				this.replyToNotifications();
				this.paths[i].update(delta_t, this.speed, this.limitX, this.limitY, this.limitZ);
			}
		}
	}
}