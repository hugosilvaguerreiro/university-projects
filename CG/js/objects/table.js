class table extends baseObject {
	constructor(PosX, PosY, PosZ, Width, Height, WSegments, HSegments) {
		super(new THREE.Vector3(PosX, PosY, PosZ));
		var geometry = new THREE.PlaneGeometry( Width, Height, WSegments,HSegments );
		this.matPhong = TABLE_MATERIAL[0];
		this.matGouroud = TABLE_MATERIAL[1];
		this.mesh = new THREE.Mesh( geometry, this.matGouroud );
		this.mesh.rotation.x = -Math.PI/2;
		this.add(this.mesh);
		scene.add(this);
	}
}
