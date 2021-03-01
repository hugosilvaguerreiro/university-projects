'use strict'
class baseObject extends THREE.Object3D {
	constructor(position, rotation) {
		super();
		var pos = position || new THREE.Vector3(0,0,0);
		var rot = rotation || new THREE.Vector3(0,0,0);
		this.position.copy(pos);
		this.initialPosition = this.position.clone();
		this.initialRotation = this.rotation.clone();
		this.matPhong = undefined;
		this.matGouroud = undefined;
		this.mesh = undefined;
		this.isBaseObject = true;
	}

	update(delta_time) {}

	input(action) {}

	reset() {
		this.resetPosAndRot();
		/* Specific code above */
		this.propagateReset();
	}

	getPosition(PosX, PosY, PosZ) {
		return this.position;
	}

	setPosition(PosX, PosY, PosZ) {
		this.position.set(PosX, PosY, PosZ);
	}

	setInitialPosition(PosX, PosY, PosZ) {
		this.setPosition(PosX, PosY, PosZ);
		this.initialPosition.copy(this.position);
	}

	setRotation(RotX, RotY, RotZ) {
		this.rotation.set(RotX, RotY, RotZ);
	}

	setInitialRotation(RotX, RotY, RotZ) {
		this.setRotation(RotX, RotY, RotZ);
		this.initialRotation.copy(this.rotation);
	}

	resetPosAndRot() {
		this.setPosition(this.initialPosition.x, this.initialPosition.y, this.initialPosition.z);
		this.setRotation(this.initialRotation.x, this.initialRotation.y, this.initialRotation.z);
	}

	propagateReset() {
		for(var i=0; i<this.children.length; i++) {
			if(this.children[i].isBaseObject)
				this.children[i].reset();
		}
	}

	toggleMesh() {
		for (var i = this.children.length - 1; i >= 0; i--) {
			if (this.children[i] instanceof THREE.Mesh)
				this.children[i].material = (this.children[i].material == this.matPhong)? this.matGouroud : this.matPhong;
		}
	}


	setWireframe(activated) {
		if(this.matPhong)
			this.matPhong.wireframe = activated;
		if(this.matGouroud)
			this.matGouroud.wireframe = activated;
	}

	propagateToggleMesh() {
		for(var i=0; i<this.children.length; i++) {
			if (this.children[i].isBaseObject)
				this.children[i].toggleMesh();
		}
	}
}

var DEFAULT_BOUNDING_RADIUS = 1;

class collidableObject extends baseObject {
	constructor(position, boundingRadius, boundingOffset) {
		super(position);
		this.boundingRadius = boundingRadius || DEFAULT_BOUNDING_RADIUS;
		this.boundingOffset = boundingOffset || new THREE.Vector3(0,0,0);
		this.isCollidableObject = true;
	}

	getTentativePosition() {
		return this.position.clone();
	}

	getBoundingCenter() { //must have for hasCollision
		return this.getTentativePosition().add(this.boundingOffset);
	}

	setBoundingOffset(v) {
		this.boundingOffset.copy(v);
	}

	getBoundingRadius() { //must have for hasCollision
		return this.boundingRadius;
	}

	setBoundingRadius(value) {
		this.boundingRadius = value;
	}
}

var DEFAULT_STOP_THRESHOLD = 5;
var DEFAULT_FRICTION_COEFFICIENT = 100;

class movingObject extends collidableObject {
	constructor(position, boundingRadius, speedStopThreshold, frictionCoefficient, speed, acceleration) {
		super(position, boundingRadius);
		this.speedStopThreshold = speedStopThreshold || DEFAULT_STOP_THRESHOLD;
		this.frictionCoefficient = frictionCoefficient || DEFAULT_FRICTION_COEFFICIENT;
		this.speed = speed || new THREE.Vector3(0,0,0);
		this.initialSpeed = this.speed.clone();
		this.acceleration = acceleration || new THREE.Vector3(0,0,0);
		this.initialAcceleration = this.acceleration.clone();
		this.tentativePos = this.position.clone();
		this.isMovingObject = true;
	}

	setSpeedStopThreshold(value) {
		this.speedStopThreshold = value;
	}

	setPosition(PosX, PosY, PosZ) {
		this.position.set(PosX, PosY, PosZ);
		this.tentativePos.set(PosX, PosY, PosZ);
	}

	getSpeed() {
		return this.speed.clone();
	}

	setSpeed(v) {
		this.speed.copy(v);
	}

	getAcceleration() {
		return this.acceleration.clone();
	}

	setAcceleration(v) {
		return this.acceleration.copy(v);
	}

	getFriction() {
		return this.speed.clone().normalize().negate().multiplyScalar(this.frictionCoefficient);
	}

	setFrictionCoefficient(value) {
		this.frictionCoefficient = value;
	}

	update(delta_time) {
		this.updatePhysics(delta_time);
	}

	updatePhysics(delta_time) {
		this.position.copy(this.getTentativePosition());
		this.speedUpdate(delta_time);
		this.tentativePos.add(this.speed.clone().multiplyScalar(delta_time));
	}

	getTentativePosition() {
		return this.tentativePos.clone();
	}

	speedUpdate(delta_time) {
		var preFriction = this.speed.add(this.acceleration.clone().multiplyScalar(delta_time));
		var postFriction = this.speed.add(this.getFriction().multiplyScalar(delta_time));
		if(preFriction.dot(postFriction) <= 0 || postFriction.length() <= this.speedStopThreshold) {
			this.speed.set(0,0,0);
		}
	}

	reset() {
		this.resetPosAndRot();
		this.resetSpeedAndAccel();
		
		this.propagateReset();
	}

	resetSpeedAndAccel() {
		this.setSpeed(this.initialSpeed);
		this.setAcceleration(this.initialAcceleration);
	}
}

var DEFAULT_MASS = 1;

class physicalObject extends movingObject {
	constructor(position, boundingRadius, speedStopThreshold, frictionCoefficient, mass) {
		super(position, boundingRadius, speedStopThreshold, frictionCoefficient);
		this.mass = mass || DEFAULT_MASS;
		this.tentativeSpeed = this.speed.clone();
		this.isPhysicalObject = true;
	}

	getMass() {
		return this.mass;
	}

	setMass(value) {
		this.mass = value;
	}

	setSpeed(v) {
		this.speed.copy(v);
		this.tentativeSpeed.copy(v);
	}

	handleCollision(otherObject, unclip) {
		if(unclip) this.resolveClipping(otherObject);
		this.receiveKineticEnergy(otherObject);
	}

	resolveClipping(otherObject) {
		//if(!otherObject.isCollidableObject) return;
		var unclipDir = this.getBoundingCenter().sub(otherObject.getBoundingCenter());
		unclipDir.setLength(this.getBoundingRadius()+otherObject.getBoundingRadius()-unclipDir.length()+0.1);
		this.tentativePos.add(unclipDir);
	}

	/* elastic collisions formula:
						2 * m2       (v1-v2) . (x1-x2)						m: mass
		v1'	=  v1  -  ----------- * -------------------- * (x1-x2)			v: speed
					    m1 + m2		   ||x1 - x2||^2						x: position
	*/
	receiveKineticEnergy(otherObject) {
		var m1 = this.getMass(); var m2 = otherObject.getMass() || 100*m1;
		var v1 = this.getSpeed(); var v2 = otherObject.getSpeed();
		var x1 = this.getBoundingCenter(); var x2 = otherObject.getBoundingCenter();
		var factor = (2*m2/(m1+m2))*(v1.sub(v2)).dot(x1.clone().sub(x2))/(x1.distanceToSquared(x2));
		this.tentativeSpeed.sub((x1.sub(x2)).multiplyScalar(factor));
	}

	speedUpdate(delta_time) {
		this.speed.copy(this.tentativeSpeed);
		var preFriction = this.speed.add(this.acceleration.clone().multiplyScalar(delta_time));
		var postFriction = this.speed.add(this.getFriction().multiplyScalar(delta_time));
		if(preFriction.dot(postFriction) <= 0 || postFriction.length() <= this.speedStopThreshold) {
			this.speed.set(0,0,0);
		}
		this.tentativeSpeed.copy(this.speed);
	}
}