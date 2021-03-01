
/*
    pos - position to create the plate
    radius - radius of the plate
    world - the world to insert the new plate
    --optional--
    contents - array of objects of type food to insert in the plate
    atributes - atributes of the plate (plate image, color, etc);
*/
function Plate(pos, radius, world, img, atributes) {

    this.contents = []; //food in the plate;
    this.contentsSize = 0;
    this.atributes = atributes || {};
    this.canvas = world.canvas;
    this.pos = {
        "x": pos.x,
        "y": pos.y
    };
    this.radius = radius;
    this.image = this.canvas.image(img, pos.x - radius, pos.y - radius, 2 * radius, 2 * radius);
    this.plate = world.addEntity(C(V(pos.x, pos.y), radius), {
        solid: true,
        draggable: true
    }, this.image);


    this.plate.display.attr({
        opacity: 0
    });
    this.plate.display.toBack();
    this.image.drag(moveDragPlate(this.plate, radius, this, world), startDragPlate(this, radius), endDragPlate())
    Plate.prototype.moveFood = function(posX, posY) {
        for (i = 0; i < this.contentsSize; i++) {
            //this.contents[i].image.attr({transform: transform + 'T'+ [dx,dy]});
            this.contents[i].moveFood({
                "x": posX,
                "y": posY
            });

        }
    }

    /*
    posInPlate can take the following values:
        -"topLeft"
        -"topRight"
        -"bottomLeft"
        -"bottomRight"
        -"topHalf"
        -"bottomHalf"
        -"leftHalf" note:this rotates the image 90º degrees
        -"rightHalf" not:this rotates the image 90ª degrees
    */
    Plate.prototype.addFood = function(imgSrc, posInPlate) {
        function createFood(plate, translationX, translationY, width, height) {
            var food = new Food({
                    "x": plate.pos.x + translationX,
                    "y": plate.pos.y + translationY
                }, {
                    "width": width,
                    "height": height
                },
                imgSrc, {
                    "x": translationX,
                    "y": translationY
                },
                plate.canvas);

            plate.contents[plate.contentsSize++] = food;
            return food;
        }
        switch (posInPlate) {
            case "topLeft":
                createFood(this, -this.radius/1.2, -this.radius/1.2, this.radius/1.2, this.radius/1.2);
                break;
            case "topRight":
                createFood(this, 0, -this.radius/1.2, this.radius/1.2, this.radius/1.2);
                break;
            case "bottomLeft":
                createFood(this, -this.radius/1.2, 0, this.radius/1.2, this.radius/1.2);
                break;
            case "bottomRight":
                createFood(this, 0, 0, this.radius/1.2, this.radius/1.2);
                break;
            case "topHalf":
                createFood(this, -(radius / 2), -(this.radius / 2), this.radius, this.radius / 2);
                break;
            case "bottomHalf":
                createFood(this, -(radius / 2), 0, this.radius, this.radius / 1.7);
                break;
            case "leftHalf":
                var food = createFood(this, -this.radius / 1.4, -this.radius / 2, this.radius / 1.7, this.radius);
                break;
            case "rightHalf":
                var food = createFood(this, 0, -this.radius / 1.4, this.radius / 1.7, this.radius);
                break;
            default:
                break;
        }

    }

    Plate.prototype.price = function() {
        var price = 0;
        for(i=0; i< this.contents; i++) {
            price += this.contents[i].price;
        }
        return price;
    }
}

