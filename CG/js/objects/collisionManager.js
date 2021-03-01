class collisionManager {
	constructor(playerCar, cheerioList, orangesList, butterList) {
		this.car = playerCar;
		this.cheerios = cheerioList;
		this.oranges = orangesList;
		this.butters = butterList;
		this.xLimits = [-405, 405]
		this.zLimits = [-230, 230]
		//this.tears = ["(┳ _ ┳)"];
	}

	checkAllCollisions() {
		this.checkCarOrange();
		this.checkCarButter();
		this.checkCarCheerios();
		this.checkCheerioCheerio();
		this.checkCarInside();
		this.checkCheerioInside();
	}

	checkCarButter() {
		for (var i = 0; i < this.butters.length; i++) {
			if (this.hasCollision(this.car, this.butters[i])) {
				this.car.butterCollision();
			}
		}
	}

	checkCarOrange() {
		for (var i = 0; i < this.oranges.length; i++) {
			if (this.hasCollision(this.car, this.oranges[i])) {
				this.car.orangeCollision();
				if(!dead) {
					this.car.reset();
				}

				break;
			}
		}
	}

	checkCarCheerios() {
		for (var i = 0; i < this.cheerios.length; i++) {
			if (this.hasCollision(this.car, this.cheerios[i])) {
				this.cheerios[i].handleCollision(this.car, true);
			}
		}
	}

	checkCarInside() {
		if (!this.isInside(this.car)) {
			this.car.tableCollision();
			if(!dead) {
				this.car.reset();
			}

		}
	}

	checkCheerioCheerio() {
		for (var i = 0; i < this.cheerios.length; i++) {
			for (var j = i + 1; j < this.cheerios.length; j++) {
				if (this.hasCollision(this.cheerios[i], this.cheerios[j])) {

					this.cheerios[i].handleCollision(this.cheerios[j], true);
					this.cheerios[j].handleCollision(this.cheerios[i], true);
				}
			}
		}
	}

	checkCheerioInside() {
		for (var i = 0; i < this.cheerios.length; i++) {
			if (!this.isInside(this.cheerios[i])) {
				this.cheerios[i].setPosition(0,1000,0);
			}
		}
	}

	isInside(thing) {
		var pos = thing.getTentativePosition();
		return  pos.x > this.xLimits[0] && pos.x < this.xLimits[1] && pos.z > this.zLimits[0] && pos.z < this.zLimits[1]
	}

	CheckCarCheerio() {
		for (var i = 0; i < this.cheerios.length; i++) {
			if (this.hasCollision(this.car, this.cheerios[i])) {
				this.cheerios[i].handleCollision(this.car, true);
			}
		}
	}

	CheckCheerioCheerio() {
		for (var i = 0; i < this.cheerios.length; i++) {
			for (var j = i + 1; j < this.cheerios.length; j++) {
				if (this.hasCollision(this.cheerios[i], this.cheerios[j])) {

					this.cheerios[i].handleCollision(this.cheerios[j], true);
					this.cheerios[j].handleCollision(this.cheerios[i], true);
				}
			}
		}
	}

	hasCollision(first, second) {
		return Math.pow(first.getBoundingRadius() + second.getBoundingRadius(), 2)-0.1 >=
		first.getBoundingCenter().distanceToSquared(second.getBoundingCenter());
	}

	reset() {
		playerCar.reset();
		pathRandomizer.reset();
		for(var j=0; j<updateList.length; j++) {
			if(updateList[j].isBaseObject) {
				updateList[j].reset();
			}
		}
	}
	input(action) {
		if(action == "reset") {
			this.reset();
			dead = false;
			if(pause) {
				gamePause();
			}
		}
	}
}
