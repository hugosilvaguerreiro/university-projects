package aasma_proj.agent;

import aasma_proj.GUI.Resources;
import aasma_proj.items.Order;
import aasma_proj.items.ingredients.Ingredient;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

public abstract class Agent {

    public Image lookLeft, lookRight, lookFront, lookBack;
    public ImageIcon icon_no_job, icon_coordinator;
    public Point point;
    public Color color;
    public Order order;
    public Ingredient item;
    public Ingredient lastItemHeld;
    public Ingredient desiredIngred;
    public Point ahead;
    public String name;

    public enum State {COOP_IDLE, COOP_WORKING, COOP_COORDINATOR, SINGLE_IDLE, SINGLE_ORDER}
    public enum Type {
        FAT, THIN, KAREN, BIG_SMOKE;
        public static Type getRandomType() {
            Random random = new Random();
            return values()[random.nextInt(values().length)];
        }
    }
    public String[] Names = {
            "Adrian", "Aiden", "Alex", "Amari", "Bay",
            "Blaine", "Blake", "Chris", "Clay",
            "Corey", "Dana", "Dale", "Daryl",
            "Emery", "Finley", "Glenn", "Gray", "Hunter",
            "James", "Jamie", "Jayden", "Jean", "Jesse",
            "Kaden", "Kai", "Karter", "Logan", "London",
            "Lou", "Mason","Max", "Quinn", "Ray", "Reagan", "Reed",
            "Reese", "Remy", "Riley", "River", "Roan",
            "Rory", "Skylar", "Spencer", "West", "Winter"
    };
    public State state;
    protected Type type;

    public int direction = 90;

    public Agent(Point point, Color color){
        this.point = point;
        this.color = color;
    }

    public Agent(Point point) {
        this(point, Type.THIN, null);
    }

    public Agent(Point point, Type type, State state) {
        this.state = state;
        Random rand = new Random();
        this.name = Names[rand.nextInt(Names.length)];
        this.type = type;
        try {
            switch (type) {
                case THIN:
                    this.lookLeft = ImageIO.read(Resources.cook01_l);
                    this.lookRight = ImageIO.read(Resources.cook01_r);
                    this.lookFront = ImageIO.read(Resources.cook01_f);
                    this.lookBack = ImageIO.read(Resources.cook01_b);
                    this.icon_no_job = Resources.cook01_no_job;
                    this.icon_coordinator = Resources.cook01_coordinator;
                    break;
                case FAT:
                    this.lookLeft = ImageIO.read(Resources.cook02_l);
                    this.lookRight = ImageIO.read(Resources.cook02_r);
                    this.lookFront = ImageIO.read(Resources.cook02_f);
                    this.lookBack = ImageIO.read(Resources.cook02_b);
                    this.icon_no_job = Resources.cook02_no_job;
                    this.icon_coordinator = Resources.cook02_coordinator;
                    break;
                case BIG_SMOKE:
                    this.lookLeft = ImageIO.read(Resources.cook03_l);
                    this.lookRight = ImageIO.read(Resources.cook03_r);
                    this.lookFront = ImageIO.read(Resources.cook03_f);
                    this.lookBack = ImageIO.read(Resources.cook03_b);
                    this.icon_no_job = Resources.cook03_no_job;
                    this.icon_coordinator = Resources.cook03_coordinator;
                    break;
                case KAREN:
                    this.lookLeft = ImageIO.read(Resources.cook04_l);
                    this.lookRight = ImageIO.read(Resources.cook04_r);
                    this.lookFront = ImageIO.read(Resources.cook04_f);
                    this.lookBack = ImageIO.read(Resources.cook04_b);
                    this.icon_no_job = Resources.cook04_no_job;
                    this.icon_coordinator = Resources.cook04_coordinator;
                    break;
            }
            this.point = point;
        } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public Agent(Point point, State state) {
        this(point);
        this.state = state;
    }

    public abstract void agentDecision();

    public void changeState(State newState) {
        this.state = newState;
    }

    public void changeOrder(Order order){
        this.order = order;
    }

    /* Position ahead */
    public Point aheadPosition() {
        Point newpoint = new Point(point.x,point.y);
        switch(direction) {
            case 0: newpoint.y++; break;
            case 90: newpoint.x++; break;
            case 180: newpoint.y--; break;
            default: newpoint.x--;
        }
        return newpoint;
    }


}
