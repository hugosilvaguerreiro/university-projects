package aasma_proj.agent;

import aasma_proj.GUI.Resources;
import aasma_proj.agent.modules.CommunicationModule;
import aasma_proj.agent.modules.Message;
import aasma_proj.agent.modules.behaviourModules.BehaviourModule;
import aasma_proj.agent.modules.behaviourModules.CommunicateBehaviourModule;
import aasma_proj.agent.modules.planningModules.PathPlanningModule;
import aasma_proj.agent.modules.worldInterface.Actuators;
import aasma_proj.items.Order;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

public class CommunicatingAgent extends Agent {
    public CommunicatingAgent currentCoordinator = null;
    public CommunicationModule communicationModule;

    private BehaviourModule behaviourModule;
    public PathPlanningModule pathPlanningModule;

    public int stepsSinceOrder = 0;

    public CommunicatingAgent(Point point, Color color) {
        super(point, color);
    }

    public CommunicatingAgent(Point point) {
        super(point, State.COOP_IDLE);
        //behaviourModule = new NeverCooperateBehaviourModule(this);
        this.behaviourModule = new CommunicateBehaviourModule(this);
        this.pathPlanningModule = new PathPlanningModule(this);
        this.pathPlanningModule.setCurrentObjective(PathPlanningModule.AvailableBlock.DISPENSER);
        //behaviourModule = new NeverCooperateBehaviourModule(this);
        this.communicationModule = new CommunicationModule(this);
    }

    public CommunicatingAgent(Point point, Type type) {
        super(point, type, State.COOP_IDLE);
        this.behaviourModule = new CommunicateBehaviourModule(this);
        this.pathPlanningModule = new PathPlanningModule(this);
        this.pathPlanningModule.setCurrentObjective(PathPlanningModule.AvailableBlock.DISPENSER);
        this.communicationModule = new CommunicationModule(this);
    }

    @Override
    public void agentDecision() {
        ahead = aheadPosition();
        communicationModule.processMessage();

        if (state == State.COOP_IDLE) {
            if (desiredIngred != null) {
                changeState(State.COOP_WORKING);
                pathPlanningModule.setCurrentObjective(desiredIngred.getNextBlock());
                pathPlanningModule.setCurrentTargetComplete(false);
            } else if (order != null) {
                changeState(State.COOP_COORDINATOR);
                switchSprite();
                // request help from everyone
                if (order.ingredients.size() > 0)
                    communicationModule.broadcastMessage(new Message(this, Message.Method.HELP));
                pathPlanningModule.setCurrentObjective(PathPlanningModule.AvailableBlock.ORDER_DISPENSER);
            } else { // no desire or order: race to the order dispenser!
                if (currentCoordinator != null) {
                    communicationModule.sendMessage(new Message(this, Message.Method.ACK), currentCoordinator);
                    pathPlanningModule.setCurrentObjective(PathPlanningModule.AvailableBlock.BIN);
                    currentCoordinator = null;
                } else pathPlanningModule.setCurrentObjective(PathPlanningModule.AvailableBlock.ORDER_DISPENSER);
            }
        }
        if (state == State.COOP_WORKING) {
            stepsSinceOrder++;
            if (desiredIngred != null && pathPlanningModule.getCurrentTargetComplete()) {
                PathPlanningModule.AvailableBlock nextBlock = desiredIngred.getNextBlock();
                if (nextBlock == null) { //sub-task is done
                    desiredIngred = null;
                    changeState(State.COOP_IDLE);
                    stepsSinceOrder = 0;
                    communicationModule.broadcastMessage(new Message(this, Message.Method.ACK));
                } else { //next step in sub-task (ingredient)
                    pathPlanningModule.setCurrentObjective(nextBlock);
                    stepsSinceOrder = 0;
                }
            }
        } else if (state == State.COOP_COORDINATOR) {
            stepsSinceOrder++;
            if (order != null) {
                if (order.currentState.equals(Order.OrderState.DELIVERED)) {
                    order = null;
                    desiredIngred = null;
                    pathPlanningModule.setCurrentObjective(PathPlanningModule.AvailableBlock.ORDER_DISPENSER);
                    changeState(State.COOP_IDLE);
                    stepsSinceOrder = 0;
                    switchSprite();
                } else if (order.ingredients.size() > 0) {
                    if(order.ingredients.size() > 1)
                        communicationModule.broadcastMessage(new Message(this, Message.Method.HELP));
                    if ((order.ingredients.size() > 1)
                            && desiredIngred == null) {
                        desiredIngred = order.getIngred();
                        pathPlanningModule.setCurrentObjective(desiredIngred.getNextBlock());
                        pathPlanningModule.setCurrentTargetComplete(false);
                        currentCoordinator = this;
                    }
                }
                if (desiredIngred != null && pathPlanningModule.getCurrentTargetComplete()) {
                    PathPlanningModule.AvailableBlock nextBlock = desiredIngred.getNextBlock();
                    if (nextBlock == null) { //sub-task is done
                        desiredIngred = null;
                    } else { //next step in sub-task (ingredient)
                        pathPlanningModule.setCurrentObjective(nextBlock);
                        stepsSinceOrder = 0;
                    }
                } else if (desiredIngred == null)
                    pathPlanningModule.setCurrentObjective(PathPlanningModule.AvailableBlock.BIN);
            } else {
                desiredIngred = null;
                pathPlanningModule.setCurrentObjective(PathPlanningModule.AvailableBlock.ORDER_DISPENSER);
                changeState(State.COOP_IDLE);
                stepsSinceOrder = 0;
                switchSprite();
            }
        }

        Actuators.Action suggestedAction = pathPlanningModule.suggestAction();
        Actuators.Action originalAction = behaviourModule.selectAction(suggestedAction);
        behaviourModule.execute(originalAction);
    }

    private void switchSprite() {
        try {
            switch (this.type) {
                case THIN:
                    if (state != State.COOP_COORDINATOR) {
                        this.lookLeft = ImageIO.read(Resources.cook01_l);
                        this.lookRight = ImageIO.read(Resources.cook01_r);
                        this.lookFront = ImageIO.read(Resources.cook01_f);
                        this.lookBack = ImageIO.read(Resources.cook01_b);
                    } else {
                        this.lookLeft = ImageIO.read(Resources.cook01c_l);
                        this.lookRight = ImageIO.read(Resources.cook01c_r);
                        this.lookFront = ImageIO.read(Resources.cook01c_f);
                        this.lookBack = ImageIO.read(Resources.cook01c_b);
                    }
                    break;
                case FAT:
                    if (state != State.COOP_COORDINATOR) {
                        this.lookLeft = ImageIO.read(Resources.cook02_l);
                        this.lookRight = ImageIO.read(Resources.cook02_r);
                        this.lookFront = ImageIO.read(Resources.cook02_f);
                        this.lookBack = ImageIO.read(Resources.cook02_b);
                    } else {
                        this.lookLeft = ImageIO.read(Resources.cook02c_l);
                        this.lookRight = ImageIO.read(Resources.cook02c_r);
                        this.lookFront = ImageIO.read(Resources.cook02c_f);
                        this.lookBack = ImageIO.read(Resources.cook02c_b);
                    }
                    break;
                case BIG_SMOKE:
                    if (state != State.COOP_COORDINATOR) {
                        this.lookLeft = ImageIO.read(Resources.cook03_l);
                        this.lookRight = ImageIO.read(Resources.cook03_r);
                        this.lookFront = ImageIO.read(Resources.cook03_f);
                        this.lookBack = ImageIO.read(Resources.cook03_b);
                    } else {
                        this.lookLeft = ImageIO.read(Resources.cook03c_l);
                        this.lookRight = ImageIO.read(Resources.cook03c_r);
                        this.lookFront = ImageIO.read(Resources.cook03c_f);
                        this.lookBack = ImageIO.read(Resources.cook03c_b);
                    }
                    break;
                case KAREN:
                    if (state != State.COOP_COORDINATOR) {
                        this.lookLeft = ImageIO.read(Resources.cook04_l);
                        this.lookRight = ImageIO.read(Resources.cook04_r);
                        this.lookFront = ImageIO.read(Resources.cook04_f);
                        this.lookBack = ImageIO.read(Resources.cook04_b);
                    } else {
                        this.lookLeft = ImageIO.read(Resources.cook04c_l);
                        this.lookRight = ImageIO.read(Resources.cook04c_r);
                        this.lookFront = ImageIO.read(Resources.cook04c_f);
                        this.lookBack = ImageIO.read(Resources.cook04c_b);
                    }
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}