package aasma_proj.agent.modules.planningModules;

import aasma_proj.agent.modules.worldInterface.Actuators;
import jdk.jshell.spi.ExecutionControl;

public abstract class LearningModule {

    public abstract Actuators.Action suggestAction(Object ... params);

    public Actuators.Action suggestAction() {
        return this.suggestAction(null);
    }



    public void currentState(Object ... params) {}
    public int currentState() {return 0;}
    public void learn(int state, Actuators.Action action) {}
    public int reward(int originalState, Actuators.Action originalAction) {return -1;}
    //public abstract void
}
