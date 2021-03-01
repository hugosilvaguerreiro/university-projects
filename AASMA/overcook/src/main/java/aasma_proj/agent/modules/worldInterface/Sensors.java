package aasma_proj.agent.modules.worldInterface;

import aasma_proj.agent.Agent;
import aasma_proj.blocks.counters.*;
import aasma_proj.blocks.EmptyBlock;
import aasma_proj.world.Kitchen;

import java.awt.*;
import java.security.PublicKey;
import java.util.ArrayList;

public class Sensors {
    /* *******************
    ***** E: sensors ****
    /********************/

    public static ArrayList<Actuators.Action> availableActions(Agent agent) {
        ArrayList<Actuators.Action> elegibleActions = new ArrayList<>();
        elegibleActions.add(Actuators.Action.ROTATE_RIGHT);
        elegibleActions.add(Actuators.Action.ROTATE_LEFT);
        elegibleActions.add(Actuators.Action.STAY);
        if(isFreeCell(agent)) {
            elegibleActions.add(Actuators.Action.MOVE_AHEAD);
        }
        if(isCounter(agent)) {
            elegibleActions.add(Actuators.Action.INTERACT_WITH_BLOCK);
        }
        return elegibleActions;
    }

    /* Check if agent is carrying box */
    public static boolean item(Agent agent) {
        return agent.item != null;
    }

    /* Check if the cell ahead is floor (which means not a wall, not a shelf nor a ramp) and there are any robot there */
    public static boolean isFreeCell(Agent agent) {
        return Kitchen.getBlock(agent.ahead) instanceof EmptyBlock && Kitchen.getAgent(agent.ahead)==null;
    }

    public static boolean isAgent(Agent agent){ return Kitchen.getAgent(agent.ahead)!=null;}

    public static boolean isIngredientDispenser(Agent agent){
        return Kitchen.getBlock(agent.ahead) instanceof Dispenser;
    }


    public static boolean isOrderDispenser(Agent agent){ return Kitchen.getBlock(agent.ahead) instanceof OrderDispenser;}

    public static boolean isCounter(Agent agent) {return Kitchen.getBlock(agent.ahead) instanceof Counter;}

    public static boolean isCooker(Agent agent) {return Kitchen.getBlock(agent.ahead) instanceof Cooker;}

    public static boolean isEmptyCounter(Agent agent) {return Kitchen.getBlock(agent.ahead) instanceof EmptyCounter;}

    public static boolean isAssembly(Agent agent) {return Kitchen.getBlock(agent.ahead) instanceof Assembly;}

    public static boolean isCutting(Agent agent) {return Kitchen.getBlock(agent.ahead) instanceof CuttingBlock;}

    public static boolean isBin(Agent agent) {return Kitchen.getBlock(agent.ahead) instanceof Bin;}

    public static boolean isDeliver(Agent agent) {return Kitchen.getBlock(agent.ahead) instanceof Deliver;}


    /* Return the color of cell */
    public static Color cellColor(Agent agent) {
        return Kitchen.getBlock(agent.ahead).color;
    }

    /* Check if the cell ahead is a wall */
    private static boolean isWall(Agent agent) {
        return agent.ahead.x<0 || agent.ahead.y<0 || agent.ahead.x>=Kitchen.nX || agent.ahead.y>=Kitchen.nY;
    }

    /* Check if the cell ahead is a wall */
    private boolean isWall(int x, int y) {
        return x<0 || y<0 || x>=Kitchen.nX || y>=Kitchen.nY;
    }



}
