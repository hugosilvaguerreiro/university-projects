package aasma_proj.blocks.counters;

import aasma_proj.blocks.Block;
import aasma_proj.agent.Agent;

import java.awt.*;

public abstract class Counter extends Block {

    public Counter(Color color){
        super(color);
    }
    public Counter(Image image){
        super(image);
    }

    public Counter() {
        super();
    }

    @Override
    public Class getType(){
        return this.getClass();
    }

    public abstract boolean canInteract(Agent agent);

    public abstract void startInteraction(Agent agent);
}
