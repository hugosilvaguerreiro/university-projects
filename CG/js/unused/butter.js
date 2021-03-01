class butterSlice {
		/*Width, Height, Depth, TopHeight, TopBorderSize, Color and TopColor are optional*/
	constructor(PosX, PosY, PosZ, Width, Height, Depth) {
		var Width = Width || 30;
		var Height = Height || 20;
		var Depth = Depth || 20;
		var Color = 0xffbf00;

		//Collisions
		this.boundingRadius = 1;
		//end--

		this.slice = new THREE.Object3D();

		var angle = Math.PI/4;
		var geometry = new THREE.BoxGeometry( Width/4, Height, Depth);
		var material = new THREE.MeshBasicMaterial( {color: 0xffff00, wireframe: false} );
		var mesh = new THREE.Mesh( geometry, material );
		mesh.position.set(PosX + Width/2 + (Height/2 + Width/8)*Math.sin(angle) , PosY, PosZ ); 
		mesh.rotation.z = angle;
		this.slice.add(mesh);
	}

	setPosition(PosX, PosY, PosZ) {
		this.slice.position.set(PosX, PosY, PosZ)
	}

	setRotation(RotX, RotY, RotZ) {
		this.slice.rotation.set(RotX, RotY, RotZ);
	}

	getObject() {
		return this.slice;
	}
}

class fallenButter {
	constructor(PosX, PosY, PosZ, Width, Height, Depth) {
		var Width = Width || 30;
		var Height = Height || 20;
		var Depth = Depth || 20;
		var Color = 0xffbf00;

		this.fallenButter = new THREE.Object3D();

		var geometry = new THREE.BoxGeometry( Width, Height, Depth);
		var material = new THREE.MeshBasicMaterial( {color: Color, wireframe: false} );
		var mesh = new THREE.Mesh( geometry, material );
		mesh.position.set(PosX , PosY, PosZ );
		this.fallenButter.add(mesh);
		scene.add(this.fallenButter);
	}

	setPosition(PosX, PosY, PosZ) {
		this.fallenButter.position.set(PosX, PosY, PosZ)
	}

	setRotation(RotX, RotY, RotZ) {
		this.fallenButter.rotation.set(RotX, RotY, RotZ);
	}

	getObject() {
		return this.fallenButter;
	}
}

class butterCube {
		/*Width, Height and Depth are optional*/
	constructor(PosX, PosY, PosZ, Width, Height, Depth) {
		var Width = Width || 30;
		var Height = Height || 20;
		var Depth = Depth || 20;
		var Color = 0xffbf00;

		this.slice = new THREE.Object3D();

		var geometry = new THREE.BoxGeometry( Width, Height, Depth);
		var material = new THREE.MeshBasicMaterial( {color: 0xffff00, wireframe: false} );
		var mesh = new THREE.Mesh( geometry, material );
		mesh.rotation.set(0,Math.random()*2*Math.PI,0);
		this.slice.add(mesh);
		scene.add(this.slice);

		var corner = new THREE.Vector3(Width/2, Height/2, Depth/2);
		this.boundingRadius = corner.length()/3;

		this.setPosition(PosX , PosY, PosZ ); 
	}

	setPosition(PosX, PosY, PosZ) {
		this.slice.position.set(PosX, PosY, PosZ)
	}

	setRotation(RotX, RotY, RotZ) {
		this.slice.rotation.set(RotX, RotY, RotZ);
	}

	getObject() {
		return this.slice;
	}

	getTentativePosition() {
		return this.slice.position;
	}
}
class butterPlate {
		/*Width, Height, Depth, TopHeight, TopBorderSize, Color and TopColor are optional*/
	constructor(PosX, PosY, PosZ, Width, Height, Depth) {
		var Width = Width || 30;
		var Height = Height || 20;
		var Depth = Depth || 20;
		var Color = 0xffbf00;


		//base
		this.butterPlate = new THREE.Object3D();
		var angle = Math.PI/4;
		var geometry = new THREE.BoxGeometry( Width + (Height + Width/4)*Math.sin(angle) + 10, Height/4, Depth + 10);
		var material = new THREE.MeshBasicMaterial( {color: 0xd3d3d3, wireframe: false} );
		var mesh = new THREE.Mesh( geometry, material );
		mesh.position.set(PosX+ (Height/2 + Width/8)*Math.sin(angle), PosY - Height/2 - Height/4, PosZ);
		this.butterPlate.add(mesh);

		//front of the plate
		geometry = new THREE.BoxGeometry( Height/4 , Height/2, Depth + 10 + Height/4 );
		material = new THREE.MeshBasicMaterial( {color: 0xffffff, wireframe: false} );
		mesh = new THREE.Mesh( geometry, material );
		mesh.position.set(PosX + (Height/2 + Width/8)*Math.sin(angle) + Width, PosY - Height/2 - Height/8 , PosZ);
		this.butterPlate.add(mesh);

		//back of the plate
		geometry = new THREE.BoxGeometry( Height/4 , Height/2, Depth + 10 + Height/4);
		material = new THREE.MeshBasicMaterial( {color: 0xffffff, wireframe: false} );
		mesh = new THREE.Mesh( geometry, material );
		mesh.position.set(PosX - Width + (Height/2 + Width/8)*Math.sin(angle) , PosY - Height/2 - Height/8 , PosZ);
		this.butterPlate.add(mesh);

		geometry = new THREE.BoxGeometry( Height/4 , Height/2, Width +(Height + Width/4)*Math.sin(angle) + 10  );
		material = new THREE.MeshBasicMaterial( {color: 0xffffff, wireframe: false} );
		mesh = new THREE.Mesh( geometry, material );
		mesh.position.set(PosX + (Height/2 + Width/8)*Math.sin(angle)  , PosY - Height/2 - Height/8  , PosZ - (Depth + 10)/2);
		mesh.rotation.y = Math.PI/2;
		this.butterPlate.add(mesh);

		geometry = new THREE.BoxGeometry( Height/4 , Height/2, Width +(Height + Width/4)*Math.sin(angle) + 10  );
		material = new THREE.MeshBasicMaterial( {color: 0xffffff, wireframe: false} );
		mesh = new THREE.Mesh( geometry, material );
		mesh.position.set(PosX + (Height/2 + Width/8)*Math.sin(angle)  , PosY - Height/2 - Height/8  , PosZ + (Depth + 10)/2);
		mesh.rotation.y = Math.PI/2;
		this.butterPlate.add(mesh);
	}

	setPosition(PosX, PosY, PosZ) {
		this.butterPlate.position.set(PosX, PosY, PosZ);
	}

	setRotation(RotX, RotY, RotZ) {
		this.butterPlate.rotation.set(RotX, RotY, RotZ);
	}

	getObject() {
		return this.butterPlate;
	}
}

class butter extends randomizableObject{
	/*Width, Height and Depth  are optional*/
	constructor(PosX, PosY, PosZ, Width, Height, Depth) {
		var Width = Width || 30;
		var Height = Height || 20;
		var Depth = Depth || 20;
		var Color = 0xffbf00;
		var angle = Math.PI/4; // Don't change this angle
		super();
		this.butter = new THREE.Object3D();
		this.slice = new butterSlice(0,-Height/4,0).getObject();
		this.butterCube = new THREE.Object3D();
		this.butterPlate = new butterPlate(0,0,0).getObject();

		//main butter
		var geometry = new THREE.BoxGeometry( Width, Height, Depth );
		var material = new THREE.MeshBasicMaterial( {color: Color, wireframe: false} );
		var mesh = new THREE.Mesh( geometry, material );
		mesh.position.y = -Height/4
		this.butter.add(mesh);

		//upper cube
		geometry = new THREE.BoxGeometry( Width/4, Height/2, Depth/2);
		material = new THREE.MeshBasicMaterial( {color: 0xffef00, wireframe: false} );
		mesh = new THREE.Mesh( geometry, material );
		mesh.position.set( - Width/4 ,  Height/2 + Width/8 -Height/4, - Width/8);
		mesh.rotation.z = Math.PI/2;
		mesh.rotation.y = Math.PI/6;
		this.butterCube.add(mesh);

		this.butter.add(this.butterCube);
		this.butter.add(this.slice);
		this.butter.add(this.butterPlate);

		this.butter.position.set(PosX, PosY, PosZ);
		scene.add(this.butter);
	}

	setPosition(PosX, PosY, PosZ) {
		this.butter.position.set(PosX, PosY, PosZ)
	}

	setRotation(RotX, RotY, RotZ) {
		this.butter.rotation.set(RotX, RotY, RotZ);
	}

	getObject() {
		return this.butter;
	}
	getPosition(){
		return this.butter.position;
	}
	randomizerUpdater(delta_t) {
		this.setPosition(this.randomizerInputs.x, this.randomizerInputs.y, this.randomizerInputs.z);
	}

	update(delta_t) {

	}
}
