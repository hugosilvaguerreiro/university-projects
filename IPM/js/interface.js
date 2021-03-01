function Interface(world) {
    this.world = world;
    this.canvas = world.canvas;
    this.sideMenu = null;
    this.bigMenu = null;
    this.popUp = null;
    this.buttons = null;
    this.order = null;
    Interface.prototype.removePopUp = function() {
        if(this.popUp) {
            this.popUp.element.remove();   
            this.world.removeEntity(this.popUp.box);
            var men = "#" + this.popUp.div;
            $(men).remove();
            this.popUp = null; 
        }

    }

    Interface.prototype.popup = function(height, width, position, html) {
        if(this.popUp) {
            this.popUp.animate("shake");
        }
        else {
            var img = this.canvas.image("resources/Images/grey.jpg", position.x, position.y, height, width).attr({opacity: 0});
            var element = this.world.addEntity(P(V(position.x, position.y), [V(0, 0), V(height, 0), V(height, width), V(0, width)]), {
                solid: false,
                heavy: false,
                draggable: false,
                opacity: 0.2
            }, img);
            var menu = new Menu(this.canvas, element, position, "popUp", this);
            element.display.attr({opacity:0});
            img.toFront();
            menu.appendNewDiv("interface", "popUP", html);
            w3IncludeHTML();
            $("#popUP").hide();
            $("#popUP").show("puff");
            this.popUp = menu;
        }     
    }

    Interface.prototype.createSideMenu = function(height, width, position, type) {
        if (this.sideMenu) {
            this.removeSideMenu();
        }
        var position = position || {
            x: 0,
            y: 0
        };
        var img = this.canvas.image("resources/Images/grey.jpg", position.x, position.y, height, width);
        var element = this.world.addEntity(P(V(position.x, position.y), [V(0, 0), V(height, 0), V(height, width), V(0, width)]), {
            solid: true,
            heavy: true,
            draggable: false,
        }, img);
        var menu = new Menu(this.canvas, element, position, type, this);
        menu.box.display.attr({opacity:0});
        img.attr({opacity:0});
        this.sideMenu = menu;
        return menu;
    }

    Interface.prototype.removeSideMenu = function() {
        if(this.sideMenu.type == "meuPedido") 
            removeHiddenTrash();
        else if(this.sideMenu.type == "pagamento")
            removePaymentTrash();
        interface.sideMenu.element.remove();
        this.world.removeEntity(this.sideMenu.box);
        var men = "#" + this.sideMenu.type;
        $(men).hide("slide", {direction : "right"},function(){$(men).remove();} );
        this.sideMenu = null;
    }

    Interface.prototype.createBigMenu = function(height, width, position, type) {
        if (this.bigMenu) {
            this.removeBigMenu();
        }
        var position = position || {
            x: 0,
            y: 0
        };
        var img = this.canvas.image("resources/Images/grey.png", position.x, position.y, height, width);
        var element = this.world.addEntity(P(V(position.x, position.y), [V(0, 0), V(width, 0), V(width, height), V(0, height)]), {
            solid: true,
            heavy: false,
            draggable: true
        }, img);
        element.display.attr({opacity:0});
        var menu = new Menu(this.canvas, element, position, type, this, true);
        var world = this.world;
        img.drag(moveDragMenu(menu), startDragMenu(menu, world), endDragMenu(menu));
        img.toFront();
        this.bigMenu = menu;
        var parentDiv = document.getElementById("interface");
        var func = ' onclick="interface.removeBigMenu()"';
        var div = "<div href='#' class='btn btn-danger' id=" + menu.button + func +" style='position:absolute; top:" + menu.atributes.cy + "px;" + "left:";
        
        div += (menu.atributes.cx + width - 31) + "px; width: 27px; height: 27px; padding: 0; font-size: 16px; margin: 2px" + "''>&times;</div>";-
        $(parentDiv).append(div);
        return menu;

    }

    Interface.prototype.removeBigMenu = function() {
        this.bigMenu.element.remove();
        var menu = "#" + this.bigMenu.type;
        this.world.removeEntity(this.bigMenu.box);
        $(menu).remove();
        $("#" + this.bigMenu.button).remove();
        this.bigMenu = null;

    }

    Interface.prototype.getElement = function(pos) {
        this.canvas.getElementByPoint(this.mousePos.x, this.mousePos.y);
    }
    /*
    Creates a new Plate at a given position
    pos- object of this type {x:number, y:number}
    radius - radius of the plate
    */
    Interface.prototype.createPlate = function(pos, radius, imgSrc) {
        var plate = new Plate(pos, radius, this.world, imgSrc);
        return plate;
    }
    //simulates the world
    Interface.prototype.simulate = function() {
        this.world.simulate();
    }
    
    Interface.prototype.createButton = function(position, h, w, id, html) {
        var height = h;
        var width = w;
        var position = position || {
            x: 0,
            y: 0
        };
        var img = this.canvas.image("resources/Images/grey.jpg", position.x, position.y, width, height);
        var element = this.world.addEntity(P(V(position.x, position.y), [V(0, 0), V(width, 0), V(width, height), V(0, height)]), {
            solid: true,
            heavy: true,
            draggable: false,
        }, img);
        var menu = new Menu(this.canvas, element, position, "", this);
        menu.box.display.attr({opacity:0});
        img.attr({opacity:0});
        menu.appendNewDiv("interface", id, html);
        $("#"+id).hide();
        $("#"+id).show("puff");
        return element;
    }

    Interface.prototype.setBackground = function(image) {
        this.canvas.image(image, 0, 0, this.canvas.width, this.canvas.height).toBack();
    }

    Interface.prototype.startOrderMenu = function() {
        if(this.sideMenu)
            this.removeSideMenu();
        if(this.bigMenu)
            this.removeBigMenu();
        for(i = 0; i < this.buttons.length ; i++) {
            this.buttons[i].image.remove();
            world.removeEntity(this.buttons[i]);
            $("#sideButtons").remove();
        }
        this.buttons = null;
        this.order = new Order();
        this.order.begin();

    }
    Interface.prototype.startMenu = function() {
        if(this.sideMenu)
            this.removeSideMenu();
        if(this.bigMenu)
            this.removeBigMenu();
        for(i = 0; i < this.buttons.length ; i++) {
            this.buttons[i].image.remove();
            world.removeEntity(this.buttons[i]);
        }
        this.order.removeMenu();
        createButtons();

    }

}

