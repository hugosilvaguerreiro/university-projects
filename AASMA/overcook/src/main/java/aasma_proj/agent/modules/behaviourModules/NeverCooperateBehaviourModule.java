package aasma_proj.agent.modules.behaviourModules;

import aasma_proj.agent.Agent;
import aasma_proj.agent.Agent.State;
import aasma_proj.agent.modules.worldInterface.Actuators;
import aasma_proj.agent.modules.worldInterface.Sensors;
import aasma_proj.world.Kitchen;

import java.util.Random;

public class NeverCooperateBehaviourModule extends BehaviourModule {

    private Random random = new Random();
    private int lastBlockedDir = -1;

    public NeverCooperateBehaviourModule(Agent agent) {

        super(agent);
    }

    @Override
    public Actuators.Action selectAction() {
        if(agent.state == State.SINGLE_IDLE) {
            if(Sensors.isOrderDispenser(agent)) {
                return Actuators.Action.INTERACT_WITH_BLOCK;
            }else if (Sensors.isFreeCell(agent)){
                return Actuators.Action.MOVE_AHEAD;
            } else {
                Actuators.Action action;
                do{
                    action = Actuators.Action.getRandomAction();
                } while(action == Actuators.Action.MOVE_AHEAD);
                return action;
            }
        }else {
            return Actuators.Action.STAY;
        }
    }

    @Override
    public Actuators.Action selectAction(Actuators.Action suggestedAction) {
        Actuators.Action action = selectAction();
        if(action == suggestedAction){
            return suggestedAction;
        }
        else {
            switch (suggestedAction) {
                case STAY:
                    return action;
                case MOVE_AHEAD:
                    if(Sensors.isOrderDispenser(agent) && agent.state.equals(State.SINGLE_IDLE)) {
                        return Actuators.Action.INTERACT_WITH_BLOCK;
                    }else if(Sensors.isIngredientDispenser(agent) && agent.state.equals(State.SINGLE_ORDER)) {
                        return Actuators.Action.INTERACT_WITH_BLOCK;
                    }else if (!Sensors.isFreeCell(agent)) {
                        lastBlockedDir = agent.direction;
                        if (random.nextBoolean()) return Actuators.Action.ROTATE_LEFT;
                        else return Actuators.Action.ROTATE_RIGHT;
                    }else return Actuators.Action.MOVE_AHEAD;
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
}
