function CollisionDetector() {
	
	this.vehicles = [];
	this.woodenLogs = [];
	this.turtles = [];

	this.setObjects = function(vehicles, woodenLogs, turtles, player) {
		this.playerObject = player;
		this.player = new THREE.Box3().setFromObject(player);

		for(var i=0; i<vehicles.length; i++) {
			this.vehicles[i] = { box: new THREE.Box3().setFromObject(vehicles[i]) };
		}

		for(var i=0; i<turtles.length; i++) {
			this.turtles[i] = { box: new THREE.Box3().setFromObject(turtles[i]), pace: turtles[i].movePace };
		}
		
		for(var i=0; i<woodenLogs.length; i++) {
			this.woodenLogs[i] = { box: new THREE.Box3().setFromObject(woodenLogs[i].mesh), pace: woodenLogs[i].movePace };
		}
	}

	this.setCarObject = function(car) {
		this.carObject = car;
		this.car = new THREE.Box3().setFromObject(car);
	}

	this.checkCarCollision = function() {
		if(this.player.intersectsBox(this.car)) {
			return { collided: true, object: "vehicles" };
		}

		return { collided: false, object: "none" };
	}

	this.checkCollision = function(perspectiveCamera) {
        var i;
        const pPos = new THREE.Vector3();
		this.player.getCenter(pPos);
		var pos = perspectiveCamera ? pPos.x : pPos.z;
		var found = false;
		if(pos < 98.6 && pos > 48.5) {
			count = 0;
			for(i=0; i<this.woodenLogs.length; i++) {
				if(this.player.intersectsBox(this.woodenLogs[i].box)) {

					found = true;
					this.playerObject.position.z -=  this.woodenLogs[i].pace;

					if(this.playerObject.position.z > 45 || this.playerObject.position.z < -45){

						return { collided: true, object: "logs" };
					}

					return { collided: false, object: "logs" };
				}	

				else {
					count++;
				}			
			}

			for(i=0; i<this.turtles.length; i++) {
				if(this.player.intersectsBox(this.turtles[i].box)) {

					found = true;
					this.playerObject.position.z += this.turtles[i].pace;

					if(this.playerObject.position.z > 45 || this.playerObject.position.z < -45){

						return { collided: true, object: "turtles" };
					}

					return { collided: false, object: "turtles" };
				}
				
				else {
					count++;
				}			
			}


			if(count == this.woodenLogs.length + this.turtles.length) {
				return { collided: true, object: "water" };
			}
		}
		else {
			for(i=0; i<this.vehicles.length; i++) {
				if(this.player.intersectsBox(this.vehicles[i].box)) {
					//play death animation
					return { collided: true, object: "vehicles" };
				}
			}
		}

		return { collided: false, object: "none" };
	}
}