package aasma_proj.items.ingredients;

import aasma_proj.agent.modules.planningModules.PathPlanningModule;
import aasma_proj.blocks.Block;
import aasma_proj.blocks.counters.Dispenser;
import aasma_proj.blocks.EmptyBlock;
import aasma_proj.blocks.counters.*;
import aasma_proj.items.Item;
import aasma_proj.world.Kitchen;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

public abstract class Ingredient extends Item {
    protected LinkedList<PathPlanningModule.AvailableBlock> blockOrder;

    public enum ItemState { RAW, PROCESSED, SPOILED }

    public ItemState currentState = ItemState.RAW;

    public Ingredient(Color color, Point point){
        super(color, point);
    }
    public Ingredient(Image image, Point point){
        super(image, point);
    }
    public Ingredient() {
        super();
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

    public void setState(ItemState state){
        currentState = state;
    }

    public PathPlanningModule.AvailableBlock getNextBlock(){
        if (blockOrder.size() != 0)
            return blockOrder.removeFirst();
        else return null;
    }
}
