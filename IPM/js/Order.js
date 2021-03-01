function Order() {
	this.currentOrder = null;
	this.sideMenu = null;
	this.plates = [];

	Order.prototype.begin = function()  {
		var h = $(window).height();
		var w = $(window).width();
		var heigth = h/4+2;
		var width = w/2;
		var position = {"x": w/4, "y": h - heigth-30};
		interface.buttons=createOrderButtons();
        var img = interface.canvas.image("resources/Images/grey.jpg", position.x, position.y, width, heigth + 25);
        var element = interface.world.addEntity(P(V(position.x, position.y), [V(0, 0), V(width, 0), V(width, heigth), V(0, heigth)]), {
            solid: true,
            heavy: true,
            draggable: false,
            opacity: 0.2
        }, img);
        var menu = new Menu(interface.canvas, element, position, "orderShower", interface);
        menu.box.display.attr({opacity:0});
        img.attr({opacity:0});
        this.sideMenu = menu;
        menu.appendNewDiv("interface", "orderShower", "resources/htmls/order/orderShower.html",true);
        w3IncludeHTML();
        startTrash();
        $("#orderShower").css({"max-height":h*(2/5) +2});
        $("#orderShower").css({"bottom":"0"});
        

	}
    Order.prototype.removeMenu = function() {
        this.sideMenu.element.remove();
        interface.world.removeEntity(this.sideMenu.box);
        var men = "#" + this.sideMenu.type;
        $(men).hide("puff",function(){$(men).remove();} );
        this.sideMenu = null;
        $("#orderButtons").remove();
    }
}

