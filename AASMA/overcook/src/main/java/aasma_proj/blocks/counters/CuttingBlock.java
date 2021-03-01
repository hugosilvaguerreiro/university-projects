package aasma_proj.blocks.counters;

import aasma_proj.GUI.Resources;
import aasma_proj.agent.Agent;
import aasma_proj.items.ingredients.Ingredient;
import aasma_proj.world.Kitchen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.TimerTask;
import java.util.Timer;

public class CuttingBlock extends Counter {
    Timer timer = new Timer();
    TimerTask task;
    int seconds = 0;

    public CuttingBlock(Color color) {
        super(color);
    }

    public CuttingBlock(Image image) {
        super(image);
    }

    public CuttingBlock() {
        super();
        try {
            this.image = ImageIO.read(Resources.cutting_table);
        } catch (IOException e) {
            this.color = Resources.cutting_table_color;
        }
    }

    @Override
    public Class getType() {
        return this.getClass();
    }

    public boolean canInteract(Agent agent) {
        if (agent.item == null && Kitchen.getItem(agent.ahead) == null) //agent has no item and counter has no item
            return false;
        if (agent.item != null && Kitchen.getItem(agent.ahead) != null) // agent has item but counter already has item
            return false;
        return true;
    }

    @Override
    public void startInteraction(Agent agent) {
        if (agent.item == null) //agent wants to grab ingredient
            //TODO transform tomato into sliced tomato, pex
            return;
        //System.out.println("Got a " + agent.item.getClass().getName());
        if (agent.item.currentState == Ingredient.ItemState.SPOILED){
            return;
        }
        agent.item.setState(Ingredient.ItemState.PROCESSED);
    }

}
