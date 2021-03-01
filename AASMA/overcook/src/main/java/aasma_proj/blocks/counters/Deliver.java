package aasma_proj.blocks.counters;

import aasma_proj.GUI.Resources;
import aasma_proj.GUI.Util;
import aasma_proj.agent.Agent;
import aasma_proj.agent.BaselineAgent;
import aasma_proj.agent.CommunicatingAgent;
import aasma_proj.items.Order;
import aasma_proj.items.ingredients.Dish;
import aasma_proj.world.Kitchen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Deliver extends Counter {

    public Deliver(Color color){
        super(color);
    }

    public Deliver(Image image){
        super(image);
    }
    public Deliver() {
        this(Kitchen.KitchenSide.RIGHT);
    }
    public Deliver(Kitchen.KitchenSide side) {
        super();
        try {
            this.image = Util.rotateImage(ImageIO.read(Resources.delivery_table), 270);
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
        } catch (IOException e) {
            this.color = Resources.delivery_table_color;
        }
    }

    @Override
    public Class getType(){
        return this.getClass();
    }

    public boolean canInteract(Agent agent){
        // can only canInteract if the agent isn't looking to grab an item
        return agent.item instanceof Dish;
    }

    @Override
    public void startInteraction(Agent agent) {
        if(agent instanceof BaselineAgent)
            agent.order.currentState = Order.OrderState.DELIVERED;
        else
            ((CommunicatingAgent) agent).currentCoordinator.order.currentState = Order.OrderState.DELIVERED;
    }
}
