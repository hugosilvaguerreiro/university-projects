package aasma_proj.items.ingredients;

import aasma_proj.agent.modules.planningModules.PathPlanningModule;
import aasma_proj.blocks.counters.Dispenser;
import aasma_proj.world.Kitchen;

import java.awt.*;
import java.util.LinkedList;

public class Ruined extends Ingredient {

    private void setBlockOrder(){
        LinkedList<PathPlanningModule.AvailableBlock> blockOrder = new LinkedList<>();
        blockOrder.add(PathPlanningModule.AvailableBlock.BIN);
        this.blockOrder = blockOrder;
    }

    public Ruined(Color color, Point point){
        super(color, point);
    }
    public Ruined(Image image, Point point){
        super(image, point);
    }
    public Ruined(Point point) {
        this(Color.black, point);
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
