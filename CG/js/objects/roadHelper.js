'use strict'

var TorusRadiusDEFAULT = 5;
var TorusTubeRadiusDEFAULT = 3;
var RadialSegmentsDEFAULT = 14;
var TubularSegmentsDEFAULT = 14;
var MassDEFAULT = 100;

function straightLine(spacing, start, end, includeLast) {
	var doLast = includeLast || true;
	var direction = end.clone().sub(start);
	var distance = direction.length();
	var noPos = Math.floor(distance/spacing);
	spacing = distance/noPos || 0;
	var dirIncrement = direction.normalize().multiplyScalar(spacing);

	var posList = [];
	for(var i = 0; i<noPos; i++) {
		posList.push(start.clone().addScaledVector(dirIncrement, i));
	}
	if(includeLast) {
		posList.push(end.clone());
	}
	return posList;
}

function curvedLine(spacing, start, end, up, offset, includeLast) {
	var direction = end.clone().sub(start);
	var side = up.clone().cross(direction).normalize();
	side.multiplyScalar(offset);
	var center = side.addScaledVector(direction, 1/2);
	var r_start = start.clone().sub(center);
	var total_angle = r_start.angleTo(end.clone().sub(center));
	var angleSpacing = 2*Math.asin(spacing/(2*r_start.length()))
	var noPos = Math.floor(total_angle/angleSpacing);
	//console.log(noPos);
	var angleSpacing = (total_angle/noPos) || 0;

	var posList = [];
	posList.push(start);
	for(var i = 1; i<noPos; i++) {
		posList.push(center.clone().add(r_start.applyAxisAngle(up, angleSpacing)));
	}
	if(includeLast) {
		posList.push(end);
	}
	return posList;
}

function circleLine(spacing, center, radius, startAngle, endAngle, offset) {
	var posList = [];
	for(var i = startAngle; i<endAngle; i = i + spacing) {
		posList.push(new THREE.Vector3(center.x + radius*Math.cos(i+offset), center.y, center.z + radius*Math.sin(i+offset)));
	}
	return posList;
}

function fillPos(posList) {
	var length = posList.length;
	cheerioList = [];
	for(var i=0; i<length; i++) {
		var pos = posList.pop();
		var newCheerio = new cheerio(pos, TorusRadiusDEFAULT, TorusTubeRadiusDEFAULT, RadialSegmentsDEFAULT, TubularSegmentsDEFAULT, MassDEFAULT);
		cheerioList.push(newCheerio);
		scene.add(newCheerio);
	}
	return cheerioList;
}
