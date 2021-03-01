function Menu(canvas, element, pos, type, interface, bigMenu) {
    this.element = element.image;
    this.box = element;
    this.toolbar = 30; //change to set the toolbar of the menu bigger
    this.button = null;
    this.div = null;
    this.type = type;
    this.bigMenu = false || bigMenu;
    this.atributes = {
        cx: pos.x,
        cy: pos.y
    };
    element.addMenu(this);
    var parentDiv = document.getElementById("interface");
    this.button = type + "button";

    Menu.prototype.addButton = function(button) {
        this.button = button;
    }
    Menu.prototype.getElement = function() {
        return this.element;
    }
    Menu.prototype.setPosition = function(pos) {
        this.pos = pos;
    }
    Menu.prototype.getPosition = function() {
        return this.pos;
    }

    Menu.prototype.updateDisplay = function() {
        this.atributes.cx = this.pos.x;
        this.atributes.cy = this.pos.y;
        this.element.attr(this.atributes);
    }
    /*
        Appends a new div to a given parent div

        ParentId- id of the div where your new div will be inserted
        id- the id you want to giv your new div
    
        */
    Menu.prototype.appendNewDiv = function(parentId, id, html, hammer) {
        this.div = id;
        var hammer = hammer || false;
        var parentDiv = document.getElementById("interface"); //TODO: change this to not be hardcoded
        var width = this.element.attr("width"); //this.element.width || $(parentDiv).width();

        var height = this.element.attr("height"); //this.element.height || $(parentDiv).height();
 

        var positionx = this.atributes.cx;
        var positiony = this.atributes.cy;
        if(this.bigMenu) {
            positiony += this.toolbar;
            height -= this.toolbar;
        }
        
        var html = html || "";
        var attr = 'overflow: hidden;';

        var div = '<div  w3-include-html=' + html + ' id= "' + id + '" ';
        //var div='<div w3-include-html=' +html+ '><\div>';
        div += 'style="' + 'width: ' + width + 'px; ';
        if(!hammer)
            div += 'height:' + height + 'px; ';
        if(!hammer)
         div += 'top: ' + positiony + 'px;';
        div += 'left: ' + this.atributes.cx + 'px; ';
        div += 'position: absolute; ' + attr + '">';
        div += '<\div>';
        $(parentDiv).fadeIn().append(div);
    }
    Menu.prototype.animate = function(efect) {
        var div = "#" + this.div;
        $(div).effect(efect);
    }
}
