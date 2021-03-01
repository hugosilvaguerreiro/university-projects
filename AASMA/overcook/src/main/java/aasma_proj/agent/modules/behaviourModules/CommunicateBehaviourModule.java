package aasma_proj.agent.modules.behaviourModules;

import aasma_proj.agent.Agent;
import aasma_proj.agent.BaselineAgent;
import aasma_proj.agent.CommunicatingAgent;
import aasma_proj.agent.modules.worldInterface.Actuators;
import aasma_proj.agent.modules.worldInterface.Sensors;
import aasma_proj.blocks.counters.Counter;
import aasma_proj.blocks.counters.Dispenser;
import aasma_proj.blocks.counters.OrderDispenser;
import aasma_proj.world.Kitchen;

import java.util.Random;

public class CommunicateBehaviourModule extends BehaviourModule {
    public CommunicateBehaviourModule(Agent agent) {
        super(agent);
    }
    private int lastBlockedDir = -1;
    private Random random = new Random();

    @Override
    public Actuators.Action selectAction() {
        if(agent.state == Agent.State.SINGLE_IDLE || agent.state == Agent.State.SINGLE_ORDER) {
            if(Sensors.isOrderDispenser(agent)) {
                return Actuators.Action.INTERACT_WITH_BLOCK;
            }
            else if (Sensors.isFreeCell(agent)) {
                return Actuators.Action.MOVE_AHEAD;
            }
            else if (Sensors.isCounter(agent)){
                return Actuators.Action.INTERACT_WITH_BLOCK;
            }
            else {
                Actuators.Action action;
                do{
                    action = Actuators.Action.getRandomAction();
                } while(action == Actuators.Action.MOVE_AHEAD);
                return action;
            }
        }
        else {
            return Actuators.Action.STAY;
        }
    }

    @Override
    public Actuators.Action selectAction(Actuators.Action suggestedAction) {
        Actuators.Action action = selectAction();
        if(action == suggestedAction){
            lastBlockedDir = -1;
            return suggestedAction;
        }
        else {
            switch (suggestedAction) {
                case STAY:
                    return action;
                case MOVE_AHEAD:
                    if(Sensors.isOrderDispenser(agent) && ((OrderDispenser) Kitchen.getBlock(agent.ahead)).canInteract(agent)) {
                        return Actuators.Action.INTERACT_WITH_BLOCK;
                    }
                    else if (!Sensors.isFreeCell(agent)) {
                        if(Kitchen.getBlock(agent.ahead).getClass() ==  ((CommunicatingAgent) agent).pathPlanningModule.getCurrentTarget().className){
                            if (Sensors.isCounter(agent)){
                                if (!(Sensors.isAssembly(agent) || Sensors.isDeliver(agent) || Sensors.isIngredientDispenser(agent))){
                                    if (agent.item == null) {
                                        ((CommunicatingAgent) agent).pathPlanningModule.setCurrentTargetComplete(true);
                                    }
                                    if(((Counter)Kitchen.getBlock(agent.ahead)).canInteract(agent))
                                        return Actuators.Action.INTERACT_WITH_BLOCK;
                                }
                                else if (Sensors.isIngredientDispenser(agent)
                                        && ((Dispenser)Kitchen.getBlock(agent.ahead)).ingredient == agent.desiredIngred.getClass()){
                                    if(((Counter)Kitchen.getBlock(agent.ahead)).canInteract(agent)) {
                                        ((CommunicatingAgent) agent).pathPlanningModule.setCurrentTargetComplete(true);
                                        return Actuators.Action.INTERACT_WITH_BLOCK;
                                    }
                                }
                                else{
                                    if(!(Sensors.isIngredientDispenser(agent)
                                        && ((Dispenser)Kitchen.getBlock(agent.ahead)).ingredient != agent.desiredIngred.getClass())
                                        && ((Counter)Kitchen.getBlock(agent.ahead)).canInteract(agent)) {
                                        ((CommunicatingAgent) agent).pathPlanningModule.setCurrentTargetComplete(true);
                                        return Actuators.Action.INTERACT_WITH_BLOCK;
                                    }
                                }
                            }
                        }
                        // "staying" overrides behaviour core actions.
                        if(agent instanceof CommunicatingAgent
                            && ((CommunicatingAgent) agent).pathPlanningModule.stay == 0){
                            ((CommunicatingAgent) agent).pathPlanningModule.findCurrentTarget();
                            lastBlockedDir = agent.direction;
                        }
                        else if (agent instanceof BaselineAgent){
                            ((BaselineAgent) agent).pathPlanningModule.findCurrentTarget();
                            lastBlockedDir = agent.direction;
                        }
                        if (random.nextBoolean()) return Actuators.Action.ROTATE_LEFT;
                        else return Actuators.Action.ROTATE_RIGHT;
                    }
                    else return Actuators.Action.MOVE_AHEAD;
                case ROTATE_RIGHT:
                    if(Sensors.isFreeCell(agent) && (agent.direction + 90) % 360 == lastBlockedDir) {
                        lastBlockedDir = -1;
                        return Actuators.Action.MOVE_AHEAD;
                    }
                    else return Actuators.Action.ROTATE_RIGHT;
                case ROTATE_LEFT:
                    if(Sensors.isFreeCell(agent) && (agent.direction - 90 + 360) % 360 == lastBlockedDir) {
                        lastBlockedDir = -1;
                        return Actuators.Action.MOVE_AHEAD;
                    }
                    else return Actuators.Action.ROTATE_LEFT;
                default:
                    return suggestedAction;

            }
        }
    }

    @Override
    public void execute(Actuators.Action action) {
        switch (action){
            case MOVE_AHEAD:
                Actuators.moveAhead(agent);
                break;
            case ROTATE_LEFT:
                Actuators.rotateLeft(agent);
                break;
            case ROTATE_RIGHT:
                Actuators.rotateRight(agent);
                break;
            case INTERACT_WITH_BLOCK:
                Actuators.interactWithBlock(agent);
                break;
        }
    }
}
