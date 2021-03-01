class AnalysisHolder {
	constructor(ROWS, COLS) { //exists or is burning, 2 states
        this.forest = utils.create2DArray(ROWS, COLS, null);;
        this.events = []
        this.percentageStorage = []
    }
    

    treeChanged(tree,timestep,row, col, percentage_burned) {
        
        if (this.events.length  == timestep) {
            this.events.push(
                {
                    timestep: timestep,
                    events : []
                }
            );
        }

        this.events[timestep].events.push({
            row : row,
            col: col,
            state: tree.getContinuousState()
        });
    }

    export(wind_direction, wind_intensity, growth_probability) {
        return {
            forest: this.forest,
            events: this.events,
            params: {
                wind_direction: wind_direction,
                wind_intensity: wind_intensity,
                growth_probability: growth_probability
            },
            stats : {
                percentage_burned : this.percentageStorage
            }
        }
    }

}