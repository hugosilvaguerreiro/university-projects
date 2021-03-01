package aasma_proj.world;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import aasma_proj.GUI.GUI;
import aasma_proj.GUI.Resources;
import aasma_proj.agent.*;
import aasma_proj.agent.modules.Message;
import aasma_proj.agent.modules.worldInterface.Sensors;
import aasma_proj.blocks.*;
import aasma_proj.blocks.counters.*;
import aasma_proj.items.Item;
import aasma_proj.items.ingredients.Bun;
import aasma_proj.items.ingredients.Meat;
import aasma_proj.items.ingredients.Tomato;

import javax.imageio.ImageIO;

public class Kitchen {
    public static boolean heatmapActivated = false;
    public static int nX = 10, nY = 10;
    public static Color[] colors; //blocks Color
    private static Block[][] board;
    public static int[][] heatmap;
    public static List<Agent> robots;
    private static List<Item> items;
    private static ArrayList<Item>[][] itemsPos;
    private static List<Block> blocks;
    private static Agent[][] agents;
    private static aasma_proj.GUI.GUI GUI;
    public static int simulationSpeed = 10;

    public static enum KitchenSide {LEFT, RIGHT, UP, DOWN};
    public static enum Layout {LEVEL1,LEVEL2, LEVEL3, RANDOM};

    /****************************
     ***** A: SETTING BOARD *****
     ****************************/
    public static int dishDeliverX = 0; public static int dishDeliverY = 7;
    public static int dishAssemblyX = 0; public static int dishAssemblyY = 4;
    public static int cookerX = 2; public static int cookerY = 9;
    public static int binX = 7; public static int binY = 0;
    public static int ingredientsX = 9; public static int ingredientsY = 4;

    public static int cuttingX = 5; public static int cuttingY = 9;
    public static int orderDispX = 0; public static int orderDispY = 5;
    public static int stepCount = 0;

    public static boolean baseline = false;
    public static int nrOfCooks = 4;
    public static int nrOfStoves = 1;
    public static int nrOfAssembly = 1;
    public static int nrOfCutting = 1;
    public static Layout kitchenLayout = Layout.RANDOM;

    public static void insertCooks() {
        agents = new Agent[nX][nY];
        Random r = new Random();
        /** D: create agents */
        robots = new ArrayList<Agent>();
        for(int i = 0; i< nrOfCooks; i++) {
            int x;
            int y;
            while (true) {
                x = r.nextInt(nX);
                y = r.nextInt(nY);
                if(Kitchen.getBlock(new Point(x,y)) instanceof EmptyBlock && agents[x][y] == null) {
                    break;
                }
            }
            Agent agent = null;
            if(baseline) {
                agent =new BaselineAgent(new Point(x,y),Agent.Type.getRandomType());
            }else {
                agent =new CommunicatingAgent(new Point(x,y),Agent.Type.getRandomType());
            }

            robots.add(agent);
            agents[agent.point.x][agent.point.y]=agent;
        }
        //for(Agent agent : robots) agents[agent.point.x][agent.point.y]=agent;
    }

    private static void insertStoves() {
        int x;
        int y;
        Random r = new Random();
        for(int i =0; i< nrOfStoves; i++) {
            while(true) {
                x = r.nextInt(nX);
                y = r.nextInt(nY);
                if(Kitchen.getBlock(new Point(x,y)) instanceof EmptyCounter) {
                    if((x == 0 && y == 0) || (x == 0 && y == nY-1)
                            || (x == nX-1 && y == 0) || (x == nX-1 && y == nY-1)) {
                        continue;
                    }
                    else {
                        break;
                    }
                }
            }
            board[x][y] = new Cooker();
        }
    }

    private static void insertCutting() {
        int x;
        int y;
        Random r = new Random();
        for(int i =0; i< nrOfCutting; i++) {
            while(true) {
                x = r.nextInt(nX);
                y = r.nextInt(nY);
                if(Kitchen.getBlock(new Point(x,y)) instanceof EmptyCounter) {
                    if((x == 0 && y == 0) || (x == 0 && y == nY-1)
                            || (x == nX-1 && y == 0) || (x == nX-1 && y == nY-1)) {
                        continue;
                    }
                    else {
                        break;
                    }
                }
            }
            board[x][y] = new CuttingBlock();
        }
    }

    private static void insertAssembly() {
        int x;
        int y;
        Random r = new Random();
        for(int i =0; i< nrOfAssembly; i++) {
            while(true) {
                x = r.nextInt(nX);
                y = r.nextInt(nY);
                if(Kitchen.getBlock(new Point(x,y)) instanceof EmptyCounter) {
                    if((x == 0 && y == 0) || (x == 0 && y == nY-1)
                            || (x == nX-1 && y == 0) || (x == nX-1 && y == nY-1)) {
                        continue;
                    }
                    else {
                        break;
                    }
                }
            }
            board[x][y] = new Assembly();
        }
    }

    public static void initialize() {
        /** A: create default board */
        board = new Block[nX][nY];
        heatmap = new int[nX][nY];
        for (int i = 0; i < nX; i++) {
            for (int j = 0; j < nY; j++) {
                try {
                    board[i][j] = new EmptyBlock(ImageIO.read(Resources.floor));
                    //board[i][j] = new EmptyBlock(Color.white);
                } catch (IOException e) {
                    board[i][j] = new EmptyBlock(Color.lightGray);
                }

            }
        }

        /** B: create level */
        List<Integer> notIngsY = new ArrayList<>();
        notIngsY.addAll(Arrays.asList(0, 1, 2, 3, 7, 8, 9));
        items = new ArrayList<>();

        for (int i = 0; i < nX; i++){
            for (int j = 0; j < nY; j++){
                if (i == dishDeliverX && j == dishDeliverY){
                    board[i][j] = new Deliver();
                }
                else if (i == dishAssemblyX && j == dishAssemblyY&& kitchenLayout != Layout.RANDOM){
                    board[i][j] = new Assembly();

                }
                else if (i == cookerX && j == cookerY && kitchenLayout != Layout.RANDOM){
                    board[i][j] = new Cooker();
                }
                else if (i == binX && j == binY){
                    board[i][j] = new Bin();
                }
                else if (i == ingredientsX && j == ingredientsY){
                        board[i][j] = new Dispenser(Tomato.class);
                        board[i][j+1] = new Dispenser(Bun.class);
                        board[i][j+2] = new Dispenser(Meat.class);
                        items.add(new Tomato(new Point(i,j)));
                        items.add(new Bun(new Point(i,j+1)));
                        items.add(new Meat(new Point(i, j+2)));
                }
                else if (i == cuttingX && j == cuttingY && kitchenLayout != Layout.RANDOM){
                    board[i][j] = new CuttingBlock();
                }
                else if (i == orderDispX && j == orderDispY){
                    board[i][j] = new OrderDispenser();
                }
                else{
                    if (i == 0 || (i == 9 && notIngsY.contains(j))) {
                        board[i][j] = new EmptyCounter();
                        board[i][j] = new EmptyCounter();
                    }
                    else if (j == 0 || j == 9) {
                        board[i][j]  = new EmptyCounter();
                    }
                    //else if ((i == 4 && j == 4) || (i == 4 && j == 5) ||
                    //        (i == 5 && j == 4) || (i == 5 && j == 5)){ /*||
                    //        (i == 5 && j == 6) || (i == 5 && j == 7) ||
                    //        (i == 5 && j == 2) || (i == 5 && j == 3)||
                     //       (i == 6 && j == 8)){*/
                   //     board[i][j] = new EmptyCounter();
                    // }
                }
            }
        }
        board[9][3] = new Assembly();
        if(kitchenLayout == Layout.LEVEL2){
            board[2][0] = new Assembly();
        }else if (kitchenLayout == Layout.LEVEL3){
            board[2][0] = new Assembly();
            board[9][7] = new Assembly();
            board[5][0] = new Assembly();
            board[7][9] = new Assembly();
        }

        /** C: add items */
        itemsPos = new ArrayList[nX][nY];
        for (int x = 0; x < nX; x++){
            for (int y = 0; y < nY; y++){
                itemsPos[x][y] = new ArrayList<>();
            }
        }

        for (Item i : items){
            itemsPos[i.point.x][i.point.y].add(i);
        }

        if(kitchenLayout == Layout.RANDOM) {
            insertStoves();
            insertAssembly();
            insertCutting();
            insertCooks();
        }else if(kitchenLayout == Layout.LEVEL1) {
            /** D: create agents */
            int nrobots = 3;
            robots = new ArrayList<Agent>();
            //for(int j=ingredientsY; j<ingredientsY + nrobots; j++)
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY),Agent.Type.THIN));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY-1),Agent.Type.FAT));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY+1),Agent.Type.BIG_SMOKE));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY+2),Agent.Type.KAREN));
            agents = new Agent[nX][nY];
            for(Agent agent : robots) agents[agent.point.x][agent.point.y]=agent;
        }else if(kitchenLayout == Layout.LEVEL2) {
            /** D: create agents */
            int nrobots = 5;
            robots = new ArrayList<Agent>();
            //for(int j=ingredientsY; j<ingredientsY + nrobots; j++)
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY),Agent.Type.THIN));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY-1),Agent.Type.FAT));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY+1),Agent.Type.BIG_SMOKE));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY+2),Agent.Type.KAREN));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY+3),Agent.Type.THIN));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY+4),Agent.Type.KAREN));
            agents = new Agent[nX][nY];
            for(Agent agent : robots) agents[agent.point.x][agent.point.y]=agent;
        } else if(kitchenLayout == Layout.LEVEL3) {
            /** D: create agents */
            int nrobots = 9;
            robots = new ArrayList<Agent>();
            //for(int j=ingredientsY; j<ingredientsY + nrobots; j++)
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY),Agent.Type.THIN));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY-1),Agent.Type.FAT));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY+1),Agent.Type.BIG_SMOKE));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY+2),Agent.Type.KAREN));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY+3),Agent.Type.THIN));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY+4),Agent.Type.KAREN));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY-2),Agent.Type.BIG_SMOKE));
            robots.add(new CommunicatingAgent(new Point(ingredientsX -1,ingredientsY-3),Agent.Type.KAREN));
            agents = new Agent[nX][nY];
            for(Agent agent : robots) agents[agent.point.x][agent.point.y]=agent;
        }
    }


    /****************************
     ***** B: BOARD METHODS *****
     ****************************/

    public static Agent getAgent(Point point) {
        return agents[point.x][point.y];
    }
    public static Block getBlock(Point point) {
        return board[point.x][point.y];
    }
    public static Item getItem(Point point){
        int lastIndex = itemsPos[point.x][point.y].size() -1;
        if (itemsPos[point.x][point.y].size() == 0) return null;
        return itemsPos[point.x][point.y].get(lastIndex); //TODO
    }
    public static ArrayList<Item> getAllItems(Point point){
        return itemsPos[point.x][point.y];
    }
    public static void updateAgentPosition(Point point, Point newpoint) {
        agents[newpoint.x][newpoint.y] = agents[point.x][point.y];
        agents[point.x][point.y] = null;
    }
    public static void removeItem(Point point) {
        int lastIndex = itemsPos[point.x][point.y].size() -1;
        items.remove(itemsPos[point.x][point.y].get(lastIndex)); //TODO
        itemsPos[point.x][point.y].remove(lastIndex);
    }
    public static void insertItem(Item item, Point point) {
        itemsPos[point.x][point.y].add(item);
        items.add(item);
    }

    /***********************************
     ***** C: ELICIT AGENT ACTIONS *****
     ***********************************/

    private static RunThread runThread;

    public static class RunThread extends Thread {

        int time;

        public RunThread(int time){
            this.time = time*time;
        }

        public void run() {
            while(true){
                step();
                try {
                    sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void run(int time) {
        Kitchen.runThread = new RunThread(time);
        Kitchen.runThread.start();
    }


    public static void reset() {
        Kitchen.stepCount = 0;
        removeObjects();
        initialize();
        GUI.displayBoard();
        displayObjects();
        GUI.update();
    }

    public static void sendMessage(Message message, Agent agent) {
        for(Agent a : robots){
            if(a.equals(agent))
                continue;
            ((CommunicatingAgent)a).communicationModule.receiveMessage(message);
        }
    }

    /*public static void sendMessage(Agent.Action action, Point pt) {
        for(Agent a : robots) a.receiveMessage(action, pt);
    }*/

    public static void step() {
        stepCount++;
        //System.out.println("----------------------");
        if(stepCount%1000 == 0){
            System.out.println("-------------------------");
            System.out.println("ITERATION\t" + stepCount);
            System.out.println("ORDERS:\t\t" + Kitchen.getAllItems(new Point(Kitchen.dishDeliverX, Kitchen.dishDeliverY)).size());
            System.out.println("-------------------------");
        }
        removeObjects();
        for(Agent a : robots) {
            a.agentDecision();
            heatmap[a.point.x][a.point.y] ++;
        }
        displayObjects();
        GUI.update();
    }

    public static void stop() {
        runThread.interrupt();
        runThread.stop();
    }

    public static void displayObjects(){
        for(Agent agent : robots) GUI.displayObject(agent);
        for(Item item : items) GUI.displayObject(item);
    }

    public static void removeObjects(){
        for(Agent agent : robots) GUI.removeObject(agent);
        for(Item item : items) GUI.removeObject(item);
    }

    public static void associateGUI(GUI graphicalInterface) {
        GUI = graphicalInterface;
    }
}

