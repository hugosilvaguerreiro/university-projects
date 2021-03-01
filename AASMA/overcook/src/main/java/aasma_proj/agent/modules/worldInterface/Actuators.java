package aasma_proj.agent.modules.worldInterface;

import aasma_proj.agent.Agent;
import aasma_proj.agent.CommunicatingAgent;
import aasma_proj.blocks.counters.Bin;
import aasma_proj.blocks.counters.Dispenser;
import aasma_proj.blocks.counters.CuttingBlock;
import aasma_proj.blocks.counters.OrderDispenser;
import aasma_proj.items.Item;
import aasma_proj.items.Order;
import aasma_proj.items.ingredients.Bun;
import aasma_proj.items.ingredients.Ingredient;
import aasma_proj.items.ingredients.Meat;
import aasma_proj.items.ingredients.Tomato;
import aasma_proj.world.Kitchen;
import aasma_proj.blocks.Block;
import aasma_proj.blocks.counters.Counter;

import java.util.Random;

public abstract class Actuators {


    public enum Action {
        STAY, MOVE_AHEAD, ROTATE_RIGHT, ROTATE_LEFT, INTERACT_WITH_BLOCK;

        public static Action getRandomAction() {
            Random random = new Random();
            Action a = values()[random.nextInt(values().length)];
            while ( a == INTERACT_WITH_BLOCK)
                a = values()[random.nextInt(values().length)];
            return a;
        }
    }

    /* Rotate agent randomly */
    public static void rotateRandomly(Agent agent) {
        Random random = new Random();
        if(random.nextBoolean()) Actuators.rotateLeft(agent);
        else rotateRight(agent);
    }

    /* Rotate agent to right */
    public static void rotateRight(Agent agent) {
        agent.direction = (agent.direction+90)%360;
    }

    /* Rotate agent to left */
    public static void rotateLeft(Agent agent) {
        agent.direction = (agent.direction-90+360)%360;
    }

    /* Move agent forward */
    public static void moveAhead(Agent agent) {
        Kitchen.updateAgentPosition(agent.point, agent.ahead);
        if(Sensors.item(agent)) agent.item.moveItem(agent.ahead);
        agent.point = agent.ahead;
    }

    public static void interactWithBlock(Agent agent) {
        Block block = Kitchen.getBlock(agent.ahead);
        if(block instanceof Counter){
            Counter c = (Counter) block;
            boolean canInteract = c.canInteract(agent);
            if(canInteract){
                if(block instanceof OrderDispenser){
                    if (agent.order == null) {
                        agent.order = Order.generateOrder();
                    }
                }
                else if(agent.item == null && Kitchen.getItem(agent.ahead) != null){
                    c.startInteraction(agent);
                    grabItem(agent);
                }
                else{
                    if(agent.item != null) {
                        c.startInteraction(agent);
                        dropItem(agent);
                    }
                }
            }
            else if(agent instanceof CommunicatingAgent){
                ((CommunicatingAgent) agent).pathPlanningModule.setCurrentTargetComplete(false);
            }
        }
    }

    public static void stay(Agent agent) {
    }

    private static void grabItem(Agent agent){
        Ingredient i = (Ingredient)Kitchen.getItem(agent.ahead);
        agent.item = i;
        agent.item.grabItem(agent.point);
    }

    private static void dropItem(Agent agent){
        agent.item.dropItem(agent.ahead);
        agent.lastItemHeld = agent.item;
        agent.item = null;
        //tmp.Board.sendMessage(Action.drop, ahead);
    }

}
