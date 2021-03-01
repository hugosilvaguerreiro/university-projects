package aasma_proj.blocks.counters;

import aasma_proj.GUI.Resources;
import aasma_proj.agent.Agent;
import aasma_proj.world.Kitchen;

import java.awt.*;

public class EmptyCounter extends Counter {

    public EmptyCounter(Color color){
        super(color);
    }

    public EmptyCounter(Image image){
        super(image);
    }

    public EmptyCounter() {
        super();
        this.color = Resources.empty_counter_color;
    }

    @Override
    public Class getType(){
        return this.getClass();
    }

    public boolean canInteract(Agent agent){
        // can only canInteract if the block has an item to grab, or the agent can drop a block.
        return Kitchen.getItem(agent.ahead) != null || agent.item != null;
    }

    @Override
    public void startInteraction(Agent agent) {}
}
