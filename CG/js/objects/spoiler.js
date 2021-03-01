class cube extends baseObject {
	constructor(position, width, height, depth, basic) {
		super(position);
		if(!basic) {
			this.matPhong = CAR_SPOILTOP_MATERIAL[0];
			this.matPhong.side = THREE.DoubleSide;
			this.matGouroud = CAR_SPOILTOP_MATERIAL[1];
			this.matGouroud.side = THREE.DoubleSide;
		}
		else {
			this.matPhong = CAR_SPOILTOP_MATERIAL[2];
			this.matPhong.side = THREE.DoubleSide;
			this.matGouroud = CAR_SPOILTOP_MATERIAL[2];
			this.matGouroud.side = THREE.DoubleSide;
		}

		var geometry = new THREE.Geometry();

		geometry.vertices.push( new THREE.Vector3(0, 0, 0 ) );//0
		geometry.vertices.push( new THREE.Vector3( 0, 0, depth));//1
		geometry.vertices.push( new THREE.Vector3( width, 0, 0));//2
		geometry.vertices.push( new THREE.Vector3( width, 0, depth));//3
		
		geometry.vertices.push( new THREE.Vector3(0, height, 0 ) );//4
		geometry.vertices.push( new THREE.Vector3( 0, height, depth));//5
		geometry.vertices.push( new THREE.Vector3( width, height, 0));//6
		geometry.vertices.push( new THREE.Vector3( width, height, depth));//7

		geometry.faces.push( new THREE.Face3( 0, 1, 2 ) );
		geometry.faces.push( new THREE.Face3( 3, 2, 1 ) );//down face
		
		
		geometry.faces.push( new THREE.Face3( 4, 5, 6 ) );//up face
		geometry.faces.push( new THREE.Face3( 7, 6, 5 ) );
		
		
		geometry.faces.push( new THREE.Face3( 1, 5, 7 ) );//front face
		geometry.faces.push( new THREE.Face3( 7, 3, 1 ) );
		
		geometry.faces.push( new THREE.Face3( 0, 4, 6 ) );//back face
		geometry.faces.push( new THREE.Face3( 6, 2, 0 ) );
		
		geometry.faces.push( new THREE.Face3( 2, 3, 7 ) );//right face
		geometry.faces.push( new THREE.Face3( 7, 6, 2 ) );
		
		geometry.faces.push( new THREE.Face3( 0, 1, 5 ) );//left face
		geometry.faces.push( new THREE.Face3( 5, 4, 0 ) );

		
		
		geometry.computeFaceNormals();
		geometry.computeVertexNormals();

		var mesh = new THREE.Mesh( geometry, this.matGouroud );
		mesh.position.set(-width/2, -height/2, -depth/2);
		this.add(mesh);
	}

}

class spoiler extends baseObject {
	constructor(PosX, PosY, PosZ, scale, basic) {
		super(new THREE.Vector3(PosX, PosY, PosZ));
		if(!basic) {
			this.matPhong = CAR_BODY_MATERIAL[0];
			this.matPhong.side = THREE.DoubleSide;
			this.matGouroud = CAR_BODY_MATERIAL[1];
			this.matGouroud.side = THREE.DoubleSide;			
		}
		else {
			this.matPhong = CAR_BODY_MATERIAL[2];
			this.matPhong.side = THREE.DoubleSide;
			this.matGouroud = CAR_BODY_MATERIAL[2];
			this.matGouroud.side = THREE.DoubleSide;	
		}


		var geometry = new THREE.Geometry();
		
		var B = scale ;
		var b = scale*0.7;
		var h = scale*0.5; 
		var h2 = scale*0.8;
		var h3 = scale*3;
		var factor = scale;

			//first trapezoid vertices
		geometry.vertices.push( new THREE.Vector3(0, 0, 0 ));//0
		geometry.vertices.push( new THREE.Vector3( B, 0, 0));//1
		geometry.vertices.push( new THREE.Vector3( (B-b)/2, 0,h ));//2
		geometry.vertices.push( new THREE.Vector3( (B+b)/2, 0, h));//3
		geometry.vertices.push( new THREE.Vector3( (B-b)/2, 0, 0));//4
		geometry.vertices.push( new THREE.Vector3( (B+b)/2, 0, 0));//5
		
		//second trapezoid vertices
		geometry.vertices.push( new THREE.Vector3( 0, 0, h3));//6
		geometry.vertices.push( new THREE.Vector3( B, 0, h3));//7
		geometry.vertices.push( new THREE.Vector3( (B-b)/2, 0, h3 -h ));//8
		geometry.vertices.push( new THREE.Vector3( (B+b)/2, 0, h3 -h));//9
		geometry.vertices.push( new THREE.Vector3( (B-b)/2, 0, h3));//10
		geometry.vertices.push( new THREE.Vector3( (B+b)/2, 0, h3));//11
		
		//first side vertices
		geometry.vertices.push( new THREE.Vector3( 0, h2, 0));//12
		geometry.vertices.push( new THREE.Vector3( B, h2, 0));//13
		
		//second side vertices
		geometry.vertices.push( new THREE.Vector3( 0, h2, h3));//14
		geometry.vertices.push( new THREE.Vector3( B, h2, h3));//15
		
		
		geometry.vertices.push( new THREE.Vector3( B/3, h2, 0));//16
		geometry.vertices.push( new THREE.Vector3( B/3, h2, h3));//17
		
		
		//first trapezoid
		geometry.faces.push( new THREE.Face3( 0, 4, 2 ) );//right triangle
		
		geometry.faces.push( new THREE.Face3( 4, 5, 3 ) );//square
		geometry.faces.push( new THREE.Face3( 3, 2, 4 ) );
		
		geometry.faces.push( new THREE.Face3( 5, 1, 3 ) )//left triangle
		
		
		//second trapezoid
		geometry.faces.push( new THREE.Face3( 6, 10, 8 ) );//right triangle
		
		geometry.faces.push( new THREE.Face3( 10, 11, 9 ) );//square
		geometry.faces.push( new THREE.Face3( 9, 8, 10 ) );
		
		geometry.faces.push( new THREE.Face3( 11, 7, 9 ) )//left triangle
		
		//first side
		geometry.faces.push( new THREE.Face3( 0, 12, 1 ) );
		geometry.faces.push( new THREE.Face3( 1, 12, 16 ) );
		
		//second side
		geometry.faces.push( new THREE.Face3( 6, 14, 7 ) );
		geometry.faces.push( new THREE.Face3( 7, 14, 17 ) );
		
		var cubeUp = new cube(new THREE.Vector3(0, scale*0.05, 0), h3 + factor, scale*0.1, B, basic);
		cubeUp.rotation.set(Math.PI/16, Math.PI/2, 0, "YXZ");
		cubeUp.position.y += h2;
		
		geometry.computeFaceNormals();
		geometry.computeVertexNormals();
		
		var spoiler = new THREE.Mesh( geometry, this.matPhong );
		spoiler.position.set(0,0,-h3/2);
		this.add(spoiler);
		this.add(cubeUp);
	}

}