package aasma_proj.agent;

import aasma_proj.agent.modules.behaviourModules.BehaviourModule;
import aasma_proj.agent.modules.behaviourModules.SimpleBehaviourModule;
import aasma_proj.agent.modules.planningModules.LearningModule;
import aasma_proj.agent.modules.planningModules.PathPlanningModule;
import aasma_proj.agent.modules.planningModules.QLearningModule;
import aasma_proj.agent.modules.worldInterface.Actuators;

import java.awt.*;

public class LearningAgent extends Agent{
    public BehaviourModule behaviourModule;
    public LearningModule learningModule;

    public LearningAgent(Point point, Color color) {
        super(point, color);
    }

    public LearningAgent(Point point) {
        super(point, State.SINGLE_IDLE);
        //behaviourModule = new NeverCooperateBehaviourModule(this);
        learningModule = new QLearningModule(this, 0.9, 1, 0.9);
    }

    @Override
    public void agentDecision() {

        ahead = aheadPosition(); //percept

        Actuators.Action suggestedAction = learningModule.suggestAction();
        int originalState = learningModule.currentState();

        BehaviourModule.execute(this, suggestedAction);

        learningModule.learn(originalState, suggestedAction);




        //proactiveDecision(); /* DBI */
        //reactiveDecision();
        //learningDecision(originalState,originalAction); /* RL */
    }


}
