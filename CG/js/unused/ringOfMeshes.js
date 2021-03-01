class ringOfMeshes {
	constructor(geometry, material, number, minRadius, maxRadius, theta, radialFreq, rotateFreq, isStatic) {
		this.ring = new THREE.Object3D();
		this.meshes = [];
		this.number = 0;
		this.geometry = geometry;
		this.material = material;
		this.isStatic = (isStatic != undefined)? isStatic : false;

		this.set(number, minRadius, maxRadius, theta, radialFreq, rotateFreq);
		scene.add(this.ring);
	}

	update(delta_time) {
		if(!this.isStatic)	{
			this.theta += delta_time*this.rotateFreq;//*2*Math.PI;
			this.radiusTheta += delta_time*this.radialFreq;
			var radius = this.radiusAverage + this.radiusDelta*Math.sin(this.radiusTheta);
			for(var i=0; i<this.number; i++) {
				var specificTheta = this.theta+i*(2*Math.PI/this.number);
				this.meshes[i].position.set(radius*Math.cos(specificTheta), radius*Math.sin(specificTheta), 0);
				this.meshes[i].rotation.z += delta_time*this.rotateFreq;
			}
		}
	}

	set(number, minRadius, maxRadius, theta, radialFreq, rotateFreq) {

		if(this.number < number) {
			for(var i=this.number; i < number; i++) {
				var newMesh = new THREE.Mesh( this.geometry, this.material )
				this.meshes.push( newMesh );
				this.ring.add( newMesh );
			}
		} else if (this.number > number) {
			for(var i=number; i < this.number; i++) { 
				var extraMesh = this.meshes.pop();
				scene.remove(extraMesh);
				extraMesh.geometry.dispose();
				extraMesh.material.dispose();
				extraMesh.parent.remove(extraMesh);
				extraMesh = undefined;
			}
		}

		this.number = number;
		this.radiusAverage = (minRadius+maxRadius)/2;
		this.radiusDelta = (maxRadius-minRadius)/2;
		this.radiusTheta = 0;
		this.theta = theta;
		this.radialFreq = radialFreq;
		this.rotateFreq = rotateFreq;

		var interval = 2*Math.PI/number;

		for(var i=0; i<this.number; i++) {
			this.meshes[i].position.set(minRadius*Math.cos(theta), minRadius*Math.sin(theta), 0);
			this.meshes[i].rotation.z = theta-(1/2)*Math.PI;
			theta += interval;
		}
	}

	remove() {
		this.set(0, 0, 0, 0, 0, 0);
		this.meshes = undefined;
	}

	getMesh(i) {
		return this.meshes[i];
	}

	getObject() {
		return this.ring;
	}
}