package aasma_proj.items;

import aasma_proj.blocks.Block;
import aasma_proj.blocks.counters.Dispenser;
import aasma_proj.world.Kitchen;

import java.awt.*;
import java.util.ArrayList;

public class Item {

    public Color color;
    public Image image;
    public Point point;


    public Item(Color color, Point point){
        this.color = color;
        this.image = null;
        this.point = point;
    }
    public Item(Image image, Point point){
        this.color = null;
        this.image = image;
        this.point = point;
    }

    public Item() {
        this.color = null;
        this.image = null;
    }

    public void grabItem(Point newpoint) {
        Kitchen.removeItem(point);
        point = newpoint;
    }

    public void dropItem(Point newpoint) {
        Kitchen.insertItem(this,newpoint);
        point = newpoint;
    }

    public void moveItem(Point newpoint) {
        Kitchen.removeItem(point);
        Kitchen.insertItem(this, newpoint);
        point = newpoint;
    }
}
