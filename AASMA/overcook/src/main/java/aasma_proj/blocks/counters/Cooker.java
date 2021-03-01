package aasma_proj.blocks.counters;

import aasma_proj.GUI.Resources;
import aasma_proj.agent.Agent;
import aasma_proj.items.ingredients.Bun;
import aasma_proj.items.ingredients.Ingredient;
import aasma_proj.items.ingredients.Meat;
import aasma_proj.items.ingredients.Tomato;
import aasma_proj.world.Kitchen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Cooker extends Counter {
    public Integer timer;

    public Cooker() {
        this(Kitchen.KitchenSide.RIGHT);
    }

    public Cooker(Kitchen.KitchenSide side) {
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
            this.image = ImageIO.read(Resources.cooker);
        } catch (IOException e) {
            this.color = Resources.cooker_color;
        }
    }

    public Cooker(Color color){
        super(color);
    }
    public Cooker(Image image){
        super(image);
    }

    @Override
    public Class getType(){
        return this.getClass();
    }

    public boolean isCounting(){ return timer != null;}

    public void incrementTimer(){
        if (timer != null){
            timer++;
        }
        if (timer != null && timer == 5){
            Ingredient i = (Ingredient)Kitchen.getItem(new Point(Kitchen.cookerX, Kitchen.cookerY));
            i.setState(Ingredient.ItemState.PROCESSED);
            timer = null;
        }
    }

    public boolean canInteract(Agent agent) {
        if (agent.item == null) {
            if (Kitchen.getItem(agent.ahead) == null) //agent has no item and counter has no item
                return false;
            /*if (timer != null) //item is cooking
                return false;*/
        }
        if (agent.item != null){
            if (Kitchen.getItem(agent.ahead) != null) // agent has item but counter already has item)
                return false;
            if (((Ingredient) Kitchen.getItem(agent.item.point)).currentState == Ingredient.ItemState.PROCESSED) //item is cooked
                return false;
            /*if (((Ingredient) Kitchen.getItem(agent.ahead)).currentState == Ingredient.ItemState.SPOILED) //item is spoiled
                return false;*/
        }

        return true;
    }

    @Override
    public void startInteraction(Agent agent) {
        if (agent.item == null) { //agent wants to grab ingredient
            //timer = null;
            return;
        }
        //timer = 0;
        //System.out.println("Got a " + agent.item.getClass().getName());
        if (agent.item instanceof Bun || agent.item instanceof Tomato){
            agent.item.setState(Ingredient.ItemState.SPOILED);
        }
        else if (agent.item instanceof Meat){
            agent.item.setState(Ingredient.ItemState.PROCESSED);
        }

    }
}
