package aasma_proj.blocks;

import java.awt.*;
import java.lang.reflect.Type;

public class Block {

    public Color color;
    public Image image;

    public Block(Color color) {
        this.color = color;
        this.image = null;
    }

    public Block(Image image){
        this.color = null;
        this.image = image;

    }

    public Block() {
        this.color = null;
        this.image = null;
    }

    public Class getType(){
        return this.getClass();
    }
}
