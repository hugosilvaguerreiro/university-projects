package aasma_proj.items;

import aasma_proj.blocks.counters.Assembly;
import aasma_proj.items.ingredients.*;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

public class Order{

    public LinkedList<Ingredient> ingredients;
    public LinkedList<Ingredient> ingredientsCopy;
    public Assembly assembly = null;

    public Order(LinkedList<Ingredient> ingredients){
        this.ingredients = ingredients;
        this.ingredientsCopy = (LinkedList)ingredients.clone();
        this.currentState = OrderState.UNFINISHED;
    }

    public enum OrderState {UNFINISHED, FINISHED, DELIVERED}

    public OrderState currentState;

    public static Order generateOrder(){
        LinkedList<Ingredient> ingredients = new LinkedList<>();
        ingredients.add(new Bun());
        ingredients.add(new Meat());
        ingredients.add(new Tomato());
        ingredients.add(new Dish());
        return new Order(ingredients);
    }

    public Ingredient getIngred(){
        if (ingredients.size() == 0)
            return null;
        return ingredients.removeFirst();
    }

    public void setAssembly(Assembly assembly){
        this.assembly = assembly;
    }
}
