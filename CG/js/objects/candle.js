class candleStick extends baseObject {
	constructor(position,scale, radiusSegments) {
		super(new THREE.Vector3(0,0,0));
		this.matPhong = CANDLE_MATERIAL[0];
		this.matGouroud = CANDLE_MATERIAL[1];
    	var candle1 = new THREE.CylinderGeometry( scale*0.6, scale*0.8, scale*5, radiusSegments);
    	var mesh = new THREE.Mesh(candle1, this.matGouroud);
    	position.y += (scale*5)/2;
   		mesh.position.y = position.y;

   		this.add(mesh)
   	}
}

class candle extends baseObject {
	constructor(position,scale, intensity, color) {
		var scale = scale || 10;
		var radiusSegments = 10;
		var intensity = intensity || 1;
		var distance = 250;
		var decay = 0 
		var color = color || 0xffffff;
		position.y += (scale*0.36)/2
		super(position);
		this.intensity = intensity;
		this.matPhong = CANDLE_BASE_MATERIAL[0];
		this.matGouroud = CANDLE_BASE_MATERIAL[1];
		this.globalToggle = true;

		var base1 = new THREE.CylinderGeometry( scale*1.10, scale*1.34, scale*0.36, radiusSegments );
    	var mesh = new THREE.Mesh(base1, this.matGouroud);

		var base2 = new THREE.CylinderGeometry( scale*0.7, scale*1.04, scale*0.72, radiusSegments);
    	var mesh2 = new THREE.Mesh(base2, this.matGouroud);
    	position.y += (scale*0.72)/2;
    	mesh2.position.y = position.y;

		var middleHolster = new THREE.CylinderGeometry( scale*0.34, scale*0.48, scale*1.5, radiusSegments);
    	var mesh3 = new THREE.Mesh(middleHolster, this.matGouroud);
    	position.y += (scale*1.5)/2;
    	mesh3.position.y = position.y;

		var middleHolsterTop = new THREE.CylinderGeometry( scale*1.44, scale*0.50, scale*0.48, radiusSegments);
    	var mesh4 = new THREE.Mesh(middleHolsterTop, this.matGouroud);
    	position.y += (scale*0.48)/2;
    	mesh4.position.y = position.y;

    	var top1 = new THREE.CylinderGeometry( scale, 0, scale*1.5, radiusSegments);
    	var mesh5 = new THREE.Mesh(top1, this.matGouroud);
    	position.y += (scale*1.5)/2;
    	mesh5.position.y = position.y;

    	var top2 = new THREE.CylinderGeometry( scale*2, scale*0.5, scale*0.6, radiusSegments);
    	var mesh6 = new THREE.Mesh(top2, this.matGouroud);
    	position.y += (scale*0.8)/2;
    	mesh6.position.y = position.y;

    	this.add(mesh);
    	this.add(mesh2);
    	this.add(mesh3);
    	this.add(mesh4);
    	this.add(mesh5);
    	this.add(mesh6);
    	this.add(new candleStick(position, scale, radiusSegments));
    	

    	this.light = new THREE.PointLight( color, intensity, distance);
		this.light.position.set( position.x, position.y + scale*0.2, position.z);
		scene.add( this.light )
	}

	input(action) {
		if(action == "lightsToggle") {
			this.light.visible = !this.light.visible;
		}
		if(action == "toggleLight" ) {
			if(this.globalToggle) {
				this.light.visible = false;
				this.globalToggle = false;
			}
			else {
				this.light.visible = true;
				this.globalToggle = true;
			}
			

		}
	}
}
