'use strict'
class cheerio {
	constructor(radius, tubeRadius, radialSegments, tubularSegments, mass) {
		//movement
		this.speed = new THREE.Vector3(0,0,0);
		this.speedStopThreshold = 3;
		this.attritionValue = 100;

		//collisions
		this.mass = 1;
		this.boundingRadius = radius+tubeRadius;
		this.final = false;
		this.tentativePos;
		this.incomingList = [];	//(position from, boundingRadius, speed vector, mass) 

		//mesh
		this.geometry = new THREE.TorusGeometry(radius, tubeRadius, radialSegments, tubularSegments);
		this.material = new new THREE.MeshPhongMaterial();
		var cheerioMesh = new THREE.Mesh( this.geometry, this.material );
		cheerioMesh.rotation.x = Math.PI/2;
		this.cheerioObj = new THREE.Object3D().add(cheerioMesh);

	}

	update(delta_t) { //after first tentative and collision handling
		this.setPosition(this.tentativePos.x, this.tentativePos.y, this.tentativePos.z);
		this.final = false;
	}


	firstTentative(delta_t) { //before any collision detection
		this.applyAttrition(delta_t); 
		this.tentativePos = this.cheerioObj.position.clone().addScaledVector(this.speed, delta_t);
		//console.log(this.tentativePos);
	}

	collisionHandling() { //after every cheerio has had firstTentative calculated
		this.resolveClipping();
		this.computeSpeed();
		this.incomingList = [];
	}

	resolveClipping() { //also 
		var collsNo = this.incomingList.length;
		var unclipDir = new THREE.Vector3(0,0,0);
		for(var i=0; i<collsNo; i++) {
			if(this.incomingList[i][4] == false) {
				var fromDir = this.tentativePos.clone().sub(this.incomingList[i][0]);
				var distance = fromDir.length();
				unclipDir.add(fromDir.normalize().multiplyScalar(this.boundingRadius+this.incomingList[i][1]-distance));
			}
		}
		this.tentativePos.add(unclipDir);
	}

	computeSpeed() {
		var collsNo = this.incomingList.length;
		for(var i=0; i<collsNo; i++) {
			var m2 = this.incomingList[i][3];
			var v2 = this.incomingList[i][2];
			var fromDir = this.tentativePos.clone().sub(this.incomingList[i][0]);
			var distanceSquared = this.tentativePos.distanceToSquared(this.incomingList[i][0]);
			var factor = (2*m2/(this.mass+m2))*((this.speed.clone().sub(v2)).dot(fromDir)/(distanceSquared));
			this.speed.sub(fromDir.multiplyScalar(factor));
		}
	}

	incomingCollision(singleCollision) { //[fromPos, boundingRadius, speed, mass]
		this.incomingList.push(singleCollision);
		this.collisionHandling();
	}

	getCollisionResponse(dontUnclip) {
		return [this.tentativePos.clone(), this.boundingRadius, this.speed.clone(), this.mass, dontUnclip];
	}

	applyAttrition(delta_t) {
		if (this.speed.length() < this.speedStopThreshold) {
			this.speed.multiplyScalar(0);
		} else {
			this.speed.sub(this.speed.clone().normalize().multiplyScalar(this.attritionValue*delta_t));
		}
		//console.log(this.speed);
	}

	isFinal() {
		return this.final;
	}

	setFinal() {
		this.final = true;
	}

	setPosition(PosX, PosY, PosZ) {
		this.cheerioObj.position.set(PosX, PosY, PosZ)
	}

	setRotation(RotX, RotY, RotZ) {
		this.cheerioObj.rotation.set(RotX, RotY, RotZ);
	}

	getObject() {
		return this.cheerioObj;
	}

	getTentativePosition() {
		return this.tentativePos;
	}

}
