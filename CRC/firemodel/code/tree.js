class Tree {

	constructor(type, fireThreshold, colorR, colorG, colorB, discrete_state, continuous_state, tHeight, colorWithHeight) { //exists or is burning, 2 states
		this.fireThreshold = fireThreshold;
		this.colorR = colorR;
		this.colorG = colorG;
		this.colorB = colorB;
		this.state = discrete_state;
		this.state_wind = continuous_state
		this.type = type;
		this.terrainHeight = tHeight;
		this.colorWithHeight = colorWithHeight;
		this.burnRate = 0.1;
	}

	getBurnRate(){ return this.burnRate; }

	getFireThreshold() {
		return this.fireThreshold;
	}

	getState() {
		return this.state;
	}

    getContinuousState() {
		return this.state_wind;
	}

	setState(state) {
		this.state = state;
    }
    
    setContinuousState(state) {
		this.state_wind = state;
	}

	getColor() {
		if (this.state == 0) return utils.rgb(this.colorR, this.colorG, this.colorB);
		if (this.state == 1) return utils.rgb(255, 1, 1);
	}
	getType() {
		return this.type;
	}

	getColorWithHeight() {
		if (this.state == 0) return this.colorWithHeight;
		if (this.state == 1) return utils.rgb(255 - 255 * (this.state_wind < 1 ? this.state_wind : 1), 1, 1);
	}

}