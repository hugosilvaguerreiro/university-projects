package aasma_proj.agent.modules;

import aasma_proj.agent.Agent;
import aasma_proj.agent.CommunicatingAgent;
import aasma_proj.agent.modules.planningModules.PathPlanningModule;
import aasma_proj.agent.modules.worldInterface.Actuators;
import aasma_proj.items.Order;
import aasma_proj.items.ingredients.Dish;
import aasma_proj.items.ingredients.Ingredient;
import aasma_proj.world.Kitchen;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import static aasma_proj.agent.modules.Message.Method.CARRY_ON;

public class CommunicationModule {
    private LinkedList<Message> incomingMessages;
    private ArrayList<CommunicatingAgent> peerAgents;
    private CommunicatingAgent agent;
    private Order lastOrder = null;
    private Random random = new Random();

    public CommunicationModule(CommunicatingAgent agent){
        this.agent = agent;
        this.incomingMessages = new LinkedList<>();
        this.peerAgents = new ArrayList<>();
    }

    public void broadcastMessage(Message message){
        Kitchen.sendMessage(message, agent);
    }
    public void sendMessage(Message message, CommunicatingAgent recipient){
        recipient.communicationModule.receiveMessage(message);
    }
    public void receiveMessage(Message message){
        this.incomingMessages.push(message);
    }
    public void processMessage(){
        boolean sendMoreACKs = true;
        if(agent.order != null)
            lastOrder = new Order((LinkedList<Ingredient>) agent.order.ingredients.clone());
        while(!incomingMessages.isEmpty()) {
            Message messageToProcess = incomingMessages.removeFirst();
            switch (messageToProcess.getMethod()) {
                case FETCH_INGREDIENT:
                    // need message to have a param so the agent knows what he's supposed to fetch
                    if (messageToProcess.getParams().size() == 2
                            && messageToProcess.getParam(0) instanceof Ingredient
                            && messageToProcess.getParam(1) instanceof Order) {
                        if (agent.item != null
                                && agent.item.equals(messageToProcess.getParam(0))) {
                            // TODO: agent should bring item to someone

                        }
                        if(messageToProcess.getParam(1) instanceof Dish)
                        agent.order = (Order) messageToProcess.getParam(1);
                        agent.desiredIngred = (Ingredient) messageToProcess.getParam(0);
                        agent.currentCoordinator = messageToProcess.getSender();
                    }
                    break;
                case ACK:
                    if (agent.order != null
                            && agent.order.ingredients.size() > 1
                            && messageToProcess.getSender().desiredIngred == null)
                        sendMessage(new Message(agent, Message.Method.FETCH_INGREDIENT, agent.order.getIngred(), lastOrder)
                                        , messageToProcess.getSender());
                    else
                        sendMessage(new Message(agent, CARRY_ON), messageToProcess.getSender());
                    break;
                case HELP:
                    if(agent.state == Agent.State.COOP_IDLE && sendMoreACKs) {
                        sendMessage(new Message(agent, Message.Method.ACK), messageToProcess.getSender());
                        sendMoreACKs = false;
                    }
                case MOVE_AWAY:
                    if(messageToProcess.getParams().size() == 2
                        && messageToProcess.getParam(0) instanceof Integer
                        && messageToProcess.getParam(1) instanceof Boolean){

                        int senderSteps = (Integer) messageToProcess.getParam(0);
                        boolean senderBlocked = (Boolean) messageToProcess.getParam(1);

                        /*System.out.println("---BLOCK!---\n" + messageToProcess.getSender().name + senderSteps + "\n"
                                + agent.name + agent.stepsSinceOrder + "\n"
                                + senderBlocked + (agent.pathPlanningModule.checkAdjPos() == null));*/
                        // If you're carrying a dish you're useless anyway.
                        if(agent.desiredIngred != null && agent.desiredIngred instanceof Dish){
                            agent.pathPlanningModule.moveAway();
                        }
                        if(agent.pathPlanningModule.stay > 0)
                            agent.pathPlanningModule.moveAway();
                        if(senderBlocked && agent.pathPlanningModule.checkAdjPos() != null)
                            agent.pathPlanningModule.moveAway();
                        else if(agent.pathPlanningModule.checkAdjPos() == null && !senderBlocked) {
                            sendMessage(
                                new Message(agent, Message.Method.MOVE_AWAY, agent.stepsSinceOrder, true),
                                messageToProcess.getSender());
                            messageToProcess.getSender().pathPlanningModule.blockedby = null;
                            agent.pathPlanningModule.blockedby = messageToProcess.getSender();
                        }else if(senderSteps < agent.stepsSinceOrder){
                            sendMessage(
                                    new Message(agent,Message.Method.MOVE_AWAY, agent.stepsSinceOrder, false),
                                    messageToProcess.getSender());
                            messageToProcess.getSender().pathPlanningModule.blockedby = null;
                            agent.pathPlanningModule.blockedby = messageToProcess.getSender();
                        }
                        else if(senderSteps > agent.stepsSinceOrder)
                            agent.pathPlanningModule.moveAway();
                        else{
                            if (random.nextBoolean()){
                                //System.out.println("random pick: " + messageToProcess.getSender().name);
                                sendMessage(
                                        new Message(agent,Message.Method.MOVE_AWAY, agent.stepsSinceOrder, false),
                                        messageToProcess.getSender());
                                agent.stepsSinceOrder++; //break tie for good
                                messageToProcess.getSender().pathPlanningModule.blockedby = null;
                                agent.pathPlanningModule.blockedby = messageToProcess.getSender();
                            }
                            else{
                                //System.out.println("random pick: " + agent.name);
                                agent.pathPlanningModule.moveAway();
                                messageToProcess.getSender().stepsSinceOrder++;
                            }
                        }
                    }
                    break;
                case MOVE_BACK:
                    //System.out.println("UNBLOCK!");
                    agent.pathPlanningModule.moveBack();
                case CARRY_ON:
                    break;
            }
        }
    }

}
