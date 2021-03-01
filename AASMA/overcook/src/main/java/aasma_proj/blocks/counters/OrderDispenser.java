package aasma_proj.blocks.counters;

import aasma_proj.agent.Agent;
import aasma_proj.items.Order;

import aasma_proj.GUI.Resources;
import aasma_proj.world.Kitchen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class OrderDispenser extends Counter{
    public Image orders;
    public OrderDispenser(Color color){ super(color); }

    public OrderDispenser(Image image){
        super(image);
    }

    public OrderDispenser(){

        this(Kitchen.KitchenSide.RIGHT);

    }

    public OrderDispenser(Kitchen.KitchenSide side) {
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
            this.image = ImageIO.read(Resources.assembly_table);
            this.orders = ImageIO.read(Resources.orders);
        } catch (IOException e) {
            this.color = Resources.order_dispenser_color;
        }
    }
    public Order getOrder(){
        return Order.generateOrder();
    }

    @Override
    public Class getType(){
        return this.getClass();
    }

    public boolean canInteract(Agent agent){
        // can only canInteract if the agent isn't going to drop items on the dispenser
        return agent.item == null
                && (agent.order == null ||(agent.order != null && agent.order.currentState == Order.OrderState.FINISHED))
                && agent.desiredIngred == null;
    }

    @Override
    public void startInteraction(Agent agent) {}
}
