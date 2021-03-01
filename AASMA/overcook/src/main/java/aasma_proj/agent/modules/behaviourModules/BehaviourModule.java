package aasma_proj.agent.modules.behaviourModules;

import aasma_proj.agent.Agent;
import aasma_proj.agent.modules.worldInterface.Actuators;

public abstract class BehaviourModule {
    public Agent agent;

    public BehaviourModule() {this.agent = null;}
    public BehaviourModule(Agent agent) {
        this.agent = agent;
    }

    public abstract Actuators.Action selectAction();
    public abstract Actuators.Action selectAction(Actuators.Action suggestedAction);
    public void execute(Actuators.Action action) {
        BehaviourModule.execute(agent, action);
    }
    public static void execute(Agent agent, Actuators.Action action) {
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
            case STAY:
                Actuators.stay(agent);
            /*case PICK_INGREDIENT:
                break;*/
        }
    }

}
