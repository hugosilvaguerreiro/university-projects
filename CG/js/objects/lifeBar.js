class lifeBar extends baseObject {
	constructor(position, scale, nrOfLives) {
		var radiusSegments = 10;
		var offset = scale*0.05;
		var width = scale;
		var widthPerLife = scale*4;
		super(position)

		this.matPhong = new THREE.MeshBasicMaterial();
		this.matGouroud = new THREE.MeshBasicMaterial();
		this.nrOfLives = nrOfLives;
		this.nrOfLivesOriginal = nrOfLives
		this.lives = [];


		for(var i=0; i<nrOfLives; i++) {
    		var life = new car(-scale*4, 0, (i+2/3)*(widthPerLife + offset), scale, true);
    		var object = new THREE.Object3D();
    		object.add(life)
    		this.lives[i] = object;
    		this.add(object);
		}
	}

	removeLife() {
		if(this.nrOfLives -1 > 0) {
			console.log(this.nrOfLives)
			this.lives[this.nrOfLives - 1].visible = false;
			this.nrOfLives -= 1;
			return true;
		}
		else {
			this.lives[this.nrOfLives - 1].visible = false;
			this.nrOfLives = this.nrOfLivesOriginal;
			clock.stop();
			pause = true;
			return false;
		}
	}
	getLives() {
		return this.nrOfLives;
	}
	input(action){
		if(action == "reset") {
			for(var i = 0; i<this.nrOfLivesOriginal; i++) {
				this.lives[i].visible = true;
				this.nrOfLives = this.nrOfLivesOriginal;
			}
		}
	}
}