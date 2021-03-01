/* DRAG HANDLER FOR MENU*/
function startDragMenu(menu) {
    return function() {
        this.ox = this.attr("x");
        this.oy = this.attr("y");
        var div = "#" + menu.div;
        var toolbar = this.attr("y") + menu.toolbar;
        $(div).css({
            top: toolbar
        });
        $(div).css({
            left: this.attr("x")
        });
    };
}
// Create a Raphael move drag handler for specified entity
function moveDragMenu(menu, world) {
    return function(dx, dy) {
        this.attr({
            x: this.ox + dx,
            y: this.oy + dy
        });
        var div = "#" + menu.div;
        var toolbar = this.attr("y") + menu.toolbar;
        $(div).css({
            top: toolbar
        });
        $(div).css({
            left: this.attr("x")
        });
        $("#"+ menu.button).css({top:this.attr("y")});
        $("#"+ menu.button).css({left:(this.attr("x")+this.attr("width")-31)});        
        menu.box.data.pos.x = this.attr("x");
        menu.box.data.pos.y = this.attr("y");
    };
}

// Create a Raphael end drag handler for specified entity
function endDragMenu(menu) {
    return function() {

    };
}

/*DRAG HANDLER FOR BUTTON*/
function startDragButton(handler) {
    return function() {
        this.unclick(handler);
        this.click(handler);
        this.ox = this.attr("x");
        this.oy = this.attr("y");


    };
}

function moveDragButton(box, world, type, size) {
    return function(dx, dy) {

        box.data.pos.x = this.ox + dx;
        box.data.pos.y = this.oy + dy;

        this.attr({
            x: this.ox + dx,
            y: this.oy + dy
        });
        world.simulate();
    };
}

function endDragPlate() {
    return function() {

    };
}

function startDragPlate(plate, size) {
    return function() {
        this.ox = this.attr("x");
        this.oy = this.attr("y");
        //plate.moveFood(this.attr("x")+size, this.attr("y")+size);
    };
}

function moveDragPlate(box, size, plate, world) {
    return function(dx, dy) {

        box.data.pos.x = this.ox + size + dx;
        box.data.pos.y = this.oy + size + dy;

        this.attr({
            x: this.ox + dx,
            y: this.oy + dy
        });
        world.simulate();
        plate.moveFood(box.data.pos.x , box.data.pos.y );
        world.simulate();
    };
}

function endDragButton() {
    return function() {};
}