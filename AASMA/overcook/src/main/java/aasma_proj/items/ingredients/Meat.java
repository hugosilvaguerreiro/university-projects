package aasma_proj.items.ingredients;

import aasma_proj.GUI.Resources;
import aasma_proj.GUI.Util;
import aasma_proj.agent.modules.planningModules.PathPlanningModule;
import aasma_proj.blocks.counters.Dispenser;
import aasma_proj.world.Kitchen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;

public class Meat extends Ingredient {

    private void setBlockOrder(){
        LinkedList<PathPlanningModule.AvailableBlock> blockOrder = new LinkedList<>();
        blockOrder.add(PathPlanningModule.AvailableBlock.DISPENSER);
        blockOrder.add(PathPlanningModule.AvailableBlock.COOKER);
        blockOrder.add(PathPlanningModule.AvailableBlock.ASSEMBLY);
        this.blockOrder = blockOrder;
    }

    public Meat(Color color, Point point){
        super(color, point);
        this.setBlockOrder();
    }
    public Meat(Image image, Point point){
        super(image, point);
        this.setBlockOrder();
    }
    public Meat(Point point){
        super();
        try {
            this.image = Util.flipImageHorizontally(ImageIO.read(Resources.meat));
            this.point = point;

        } catch (IOException e) {
            this.color = Color.pink;
        }
        this.setBlockOrder();
    }
    public Meat(){
        this.setBlockOrder();
    }

    @Override
    public void grabItem(Point newpoint) {
        Kitchen.removeItem(point);
        point = newpoint;
        Kitchen.insertItem(this,newpoint);
    }

    @Override
    public void dropItem(Point newpoint) {
        Kitchen.removeItem(point);
        point = newpoint;
        Kitchen.insertItem(this,newpoint);
    }
}
