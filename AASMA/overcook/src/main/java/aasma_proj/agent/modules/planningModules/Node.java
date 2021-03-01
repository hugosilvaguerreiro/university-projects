package aasma_proj.agent.modules.planningModules;

import java.awt.*;
import java.util.ArrayList;

public class Node
{
    Node parent;
    Point data;
    boolean visited = false;
    ArrayList<Node> neighbours;

    Node(Point data)
    {
        this.data=data;
        this.neighbours=new ArrayList<>();

    }
    public void addneighbours(Node neighbourNode)
    {
        this.neighbours.add(neighbourNode);
    }
    public ArrayList<Node> getNeighbours() {
        return neighbours;
    }
}