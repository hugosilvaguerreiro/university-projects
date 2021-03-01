package aasma_proj.agent.modules.planningModules;

import aasma_proj.agent.Agent;
import aasma_proj.agent.modules.worldInterface.Actuators;
import aasma_proj.agent.modules.worldInterface.Sensors;
import aasma_proj.items.Order;
import aasma_proj.items.ingredients.Bun;
import aasma_proj.items.ingredients.Ingredient;
import aasma_proj.items.ingredients.Meat;
import aasma_proj.items.ingredients.Tomato;
import aasma_proj.world.Kitchen;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class QLearningModule extends LearningModule{


    //########### FEATURES #########
    public static enum availableIngredientsToModel {
        BUN(Bun.class), MEAT(Meat.class), TOMATO(Tomato.class);

        public Class classname;
        private availableIngredientsToModel(Class classname) {
            this.classname = classname;
        }
    }

    private int nrDirections = 4;
    private int nrPossiblePositions = Kitchen.nX * Kitchen.nY;
    private int nrCurrentOrderItem = availableIngredientsToModel.values().length;
    private int nrCurrentOrderState = Order.OrderState.values().length + 1; //+1 because the order can be null, i.e its not doing any order.
    private int currentItemState = Ingredient.ItemState.values().length + 1; //+1 because the object can ben null, i.e its not carrying an item.
    //##############################

    int NStates = nrDirections * nrPossiblePositions * nrCurrentOrderItem * nrCurrentOrderState * currentItemState;
    int NActions = Actuators.Action.values().length;
    double[][] model;

    int total = 100000000;
    int it = 0;
    private double discount;// suggested value: 0.9;
    private double learningRate;// suggested value: 0.8;
    private double explorationRate;// suggested value: 0.05;
    private double dec;

    Agent agent;
    private List<Actuators.Action> actions;
    private List<availableIngredientsToModel> availableIngredients;
    private List<Ingredient.ItemState> itemStates;
    private List<Order.OrderState> orderStates;

    private Random random = new Random();

    public QLearningModule(Agent agent, double learningRate, double explorationRate, double discount) {
        this.agent = agent;

        actions = Arrays.asList(Actuators.Action.values());
        availableIngredients = Arrays.asList(availableIngredientsToModel.values());
        itemStates = Arrays.asList(Ingredient.ItemState.values());
        orderStates = Arrays.asList(Order.OrderState.values());


        this.explorationRate = explorationRate;
        this.learningRate = learningRate;
        this.discount = discount;
        initQFunction();

    }

    private void initQFunction() {
        model = new double[NStates][NActions];
        dec = (discount-0.1)/total; //Calculate the decrease value to be applied to the discount rate

    }

    @Override
    public int currentState() {
        int state = 0;
        Point currentPosition = agent.point;
        int direction = agent.direction/90;
        int currentItem = 0; //0 -> null, not holding anything
        int currentItemState = 0; //0 -> null, not holding anything
        int currentOrderState = 0; //0 -> null, not holding any order
        int currentOrderItem = 0; //0 -> null, not holding any order
        if(agent.item != null) {
            currentItem = IntStream.range(0, availableIngredientsToModel.values().length)
                    .filter(i -> availableIngredientsToModel.values()[i].classname.equals(agent.item.getClass()))
                    .findFirst().getAsInt() + 1;

            currentItemState = itemStates.indexOf(((Ingredient)agent.item).currentState) + 1;
            //currentItemState = IntStream.range(0, Ingredient.ItemState.values().length)
            //        .filter(i -> Ingredient.ItemState.values()[i].equals(((Ingredient)agent.item).currentState))
            //        .findFirst().getAsInt() + 1;
        }

        if(agent.order != null) {
            currentOrderState = orderStates.indexOf(agent.order.currentState) + 1;
            //currentOrderState =  IntStream.range(0, Order.OrderState.values().length)
            //        .filter(i -> Order.OrderState.values()[i].equals(agent.order.currentState))
            //        .findFirst().getAsInt() +1;
            currentOrderItem = IntStream.range(0, availableIngredientsToModel.values().length)
                    .filter(i -> availableIngredientsToModel.values()[i].classname.equals(agent.desiredIngred.getClass()))
                    .findFirst().getAsInt() +1;
        }

        state += currentPosition.x*Kitchen.nY + currentPosition.y;
        state += direction * Kitchen.nX * Kitchen.nY;
        state += currentItem * nrDirections * Kitchen.nX * Kitchen.nY;
        state += currentItemState * availableIngredientsToModel.values().length * nrDirections * Kitchen.nX * Kitchen.nY;
        state += currentOrderState * Ingredient.ItemState.values().length * availableIngredientsToModel.values().length
                                    * nrDirections * Kitchen.nX * Kitchen.nY;
        state += currentOrderItem * Order.OrderState.values().length * Ingredient.ItemState.values().length
                * availableIngredientsToModel.values().length * nrDirections * Kitchen.nX * Kitchen.nY;
        return state;
    }


    @Override
    public int reward(int state, Actuators.Action action) {
        /*
        * Reward after choosing an action and executed it.
        * Observe what happened to the world.
        * */
        switch(action) {
            case MOVE_AHEAD:
                if(Sensors.isIngredientDispenser(agent) && agent.item == null && agent.order != null)
                    return 100;
                if(Sensors.isOrderDispenser(agent) && agent.item == null && agent.order == null)
                    return 100;
                if((Sensors.isCooker(agent) || Sensors.isCutting(agent))
                        && agent.item != null && agent.item.currentState == Ingredient.ItemState.RAW)
                    return 100;
                //if(Sensors.isBin(agent) && agent.item != null && agent.item.currentState == Ingredient.ItemState.SPOILED)
                //    return 100;
                if(Sensors.isFreeCell(agent))
                    return 2;
            case STAY:
                if(Kitchen.getAgent(agent.ahead) != null)
                    return 20;
            case ROTATE_RIGHT:
            case ROTATE_LEFT:
                if(Sensors.isEmptyCounter(agent))
                    return 1;
                //return 1;
            case INTERACT_WITH_BLOCK:
                if(Sensors.isIngredientDispenser(agent))
                    //System.out.println("INTERACTED");
                //if(Sensors.isEmptyCounter(agent))
                //    return -100;
                if(Sensors.isIngredientDispenser(agent) && agent.lastItemHeld == agent.item)
                    return -100;
                if(Sensors.isCooker(agent) && agent.lastItemHeld != null && agent.lastItemHeld instanceof Meat)
                    return 100;
                if(Sensors.isCooker(agent) && agent.item != null && agent.item.currentState == Ingredient.ItemState.PROCESSED)
                    return 100;
                if(Sensors.isCooker(agent) && agent.item != null && agent.item.currentState == Ingredient.ItemState.SPOILED)
                    return -100;
                if(Sensors.isCooker(agent) && agent.item == null && agent.lastItemHeld == null)
                    return -100;
            default :
                return 0;
        }
    }

    @Override
    public void learn(int originalState, Actuators.Action originalAction) {
        it++;
        System.out.println(it);
        double u = reward(originalState, originalAction);
        double prevq = getQ(originalState,originalAction);
        double predError = 0;

        discount = Math.max(discount - dec,0.05);

        //agent= aheadPosition(); //percept
        predError = u + discount*getMaxQ(currentState()) - prevq;

        setQ(originalState, originalAction, prevq+(learningRate * predError));
    }

    private double getQ(int state, Actuators.Action action) {
        return model[state][actions.indexOf(action)];
    }
    private double setQ(int state, Actuators.Action action, double newQ) {
        return model[state][actions.indexOf(action)] = newQ;
    }


    @Override
    public Actuators.Action suggestAction(Object... params) {
        discount -= dec;
        if(random.nextDouble() < explorationRate) return randomAction();
        return eGreedySelection();
    }

    private double getMaxQ(int state) {
        double max = Double.NEGATIVE_INFINITY;
        for (double v : model[state]) max = Math.max(v, max);
        return max;
    }


    /* Select a random action */
    private Actuators.Action randomAction() {
        ArrayList<Actuators.Action> possibleActions = Sensors.availableActions(agent);
        return possibleActions.get(random.nextInt(possibleActions.size()));
    }

    /* SoftMax action selection */
    private Actuators.Action softMax() {
        /*List<Integer> validActions = availableActions(); //index of available actions
        double[] cumulative = new double[validActions.size()];
        cumulative[0]=Math.exp(getQ(getState(point,direction,cargo),actions.get(0))/(epsilon*100.0));
        for(int i=1; i<validActions.size(); i++)
            cumulative[i]=Math.exp(getQ(getState(point,direction,cargo),actions.get(i))/(epsilon*100.0))+cumulative[i-1];
        double total = cumulative[validActions.size()-1];
        double cut = random.nextDouble()*total;
        for(int i=0; i<validActions.size(); i++)
            if(cut<=cumulative[i]) return actions.get(validActions.get(i));*/
        return null;
    }

    /* eGreedy action selection */
    private Actuators.Action eGreedySelection() {
        ArrayList<Actuators.Action> validActions = Sensors.availableActions(agent); //index of available actions
        if(random.nextDouble() > discount)
            return validActions.get(random.nextInt(validActions.size()));
        else return getMaxActionQ(currentState(), validActions);
    }

    /* Gets the index of maximum Q-value action for a states */
    private Actuators.Action getMaxActionQ(int state, ArrayList<Actuators.Action> validActions) {
        double max = Double.NEGATIVE_INFINITY;
        int maxIndex = -1;
        for(int i=0; i < validActions.size(); i++) {
            double v = model[state][actions.indexOf(validActions.get(i))];
            if(v > max) {
                max = v;
                maxIndex = i;
            }
        }
        return validActions.get(maxIndex);
    }
    /* Learns policy up to a certain step and then uses policy to behave */
    /*public void learningDecision(int originalState, Agent.Action originalAction) {
        it++;
        double u = reward(originalState,originalAction);
        double prevq = getQ(originalState,originalAction);
        double predError = 0;

        epsilon = Math.max(epsilon-dec,0.05);
        ahead = aheadPosition(); //percept

        switch(learningApproach) {
            case SARSA :
                Agent.Action newAction = selectAction();
                predError = u + discount*getQ(getState(point,direction,cargo), newAction) - prevq; break;
            case QLearning : predError = u + discount*getMaxQ(getState(point,direction,cargo)) - prevq; break;
        }
        setQ(originalState, originalAction, prevq+(learningRate * predError));
        if(it%1000==0) System.out.println("e="+epsilon+"\n"+qToString());
    }

    public void executeQ() {
        if(random.nextDouble()<randfactor) execute(randomAction());
        else execute(getMaxActionQ(getState(point,direction,cargo),availableActions()));
    }*/



}
