package aasma_proj.GUI;

import aasma_proj.agent.Agent;
import aasma_proj.agent.CommunicatingAgent;
import aasma_proj.blocks.Block;
import aasma_proj.blocks.counters.Deliver;
import aasma_proj.blocks.counters.EmptyCounter;
import aasma_proj.blocks.counters.OrderDispenser;
import aasma_proj.items.Item;
import aasma_proj.items.ingredients.Bun;
import aasma_proj.items.ingredients.Tomato;
import aasma_proj.world.Kitchen;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.*;


/**
 * Graphical interface
 * @author Rui Henriques
 */
public class GUI extends JFrame {

    private static final long serialVersionUID = 1L;

    static JTextField speed;
    static JTextPane stats;
    static JPanel boardPanel;
    static JSlider simulSpeed;
    static JButton run, reset, step;
    private int nX, nY;


    private final int DEFAULT_WINDOW_WIDTH=800;
    private final int DEFAULT_WINDOW_HEIGHT=800;
    private double width_adjustment = 1;
    private double height_adjustment = 1;
    private JLabel ordersStatus;
    private JSlider nrWorkers;
    private JSlider nrCookers;
    private JSlider nrAssembly;
    private JSlider nrCutting;
    private JComboBox chooseLayout;
    private JTree tree;
    public static boolean heatmapActivated=true;
    private DefaultMutableTreeNode root;
    private DefaultMutableTreeNode coordinatorsNode;
    private DefaultMutableTreeNode joblessNode;
    private Hashtable<String, DefaultMutableTreeNode> coordinatees = new Hashtable<>();
    private JSlider heatmapSlider;
    private JComboBox chooseBaseline;

    public class Cell extends JPanel {

        private static final long serialVersionUID = 1L;

        public List<Agent> agents = new ArrayList<Agent>();
        public List<Block> blocks = new ArrayList<>();
        public List<Item> items = new ArrayList<>();
        public Point point;
        public Cell(Point point) {
            super();
            this.point = point;
        }
        public float sigmoid(float x) {
            return (float)(1/( 1 + Math.pow(Math.E,(-1*x))));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for(Block b : blocks) {
                if( b.image != null) {
                    if(b instanceof OrderDispenser) {
                        g.drawImage(b.image, 0,0,(int)(50*width_adjustment),(int)(50*height_adjustment), this);
                        g.drawImage(((OrderDispenser)b).orders,0,0,(int)(45*width_adjustment),(int)(45*height_adjustment), this);
                    }else if(b instanceof EmptyCounter) {
                        super.paintComponent(g);
                    }else {
                        g.drawImage(b.image, 0,0,(int)(50*width_adjustment),(int)(50*height_adjustment), this);
                    }
                }
                else {
                    super.paintComponent(g);
                }
            }
            for(Agent agent : agents) {
                //g.setColor(agent.color);

                switch(agent.direction) {
                    case 0:  g.drawImage(agent.lookBack, 0, 0, (int)(50*width_adjustment), (int)(50*height_adjustment), this); break;
                    case 90: g.drawImage(agent.lookRight, 0, 0, (int)(50*width_adjustment), (int)(50*height_adjustment), this); break;
                    case 180:g.drawImage(agent.lookFront, 0, 0, (int)(50*width_adjustment), (int)(50*height_adjustment), this); break;
                    default: g.drawImage(agent.lookLeft, 0, 0, (int)(50*width_adjustment), (int)(50*height_adjustment), this);
                }
                //g.fillRect(0,40, 50,10);
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial Black", Font.BOLD, 15));
                g.drawString(agent.name, 0,40);
            }


            for (Item i : items){
                if(i instanceof Bun) {
                    g.drawImage(i.image, 15, 15, (int)(25*width_adjustment), (int)(25*height_adjustment), this);
                }else if(i instanceof Tomato) {
                    g.drawImage(i.image, 15, 15, (int)(17*width_adjustment), (int)(17*height_adjustment), this);
                }
                else {
                    g.drawImage(i.image, 15, 15, (int)(20*width_adjustment), (int)(20*height_adjustment), this);
                }
            }
            if(GUI.heatmapActivated) {

                Graphics2D g2d = (Graphics2D)(g);
                float value;
                if(Kitchen.stepCount == 0 || Kitchen.heatmap[point.x][point.y] == 0) {
                    value = 1;
                } else {
                    value = 1-(float) Math.min((float)Kitchen.heatmap[point.x][point.y] / Kitchen.stepCount*1.9, 1);
                }
                g2d.setColor(new Color(1,value, value,(float)heatmapSlider.getValue()/10 ));
                g2d.fill(new Rectangle2D.Double(0, 0, (int)(50*width_adjustment), (int)(50*height_adjustment)));
            }
        }

    }

    public GUI() {
        setTitle("Overcook");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setSize(DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        add(createButtonPanel());
        add(createStatsPanel());


        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                width_adjustment = ((double)GUI.this.getWidth())/DEFAULT_WINDOW_WIDTH;
                height_adjustment= ((double)GUI.this.getHeight())/DEFAULT_WINDOW_HEIGHT;
                boardPanel.setSize((int)(500*width_adjustment), (int)(500*height_adjustment));
            }
        });


        Kitchen.initialize();
        Kitchen.associateGUI(this);
        add(createTreePanel());

        boardPanel = new JPanel();
        boardPanel.setSize(new Dimension(500,500));
        boardPanel.setLocation(new Point(20,20));

        nX = Kitchen.nX;
        nY = Kitchen.nY;
        boardPanel.setLayout(new GridLayout(nX,nY));
        for(int i=0; i<nX; i++)
            for(int j=0; j<nY; j++)
                boardPanel.add(new Cell(new Point(j,nY-1-i)));

        displayBoard();
        Kitchen.displayObjects();
        update();

        add(boardPanel);
        add(createConfigPanel());

    }
    public void createSpeedSlider(JPanel panel) {
        simulSpeed = new JSlider(1, 20, 10);
        // Set the labels to be painted on the simulSpeed
        simulSpeed.setPaintLabels(true);

        // Add positions label in the simulSpeed
        Hashtable position = new Hashtable();
        position.put(1, new JLabel("slow"));
        //position.put(10, new JLabel("10"));
        position.put(20, new JLabel("fast"));
        JLabel status = new JLabel("Simulation speed", JLabel.CENTER);
        simulSpeed.setLabelTable(position);
        simulSpeed.setMajorTickSpacing(2);
        simulSpeed.setPaintTicks(true);
        simulSpeed.setSnapToTicks(true);
        panel.add(status);
        panel.add(simulSpeed);
    }

    public void createNrOfWorkersSlider(JPanel panel) {
        nrWorkers = new JSlider(1, 10, 4);
        // Set the labels to be painted on the simulSpeed
        nrWorkers.setPaintLabels(true);

        // Add positions label in the simulSpeed
        Hashtable position = new Hashtable();
        position.put(1, new JLabel("1"));
        position.put(5, new JLabel("5"));
        position.put(10, new JLabel("10"));
        JLabel status = new JLabel("Nr of cooks", JLabel.CENTER);
        nrWorkers.setLabelTable(position);
        nrWorkers.setMajorTickSpacing(1);
        nrWorkers.setPaintTicks(true);
        nrWorkers.setSnapToTicks(true);
        panel.add(status);
        panel.add(nrWorkers);
    }

    public void createNrOfAssembly(JPanel panel) {
        nrAssembly = new JSlider(1, 10, 1);
        // Set the labels to be painted on the simulSpeed
        nrAssembly.setPaintLabels(true);

        // Add positions label in the simulSpeed
        Hashtable position = new Hashtable();
        position.put(1, new JLabel("1"));
        position.put(5, new JLabel("5"));
        position.put(10, new JLabel("10"));
        JLabel status = new JLabel("Nr of assembly", JLabel.CENTER);
        nrAssembly.setLabelTable(position);
        nrAssembly.setMajorTickSpacing(1);
        nrAssembly.setPaintTicks(true);
        nrAssembly.setSnapToTicks(true);
        panel.add(status);
        panel.add(nrAssembly);
    }

    public void createNrCutting(JPanel panel) {
        nrCutting = new JSlider(1, 10, 1);
        // Set the labels to be painted on the simulSpeed
        nrCutting.setPaintLabels(true);

        // Add positions label in the simulSpeed
        Hashtable position = new Hashtable();
        position.put(1, new JLabel("1"));
        position.put(5, new JLabel("5"));
        position.put(10, new JLabel("10"));
        JLabel status = new JLabel("Nr of cutting", JLabel.CENTER);
        nrCutting.setLabelTable(position);
        nrCutting.setMajorTickSpacing(1);
        nrCutting.setPaintTicks(true);
        nrCutting.setSnapToTicks(true);
        panel.add(status);
        panel.add(nrCutting);
    }



    public void createNrOfCookers(JPanel panel) {
        nrCookers = new JSlider(1, 10, 1);
        // Set the labels to be painted on the simulSpeed
        nrCookers.setPaintLabels(true);

        // Add positions label in the simulSpeed
        Hashtable position = new Hashtable();
        position.put(1, new JLabel("1"));
        position.put(5, new JLabel("5"));
        position.put(10, new JLabel("10"));
        JLabel status = new JLabel("Nr of stoves", JLabel.CENTER);
        nrCookers.setLabelTable(position);
        nrCookers.setMajorTickSpacing(1);
        nrCookers.setPaintTicks(true);
        nrCookers.setSnapToTicks(true);
        panel.add(status);
        panel.add(nrCookers);
    }
    private void createheatmapOptions(JPanel config_panel) {
        heatmapSlider = new JSlider(0, 10, 0);
        // Set the labels to be painted on the simulSpeed
        heatmapSlider.setPaintLabels(true);

        // Add positions label in the simulSpeed
        Hashtable position = new Hashtable();
        position.put(0, new JLabel("0"));
        position.put(5, new JLabel("50"));
        position.put(10, new JLabel("100"));
        JLabel status = new JLabel("heatmap", JLabel.CENTER);
        heatmapSlider.setLabelTable(position);
        heatmapSlider.setMajorTickSpacing(1);
        heatmapSlider.setSnapToTicks(true);
        heatmapSlider.setPaintTicks(true);
        config_panel.add(status);
        config_panel.add(heatmapSlider);
    }

    public void createOptions(JPanel panel) {
        String[] layouts = { "Custom", "Layout1","Layout2", "Layout3"};

        //Create the combo box, select item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        chooseLayout = new JComboBox(layouts);
        chooseLayout.setPreferredSize(new Dimension(200, 20));

        chooseLayout.setSelectedIndex(0);
        chooseLayout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String layout = (String)cb.getSelectedItem();
                String[] layouts = { "Custom", "Layout1","Layout2", "Layout3"};

                switch (layout) {
                    case "Layout1":
                        Kitchen.kitchenLayout = Kitchen.Layout.LEVEL1;
                        nrWorkers.setEnabled(false);
                        nrCookers.setEnabled(false);
                        nrAssembly.setEnabled(false);
                        nrCutting.setEnabled(false);
                        break;
                    case "Layout2":
                        Kitchen.kitchenLayout = Kitchen.Layout.LEVEL2;
                        nrWorkers.setEnabled(false);
                        nrCookers.setEnabled(false);
                        nrAssembly.setEnabled(false);
                        nrCutting.setEnabled(false);
                        break;
                    case "Layout3":
                        Kitchen.kitchenLayout = Kitchen.Layout.LEVEL3;
                        nrWorkers.setEnabled(false);
                        nrCookers.setEnabled(false);
                        nrAssembly.setEnabled(false);
                        nrCutting.setEnabled(false);
                        break;
                    case "Custom":
                        Kitchen.kitchenLayout = Kitchen.Layout.RANDOM;
                        nrWorkers.setEnabled(true);
                        nrCookers.setEnabled(true);
                        nrAssembly.setEnabled(true);
                        nrCutting.setEnabled(true);
                        break;
                }



            }
        });
        //petList.addActionListener();
        panel.add(chooseLayout);
    }

    public void createBaselineOption(JPanel panel) {
        String[] layouts = { "Communication", "Baseline"};

        //Create the combo box, select item at index 4.
        //Indices start at 0, so 4 specifies the pig.
        chooseBaseline = new JComboBox(layouts);
        chooseBaseline.setPreferredSize(new Dimension(200, 20));

        chooseBaseline.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String layout = (String)cb.getSelectedItem();
                switch (layout) {
                    case "Baseline":
                        Kitchen.baseline = true;
                        break;
                    default:
                        Kitchen.baseline = false;
                        break;
                }
            }
        });
        chooseBaseline.setSelectedIndex(0);
        //petList.addActionListener();
        panel.add(chooseBaseline);
    }


    public JPanel createConfigPanel() {
        JPanel config_panel = new JPanel();
        config_panel.setSize(new Dimension(200,500));
        config_panel.setLocation(new Point(550,80));
        createSpeedSlider(config_panel);
        config_panel.add(Box.createRigidArea(new Dimension(0, 15)));
        createNrOfWorkersSlider(config_panel);
        config_panel.add(Box.createRigidArea(new Dimension(0, 15)));
        createNrOfCookers(config_panel);
        config_panel.add(Box.createRigidArea(new Dimension(0, 15)));
        createNrOfAssembly(config_panel);
        config_panel.add(Box.createRigidArea(new Dimension(0, 15)));
        createNrCutting(config_panel);
        config_panel.add(Box.createRigidArea(new Dimension(0, 15)));
        createheatmapOptions(config_panel);
        config_panel.add(Box.createRigidArea(new Dimension(0, 15)));
        createOptions(config_panel);
        config_panel.add(Box.createRigidArea(new Dimension(0, 15)));
        createBaselineOption(config_panel);
        config_panel.setLayout(new BoxLayout(config_panel, 1));

        return config_panel;
    }



    public void createOrdersStats(JPanel panel) {
        // yes, i'd like to use \t but i guess JLabels can't parse that
        ordersStatus = new JLabel("Orders completed: 0    Steps passed: 0", JLabel.CENTER);
        panel.add(ordersStatus);
    }

    public void createTree(JPanel panel)
    {
        //create the root node
        root = new DefaultMutableTreeNode("Agents");
        
        //create the child nodes
        coordinatorsNode = new DefaultMutableTreeNode("Coordinators");
        coordinatorsNode.setUserObject("coordinator");
        joblessNode = new DefaultMutableTreeNode("Jobless");
        joblessNode.setUserObject("jobless");
        for(Agent agent : Kitchen.robots) {
            DefaultMutableTreeNode worker = new DefaultMutableTreeNode(agent.name);
            worker.setUserObject(agent);
            joblessNode.add(worker);
        }
        //add the child nodes to the root node
        root.add(coordinatorsNode);
        root.add(joblessNode);

        //create the tree by passing in the root node
        tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setPreferredSize(new Dimension(100,200));
        panel.add(tree);


        DefaultTreeModel model = new DefaultTreeModel(root);
        tree.setModel(model);

        tree.setCellRenderer(new MyRenderer());






    }

    //public void create
    public JPanel createStatsPanel() {
        JPanel stats = new JPanel();
        stats.setSize(new Dimension(500,20));
        stats.setLocation(new Point(50,530));

        createOrdersStats(stats);
        stats.setLayout(new BoxLayout(stats, 1));

        return stats;
    }
    public JPanel createTreePanel() {
        JPanel stats = new JPanel();
        stats.setSize(new Dimension(500,200));
        stats.setLocation(new Point(50,560));
        createTree(stats);



        return stats;
    }

    public void displayBoard() {
        for(int i=0; i<nX; i++){
            for(int j=0; j<nY; j++){
                int row=nY-j-1, col=i;
                Block block = Kitchen.getBlock(new Point(i,j));
                Cell p = ((Cell)boardPanel.getComponent(row*nX+col));
                p.setBackground(block.color);
                p.blocks.add(block);
                //p.setBorder(BorderFactory.createLineBorder(Color.white));
            }
        }
    }

    public void removeObject(Agent object) {
        int row=nY-object.point.y-1, col=object.point.x;
        Cell p = (Cell)boardPanel.getComponent(row*nX+col);
        p.agents.remove(object);
    }

    public void removeObject(Item object) {
        int row=nY-object.point.y-1, col=object.point.x;
        Cell p = (Cell)boardPanel.getComponent(row*nX+col);
        p.items.remove(object);
    }

    public void displayObject(Agent object) {
        int row=nY-object.point.y-1, col=object.point.x;
        Cell p = (Cell)boardPanel.getComponent(row*nX+col);

        p.agents.add(object);
    }

    public void displayObject(Item object) {
        int row=nY-object.point.y-1, col=object.point.x;
        Cell p = (Cell)boardPanel.getComponent(row*nX+col);
        p.items.add(object);
    }

    private void cleanCoordinatorNode() {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        coordinatorsNode.removeAllChildren();
        coordinatees = new Hashtable<>();
        model.reload(coordinatorsNode);
    }

    private void createCoordinator(CommunicatingAgent agent) {
        if(!coordinatees.containsKey(agent.name)) {
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
//            DefaultMutableTreeNode coordinator = new DefaultMutableTreeNode(agent.name);
            DefaultMutableTreeNode coordinator = new DefaultMutableTreeNode("test");

            coordinator.setUserObject(agent);
            coordinatorsNode.add(coordinator);
            model.reload();
            coordinatees.put(agent.name, coordinator);
            model.reload(coordinatorsNode);
        }
    }

    private void createCoordinatee(CommunicatingAgent agent) {
        DefaultMutableTreeNode coordinator = coordinatees.get(agent.currentCoordinator.name);
        if(coordinator != null) {
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            DefaultMutableTreeNode worker = new DefaultMutableTreeNode(agent.name);
            worker.setUserObject(agent);
            coordinator.add(worker);
            model.reload(coordinatorsNode);
            /*HashMap<String, DefaultMutableTreeNode> workers = coordinatees.get(coordinatorName);
            workers.put(name, )
            coordinatees.put(name, new HashMap<>());*/
        }

    }

    private void cleanJoblessNode() {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        joblessNode.removeAllChildren();
        model.reload(joblessNode);
    }

    private void createJobless(Agent agent) {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode worker = new DefaultMutableTreeNode(agent.name);
        worker.setUserObject(agent);
        joblessNode.add(worker);
        model.reload(joblessNode);
    }

    private void updateTree() {
        cleanCoordinatorNode();
        cleanJoblessNode();
        for(Agent a: Kitchen.robots) {
            switch (a.state) {
                case COOP_IDLE:
                    createJobless(a);
                    break;
                case COOP_WORKING:
                    CommunicatingAgent agent = (CommunicatingAgent)a;
                    if(agent.currentCoordinator != null && agent.currentCoordinator.name != null)
                        createCoordinator(agent.currentCoordinator);
                        createCoordinatee(agent);
                    break;
                case COOP_COORDINATOR:
                    createCoordinator((CommunicatingAgent)a);
                    break;
            }
        }
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        //DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        tree.expandPath(new TreePath(coordinatorsNode.getPath()));
        tree.expandPath(new TreePath(joblessNode.getPath()));

        model.reload(coordinatorsNode);
        model.reload(joblessNode);
        for(DefaultMutableTreeNode node : coordinatees.values()) {
            tree.expandPath(new TreePath(node.getPath()));
            model.reload(node);
        }
    }



    public void update() {
        updateTree();

        ArrayList<Item> items = Kitchen.getAllItems(new Point(Kitchen.dishDeliverX, Kitchen.dishDeliverY));
        String text = "Orders completed: 0";
        if(items != null) {
            text = "Orders completed: "+items.size();
        }
        // yes, i'd like to use \t but i guess JLabels can't parse that
        text += "            Steps taken: " + Kitchen.stepCount;
        ordersStatus.setText(text);
        boardPanel.invalidate();
        boardPanel.repaint();

    }

    private Component createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setSize(new Dimension(300,50));
        panel.setLocation(new Point(520,20));

        step = new JButton("Step");
        panel.add(step);
        step.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if(run.getText().equals("Run")) Kitchen.step();
                else Kitchen.stop();
            }
        });
        reset = new JButton("Reset");
        panel.add(reset);
        reset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                //String[] layouts = { "Layout1", "Random"};

                //kitchenLayout
                Kitchen.nrOfCooks = nrWorkers.getValue();
                Kitchen.nrOfStoves = nrCookers.getValue();
                Kitchen.nrOfAssembly = nrAssembly.getValue();
                Kitchen.nrOfCutting = nrCutting.getValue();

                Kitchen.reset();
            }
        });
        run = new JButton("Run");

        panel.add(run);
        run.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if(run.getText().equals("Run")){
                    int time = -1;
                    simulSpeed.setEnabled(false);
                    nrWorkers.setEnabled(false);
                    nrCookers.setEnabled(false);
                    nrAssembly.setEnabled(false);
                    nrCutting.setEnabled(false);
                    step.setEnabled(false);
                    reset.setEnabled(false);
                    chooseLayout.setEnabled(false);
                    chooseBaseline.setEnabled(false);

                    try {
                        time = 21 - simulSpeed.getValue();
                        //time = Integer.valueOf(speed.getText());
                    } catch(Exception e){
                        JTextPane output = new JTextPane();
                        output.setText("Please insert an integer value to set the time per step\nValue inserted = "+speed.getText());
                        JOptionPane.showMessageDialog(null, output, "Error", JOptionPane.PLAIN_MESSAGE);
                    }
                    if(time>0){
                        Kitchen.run(time);
                        run.setText("Stop");
                    }
                } else {
                    simulSpeed.setEnabled(true);
                    nrWorkers.setEnabled(true);
                    nrCookers.setEnabled(true);
                    nrAssembly.setEnabled(true);
                    nrCutting.setEnabled(true);
                    step.setEnabled(true);
                    reset.setEnabled(true);
                    chooseLayout.setEnabled(true);
                    chooseBaseline.setEnabled(true);
                    Kitchen.stop();
                    run.setText("Run");
                }
            }
        });
        JDialog nrTasksDone = new JDialog();
        //speed = new JTextField("10");
        //speed.setMargin(new Insets(5,5,5,5));
        // panel.add(speed);
        return panel;
    }

    private class MyRenderer extends DefaultTreeCellRenderer {
        Icon cookIcon;

        public MyRenderer() {

        }

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
                    hasFocus);
            if (isAgent(value)) {
                setIcon(getAgentIcon(value));
            } else if(isCoordinatorNode(value))  {
                setIcon(Resources.coordinator_icon);
                //setToolTipText(null); // no tool tip
            } else {
                setIcon(Resources.no_job_icon);
            }

            return this;
        }

        private boolean isCoordinatorNode(Object value) {
            try{
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                String val = (String)node.getUserObject();
                return val.equals("coordinator");
            }catch (Exception e) {
                return false;
            }

        }

        private boolean isAgent(Object value) {
            try {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                CommunicatingAgent agent = (CommunicatingAgent) node.getUserObject();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        protected boolean isCoordinator(Object value) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            CommunicatingAgent agent = (CommunicatingAgent) node.getUserObject();
            return  agent.state == Agent.State.COOP_COORDINATOR;
        }

        protected ImageIcon getAgentIcon(Object value) {
            try{
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                CommunicatingAgent agent = (CommunicatingAgent) node.getUserObject();
                setText(agent.name);
                if(agent.state == Agent.State.COOP_COORDINATOR) {

                    return agent.icon_coordinator;
                }else {
                    return agent.icon_no_job;
                }
            }catch (ClassCastException e) {
                return null;
            }

        }

        protected boolean isTutorialBook(Object value) {
            /*DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            BookInfo nodeInfo = (BookInfo) (node.getUserObject());
            String title = nodeInfo.bookName;
            if (title.indexOf("Tutorial") >= 0) {
                return true;
            }*/

            return true;
        }
    }
}