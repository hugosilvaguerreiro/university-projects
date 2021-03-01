class globalLight extends baseObject {
	constructor(position, intensity, color) {
			super(position);
			this.globalToggle = true;
			this.color = color || 0xffffff;
			this.intensity = intensity || 1;
			this.directionalLight = new THREE.DirectionalLight( this.color, this.intensity );
			this.directionalLight.position.set(position.x, position.y, position.z);
			scene.add( this.directionalLight );
	}

	input(action) {
		if(action == "dayNightToggle") {
			this.directionalLight.visible = !this.directionalLight.visible;
		}
		if(action == "toggleLight" ) {
			if(this.globalToggle) {
				this.directionalLight.visible = false;
				this.globalToggle = false;
			}
			else {
				this.directionalLight.visible = true;
				this.globalToggle = true;
			}
			

		}
	}
}