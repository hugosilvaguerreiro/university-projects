package aasma_proj.blocks.counters;

import aasma_proj.GUI.Resources;
import aasma_proj.GUI.Util;
import aasma_proj.agent.Agent;
import aasma_proj.items.ingredients.Bun;
import aasma_proj.items.ingredients.Ingredient;
import aasma_proj.items.ingredients.Meat;
import aasma_proj.items.ingredients.Tomato;
import aasma_proj.world.Kitchen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class Dispenser extends Counter {

    public Class ingredient;

    public Dispenser(Color color, Class ingredient){
        super(color);
        this.ingredient = ingredient;
    }

    public Dispenser(Image image){
        super(image);
        this.ingredient = ingredient;
    }

    public Dispenser(Class ingredient) {
        this(Kitchen.KitchenSide.RIGHT, ingredient);

    }
    public Dispenser(Kitchen.KitchenSide side, Class ingredient) {
        super();
        try {
            this.image = Util.flipImageHorizontally(ImageIO.read(Resources.fridge));
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
            this.ingredient = ingredient;

        } catch (IOException e) {
            this.color = Resources.fridge_color;
        }
    }

    @Override
    public Class getType(){
        return this.getClass();
    }

    public boolean canInteract(Agent agent){
        // can only canInteract if the agent isn't going to drop items on the dispenser
        return agent.item == null;
    }

    @Override
    public void startInteraction(Agent agent) {
        if (this.ingredient == Tomato.class){
            Kitchen.insertItem(new Tomato(agent.ahead),agent.ahead);
        }
        else if (this.ingredient == Bun.class){
            Kitchen.insertItem(new Bun(agent.ahead),agent.ahead);
        }
        else if (this.ingredient == Meat.class){
            Kitchen.insertItem(new Meat(agent.ahead),agent.ahead);
        }
    }
}
