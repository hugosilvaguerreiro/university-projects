class ROM extends baseObject {
	constructor(position, number, radius, scale) {
		super(position);
		var cylgeometry = new THREE.CylinderGeometry( scale*0.02, scale*0.02, scale*0.2, 3);
		if ( typeof ROM.mat1 == 'undefined' || typeof ROM.mat2 == 'undefined' ) { //static values
			console.log("HEY");
			ROM.mat1 = new THREE.MeshBasicMaterial( {color: 0xc0c0c0, wireframe: false} );
			ROM.mat2 = new THREE.MeshBasicMaterial( {color: 0xc0c0c0, wireframe: false} );
		}
		var theta = 0;
		var interval = 2*Math.PI/number;
		for(var i=0; i < number; i++) {
			var newMesh = new THREE.Mesh( cylgeometry, ROM.mat1 )
			newMesh.position.set(radius*Math.cos(theta), radius*Math.sin(theta), 0);
			newMesh.rotation.z = theta-(1/2)*Math.PI;
			console.log(newMesh.position);
			theta += interval;
			this.add( newMesh );
		}
	}

	toggleMesh() {
		for(var i=0; i<this.children.length; i++) {
			this.children[i].mesh.material = 
			(this.children[i].mesh.material == ROM.mat1)? ROM.mat2 : ROM.mat1;
		}
	}

	setWireframe(activated) {
		ROM.mat1.wireframe = activated;
		ROM.mat2.wireframe = activated;
	}

}