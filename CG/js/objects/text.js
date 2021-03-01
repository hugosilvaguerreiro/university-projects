class text extends THREE.object3D {
	constructor(position, text, scale) {
		var loader = new THREE.FontLoader();

		loader.load( 'fonts/helvetiker_regular.typeface.json', function ( font ) {

			var geometry = new THREE.TextGeometry( text, {
				font: font,
				size: scale,
				height: 5,
				curveSegments: 12,
				bevelEnabled: true,
				bevelThickness: 10,
				bevelSize: 8,
				bevelSegments: 5
			} );
		} );
		this.add(loader);
	}
}
