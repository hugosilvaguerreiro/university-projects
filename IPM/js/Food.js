/*

*/
function Food(pos, size, imgSrc, positionInPlate, canvas, price) {
    this.pos = pos;
    this.size = size;
    this.imgScr = imgSrc;
    this.price = price || 0;
    this.positionInPlate = positionInPlate;
    this.canvas = canvas;
    this.image = canvas.image(imgSrc, pos.x, pos.y, size.width, size.height);
    Food.prototype.moveFood = function(newPos) {
        this.pos.x = newPos.x;
        this.pos.y = newPos.y;
        this.image.attr({
            "x": newPos.x + this.positionInPlate.x,
            "y": newPos.y + this.positionInPlate.y
        });
    }

}
