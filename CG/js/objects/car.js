class carSpotlight extends baseObject {
	constructor(PosX, PosY, PosZ, scale, basic) {
		super(new THREE.Vector3(PosX, PosY, PosZ));

		var geometry = new THREE.SphereGeometry( scale*0.05, 8, 8 );
		if(!basic) {
			this.matPhong = CAR_HEADLIGHT_MATERIAL[0];
			this.matGouroud = CAR_HEADLIGHT_MATERIAL[1];
		}
		else {
			this.matPhong = CAR_HEADLIGHT_MATERIAL[2];
			this.matGouroud = CAR_HEADLIGHT_MATERIAL[2];
		}
		this.mesh = new THREE.Mesh( geometry, this.matGouroud);
		this.add(this.mesh);

		this.spotLight = new THREE.SpotLight( 0xEEE8AA, 2, 100, 0.7, 1, 1 );
		this.spotLight.position.set(scale*0.05,scale*0.02,0);
		this.spotLight.target = this.mesh;
		this.add(this.spotLight);
	}

	on() {
		this.spotLight.intensity = 1;
	}

	off() {
		this.spotLight.intensity = 0;
	}
}

class dome extends baseObject {
	constructor(PosX, PosY, PosZ, scale, basic) {
		super(new THREE.Vector3(PosX, PosY, PosZ));

		var geometry = new THREE.CylinderGeometry(scale*1.5, scale*1.5, scale*2, 16, 1, false, Math.PI/2+0.5, Math.PI-1);
		if(!basic) {
			this.matPhong = CAR_DOME_MATERIAL[0];
			this.matGouroud = CAR_DOME_MATERIAL[1];
		}
		else {
			this.matPhong = CAR_DOME_MATERIAL[2];
			this.matGouroud = CAR_DOME_MATERIAL[2];
		}
		this.mesh = new THREE.Mesh( geometry, this.matGouroud );
		this.mesh.rotation.set(Math.PI/2, 0, 0);
		//this.cyl.position.set(0, 0, scale*0.75);
		this.add(this.mesh);
	}
}

class rims extends baseObject {
	constructor(PosX, PosY, PosZ, number, radius, scale, basic) {
		super(new THREE.Vector3(PosX, PosY, PosZ));
		var cylgeometry = new THREE.CylinderGeometry( scale*0.02, scale*0.02, scale*0.2, 3);
		if(!basic) {
			this.matPhong = CAR_RIMS_MATERIAL[0];
			this.matGouroud = CAR_RIMS_MATERIAL[1];
		}
		else {
			this.matPhong = CAR_RIMS_MATERIAL[2];
			this.matGouroud = CAR_RIMS_MATERIAL[2];
		}
		var theta = 0;
		var interval = 2*Math.PI/number;
		for(var i=0; i < number; i++) {
			var newMesh = new THREE.Mesh( cylgeometry, this.matGouroud );
			newMesh.position.set(radius*Math.cos(theta), radius*Math.sin(theta), 0);
			newMesh.rotation.z = theta-(1/2)*Math.PI;
			theta += interval;
			this.add( newMesh );
		}
	}
}


class wheelHub extends baseObject {
	constructor(PosX, PosY, PosZ, scale, basic) {
		super(new THREE.Vector3(PosX, PosY, PosZ));

		var geometry = new THREE.SphereGeometry( scale*0.1, 8, 8 );
		if(!basic) {
			this.matPhong = CAR_HUB_MATERIAL[0];
			this.matGouroud = CAR_HUB_MATERIAL[1];
		}
		else {
			this.matPhong = CAR_HUB_MATERIAL[2];
			this.matGouroud = CAR_HUB_MATERIAL[2];
		}
		this.mesh = new THREE.Mesh( geometry, this.matGouroud);
		this.hubRims = new rims(0, 0, 0, 10, scale*0.2, scale);

		this.add(this.mesh);
		this.add(this.hubRims);
	}
}

class wheel extends baseObject {
	constructor(PosX, PosY, PosZ, scale, basic) {
		super(new THREE.Vector3(PosX, PosY, PosZ));

		this.radius = scale*.4
		var geometry = new THREE.TorusGeometry( scale*.4, scale * .15, 8, 20 );
		if(!basic) {
			this.matPhong = CAR_WHEEL_MATERIAL[0];
			this.matGouroud = CAR_WHEEL_MATERIAL[1];
		}
		else {
			this.matPhong = CAR_WHEEL_MATERIAL[2];
			this.matGouroud = CAR_WHEEL_MATERIAL[2];
		}
		
		this.mesh = new THREE.Mesh( geometry, this.matGouroud );
		this.hub = new wheelHub(0, 0, 0, scale);
		this.mesh.rotation.set(Math.PI/2,0,0);
		this.hub.setRotation(Math.PI/2,0,0);
		this.wheelJoint = new THREE.Object3D();
		this.wheelJoint.add(this.mesh);
		this.wheelJoint.add(this.hub);
		this.add(this.wheelJoint);
	}

	spin(angle) {
		this.wheelJoint.rotateOnAxis(this.up, angle);
		this.wheelJoint.rotateOnAxis(this.up, angle);
	}

}


class axleAndWheel extends baseObject {
	constructor(PosX, PosY, PosZ, scale, basic) {
		super(new THREE.Vector3(PosX, PosY, PosZ));

		var geometry = new THREE.CylinderGeometry( scale*0.1, scale*0.1, scale*3, 10);
		if ( !basic ) {
			this.matPhong = CAR_HUB_MATERIAL[0];
			this.matGouroud = CAR_HUB_MATERIAL[1];
		}
		else {
			this.matPhong = CAR_HUB_MATERIAL[2];
			this.matGouroud = CAR_HUB_MATERIAL[2];
		}
		
		this.mesh = new THREE.Mesh( geometry, this.matGouroud );
		this.leftWheel = new wheel(0, 1.5*scale, 0, scale);
		this.rightWheel = new wheel(0, -1.5*scale, 0, scale);

		this.add(this.mesh);
		this.add(this.leftWheel);
		this.add(this.rightWheel);
	}

	turnWheels(angle) {
		this.leftWheel.setRotation(angle,0,0);
		this.rightWheel.setRotation(angle,0,0);
	}
	rotateWheels(angle) {
		this.leftWheel.spin(angle);
		this.rightWheel.spin(angle);
	}

}


class car extends physicalObject {
	constructor(PosX, PosY, PosZ, scale, basic) {
		super(new THREE.Vector3(PosX, PosY, PosZ), 3*scale, 5, 20, 100);
		this.basic = basic || false;
		this.setBoundingOffset(new THREE.Vector3(0,-3,0));
		//Movement Defaults (Simple version)
		this.direction = new THREE.Vector3(-1,0,0);
		this.initialDirection = this.direction.clone();

		this.stuck = false;
		this.keyInputs = []; //up, down, left, right, lightsOn
		this.keyInputs["up"] = this.keyInputs["down"] = this.keyInputs["left"]
		= this.keyInputs["right"] = this.keyInputs["lightsOn"] = false;
		this.turnDirection = 0; //positive means left
		this.turnSpeed = 3;

		this.forwardMaxSpeed = 250;
		this.backwardsMaxSpeed = 100;
		this.globalToggle = true;
		//Creation

		this.ornament = new ornament(-scale*2.4, scale*0.12, 0, scale*0.05, this.basic);
		this.ornament.setRotation(0,0,Math.PI/16);
		this.spoiler = new spoiler(scale*1.6, scale*0.3, 0, scale*0.7, this.basic);
		this.spoiler.setRotation(0, Math.PI, 0);
		this.chassis = new chassis(0, -scale*0.5, 0, scale*0.9, this.basic);
		this.rearAxis = new axleAndWheel(scale*1.5, -scale*0.3, 0, scale, this.basic);
		this.frontAxis = new axleAndWheel(-scale*1.7, -scale*0.3, 0, scale, this.basic);
		this.rearAxis.setInitialRotation(Math.PI/2, Math.PI/2, 0);
		this.frontAxis.setInitialRotation(Math.PI/2, Math.PI/2, 0);

		this.dome = new dome(0, -scale*.7, scale*0, scale, basic);

		this.carSpotLeft = new carSpotlight(-scale*2.65, 0, +0.8*scale, scale);
		this.carSpotRight = new carSpotlight(-scale*2.65, 0, -0.8*scale, scale);

		this.add(this.carSpotLeft);
		this.add(this.ornament);
		this.add(this.spoiler);
		this.add(this.chassis);
		this.add(this.dome);
		this.add(this.rearAxis);
		this.add(this.frontAxis);
		this.add(this.carSpotLeft);
		this.add(this.carSpotRight);
	}

	setRotation(RotX, RotY, RotZ) {
		this.rotation.set(RotX, RotY, RotZ);
		this.direction.set(-1,0,0);
		this.direction.applyAxisAngle(this.up, RotY);
		this.speed.copy(this.direction.clone().multiplyScalar(this.speed.length()));
		this.tentativeSpeed.copy(this.speed);
	}

	axisRotation(axis, angle) {
		this.rotateOnAxis(axis, angle);
		this.direction.applyAxisAngle(axis, angle); //assuming you'll never rotate around the other axis
		this.speed.applyAxisAngle(axis, angle);
		this.tentativeSpeed.applyAxisAngle(axis, angle);
		this.acceleration.applyAxisAngle(axis, angle);
	}

	setInitialRotation(RotX, RotY, RotZ) {
		this.setRotation(RotX, RotY, RotZ);
		this.initialRotation.copy(this.rotation);
		this.initialDirection.copy(this.direction);
	}
	addLives(lives) {
		this.lives = lives
	}
	input(action) {
		//Up Arrow Key
		switch(action) {
			case "up":
				this.keyInputs["up"] = true;
				break;
			case "down":
				this.keyInputs["down"] = true;
				break;
			case "left":
				this.keyInputs["left"] = true;
				break;
			case "right":
				this.keyInputs["right"] = true;
				break;
			case "upRelease":
				this.keyInputs["up"] = false;
				break;
			case "downRelease":
				this.keyInputs["down"] = false;
				break;
			case "leftRelease":
				this.keyInputs["left"] = false;
				break;
			case "rightRelease":
				this.keyInputs["right"] = false;
				break;
			case "carLightsToggle":
				this.keyInputs["lightsOn"] = !this.keyInputs["lightsOn"];
				break;
			case "toggleLight":
				if(this.globalToggle) {
					this.keyInputs["lightsOn"] = false;
					this.globalToggle = false;
				}
				else {
					this.globalToggle = true;
				}
			
			default:
				break;
		}
	}


	simpleInputInterpret() {
		if(this.keyInputs["lightsOn"]) {
			this.carSpotLeft.on();
			this.carSpotRight.on();
		} else {
			this.carSpotLeft.off();
			this.carSpotRight.off();
		}
		if(pause) {return;}
		if(this.keyInputs["up"] && !this.keyInputs["down"] && !this.stuck) {
			if(this.speed.dot(this.direction) < 0)
				this.acceleration = this.direction.clone().multiplyScalar(+200); //moving backwards --> using breaks
			else
				this.acceleration = this.direction.clone().multiplyScalar(+75); //moving forward --> using engine
		} else if(this.keyInputs["down"] && !this.keyInputs["up"] && !this.stuck) {
			if(this.speed.dot(this.direction) >= 0)
				this.acceleration = this.direction.clone().multiplyScalar(-200) //moving forward --> using breaks
			else
				this.acceleration = this.direction.clone().multiplyScalar(-45) //moving backwards --> using engine
		} else {
			this.acceleration.setLength(0);
		}

		var turn = 0; //positive means left
		if(this.keyInputs["left"] && !this.keyInputs["right"]) {
			turn = 1;
		} else if(this.keyInputs["right"] && !this.keyInputs["left"]) {
			turn = -1;
		} else { //both keys or no keys --> keep current direction
			turn = 0;
		}
		this.turnDirection = turn;


	}

	getTurnSpeed() {
		var maxTurnAtSpeed = 50
		if(Math.abs(this.speed.length()) > maxTurnAtSpeed) {
			return this.turnSpeed - this.speed.length()/this.forwardMaxSpeed;
		} else if(Math.abs(this.speed.length()) > this.speedStopThreshold) {
			return Math.max(0, (this.speed.length()-this.speedStopThreshold)/maxTurnAtSpeed*this.turnSpeed);
		} else 
			return 0;
	}

	simpleRotationUpdate(delta_time) {
		//Setting speed within bounds
		if(this.speed.dot(this.direction) < 0) this.speed.setLength(Math.min(this.backwardsMaxSpeed, this.speed.length()));
		else this.speed.setLength(Math.min(this.forwardMaxSpeed, this.speed.length()));
		this.axisRotation(this.up, this.turnDirection*this.getTurnSpeed()*delta_time*Math.sign(this.speed.dot(this.direction)));
		this.frontAxis.turnWheels(this.turnDirection/3);
		this.frontAxis.rotateWheels(this.speed.dot(this.direction)*delta_time/(this.frontAxis.leftWheel.radius*2*Math.PI));
		this.rearAxis.rotateWheels(this.speed.dot(this.direction)*delta_time/this.frontAxis.leftWheel.radius);
	}

	getTentativePosition() {
		return this.tentativePos.clone();
	}

	butterCollision() {
		this.stuck = true;
		this.resetSpeedAndAccel();
	}
	orangeCollision() {
		if(!this.lives.removeLife()) {
			//gamePause();
			dead = true;
		}
	}
	tableCollision() {
		if(!this.lives.removeLife()) {
			//gamePause();
			dead = true;
		}
	}

	speedUpdate(delta_time) {
		this.speed.copy(this.tentativeSpeed);
		this.simpleInputInterpret();
		var preFriction = this.speed.add(this.acceleration.clone().multiplyScalar(delta_time));
		var postFriction = this.speed.add(this.getFriction().multiplyScalar(delta_time));
		if(preFriction.dot(postFriction) <= 0 ||
		(postFriction.length() <= this.speedStopThreshold)&&this.acceleration.length() == 0) {
			this.speed.set(0,0,0);
		}
		this.simpleRotationUpdate(delta_time);
		this.tentativeSpeed.copy(this.speed);
	}

	reset() {
		this.setPosition(this.initialPosition.x, this.initialPosition.y, this.initialPosition.z);
		this.setRotation(this.initialRotation.x, this.initialRotation.y, this.initialRotation.z);//this.setRotation(0,Math.PI,0);
		this.resetSpeedAndAccel();
		this.stuck = false;
		//this.propagateReset();
	}
}

class chassis extends baseObject {
	constructor(PosX, PosY, PosZ, scale, basic) {
		super(new THREE.Vector3(PosX, PosY, PosZ));

		var geometry = new THREE.Geometry();

		//vertices
		var front = [[0,0.65,0.5],[0,0.65,3],[1.5,0.75,0],[1.5,1,0.5],[1.5,1,3],[1.5,0.75,3.5]];
		var back = [[4,0.75,0],[4,1,0.5],[4,1,3],[4,0.75,3.5],[5,0.75,0.5],[5,0.75,3]];
		var under = [[0,0,3],[1.5,0,3.5],[4,0,3.5],[5,0,3],[0,0,0.5],[1.5,0,0],[4,0,0],[5,0,0.5]];
		var nose = [[-0.25,0.5,1],[-0.25,0.5,2.5],[-0.25,0,1],[-0.25,0,2.5]];
		var allV = front.concat(back).concat(under).concat(nose);

		for(var i=0; i<allV.length; i++) {
			var vertex = new THREE.Vector3(allV[i][0], allV[i][1], allV[i][2]);
			vertex.multiplyScalar(scale);
			geometry.vertices.push(vertex);
		}

		//faces
		var top = [[0,3,2],[0,1,3],[1,4,3],[1,5,4],[2,3,6],[3,7,6],					//front
		[3,4,7],[4,8,7],[4,5,8],[5,9,8],[6,7,10],[7,8,10],[8,11,10],[8,9,11],		//middle and back
		[0,20,1],[20,21,1]];														//nose top
		var sides = [[1,12,5],[12,13,5],[5,13,14],[14,9,5],[14,11,9],[14,15,11],	//left (driver perspective)
		[0,2,16],[16,2,17],[18,17,2],[18,2,6],[18,6,10],[18,10,19],					//right
		[21,23,12],[21,12,1],[16,20,0],[16,22,20],									//nose left, right
		[22,21,20],[22,23,21],[15,10,11],[15,19,10]];								//front and back
		var allF = top.concat(sides);

		for(var i=0; i<allF.length; i++) {
			var face = new THREE.Face3(allF[i][0], allF[i][1], allF[i][2]);
			geometry.faces.push(face);
		}

		//the face normals and vertex normals can be calculated automatically if not supplied above
		geometry.computeFaceNormals();
		geometry.computeVertexNormals();
		if(!basic) {
			this.matPhong = CAR_BODY_MATERIAL[0];
			this.matGouroud = CAR_BODY_MATERIAL[1];
		}
		else {
			this.matPhong = CAR_BODY_MATERIAL[2];
			this.matGouroud = CAR_BODY_MATERIAL[2];
		}
		
		this.mesh = new THREE.Mesh( geometry, this.matGouroud );
		this.mesh.position.set(-scale*5.5/2, 0, -scale*3.5/2);
		this.add(this.mesh);
	}
}

class ornament extends baseObject {
	constructor(PosX, PosY, PosZ, scale, basic) {
		super(new THREE.Vector3(PosX, PosY, PosZ));

		var geometry = new THREE.Geometry();

		//vertices
		var allV = [[0,0,2.5],[3,0,0],[3,0,2],[3,0,3],[3,0,5],[4,0,2.5],[5,0,1],[5,0,4]];

		for(var i=0; i<allV.length; i++) {
			var vertex = new THREE.Vector3(allV[i][0], allV[i][1], allV[i][2]);
			vertex.multiplyScalar(scale);
			geometry.vertices.push(vertex);
		}

		//faces
		var allF = [[0,4,1],[1,2,6],[2,3,5],[3,4,7]];

		for(var i=0; i<allF.length; i++) {
			var face = new THREE.Face3(allF[i][0], allF[i][1], allF[i][2]);
			geometry.faces.push(face);
		}

		//the face normals and vertex normals can be calculated automatically if not supplied above
		geometry.computeFaceNormals();
		geometry.computeVertexNormals();
		if(!basic) {
			this.matPhong = CAR_ORNAMENT_MATERIAL[0];
			this.matGouroud = CAR_ORNAMENT_MATERIAL[1];
		}
		else {
			this.matPhong = CAR_ORNAMENT_MATERIAL[2];
			this.matGouroud = CAR_ORNAMENT_MATERIAL[2];
		}
		this.mesh = new THREE.Mesh( geometry, this.matGouroud );
		this.mesh.position.set(0, 0, -scale*5/2);
		this.add(this.mesh);
	}
}