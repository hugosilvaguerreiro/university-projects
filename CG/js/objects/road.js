'use strict'

var RoadSpaceBetweenSegmentsDEFAULT = 20;
var RoadSegmentWidthDEFAULT = 30;
var TorusRadiusDEFAULT = 3;
var TorusTubeRadiusDEFAULT = 1;
var ColorDEFAULT = 0xaaaaaa;
var cheerioList = [];
var RadialSegmentsDEFAULT = 5;
var TubularSegmentsDEFAULT = 8;

function wrapToPI(Angle) {
	if(Angle > 0 && Angle < 2*Math.PI) {
			return Angle;
	}
	else {
		var aux = Angle/(2*Math.PI);
		var result = (Angle - Math.floor(aux)*2*Math.PI)
		return result;
	}

}

class roadSegment {
	//RoadSegmentWidth, TorusRadius, TorusTubeRadius and Color are optional
	constructor(PosX, PosY, PosZ, RoadSegmentWidth, TorusRadius, TorusTubeRadius, Color) {
		var RoadSegmentWidth = RoadSegmentWidth || RoadSegmentWidthDEFAULT;
		var TorusRadius = TorusRadius || TorusRadiusDEFAULT;
		var TorusTubeRadius = TorusTubeRadius || TorusRadiusDEFAULT // 3:1 ratio between the radius and the tube.
		var Color = Color || ColorDEFAULT;

		var Direction = Direction ||0;

		this.roadSegment = new THREE.Object3D();

		var leftTorus = new cheerio(TorusRadiusDEFAULT, TorusTubeRadiusDEFAULT, RadialSegmentsDEFAULT, TubularSegmentsDEFAULT, MassDEFAULT);
		var rightTorus = new cheerio(TorusRadiusDEFAULT, TorusTubeRadiusDEFAULT, RadialSegmentsDEFAULT, TubularSegmentsDEFAULT, MassDEFAULT);

		leftTorus.getObject().position.set( - RoadSegmentWidth, 0, 0);
		console.log(leftTorus.getObject().position)
		rightTorus.getObject().position.set(RoadSegmentWidth, 0, 0);
		cheerioList.push(leftTorus);
		cheerioList.push(rightTorus);

		this.roadSegment.add(leftTorus.getObject());
		this.roadSegment.add(rightTorus.getObject());
		this.roadSegment.position.set(PosX, PosY, PosZ);
		
	}
	setPosition(PosX, PosY, PosZ) {
		this.roadSegment.position.set(PosX, PosY, PosZ)
	}

	setRotation(RotX, RotY, RotZ) {
		this.roadSegment.rotation.set(RotX, RotY, RotZ);
	}

	getObject() {
		return this.roadSegment;
	}
}


class straightRoad {
	constructor(PosX, PosY, PosZ, Direction, nrSegments, RoadSpaceBetweenSegments, RoadSegmentWidth, TorusRadius, TorusTubeRadius, Color) {
		var RoadSpaceBetweenSegments = RoadSpaceBetweenSegments || RoadSpaceBetweenSegmentsDEFAULT;
		var RoadSegmentWidth = RoadSegmentWidth || RoadSegmentWidthDEFAULT;
		var TorusRadius = TorusRadius || TorusRadiusDEFAULT;
		var TorusTubeRadius = TorusTubeRadius || TorusRadiusDEFAULT // 3:1 ratio between the radius and the tube.
		var Color = Color || ColorDEFAULT;

		var RadialSegmentsDEFAULT = 16;
		var TubularSegmentsDEFAULT = 18;

		this.nrSegments = nrSegments - 1;

		this.straightRoad = new THREE.Object3D();
		var x = 0;
		var y = 0;
		var z = 0;

		for(var i = 0; i < nrSegments; i++) {
			var segment = new roadSegment(x, y, z,
										  RoadSegmentWidth, 
										  TorusRadius, 
										  TorusTubeRadius, 
										  Color);

			this.straightRoad.add(segment.getObject());
			z += 2*TorusRadius + RoadSpaceBetweenSegments;
		}
		this.straightRoad.rotation.y = Direction;
		this.straightRoad.position.set(PosX, PosY, PosZ);
		
	}
	setPosition(PosX, PosY, PosZ) {
		this.straightRoad.position.set(PosX, PosY, PosZ)
	}

	setRotation(RotX, RotY, RotZ) {
		this.straightRoad.rotation.set(RotX, RotY, RotZ);
	}
	//returns the position of the last road segment inserted in the straight road
	getPosition() {
		var children = this.straightRoad.children;
		return children[this.nrSegments].position;
	}

	getObject() {
		return this.straightRoad;
	}

}


class roadCircle {
	constructor(PosX, PosY, PosZ, Radius, Angle, RoadSpaceBetweenSegments, RoadSegmentWidth, TorusRadius, TorusTubeRadius, Color) {
		//STYLE VARIABLE
		this.RoadSpaceBetweenSegments = RoadSpaceBetweenSegments || RoadSpaceBetweenSegmentsDEFAULT;
		this.RoadSegmentWidth = RoadSegmentWidth || RoadSegmentWidthDEFAULT;
		this.TorusRadius = TorusRadius || TorusRadiusDEFAULT;
		this.TorusTubeRadius = TorusTubeRadius || TorusTubeRadiusDEFAULT // 3:1 ratio between the radius and the tube.
		this.Color = Color || ColorDEFAULT;
		this.Radius = Radius;

		this.roadCircle = new THREE.Object3D();

		var anglePerSegment = (2*this.TorusRadius + this.RoadSpaceBetweenSegments)/Radius;
		var perimeter = Radius*Angle;
		var nrSegmentsNeeded = Math.round(perimeter / (2*TorusRadius + RoadSpaceBetweenSegments));
		var angle = 0;

		for(var i=0; i<nrSegmentsNeeded; i++) {
			var torus = new cheerio(TorusRadiusDEFAULT, TorusTubeRadiusDEFAULT, RadialSegmentsDEFAULT, TubularSegmentsDEFAULT, MassDEFAULT);
			angle = i*anglePerSegment;
			torus.getObject().position.set(Radius*Math.sin(angle), 0, Radius*Math.cos(angle));
			cheerioList.push(torus);
			this.roadCircle.add(torus.getObject());
		}
		this.roadCircle.position.set(PosX, PosY, PosZ);
	}
	setPosition(PosX, PosY, PosZ) {
		this.roadCircle.position.set(PosX, PosY, PosZ)
	}

	setRotation(RotX, RotY, RotZ) {
		this.roadCircle.rotation.set(RotX, RotY, RotZ);
	}

	getObject() {
		return this.roadCircle;
	}

}
class curvedRoad {
	constructor(PosX, PosY, PosZ, Radius, Angle, RoadSpaceBetweenSegments, RoadSegmentWidth, TorusRadius, TorusTubeRadius, Color) {
		//STYLE VARIABLE
		this.RoadSpaceBetweenSegments = RoadSpaceBetweenSegments || RoadSpaceBetweenSegmentsDEFAULT;
		this.RoadSegmentWidth = RoadSegmentWidth || RoadSegmentWidthDEFAULT;
		this.TorusRadius = TorusRadius || TorusRadiusDEFAULT;
		this.TorusTubeRadius = TorusTubeRadius || TorusTubeRadiusDEFAULT // 3:1 ratio between the radius and the tube.
		this.Color = Color || ColorDEFAULT;
		this.Radius = Radius;

		this.curvedRoad = new THREE.Object3D();

		var curvedRd1 = new roadCircle(PosX, PosY, PosZ, Radius, Angle,
										this.RoadSpaceBetweenSegments,
										this.RoadSegmentWidth,
									    this.TorusRadius,
									  	this.TorusTubeRadius,
									  	this.Color);
		var curvedRd2 = new roadCircle(PosX, PosY, PosZ,
										Radius + 2*this.RoadSegmentWidth,
										Angle,
										this.RoadSpaceBetweenSegments,
										this.RoadSegmentWidth,
									    this.TorusRadius,
									  	this.TorusTubeRadius,
									  	this.Color);
		this.curvedRoad.add(curvedRd1.getObject());
		this.curvedRoad.add(curvedRd2.getObject());
	}
	setPosition(PosX, PosY, PosZ) {
		this.curvedRoad.position.set(PosX, PosY, PosZ)
	}

	setRotation(RotX, RotY, RotZ) {
		this.curvedRoad.rotation.set(RotX, RotY, RotZ);
	}

	getObject() {
		return this.curvedRoad;
	}
}

//USE THIS CLASS FOR ROAD CREATION, IGNORE THE OTHERS
class road {

	constructor(PosX, PosY, PosZ, RoadSpaceBetweenSegments, RoadSegmentWidth, TorusRadius, TorusTubeRadius, Color) {
		//STYLE VARIABLE
		this.RoadSpaceBetweenSegments = RoadSpaceBetweenSegments || RoadSpaceBetweenSegmentsDEFAULT;
		this.RoadSegmentWidth = RoadSegmentWidth || RoadSegmentWidthDEFAULT;
		this.TorusRadius = TorusRadius || TorusRadiusDEFAULT;
		this.TorusTubeRadius = TorusTubeRadius || TorusTubeRadiusDEFAULT // 3:1 ratio between the radius and the tube.
		this.Color = Color || ColorDEFAULT;

		this.road = new THREE.Object3D();
		//POSITION VARIABLES
		this.PosX = PosX;
		this.PosY = PosY;
		this.PosZ = PosZ; // let this one be 0 or the location of the base of the table
		this.Direction = 0; //rotation around y axis

		//CLASS SPECIFIC VARIABLES
		this.nrRoadSegments = 0; //number of road segments
		this.RoadBuilding = false; //atribute that controls wheter youre building a road or not
	}
	//PosX, PosY , PosZ and Direction are optional. 
	//You need to call this method everytime you want to start building a road at the given point
	roadBegin(Direction, PosX, PosY, PosZ) {
		this.RoadBuilding = true;
		this.Direction = Direction || this.Direction;
		this.Direction = wrapToPI(this.Direction);
		console.log(this.Direction);
		this.PosX = PosX || this.PosX;
		this.PosY = PosY || this.PosY;
		this.PosZ = PosZ || this.PosZ;
	}
	//Use this one to build a single segment of the road
	segment() {
		if(!this.RoadBuilding) {
			console.log("Error in segment: You need to start building a road first");
		}
		else {
			var segment = new roadSegment(this.PosX,
									    	this.PosY,
										    this.PosZ,
										    this.RoadSegmentWidth,
										    this.TorusRadius,
										  	this.TorusTubeRadius,
										  	this.Color);
			this.road.add(segment.getObject());
			this.PosZ += (2*this.TorusRadius + this.RoadSpaceBetweenSegments)*Math.cos(this.Direction);
			this.PosX += (2*this.TorusRadius + this.RoadSpaceBetweenSegments)*Math.sin(this.Direction);
			segment.getObject().rotation.y = this.Direction;
		}
	}
	//Use this one to build a straight road made out of nrSegments segments.
	straightRoad(nrSegments) {
		if(!this.RoadBuilding) {
			console.log("Error in straightRoad: You need to start building a road first");
		}
		else {
			if(nrSegments && nrSegments > 0) {
				var road = new straightRoad(this.PosX,
										    this.PosY,
										    this.PosZ,
										    this.Direction,
										    nrSegments,
										    this.RoadSpaceBetweenSegments,
										    this.RoadSegmentWidth,
										    this.TorusRadius,
										  	this.TorusTubeRadius,
										  	this.Color);
				this.road.add(road.getObject());
				this.PosZ += nrSegments*(2*this.TorusRadius + this.RoadSpaceBetweenSegments)*Math.cos(this.Direction);
				this.PosX += nrSegments*(2*this.TorusRadius + this.RoadSpaceBetweenSegments)*Math.sin(this.Direction);

			}			
		}
	}

	roadCurve(Angle, Radius, TurnDirection) {
		var TurnDirection = TurnDirection || 1; //Not implemented yet
		if(this.RoadBuilding) {
			var curvedRd = new curvedRoad(0,
											0,
											0,
											Radius,
											Angle,
											this.RoadSpaceBetweenSegments,
											this.RoadSegmentWidth,
										    this.TorusRadius,
										  	this.TorusTubeRadius,
										  	this.Color);

			curvedRd.setRotation(0, this.Direction + 3*Math.PI/2, 0);
			curvedRd.setPosition(this.PosX - (Radius + this.RoadSegmentWidth)*Math.sin(this.Direction + 3*Math.PI/2), this.PosY, this.PosZ- (Radius + this.RoadSegmentWidth)*Math.cos(this.Direction + 3*Math.PI/2) );

			this.PosZ = this.PosZ  + (Radius + this.RoadSegmentWidth)*Math.cos(this.Direction + 3*Math.PI/2 + Angle) - (Radius + this.RoadSegmentWidth)*Math.cos(this.Direction + 3*Math.PI/2);
			this.PosX =	this.PosX +  (Radius + this.RoadSegmentWidth)*Math.sin(this.Direction + 3*Math.PI/2 + Angle) -(Radius + this.RoadSegmentWidth)*Math.sin(this.Direction + 3*Math.PI/2);
			
			
			this.Direction += Angle ;
			this.Direction = wrapToPI(this.Direction);		
			this.road.add(curvedRd.getObject());
								
		}
		else  {
			console.log("Error in roadEnd: You need to start building a road first");
		}

	}

	roadEnd() {
		if(this.RoadBuilding) {
			scene.add(this.road); 
		}
		else  {
			console.log("Error in roadEnd: You need to start building a road first");
		}
		/*for(var i=0; i<cheerioList.length; i++) {

			scene.add(cheerioList[i].getObject())
		}*/
	}
	
	setPosition(PosX, PosY, PosZ) {
		this.road.position.set(PosX, PosY, PosZ)
				console.log(this.road)
	}
}


