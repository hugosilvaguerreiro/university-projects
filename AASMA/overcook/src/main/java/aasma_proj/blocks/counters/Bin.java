package aasma_proj.blocks.counters;

import aasma_proj.GUI.Resources;
import aasma_proj.agent.Agent;
import aasma_proj.world.Kitchen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Bin extends Counter {

    public Bin(Color color){
        super(color);
    }
    public Bin(Image image){
        super(image);
    }

    public Bin() {
        this(Kitchen.KitchenSide.RIGHT);
    }

    public Bin(Kitchen.KitchenSide side) {
        super();
        try {
            switch (side) {
                case UP:
                    break;
                case DOWN:
                    break;
                case LEFT:
                    break;
                case RIGHT:
                    break;
            }
            this.image = ImageIO.read(Resources.garbage_bin);
        } catch (IOException e) {
            this.color = Resources.garbage_color;
        }
    }

    @Override
    public Class getType(){
        return this.getClass();
    }

    public boolean canInteract(Agent agent){
        // can only canInteract if the agent isn't looking to grab an item from the bin
        return agent.item != null;
    }

    @Override
    public void startInteraction(Agent agent) {
        //remove item from kitchen
        // Kitchen.removeItem(agent.ahead);
    }
}
