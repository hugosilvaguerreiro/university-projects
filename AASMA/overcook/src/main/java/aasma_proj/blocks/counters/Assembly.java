package aasma_proj.blocks.counters;

import aasma_proj.GUI.Resources;
import aasma_proj.agent.Agent;
import aasma_proj.agent.BaselineAgent;
import aasma_proj.agent.CommunicatingAgent;
import aasma_proj.items.Item;
import aasma_proj.items.Order;
import aasma_proj.items.ingredients.Dish;
import aasma_proj.items.ingredients.Ingredient;
import aasma_proj.world.Kitchen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Assembly extends Counter {

    public HashMap<Agent, Order> order = new HashMap<>();

    public Assembly(Color color){
        super(color);
    }
    public Assembly(Image image){
        super(image);
    }

    public Assembly() {
        this(Kitchen.KitchenSide.RIGHT);
    }

    public Assembly(Kitchen.KitchenSide side) {
        super();
        try {
            switch (side) {
                case UP:
                    break;
                case DOWN:
                    break;
                case LEFT:
                    break;
                case RIGHT:
                    break;
            }
            this.image = ImageIO.read(Resources.assembly_table);
        } catch (IOException e) {
            this.color = Resources.assembly_table_color;
        }
    }

    @Override
    public Class getType(){
        return this.getClass();
    }

    public boolean shouldBeUsed(Agent agent){
        if(agent instanceof BaselineAgent){
            if(order.containsKey(agent) || order.isEmpty()) return true;
            else return false;
        }
        else if (agent instanceof CommunicatingAgent){
            if(order.containsKey(((CommunicatingAgent) agent).currentCoordinator)
                || order.containsKey(agent)
                || order.isEmpty()) return true;
            else return false;
        }
        return true;
    }

    public boolean hasOrder(Agent agent){
        if(agent instanceof BaselineAgent){
            if(order.containsKey(agent)) return true;
            else return false;
        }
        else if (agent instanceof CommunicatingAgent){
            if(order.containsKey(((CommunicatingAgent) agent).currentCoordinator))
                return true;
            else return false;
        }
        return false;
    }

    public boolean canInteract(Agent agent){
        if (agent.item != null){
            if(agent instanceof CommunicatingAgent){
                if (!order.containsKey(((CommunicatingAgent) agent).currentCoordinator)){
                    if (!order.isEmpty())
                        return false;
                }
                if(((CommunicatingAgent) agent).currentCoordinator.order.assembly != null
                        && !((CommunicatingAgent) agent).currentCoordinator.order.assembly.equals(this)){
                    ((CommunicatingAgent) agent).pathPlanningModule.findCurrentTarget();
                    return false;
                }
            }
            else if (!order.containsKey(agent)){
                if (!order.isEmpty())
                    return false;
            }
            return agent.item.currentState == Ingredient.ItemState.PROCESSED;
        }
        else{
            if(agent instanceof BaselineAgent){
                return true;
            }else if(agent instanceof CommunicatingAgent) {
                if (((CommunicatingAgent) agent).currentCoordinator != null
                        && agent.desiredIngred instanceof Dish
                        && order.containsKey(((CommunicatingAgent) agent).currentCoordinator)
                        && !order.get(((CommunicatingAgent) agent).currentCoordinator).currentState.equals(Order.OrderState.FINISHED)) {
                    ArrayList<Item> ingredients = Kitchen.getAllItems(agent.ahead);
                    if(((CommunicatingAgent) agent).currentCoordinator.order.assembly != null
                            && !((CommunicatingAgent) agent).currentCoordinator.order.assembly.equals(this)) {
                        ((CommunicatingAgent) agent).pathPlanningModule.findCurrentTarget();
                        return false;
                    }
                    boolean finished = true;
                    ArrayList<Class> addedIngs = new ArrayList<>();
                    ArrayList<Class> ingsClasses = new ArrayList<>();
                    for (Item item : ingredients) {
                        addedIngs.add(item.getClass());
                    }
                    for (int ing = 0; ing < order.get(((CommunicatingAgent) agent).currentCoordinator).ingredientsCopy.size(); ing++) {
                        ingsClasses.add(order.get(((CommunicatingAgent) agent).currentCoordinator).ingredientsCopy.get(ing).getClass());
                    }
                    ingsClasses.remove(Dish.class);
                    for (Class c : addedIngs) {
                        //System.out.println(c);
                        if (!ingsClasses.contains(c)) {
                            //System.out.println(c);
                            finished = false;
                        }
                    }
                    if (ingsClasses.size() != addedIngs.size())
                        finished = false;
                    if (finished) {
                        for (Item i : (ArrayList<Item>) ingredients.clone()) {
                            Kitchen.removeItem(agent.ahead);
                        }
                        Dish finalDish = new Dish(agent.ahead);
                        Kitchen.insertItem(finalDish, agent.ahead);
                        order.get(((CommunicatingAgent) agent).currentCoordinator).currentState = Order.OrderState.FINISHED;
                        //order.clear();
                    }
                    return finished;
                } else if (((CommunicatingAgent) agent).currentCoordinator == null)
                    return false;
                else if (agent.desiredIngred instanceof Dish
                        && order.containsKey(((CommunicatingAgent) agent).currentCoordinator)
                        && order.get(((CommunicatingAgent) agent).currentCoordinator).currentState.equals(Order.OrderState.FINISHED))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void startInteraction(Agent agent) {
        if(agent instanceof BaselineAgent){
            if (order.isEmpty()){
                order.put(agent, agent.order);
            }
            if (agent.item == null){
                ArrayList<Item> ingredients = Kitchen.getAllItems(agent.ahead);
                boolean finished = true;
                ArrayList<Class> addedIngs = new ArrayList<>();
                ArrayList<Class> ingsClasses = new ArrayList<>();
                for (Item item : ingredients){
                    addedIngs.add(item.getClass());
                }
                for (int ing = 0; ing < order.get(agent).ingredientsCopy.size(); ing++){
                    ingsClasses.add(order.get(agent).ingredientsCopy.get(ing).getClass());
                }
                ingsClasses.remove(Dish.class);
                for (Class c : addedIngs){
                    //System.out.println(c);
                    if(!ingsClasses.contains(c)){
                        //System.out.println(c);
                        finished = false;
                    }
                }
                if (finished){
                    for (Item i : (ArrayList<Item>)ingredients.clone()){
                        Kitchen.removeItem(agent.ahead);
                    }
                    Dish finalDish = new Dish(agent.ahead);
                    Kitchen.insertItem(finalDish,agent.ahead);
                    order.get(agent).currentState = Order.OrderState.FINISHED;
                    order.clear();
                }
                return;
            }
        }
        if(agent instanceof CommunicatingAgent) {
            if (order.isEmpty()) {
                order.put(((CommunicatingAgent) agent).currentCoordinator, ((CommunicatingAgent) agent).currentCoordinator.order);
                ((CommunicatingAgent) agent).currentCoordinator.order.setAssembly(this);
            }
            else if(order.get(((CommunicatingAgent) agent).currentCoordinator).ingredients.size() == 1 && agent.item != null){
                ArrayList<Item> ingredients = Kitchen.getAllItems(agent.ahead);
                boolean finished = true;
                ArrayList<Class> addedIngs = new ArrayList<>();
                ArrayList<Class> ingsClasses = new ArrayList<>();
                for (Item item : ingredients){
                    addedIngs.add(item.getClass());
                }
                addedIngs.add(agent.item.getClass());
                for (int ing = 0; ing < order.get(((CommunicatingAgent) agent).currentCoordinator).ingredientsCopy.size(); ing++){
                    ingsClasses.add(order.get(((CommunicatingAgent) agent).currentCoordinator).ingredientsCopy.get(ing).getClass());
                }
                ingsClasses.remove(Dish.class);
                for (Class c : ingsClasses){
                    //System.out.println(c);
                    if(!addedIngs.contains(c)){
                        //System.out.println(c);
                        finished = false;
                    }
                }
                if (finished){
                    agent.desiredIngred = order.get(((CommunicatingAgent) agent).currentCoordinator).getIngred();
                }
            }
            else if(order.get(((CommunicatingAgent) agent).currentCoordinator).currentState.equals(Order.OrderState.FINISHED)){
                order.clear();
            }
        }
    }

}
