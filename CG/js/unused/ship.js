/* Player ship (not OO -> don't do it like this in the future) */
function createPlayerShip(x, y, z) {
	var ship = new THREE.Object3D();

	material = new THREE.MeshBasicMaterial({ color: 0x00ff00, wireframe: true });

	addPlayerShipBody(ship, 0, +2, +3);
	addPlayerShipGun(ship, 0, 0, -4);
	addPlayerShipGun(ship, +6, 0, 0);
	addPlayerShipGun(ship, -6, 0, 0);

	scene.add(ship);

	ship.position.set(x, y, z);

	return ship;
}

function addPlayerShipBody(obj, x, y, z) {
	geometry = new THREE.CubeGeometry(10, 4, 4); // x, y, z
	mesh = new THREE.Mesh(geometry, material);
	mesh.position.set(x, y, z);

	obj.add(mesh);

	geometry = new THREE.CubeGeometry(6, 4, 8); // x, y, z
	mesh = new THREE.Mesh(geometry, material);
	mesh.position.set(x, y, z);

	obj.add(mesh);
}

function addPlayerShipGun(obj, x, y, z) {
	geometry = new THREE.CubeGeometry(2, 2, 6); // x, y, z
	mesh = new THREE.Mesh(geometry, material);
	mesh.position.set(x, y, z);

	obj.add(mesh);
}
/* end of Player Ship */