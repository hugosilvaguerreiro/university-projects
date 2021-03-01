(function () {

	var rgb = utils.rgb;

	var CELL_SIZE = 3;
    var DIRECTION = 25;

	var paused = false;
	var grow = false;

	var paint = false;
	var paintType = 0;
	var fire = false;
	var counter = 0;
	var tableIsBurning = false;
	var numTreesToBurn = 0;
	var burnCountdown = 0;
	var datagathering = true;

    
    var PERCENTAGE_DONE = false;

	// States.
	var STATES = {
		TREE: 0,
		BURNING: 1,
	};

	var COLORSMAP = utils.generateGradient(utils.rgb(165, 104, 42), utils.rgb(209, 171, 133), 15)

	var ROWS = Math.ceil(window.innerHeight / CELL_SIZE);
    var COLS = Math.ceil(window.innerWidth / CELL_SIZE);

	var ANALYSIS_HOLDER = new AnalysisHolder(ROWS, COLS);
	var pPercentage = [[]];
	var heightsMap = utils.createMap(ROWS, COLS, null);
	var WIND_MODEL = utils.generateWindModel(ROWS, COLS, 25,0, heightsMap);

	var INITIAL_P_TREES = 0.4;
    var pGrowth = 0.05;
    var windIntensity = 0;
	var PAGGREGATION = 0.2;


	var T1THRESHOLD = 0.6;
	var T2THRESHOLD = 0.4;
	var T3THRESHOLD = 0.8;
	var avgThreshold = 0;
	var multiculture = false;


	var NEIGHBORS = [
		[-1, -1],
		[+0, -1],
		[+1, -1],
		[-1, +0], 
		[+1, +0],
		[-1, +1],
		[+0, +1],
		[+1, +1],
	];


	// Returns true any trees in the 8-neighborhood of (row, col) are
	// on fire.
	var neighborhoodOnFire = function (data, row, col) {
		for (var i = 0; i < NEIGHBORS.length; i++) {
			var neighborRow = utils.mod(row + NEIGHBORS[i][1], data.length);
			var neighborCol = utils.mod(col + NEIGHBORS[i][0], data[0].length);
			var tree = data[neighborRow][neighborCol];
			if (tree != null && tree.getState() === STATES.BURNING) {
				return true;
			}
		}
		return false;
    }
    
    var neighborhoodOnFireWithWind = function (data, row, col) {
        var my_state = 0;
        if(data[row][col] != null) {
            my_state = data[row][col].getContinuousState();
        }
        var neighbors_influence = 0;
		for (var i = 0; i < NEIGHBORS.length; i++) {
			var neighborRow = row + NEIGHBORS[i][1];
            var neighborCol = col + NEIGHBORS[i][0];
            
            if(neighborRow < 0 || neighborCol < 0 || neighborRow >= ROWS || neighborCol >= COLS)
                continue;

            var tree = data[neighborRow][neighborCol];
            
        
            var neighborRowWind = utils.mod(1 + NEIGHBORS[i][1], 3);
            var neighborColWind = utils.mod(1 + NEIGHBORS[i][0], 3);
            
			var height = heightsMap[row][col];
            var diff = height - heightsMap[neighborRow][neighborCol];

            if(diff == 0) { // height igual
                height = 1;
            } else if(diff >= 1) { //height a subir
                height = 2;
            } else {
                height = 0.5;
            }
			
			if (tree != null) {
                if (tree.getContinuousState() < 1)
				    neighbors_influence += WIND_MODEL[row][col][neighborRowWind][neighborColWind] * height * tree.getContinuousState();
			}
        }
        my_state += neighbors_influence;
		return Math.max(0, Math.min(my_state, 1));
	}

	var createTreeGraph = function(data) {
		var treeMatrix = Array(ROWS*COLS).fill(Array(ROWS*COLS).fill(0));

		for(var row = 0; row < ROWS; row++){
			for(var col = 0; col < COLS; col++){
				for (var i = 0; i < NEIGHBORS.length; i++) {
					var neighborRow = row + NEIGHBORS[i][1];
					var neighborCol = col + NEIGHBORS[i][0];
					
					if(neighborRow < 0 || neighborCol < 0 || neighborRow >= ROWS || neighborCol >= COLS)
					continue;
					
					var tree = data[neighborRow][neighborCol];
					
					if (tree != null) {
						treeMatrix[row*col][neighborRow*neighborCol] = 1;
						treeMatrix[neighborRow*neighborCol][row*col] = 1;
					}
				}
			}
		}

		return treeMatrix;
	}
	
	var treeTypes = 3;
	var newTree = function (treeType, tHeight) {
		if (treeType == 1) {
			return new Tree(1, T1THRESHOLD, 1, 255, 1, STATES.TREE, 0, tHeight, utils.blend_colors(utils.rgb(1, 255, 1), COLORSMAP[tHeight], 0.6));
		} //default tree, ignites easily
		if (treeType == 2) {
			return new Tree(2, T2THRESHOLD, 0, 102, 0, STATES.TREE, 0, tHeight, utils.blend_colors(utils.rgb(0, 102, 0), COLORSMAP[tHeight], 0.6));
		} //hard to ignite

		if (treeType == 3) {
			return new Tree(1, T3THRESHOLD, 173, 255, 8, STATES.TREE, 0, tHeight, utils.blend_colors(utils.rgb(173, 255, 8), COLORSMAP[tHeight], 0.6));
		} //ignites more easily
	}
	


	// Executes one step of the system with the given
	// probabilities. The input array is not modified. A new array
	// containing the modifications is returned.
	var update = function (data, pGrowth) {
		if (paused) return data;
		if (data.length === 0) {
			return [];
		}
		if (!tableIsBurning){ //table wasn't burning before (reset)
			if (ANALYSIS_HOLDER.percentageStorage.length > 1) { //don't reset when nothing is happening (idle)
				
		
			}
			else{
				ANALYSIS_HOLDER.percentageStorage = [[0,0]];
				numTreesToBurn = 0;
				for (var row = 0; row < data.length; row++) {
					for (var col = 0; col < data[row].length; col++) {
						if (data[row][col] && data[row][col].getContinuousState() < 1 ){
							numTreesToBurn += 1;
						}
					}
				}
				burnCountdown = numTreesToBurn;
				
			}

        } 
		tableIsBurning = false;
		var oldData = Array.from(data);
		var newData = utils.create2DArray(data.length, data[0].length);
		for (var row = 0; row < data.length; row++) {
			for (var col = 0; col < data[row].length; col++) {
				var tree = data[row][col];
				if (tree == null) {
					if (grow && Math.random() < pGrowth) {
						i = Math.random() < 0.8 ? 1 : 2; //80% type 0, 20% type 1
						tree = newTree(i, heightsMap[row][col]);
					}
				} else {
                    var continuous_state = neighborhoodOnFireWithWind(oldData, row, col);
					switch (tree.getState()) {
						case STATES.TREE:                            
                            var on_fire = (continuous_state > 0 ? true : false);
                            if ((on_fire && Math.random() > Math.max(0.8 - (continuous_state-tree.getContinuousState())+tree.getFireThreshold(),0))) {
								tree = newTree(tree.getType(), heightsMap[row][col]);
                                tree.setState(STATES.BURNING);
								tree.setContinuousState(tree.getBurnRate());
								tableIsBurning = true;
                            }
							break;
						case STATES.BURNING:
                            if (continuous_state < 1) {
                                var newstate = continuous_state + tree.getBurnRate();
                                tree.setContinuousState(newstate);
                                tableIsBurning = true;
                            } else {
                                
                                if(tree.getContinuousState() != 1) {
                                    tree.setContinuousState(1);
                                    burnCountdown -= 1;
                                }

                            }
                            break;

					}
				}
				newData[row][col] = tree;
			}
		}
		var val = (numTreesToBurn - burnCountdown) / numTreesToBurn * 100
		counter += 1;
		if (tableIsBurning){
			document.getElementById('counter').innerHTML = counter;
			document.getElementById('burned').innerHTML =  val.toFixed(2) + "%";
			ANALYSIS_HOLDER.percentageStorage.push([counter, val.toFixed(2)]);
		}
		else{
			if(ANALYSIS_HOLDER.percentageStorage.length > 1 && !PERCENTAGE_DONE) {
				document.getElementById('counter').innerHTML = counter;
				document.getElementById('burned').innerHTML =  val.toFixed(2) + "%";
				ANALYSIS_HOLDER.percentageStorage.push([counter, val.toFixed(2)]);
                PERCENTAGE_DONE = true;		
			}
			
			counter = 0;
		}
		return newData;
	}

	// Creates a new canvas and returns its context.
	var createCanvas = function (height, width) {
		var canvas = document.getElementById("c");
		var context = canvas.getContext("2d");
		context.canvas.height = height;
		context.canvas.width = width;
		return context;
	}

	// Updates the given canvas context using the state stored in data.
	var draw = function (data, context) {
		// Updates the array associated with the canvas directly for performance reasons.
		var canvasData = context.getImageData(0, 0, context.canvas.width, context.canvas.height);;
		for (var row = 0; row < data.length; row++) {
			for (var col = 0; col < data[row].length; col++) {
				tree = data[row][col]
				var color = tree != null ? tree.getColorWithHeight() : COLORSMAP[heightsMap[row][col]];
                

				for (var x = 0; x < CELL_SIZE; x++) {
					for (var y = 0; y < CELL_SIZE; y++) {
						var index = (row * CELL_SIZE * CELL_SIZE * COLS +
							CELL_SIZE * COLS * y +
							col * CELL_SIZE + x) * 4;
						canvasData.data[index] = color.r;
						canvasData.data[index + 1] = color.g;
						canvasData.data[index + 2] = color.b;
						canvasData.data[index + 3] = 255; // Alpha.
					}
				}
			}
		}
		context.putImageData(canvasData, 0, 0);
	}

	var context = createCanvas(ROWS * CELL_SIZE, COLS * CELL_SIZE);

	// Initializes the state by creating a forest that is comprised
	// roughly of 70% trees and 30% empty spaces.
	var data = update(utils.create2DArray(ROWS, COLS, null),
		INITIAL_P_TREES, 0);
	draw(data, context);


	setInterval(function () {
		if (!paused) {
			data = update(data, pGrowth);
			draw(data, context);
		}
	},50);

	var updateValue = function (id, value) {
		var elem = document.getElementById(id);
		if (elem) {
			elem.innerHTML = value;
		}
	}

	var pGrowthSlider = document.getElementById("pGrowth");
	if (pGrowthSlider) {
		pGrowthSlider.value = pGrowth;
		pGrowthSlider.onchange = function () {
			pGrowth = parseFloat(pGrowthSlider.value);
			updateValue("pGrowthValue", pGrowth);
		}
	}
	
	var pAggregation = document.getElementById("pAggregation");
	if (pAggregation) {
		pAggregation.value = PAGGREGATION;
		pAggregation.onchange = function () {
			PAGGREGATION = parseFloat(pAggregation.value);
			updateValue("pAggregationValue", PAGGREGATION);
		}
    }
    
    var windIntensitySlider = document.getElementById("windIntensity");
	if (windIntensitySlider) {
		windIntensitySlider.value = windIntensity;
		windIntensitySlider.onchange = function () {
			windIntensity = parseFloat(windIntensitySlider.value);
            var elem = document.getElementById("windIntensityValue");
            if (elem) {
                elem.innerHTML = windIntensity;
            }
		}
	}

	var t1ThresholdSlider = document.getElementById("t1Threshold");
	if (t1ThresholdSlider) {
		t1ThresholdSlider.value = T1THRESHOLD;
		t1ThresholdSlider.onchange = function () {
			T1THRESHOLD = parseFloat(t1ThresholdSlider.value);
			updateValue("t1ThresholdValue", T1THRESHOLD);
			avgThreshold = (T1THRESHOLD + T2THRESHOLD + T3THRESHOLD)/3
			updateValue("avgThreshold", avgThreshold.toFixed(2));
		}
	}

	var t2ThresholdSlider = document.getElementById("t2Threshold");
	if (t2ThresholdSlider) {
		t2ThresholdSlider.value = T2THRESHOLD;
		t2ThresholdSlider.onchange = function () {
			T2THRESHOLD = parseFloat(t2ThresholdSlider.value);
			updateValue("t2ThresholdValue", T2THRESHOLD);
			avgThreshold = (T1THRESHOLD + T2THRESHOLD + T3THRESHOLD)/3
			updateValue("avgThreshold", avgThreshold.toFixed(2));
		}
	}

	var t3ThresholdSlider = document.getElementById("t3Threshold");
	if (t3ThresholdSlider) {
		t3ThresholdSlider.value = T3THRESHOLD;
		t3ThresholdSlider.onchange = function () {
			T3THRESHOLD = parseFloat(t3ThresholdSlider.value);
			updateValue("t3ThresholdValue", T3THRESHOLD);
			avgThreshold = (T1THRESHOLD + T2THRESHOLD + T3THRESHOLD)/3
			updateValue("avgThreshold", avgThreshold.toFixed(2));
		}
	}

	updateValue("pGrowthValue", pGrowth);
	updateValue("pAggregationValue", PAGGREGATION);
	updateValue("t1ThresholdValue", T1THRESHOLD);
	updateValue("t2ThresholdValue", T2THRESHOLD);
	updateValue("t3ThresholdValue", T3THRESHOLD);
	avgThreshold = (T1THRESHOLD + T2THRESHOLD + T3THRESHOLD)/3
	updateValue("avgThreshold", avgThreshold.toFixed(2));
	//updateValue("numTrees", numberTrees);



    //======================================================================================
    //============================= USER INTERACTION =======================================
    //======================================================================================

	var pausedCheckBox = document.getElementById("pausedCheckBox");
	if (pausedCheckBox) {
		pausedCheckBox.checked = paused;
		pausedCheckBox.onchange = function () {
			paused = pausedCheckBox.checked;
		}
	}


	var multiCheckBox = document.getElementById("multicultureCheckBox");
	if (multiCheckBox) {
		multiCheckBox.checked = multiculture;
		multiCheckBox.onchange = function () {
			multiculture = multiCheckBox.checked;
		}
	}

	var clearTreesBtn = document.getElementById("clearTreesBtn");
	if (clearTreesBtn) {
		clearTreesBtn.addEventListener("mousedown", function (event) {
			data = utils.create2DArray(ROWS, COLS, null);
			draw(data, context);
		});
    }
    
    var fillTrees1 = document.getElementById("type1fill");
	if (fillTrees1) {
		fillTrees1.addEventListener("mousedown", function (event) {
            data = utils.create2DArray(ROWS, COLS, null);
            for(var i = 0; i < ROWS; i++) {
                for(var j = 0; j < COLS; j++)  {
                    data[i][j] = newTree(1, heightsMap[i][j]);
                }
            }
            
			draw(data, context);
		});
	}
	
	var fillTrees2 = document.getElementById("type2fill");
	if (fillTrees2) {
		fillTrees2.addEventListener("mousedown", function (event) {
            data = utils.create2DArray(ROWS, COLS, null);
            for(var i = 0; i < ROWS; i++) {
                for(var j = 0; j < COLS; j++)  {
                    data[i][j] = newTree(2, heightsMap[i][j]);
                }
            }
            
			draw(data, context);
		});
	}
	
	var fillTrees3 = document.getElementById("type3fill");
	if (fillTrees3) {
		fillTrees3.addEventListener("mousedown", function (event) {
            data = utils.create2DArray(ROWS, COLS, null);
            for(var i = 0; i < ROWS; i++) {
                for(var j = 0; j < COLS; j++)  {
                    data[i][j] = newTree(3, heightsMap[i][j]);
                }
            }
            
			draw(data, context);
		});
    }
    
    var fillAggTrees = document.getElementById("fillAgg");
	if (fillAggTrees) {
        fillAggTrees.addEventListener("click", fillLimitAggregationTreesFunction);
	}
	
	var fillRandomTrees = document.getElementById("fillRandom");
	if (fillRandomTrees) {
        fillRandomTrees.addEventListener("click", fillRandomTreesFunction);
    }
    

	function fillLimitAggregationTreesFunction() {
        //NEIGHBORS
        P = PAGGREGATION;
		var NR_TREES = 0;
		for(var i = 0; i < ROWS; i++) {
			for(var j=0; j < COLS; j++) {
				if(Math.random() < pGrowth)
					NR_TREES++;
			}
		}

        planted_trees_type1 = 0;
        planted_trees_type2 = 0;
        planted_trees_type3 = 0;
        frontier = []
        data = utils.create2DArray(ROWS, COLS, null);
        ANALYSIS_HOLDER = new AnalysisHolder(ROWS, COLS);

        for(var i = 0; i < NR_TREES; i++) {
            if(Math.random() < P || planted_trees_type1 == 0 || (planted_trees_type2 == 0 && multiculture) || (planted_trees_type3 == 0 && multiculture)) {
                do {
                    var row = randomIntFromInterval(0, ROWS-1);
                    var col = randomIntFromInterval(0, COLS-1);
                } while(data[row][col]);
                var type = paintType;
                if(multiculture) {
                    if(planted_trees_type1 == 0){
                        type = 1;
                        planted_trees_type1++;
                    } else if(planted_trees_type2 == 0) {
                        type = 2;
                        planted_trees_type2++;
                    } else if(planted_trees_type3 == 0) {
                        type = 3;
                        planted_trees_type3 ++;
                    } else {
                        type = randomIntFromInterval(1,3);
                    }
                    data[row][col] = newTree(type, heightsMap[row][col]);
                } else {
                    data[row][col] = newTree(paintType, heightsMap[row][col]);
                    planted_trees_type1 ++;
                }
                frontier.push([row, col, type]);
            } else {
                checked = []
                var type = paintType;
                do {
                    if(multiculture)
                        type = randomIntFromInterval(1,3);
                    var new_location = null;
                    do {
                        var front_tree_loc = randomIntFromInterval(0, frontier.length-1);
                    } while(checked.includes(front_tree_loc))
                    
                    var front_tree = frontier[front_tree_loc];

                    if(front_tree[2] != type) {
                        checked.push([front_tree_loc]);
                        continue
                    }

                    checked = []

                    var n = [0,3,1,2,4,6,5,7];
                    while(n.length > 0) {

                        var j =  randomIntFromInterval(0, n.length-1);
                        var neighborRow = front_tree[0] + NEIGHBORS[j][1];
                        var neighborCol = front_tree[1] + NEIGHBORS[j][0];
                        
                        if(neighborRow < 0 || neighborCol < 0 || neighborRow >= ROWS || neighborCol >= COLS) {
                            n.splice(j, 1);
                            continue;
                        }
                        
                        if (!data[neighborRow][neighborCol]) { 
                            new_location = [neighborRow, neighborCol, type];
                        }

                        n.splice(j, 1);
                    } 
                    if(new_location == null) {
                        frontier.splice(front_tree_loc, 1);
                    }

                } while(new_location == null);
                data[new_location[0]][new_location[1]] = newTree(type, heightsMap[new_location[0]][new_location[1]]);
                frontier.push(new_location);

            }
        }    
		draw(data, context);
	}
    

	function fillRandomTreesFunction() {
		data = utils.create2DArray(ROWS, COLS, null);
            ANALYSIS_HOLDER = new AnalysisHolder(ROWS, COLS);
            for(var i = 0; i < ROWS; i++) {
                for(var j = 0; j < COLS; j++)  {
                    if(Math.random() < pGrowth) {
						if (multiculture){
							var rand = Math.random();
							if(rand < 1/3){
								data[i][j] = newTree(1, heightsMap[i][j]);
								ANALYSIS_HOLDER.forest[i][j] = newTree(1, heightsMap[i][j]);
							}
							else if (rand >= 1/3 && rand < 2/3){
								data[i][j] = newTree(2, heightsMap[i][j]);
								ANALYSIS_HOLDER.forest[i][j] = newTree(2, heightsMap[i][j]);
							}
							else{
								data[i][j] = newTree(3, heightsMap[i][j]);
								ANALYSIS_HOLDER.forest[i][j] = newTree(3, heightsMap[i][j]);
							}
						}
						else{
							data[i][j] = newTree(paintType, heightsMap[i][j]);
							ANALYSIS_HOLDER.forest[i][j] = newTree(paintType, heightsMap[i][j]);
						}
						
                    }
                }
            }

            
			draw(data, context);
	}


	function paintTrees() {
		if (event.clientX != oldX || event.clientY != oldY) {
			oldX = event.clientX;
			oldY = event.clientY;
			if (paintType) {
				mouseStillDown = true;
				drawCircle(paintType, null);
			}
		}
	}

	function lightFire() {

		var tree = data[Math.round(mouseY / CELL_SIZE)][Math.round(mouseX / CELL_SIZE)];
		if(tree){
			tree.setState(STATES.BURNING);
			tree.setContinuousState(tree.getBurnRate());
			ANALYSIS_HOLDER.percentageStorage = [[0,0]];
			PERCENTAGE_DONE = false;
			if (datagathering){
				simulationRunning = true;
			}
		}
	}

	document.getElementById("type1").addEventListener("click", function(){
		toggleTreePaint(1, false);
	});
	document.getElementById("type2").addEventListener("click", function(){
		toggleTreePaint(2, false);
	});
	document.getElementById("type3").addEventListener("click", function(){
		toggleTreePaint(3, false);
	});

    document.getElementById("fire").addEventListener("click", toggleFire);
	document.getElementById("RandomFire").addEventListener("click", toggleRandomFire);
	

	function toggleTreePaint(tPaint, fromFire) {
		if (paint == false) { //asking to paint
			if (fire == true) { //but fire is selected
				toggleFire(); //turn off fire
			}
			paint = true;
			paintType = tPaint;
			document.getElementById("type" + tPaint).style.background = 'grey';
			document.getElementById("type" + tPaint).style.background = 'lightgrey';
			window.addEventListener("mousedown", paintTrees);
		} else { 
			if(fromFire){ //turning off paint (and turning on fire)
				paint = false;
				paintType = 0;
				document.getElementById("type" + tPaint).style.background = 'lightgrey';
				document.getElementById("type"  + tPaint).style.background = 'lightgrey';
				window.removeEventListener("mousedown", paintTrees);
			}
			else{ //toggle paint type
				paintType = tPaint;
				document.getElementById("type" + tPaint).style.background = 'grey';
				document.getElementById("type" + tPaint).style.background = 'lightgrey';
			}
		}
	}



	function toggleFire() {
		if (fire == false) {
			if (paint == true) {
				toggleTreePaint(paintType, true);
			}
			fire = true;
			document.getElementById("fire").style.background = 'grey';
			window.addEventListener("mousedown", lightFire);
		} else {
			fire = false;
			document.getElementById("fire").style.background = 'lightgrey';
			window.removeEventListener("mousedown", lightFire);
		}
    }

    function randomIntFromInterval(min, max) { // min and max included 
        return Math.floor(Math.random() * (max - min + 1) + min);
      }



    function toggleRandomFire() {
        var row = randomIntFromInterval(0, ROWS);
        var col = randomIntFromInterval(0, COLS);
		try{
			var tree = data[row][col];
		} catch(exc){
			return
		}
		if(tree){
			tree.setState(STATES.BURNING);
			tree.setContinuousState(tree.getBurnRate());
			ANALYSIS_HOLDER.percentageStorage = [[0,0]];
			PERCENTAGE_DONE = false;
			if(datagathering){
				simulationRunning = true;
			}
		} else {
            toggleRandomFire();
        }
    }
    



	var table = document.getElementById("content");

	table.addEventListener("mousedown", function (event) {
		event.stopPropagation();
	});

	var btn = document.getElementById("button");
	btn.addEventListener("mousedown", function (event) {
		event.stopPropagation();
    });
    

	var oldX = null;
	var oldY = null;
	var mouseStillDown = false;

	var mouseX = null;
    var mouseY = null;
    


	function onMouseUpdate(e) {
		mouseX = e.pageX;
		mouseY = e.pageY;
	}

	document.addEventListener('mousemove', onMouseUpdate, false);
	document.addEventListener('mouseenter', onMouseUpdate, false);

	window.addEventListener("mouseup", function (event) {
		mouseStillDown = false;
	});

	var drawCircle = function (tType, interval) {
		var int = interval;
		if (!mouseStillDown) {
			if (int) {
				clearInterval(int);
			}
			return;
		}

		var radius = 40;
		for (var r = 0; r < radius; r++) {
			for (var degree = 0; degree < 360; degree++) {
				var radians = degree * Math.PI / 180;
				var x = mouseX + r * Math.cos(radians);
				var y = mouseY + r * Math.sin(radians);
				var row = Math.round(y / CELL_SIZE);
				var col = Math.round(x / CELL_SIZE);
				data[row][col] = newTree(tType, heightsMap[row][col]);
			}
		}

		draw(data, context);


		if (mouseStillDown) {
			int = setInterval(function () {
				drawCircle(tType, int);
			}, 100);
		}
    }
    var slider = $("#handle2").roundSlider({
        sliderType: "default",
        radius: 50,
        showTooltip: false,
        width: 10,
        value: 25,
        handleSize: 0,
        handleShape: "square",
        circleShape: "full",
        change : function traceEvent(e) {
            DIRECTION = this.getValue();
            WIND_MODEL = utils.generateWindModel(ROWS, COLS, DIRECTION, windIntensity,heightsMap);
        }
	});     
})();