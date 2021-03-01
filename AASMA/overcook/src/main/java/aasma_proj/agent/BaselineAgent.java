package aasma_proj.agent;

import aasma_proj.agent.modules.behaviourModules.BehaviourModule;
import aasma_proj.agent.modules.behaviourModules.NeverCooperateBehaviourModule;
import aasma_proj.agent.modules.behaviourModules.SimpleBehaviourModule;
import aasma_proj.agent.modules.planningModules.PathPlanningModule;
import aasma_proj.agent.modules.worldInterface.Actuators;
import aasma_proj.blocks.Block;
import aasma_proj.blocks.counters.OrderDispenser;
import aasma_proj.world.Kitchen;

import java.awt.*;

public class BaselineAgent extends Agent{
    private BehaviourModule behaviourModule;
    public PathPlanningModule pathPlanningModule;

    public BaselineAgent(Point point, Color color) {
        super(point, color);
    }

    public BaselineAgent(Point point) {
        super(point, State.SINGLE_IDLE);
        //behaviourModule = new NeverCooperateBehaviourModule(this);
        behaviourModule = new SimpleBehaviourModule(this);
        pathPlanningModule = new PathPlanningModule(this);
        pathPlanningModule.setCurrentObjective(PathPlanningModule.AvailableBlock.DISPENSER);
        //behaviourModule = new NeverCooperateBehaviourModule(this);
    }

    public BaselineAgent(Point point, Type type) {
        super(point, type, State.SINGLE_IDLE);
        this.behaviourModule = new SimpleBehaviourModule(this);
        this.pathPlanningModule = new PathPlanningModule(this);
        pathPlanningModule.setCurrentObjective(PathPlanningModule.AvailableBlock.DISPENSER);
    }

    @Override
    public void agentDecision() {

        ahead = aheadPosition(); //percept
        if (state == State.SINGLE_IDLE && order == null){
            pathPlanningModule.setCurrentObjective(PathPlanningModule.AvailableBlock.ORDER_DISPENSER);
        }

        if(state == State.SINGLE_IDLE && order != null){
            changeState(State.SINGLE_ORDER);
            desiredIngred = order.getIngred();
            pathPlanningModule.setCurrentObjective(desiredIngred.getNextBlock());
        }

        if  (state == State.SINGLE_ORDER && pathPlanningModule.getCurrentTargetComplete()){
            PathPlanningModule.AvailableBlock nextBlock = desiredIngred.getNextBlock();
            if (nextBlock == null){ //sub-task is done (bun pex)
                desiredIngred = order.getIngred();
                if (desiredIngred == null){ //order is done
                    state = State.SINGLE_IDLE;
                    pathPlanningModule.setCurrentObjective(PathPlanningModule.AvailableBlock.ORDER_DISPENSER);
                    order = null;
                }
                else {
                    pathPlanningModule.setCurrentObjective(desiredIngred.getNextBlock());
                }
            }
            else{ //next step in sub-task (ingredient)
                pathPlanningModule.setCurrentObjective(nextBlock);
            }
        }

        //int originalState = getState(point,direction,cargo);
        Actuators.Action suggestedAction = pathPlanningModule.suggestAction();
        //System.out.println(suggestedAction);
        Actuators.Action originalAction = behaviourModule.selectAction(suggestedAction);
        //System.out.println(originalAction);
        behaviourModule.execute(originalAction);

        if(!suggestedAction.equals(originalAction) && suggestedAction.equals(Actuators.Action.MOVE_AHEAD))
            pathPlanningModule.switchAxisPriority();
        else if(!suggestedAction.equals(originalAction) &&
                (suggestedAction.equals(Actuators.Action.ROTATE_RIGHT) || suggestedAction.equals(Actuators.Action.ROTATE_LEFT) ))
            pathPlanningModule.switchAxisPriorityInv();

        //proactiveDecision(); /* DBI */
        //reactiveDecision();
        //learningDecision(originalState,originalAction); /* RL */
    }


}
