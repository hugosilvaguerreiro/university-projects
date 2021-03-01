package aasma_proj.items.ingredients;

import aasma_proj.GUI.Resources;
import aasma_proj.GUI.Util;
import aasma_proj.blocks.counters.Dispenser;
import aasma_proj.agent.modules.planningModules.PathPlanningModule;
import aasma_proj.world.Kitchen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;

public class Bun extends Ingredient {

    private void setBlockOrder(){
        LinkedList<PathPlanningModule.AvailableBlock> blockOrder = new LinkedList<>();
        blockOrder.add(PathPlanningModule.AvailableBlock.DISPENSER);
        blockOrder.add(PathPlanningModule.AvailableBlock.CUTTING);
        blockOrder.add(PathPlanningModule.AvailableBlock.ASSEMBLY);
        this.blockOrder = blockOrder;
    }

    public Bun(Color color, Point point){
        super(color, point);
        setBlockOrder();
    }
    public Bun(Image image, Point point){
        super(image, point);
        setBlockOrder();
    }
    public Bun(Point point) {
        super();
        try {
            this.image = Util.flipImageHorizontally(ImageIO.read(Resources.bun));
            this.point = point;

        } catch (IOException e) {
            this.color = Color.orange;
        }
    }
    public Bun(){setBlockOrder();}

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
