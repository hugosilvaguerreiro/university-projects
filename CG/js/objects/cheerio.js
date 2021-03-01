class cheerio extends physicalObject {
	constructor(position, radius, tubeRadius, radialSegments, tubularSegments, mass) {
		super(position);
		this.setMass(mass);
		this.setBoundingRadius(radius+tubeRadius);
		var geometry = new THREE.TorusGeometry(radius, tubeRadius, radialSegments, tubularSegments);
		var color = Math.floor(Math.random() * 5);
		this.matPhong = CHEERIO_MATERIALS[color][0];
		this.matGouroud = CHEERIO_MATERIALS[color][1];
    	this.mesh = new THREE.Mesh( geometry, this.matGouroud );
    	this.mesh.rotation.x = Math.PI/2;
    	this.add(this.mesh);
	}
}