class butter extends collidableObject {
	constructor(position, Width, Height, Depth) {
		var corner = new THREE.Vector3(Width/2, Height/2, Depth/2);
		super(position, corner.length()/3);

		var geometry = new THREE.BoxGeometry( Width, Height, Depth);
		this.matPhong = BUTTER_MATERIAL[0];
		this.matGouroud = BUTTER_MATERIAL[1];
		this.mesh = new THREE.Mesh( geometry, this.matGouroud );
		this.add(this.mesh);
		scene.add(this);
	}
}