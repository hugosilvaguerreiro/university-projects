/* Moon invader (OO -> do it like this in the future) */
class moonInvader {
	constructor(PosX, PosY, PosZ, sphereRadius) {
		this.moon = new THREE.Object3D();
		var geometry = new THREE.SphereGeometry(sphereRadius, 32, 32);
		var material = new THREE.MeshBasicMaterial( {color: 0xff0000} );
		
		var sphere = new THREE.Mesh( geometry, material );
		this.moon.add(sphere);
		geometry = new THREE.ConeGeometry(2, 5, 32);
		material = new THREE.MeshBasicMaterial( {color: 0xff0000} );
		var number = 4;
		this.ring = new ringOfMeshes(geometry, material, number, 10, 18, Math.PI/4, Math.PI/2, Math.PI/4);

		this.moon.add(this.ring.getObject());
		console.log(this.ring.getObject());

		scene.add(this.moon);
		this.moon.position.set(PosX, PosY, PosZ);
	}

	ringSet(number, minRadius, maxRadius, theta, radialFreq, rotateFreq) {
		this.ring.set(number, minRadius, maxRadius, theta, radialFreq, rotateFreq);
	}

	update(delta_time) {
		this.ring.update(delta_time);
	}

	remove() {
		this.ring.remove();
		for (var i = this.moon.children.length - 1; i >= 0; i--) {
    		this.moon.remove(this.moon.children[i]);
		}
		scene.remove(this.moon);
	}

	getObject() {
		return this.moon;
	}

	getMesh(i) {
		return this.ring.getMesh(i);
	}
}

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
/* end of Moon Invader */
