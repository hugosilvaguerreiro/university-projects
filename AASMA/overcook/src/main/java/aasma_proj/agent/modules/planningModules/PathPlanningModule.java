package aasma_proj.agent.modules.planningModules;

import aasma_proj.agent.Agent;
import aasma_proj.agent.CommunicatingAgent;
import aasma_proj.agent.modules.Message;
import aasma_proj.agent.modules.worldInterface.Actuators;
import aasma_proj.agent.modules.worldInterface.Sensors;
import aasma_proj.blocks.Block;
import aasma_proj.blocks.EmptyBlock;
import aasma_proj.blocks.counters.*;
import aasma_proj.world.Kitchen;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public class PathPlanningModule extends LearningModule{
    public Agent agent;
    private Point currentTargetLocation;
    private Point currentWaypoint;
    private AvailableBlock currentTarget;
    private boolean currentTargetComplete;
    private Stack<Point> waypoints;
    private boolean stopSearch = false;
    public int stay = 0;
    public CommunicatingAgent blockedby;
    // X - false, Y - true. If an agent is blocked, it'll try to get around the block by going through a different axis.
    private boolean prioritizeY = false;

    public enum AvailableBlock {
        ASSEMBLY(Assembly.class), BIN(Bin.class), COOKER(Cooker.class),
        DELIVER(Deliver.class), CUTTING(CuttingBlock.class), DISPENSER(Dispenser.class),
        ORDER_DISPENSER(OrderDispenser.class), EMPTY_COUNTER(EmptyCounter.class),
        EMPTY_BLOCK(EmptyBlock.class);
        public Class className;
        AvailableBlock(Class className) { this.className = className;}
    }

    public PathPlanningModule(Agent agent) {
        this.agent = agent;
        currentTarget = null;
        currentTargetLocation = null;
        waypoints = new Stack<>();
    }

    public Point checkAdjPos(){
        for(int i = -1; i <= 1; i += 2){
            Point adj1 = new Point(agent.point.x + i,agent.point.y);
            Point adj2 = new Point(agent.point.x,agent.point.y + i);
            if(Kitchen.getBlock(adj1) instanceof EmptyBlock && Kitchen.getAgent(adj1)==null
                && (adj1.x) > 0 && (adj1.x) < Kitchen.nX
                && (adj1.y) > 0 && (adj1.y) < Kitchen.nY)
                return adj1;
            if(Kitchen.getBlock(adj2) instanceof EmptyBlock && Kitchen.getAgent(adj2)==null
                    && (adj2.x) > 0 && (adj2.x) < Kitchen.nX
                    && (adj2.y) > 0 && (adj2.y) < Kitchen.nY)
                return adj2;
        }
        return null;
    }

    public void moveAway(){
        if(stay == 0){
            stay = 5;
            Point adj = checkAdjPos();
            if(adj != null){
                currentWaypoint = adj;
            }
            //blockedby = null;
        }
    }

    public void moveBack(){
        stay = 0;
    }

    public void switchAxisPriority(){
        prioritizeY = ((agent.direction == 0 || agent.direction == 180) == !Sensors.isFreeCell(agent));
        //System.out.println(prioritizeY ? "y" : "x");
    }
    public void switchAxisPriorityInv(){
        prioritizeY = !((agent.direction == 0 || agent.direction == 180) == !Sensors.isFreeCell(agent));
        //System.out.println(prioritizeY ? "y" : "x");
    }

    public void setCurrentObjective(AvailableBlock target) {
        // Only need to change stuff if you're... actually changing it.
        // Avoids needless path recomputes.
        if(!target.equals(this.currentTarget)){
            this.currentTarget = target;
            this.currentTargetLocation = null;
            this.currentTargetComplete = false;
        }
    }
    public AvailableBlock getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTargetComplete(boolean currentTargetComplete) {
        this.currentTargetComplete = currentTargetComplete;
    }

    public boolean getCurrentTargetComplete() {
        return this.currentTargetComplete;
    }

    private void getNextWaypoint(){
        if(waypoints.size() != 0)
            currentWaypoint = waypoints.pop();
        else
            currentWaypoint = currentTargetLocation;
    }

    private void backtrackNodeTrajectory(Node node){
        while(node != null){
            waypoints.push(node.data);
            node = node.parent;
        }
    }


    private Node getBoardGraph(Point objective){
        Node start = new Node(agent.point);
        ArrayList<ArrayList<Node>> board = new ArrayList<>();
        for(int i = 0; i < Kitchen.nX; i++){
            board.add(new ArrayList<>(Kitchen.nY));
            for(int j = 0; j < Kitchen.nY; j++)
                board.get(i).add(null);
        }

        for(int i = 0; i < Kitchen.nX; i++){
            for(int j = 0; j < Kitchen.nY; j++){
                Point point = new Point(i,j);
                if(point.equals(agent.point)){
                    board.get(i).set(j, start);
                }
                else if((Kitchen.getBlock(point) instanceof EmptyBlock && Kitchen.getAgent(point)==null)
                        || point.equals(objective)){
                    board.get(i).set(j, new Node(point));
                }
            }
        }

        for(int i = 0; i < Kitchen.nX; i++){
            for(int j = 0; j < Kitchen.nY; j++) {
                if(board.get(i).get(j) == null) continue;
                if(i-1 >= 0 && board.get(i-1).get(j) != null)
                    board.get(i).get(j).addneighbours(board.get(i-1).get(j));
                if(i+1 < Kitchen.nX && board.get(i+1).get(j) != null)
                    board.get(i).get(j).addneighbours(board.get(i+1).get(j));
                if(j-1 >= 0 && board.get(i).get(j-1) != null)
                    board.get(i).get(j).addneighbours(board.get(i).get(j-1));
                if(j+1 < Kitchen.nY && board.get(i).get(j+1) != null)
                    board.get(i).get(j).addneighbours(board.get(i).get(j+1));
            }
        }

        return start;
    }


    private void bfs(Node node, Point objective)
    {
        LinkedList<Node> queue = new LinkedList<>();
        queue.add(node);
        node.visited=true;
        while (!queue.isEmpty())
        {

            Node element=queue.remove();
            if(element.data.equals(objective)){
                backtrackNodeTrajectory(element);
                return;
            }
            ArrayList<Node> neighbours=element.getNeighbours();
            for (int i = 0; i < neighbours.size(); i++) {
                Node n=neighbours.get(i);
                if(!n.visited)
                {
                    queue.add(n);
                    n.visited=true;
                    n.parent = element;

                }
            }

        }
    }

    private boolean pointBlocked(Point point){
        if(point.x-1 >= 0) {
            Point adj = new Point(point.x - 1, point.y);
            if (Kitchen.getBlock(adj) instanceof EmptyBlock
                    && (Kitchen.getAgent(adj) == null || adj.equals(agent.point)))
                return false;
        }
        if(point.x+1 < Kitchen.nX) {
            Point adj = new Point(point.x + 1, point.y);
            if (Kitchen.getBlock(adj) instanceof EmptyBlock
                    && (Kitchen.getAgent(adj) == null || adj.equals(agent.point)))
                return false;
        }
        if(point.y-1 >= 0) {
            Point adj = new Point(point.x, point.y - 1);
            if (Kitchen.getBlock(adj) instanceof EmptyBlock
                    && (Kitchen.getAgent(adj) == null || adj.equals(agent.point)))
                return false;
        }
        if(point.y+1 < Kitchen.nY) {
            Point adj = new Point(point.x, point.y + 1);
            return (Kitchen.getBlock(adj) instanceof EmptyBlock
                    && (Kitchen.getAgent(adj) == null || adj.equals(agent.point)));
        }
        return true;
    }

    private void createWayPoints(Point objective){
        if(pointBlocked(objective)) return;
        bfs(getBoardGraph(objective), objective);
        //System.out.println(agent.name + "\t" + waypoints);
    }

    private Point findSmallestDistance(ArrayList<Point> points) {
        if(currentTarget.equals(AvailableBlock.ASSEMBLY)){
            for (Point p : points) {
                if(((Assembly)Kitchen.getBlock(p)).hasOrder(agent))
                    return p;
            }
        }
        Point minimumPoint =  null;
        int minimumDistance = Kitchen.nX + Kitchen.nY + 2;
        for (Point p : points) {
            int distance = Math.abs(agent.point.x - p.x) + Math.abs(agent.point.y - p.y);
            if( distance < minimumDistance) {
                minimumDistance = distance;
                minimumPoint = p;
            }
        }
        return minimumPoint;
    }

    public Point findCurrentTarget() {
        ArrayList<Point> foundTargets = new ArrayList<>();
        for(int i =0; i < Kitchen.nX; i++) {
            for(int j = 0; j < Kitchen.nY; j++) {
                Point p = new Point(i,j);
                Block block = Kitchen.getBlock(p);
                if(block.getClass().equals(currentTarget.className)) {
                    if(currentTarget.className == Dispenser.class) {
                        if(agent.desiredIngred.getClass() == ((Dispenser)block).ingredient)
                            foundTargets.add(p);
                    }
                    else if(currentTarget.className == Assembly.class){
                        if(((Assembly)block).shouldBeUsed(agent))
                            foundTargets.add(p);
                    }
                    else{
                        foundTargets.add(p);
                    }
                }
            }
        }
        waypoints = new Stack<>();
        if(findSmallestDistance(foundTargets) != null){
            createWayPoints(findSmallestDistance(foundTargets));
            getNextWaypoint();
        }

        return findSmallestDistance(foundTargets);
    }

    @Override
    public Actuators.Action suggestAction(Object ... params) {
        if(stay > 0){
            stay--;
            if(currentWaypoint == null)
                return Actuators.Action.STAY;
        }
        if(currentTargetLocation == null) {
            currentTargetLocation = findCurrentTarget();
        }
        if(currentTargetLocation == null) {
            currentTarget = AvailableBlock.BIN;
            currentTargetLocation = findCurrentTarget();
        }
        if(agent instanceof CommunicatingAgent && stay == 0
            && Kitchen.getAgent(agent.ahead) != null && agent.ahead.equals(currentWaypoint)){
            //&& blockedby == null){
            if(Kitchen.getAgent(agent.ahead).equals(blockedby)){
                // wait for the other agent to move
                return Actuators.Action.STAY;
            }
            ((CommunicatingAgent) agent).communicationModule.sendMessage(
                    new Message((CommunicatingAgent) agent,Message.Method.MOVE_AWAY,
                            ((CommunicatingAgent) agent).stepsSinceOrder, checkAdjPos() == null),
                    (CommunicatingAgent) Kitchen.getAgent(agent.ahead));
            if(!(agent.equals(((CommunicatingAgent)Kitchen.getAgent(agent.ahead)).pathPlanningModule.blockedby)))
                blockedby = (CommunicatingAgent) Kitchen.getAgent(agent.ahead);
            // stays in its spot, waiting to see if the other agent moves
            return Actuators.Action.STAY;
        }
        /*if(agent.ahead.equals(currentTargetLocation)){
            if(blockedby != null && agent instanceof CommunicatingAgent){
                ((CommunicatingAgent) agent).communicationModule.sendMessage(
                    new Message((CommunicatingAgent) agent, Message.Method.MOVE_BACK), blockedby);
                blockedby = null;
            }
            return Actuators.Action.MOVE_AHEAD;
        }*/
        if(agent.point.equals(currentWaypoint)){
            if(waypoints.isEmpty() && !agent.ahead.equals(currentTargetLocation)){
                findCurrentTarget();
                if(waypoints.isEmpty()) currentWaypoint = currentTargetLocation;
            }
            else getNextWaypoint();
        }
        int distanceX, distanceY;
        blockedby = null;
        if(currentWaypoint != null){
            distanceX = agent.point.x - currentWaypoint.x;
            distanceY = agent.point.y - currentWaypoint.y;
        }else{
            getNextWaypoint();
            if(currentWaypoint != null){
                distanceX = agent.point.x - currentWaypoint.x;
                distanceY = agent.point.y - currentWaypoint.y;
            }
            else {
                distanceX = agent.point.x - currentTargetLocation.x;
                distanceY = agent.point.y - currentTargetLocation.y;
            }
        }
        switch (agent.direction) {
            case 0: //looking up
                if(prioritizeY){
                    if(distanceY < 0 )
                        return Actuators.Action.MOVE_AHEAD;
                    else if(distanceY > 0)
                        return Actuators.Action.ROTATE_RIGHT;
                    else {
                        if(distanceX < 0)
                            return Actuators.Action.ROTATE_RIGHT;
                        else if(distanceX > 0)
                            return Actuators.Action.ROTATE_LEFT;
                        else
                            return Actuators.Action.STAY; //is at the exact spot
                    }
                }
                else{
                    if(distanceX < 0 )
                        return Actuators.Action.ROTATE_RIGHT;
                    else if(distanceX > 0)
                        return Actuators.Action.ROTATE_LEFT;
                    else {
                        if(distanceY < 0)
                            return Actuators.Action.MOVE_AHEAD;
                        else if(distanceY > 0)
                            return Actuators.Action.ROTATE_RIGHT;
                        else
                            return Actuators.Action.STAY; //is at the exact spot
                    }
                }
            case 90: //looking right
                if(prioritizeY){
                    if(distanceY < 0)
                        return Actuators.Action.ROTATE_LEFT;
                    else if(distanceY > 0)
                        return Actuators.Action.ROTATE_RIGHT;
                    else {
                        if(distanceX < 0)
                            return Actuators.Action.MOVE_AHEAD;
                        else if(distanceX > 0)
                            return Actuators.Action.ROTATE_LEFT;
                        else
                            return Actuators.Action.STAY; //is at the exact spot
                    }
                }
                else{
                    if(distanceX < 0)
                        return Actuators.Action.MOVE_AHEAD;
                    else if(distanceX > 0)
                        return Actuators.Action.ROTATE_LEFT;
                    else {
                        if(distanceY < 0)
                            return Actuators.Action.ROTATE_LEFT;
                        else if(distanceY > 0)
                            return Actuators.Action.ROTATE_RIGHT;
                        else
                            return Actuators.Action.STAY; //is at the exact spot
                    }
                }
            case 180://looking down
                if(prioritizeY){
                    if(distanceY < 0 )
                        return Actuators.Action.ROTATE_RIGHT;
                    else if(distanceY > 0)
                        return Actuators.Action.MOVE_AHEAD;
                    else {
                        if(distanceX < 0)
                            return Actuators.Action.ROTATE_LEFT;
                        else if(distanceX > 0)
                            return Actuators.Action.ROTATE_RIGHT;
                        else
                            return Actuators.Action.STAY; //is at the exact spot
                    }
                }
                else{
                    if(distanceX < 0 )
                        return Actuators.Action.ROTATE_LEFT;
                    else if(distanceX > 0)
                        return Actuators.Action.ROTATE_RIGHT;
                    else {
                        if(distanceY < 0)
                            return Actuators.Action.ROTATE_RIGHT;
                        else if(distanceY > 0)
                            return Actuators.Action.MOVE_AHEAD;
                        else
                            return Actuators.Action.STAY; //is at the exact spot
                    }
                }
            default://looking left
                if(prioritizeY){
                    if(distanceY < 0 )
                        return Actuators.Action.ROTATE_RIGHT;
                    else if(distanceY > 0)
                        return Actuators.Action.ROTATE_LEFT;
                    else {
                        if(distanceX < 0)
                            return Actuators.Action.ROTATE_RIGHT;
                        else if(distanceX > 0)
                            return Actuators.Action.MOVE_AHEAD;
                        else
                            return Actuators.Action.STAY; //is at the exact spot
                    }
                }
                else{
                    if(distanceX < 0 )
                        return Actuators.Action.ROTATE_RIGHT;
                    else if(distanceX > 0)
                        return Actuators.Action.MOVE_AHEAD;
                    else {
                        if(distanceY < 0)
                            return Actuators.Action.ROTATE_RIGHT;
                        else if(distanceY > 0)
                            return Actuators.Action.ROTATE_LEFT;
                        else
                            return Actuators.Action.STAY; //is at the exact spot
                    }
                }
        }
    }
}
