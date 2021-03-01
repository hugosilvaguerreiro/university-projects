package pt.ulisboa.tecnico.cnv.system_tester;

import com.amazonaws.services.ec2.model.Instance;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import pt.ulisboa.tecnico.cnv.cloudManager.CloudManager;
import pt.ulisboa.tecnico.cnv.dataObject.Metric;
import pt.ulisboa.tecnico.cnv.system_tester.scripts.ScriptRunner;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

public class SystemTester {
    private static final int LOGIN = 1;
    private static final int RUN = 2;
    private static final int REQUEST = 3;
    private static final int DELETE = 4;
    private static final int LAUNCH = 5;
    private static final int DEBUG = 6;
    private static final int EXIT = 7;

    private static Random random;

    public static void main(String[] args) {
        //String HOST = Optional.ofNullable(System.getenv("HOST")).orElse("localhost");
        //int PORT = Integer.valueOf(Optional.ofNullable(System.getenv("PORT")).orElse("8001"));
        String HOST = "localhost";
        int PORT = 8000;
        CloudManager manager = CloudManager.getInstance();

        Scanner s = new Scanner(System.in);
        random = new Random();
        boolean shouldExit = false;
        do {
            System.out.println("Choose a command:");
            System.out.println("1- Login");
            System.out.println("2- Run script");
            System.out.println("3- Make request(s)");
            System.out.println("4- Delete instance");
            System.out.println("5- Launch instance");
            System.out.println("6- Debug");
            System.out.println("7- Exit");
            System.out.print("> ");

            int command = s.nextInt();
            switch (command) {
                case LOGIN:
                    System.out.println("Insert host name");
                    //1System.out.println("> ");
                    HOST = s.next();
                    System.out.println("Insert host port");
                    PORT = s.nextInt();
                    break;
                case RUN:
                    executeRunCommand(s);
                    break;
                case REQUEST:
                    executeRequestCommand(s, HOST, PORT);
                    break;
                case DELETE:
                    executeDeleteInstance(s, manager);
                    break;
                case LAUNCH:
                    System.out.println("How many instances do you wish to launch?");
                    System.out.print("> ");
                    int nr = s.nextInt();
                    System.out.print("Launching instances ...");
                    manager.launchNewInstance(nr);
                    break;
                case EXIT:
                    shouldExit = true;
                    break;
                case DEBUG:
                    ProcessDebug(s, HOST, PORT);
                    break;
                default:
                    System.out.println("ERROR: Unknown command");
                    break;
            }
        } while (!shouldExit);
    }

    private static void ProcessDebug(Scanner s, String HOST, int PORT) {
        URL url = null;
        try {
            url = new URL("http://"+HOST+":"+PORT+"/metrics");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            DataInputStream is = new DataInputStream((con.getInputStream()));
            byte[] buffer = new byte[con.getContentLength()];
            is.readFully(buffer);
            String json = new String(buffer);
            Metric[] metrics = new Gson().fromJson(json, Metric[].class);

            for(Metric metric : metrics) {
                System.out.println(metric.toString());
            }


            int responseCode = con.getResponseCode();



            //[{"parameters":{"solver_strategy":"BFS","y0":"0","y1":"512","start_y":"0","start_x":"0","x0":"0","input_image":"datasets/RANDOM_HILL_512x512_2019-02-27_09-46-42.dat","x1":"512"},"mCount":7328258,"uuid":"d1c9649d-f0ec-4bec-b8d0-214a1012b196"}]
            //JSONObject obj = new JSONObject();
            //Gson gson = new Gson();

            //JsonElement jsonElement = gson.toJsonTree(json);
            //JsonE
            //convertedObject

            System.out.println(new String(buffer));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void executeDeleteInstance(Scanner s, CloudManager manager) {
        System.out.println("Loading available instances. Please wait ...");
        ArrayList<Instance> instances = manager.getAvailableInstances();
        System.out.println("How many instances to delete? "+"(available: "+instances.size()+")");
        System.out.print("> ");
        int nrOfIntancesToDelete = Math.max(0,s.nextInt());
        nrOfIntancesToDelete = Math.min(nrOfIntancesToDelete, instances.size());

        ArrayList<Integer> toDelete = new ArrayList<>();
        for(int j=0; j < nrOfIntancesToDelete; j++) {
            System.out.println("Chose an instance to delete");

            for(int i=0; i < instances.size(); i++) {
                if(!toDelete.contains(i))
                    System.out.println((i+1)+"- "+instances.get(i).getInstanceId());
            }
            int chosenInstance = s.nextInt();
            toDelete.add(chosenInstance-1);
        }
        for(int d : toDelete) {
            if(d < instances.size()) {
                System.out.println("Destroying "+ instances.get(d).getInstanceId());
                manager.destroyInstance(instances.get(d).getInstanceId());
            }
        }
    }

    private static void executeRunCommand(Scanner s) {
        System.out.println("Choose a script to run:");
        ScriptRunner instance = ScriptRunner.getInstance(s);
        String[] list = instance.listAvailableScripts();
        for(int i =0; i < list.length; i++) {
            System.out.println((i+1)+"- "+ list[i]);
        }
        int script = s.nextInt();
        if(script -1 < 0 || script-1 >= list.length) {
            System.out.println("ERROR: Chosen script doesnt exist.");
        }else {
            instance.runScript(list[script-1]);
        }
    }

    private static void makeRequest(URL url, final String requestId) {
            Thread t = new Thread(() -> {
                try {
                    System.out.println(url.toString());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    int responseCode = con.getResponseCode();
                    System.out.println("Request "+requestId + " finished with code " + responseCode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            t.start();
    }
    private static int convertToIntOrDefault(String toConvert, int def) {
        try {
            return Integer.parseInt(toConvert);
        }catch (Exception e) {
            return def;
        }
    }

    private static String[] DATASET_SMALL = {
            "RANDOM_HILL_512x512_2019-02-27_09-56-18.dat",
            "RANDOM_HILL_512x512_2019-03-01_10-28-31.dat",
            "RANDOM_HILL_512x512_2019-03-01_10-28-39.dat",
            "RANDOM_HILL_512x512_2019-03-01_10-28-46.dat",
            "RANDOM_HILL_512x512_2019-03-01_10-28-59.dat",
            "RANDOM_HILL_512x512_2019-03-01_10-29-31.dat",
            "RANDOM_HILL_512x512_2019-02-27_09-46-42.dat"};
    private static String[] DATASET_BIG = {
            "RANDOM_HILL_1024x1024_2019-03-08_16-57-28.dat",
            "RANDOM_HILL_1024x1024_2019-03-08_16-57-37.dat",
            "RANDOM_HILL_1024x1024_2019-03-08_16-58-44.dat",
            "RANDOM_HILL_1024x1024_2019-03-08_16-59-31.dat",
            "RANDOM_HILL_1024x1024_2019-03-08_17-00-23.dat",
            "RANDOM_HILL_1024x1024_2019-03-08_17-04-10.dat"};

    private static void executeRequestCommand(Scanner s, String host, int port) {
        System.out.println("Specify how many requests to do [1]");
        System.out.print("> ");
        int number_requests = s.nextInt();

        System.out.println("Random request?");
        System.out.println("1- Specify");
        System.out.println("2- Random");
        System.out.print("> ");
        int randomOrNot = s.nextInt();

        System.out.println("Choose an image to send");
        System.out.println("1- Small");
        System.out.println("2- Big");
        System.out.print("> ");
        int smallOrBig = s.nextInt();
        String[] datasetToShow;
        int datasetSize = 0;
        if(smallOrBig == 1) {
            datasetToShow = DATASET_SMALL;
            datasetSize = 512;

        } else {
            datasetToShow = DATASET_BIG;
            datasetSize = 1024;
        }
        if(randomOrNot == 2){
            executeRandomRequest(number_requests, datasetToShow, datasetSize, host, port);
        }
        else{
            System.out.println("Choose an image to process");
            for(int i=0; i < datasetToShow.length; i++) {
                System.out.println((i+1)+"- "+datasetToShow[i]);
            }
            System.out.print("> ");
            int img = s.nextInt();
            img = Math.max(0, img-1);
            img = Math.min(img, datasetToShow.length);
            String image = datasetToShow[img];

            System.out.print("x0=[0]");
            int x0 = convertToIntOrDefault(s.nextLine(), 0);
            System.out.println("y0=[0]");
            int y0 = convertToIntOrDefault(s.nextLine(), 0);
            System.out.println("x1=[512]");
            int x1 = convertToIntOrDefault(s.nextLine(), datasetSize);
            System.out.println("y1=[512]");
            int y1 = convertToIntOrDefault(s.nextLine(), datasetSize);
            System.out.println("xs=[0]");
            int xs = convertToIntOrDefault(s.nextLine(), 0);
            System.out.println("ys=[0]");
            int ys = convertToIntOrDefault(s.nextLine(), 0);
            System.out.println("Strategy:");
            System.out.println("1- BFS");
            System.out.println("2- DFS");
            System.out.println("3- ASTAR");
            int strategy = s.nextInt();
            String strategyName;
            switch (strategy) {
                case 2:
                    strategyName = "DFS";
                    break;
                case 3:
                    strategyName = "ASTAR";
                    break;
                default:
                    strategyName="BFS";
            }

            String url = "http://"+host + ":" + port + "/climb?w="+ datasetSize + "&h=" + datasetSize + "&";
            url += "x0="+x0+"&x1="+x1+"&y0="+y0+"&y1="+y1+"&xS="+xs+"&yS="+ys+"&s="+strategyName+"&i=datasets/"+image;

            for(int i=0; i < number_requests; i++) {
                try {
                    makeRequest(new URL(url), UUID.randomUUID().toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void executeRandomRequest(int number_requests, String[] dataset, int datasetSize, String host, int port) {
        for(int i=0; i < number_requests; i++) {
            int img = random.nextInt(dataset.length);
            img = Math.max(0, img-1);
            img = Math.min(img, dataset.length);
            String image = dataset[img];

            int x0, y0, x1, y1, xs, ys, t;
            x0 = random.nextInt(datasetSize);
            y0 = random.nextInt(datasetSize);
            x1 = random.nextInt(datasetSize);
            y1 = random.nextInt(datasetSize);
            if (x0 > x1){
                t = x0;
                x0 = x1;
                x1 = t;
            }
            if (y0 > y1){
                t = y0;
                y0 = y1;
                y1 = t;
            }

            xs = x0 + random.nextInt(x1 - x0 );
            ys = y0 + random.nextInt(y1 - y0);

            int strategy = random.nextInt(3);
            String strategyName;
            switch (strategy) {
                case 0:
                    strategyName = "DFS";
                    break;
                case 1:
                    strategyName = "ASTAR";
                    break;
                default:
                    strategyName="BFS";
            }

            String url = "http://"+host + ":" + port + "/climb?w="+ datasetSize + "&h=" + datasetSize + "&";
            url += "x0="+x0+"&x1="+x1+"&y0="+y0+"&y1="+y1+"&xS="+xs+"&yS="+ys+"&s="+strategyName+"&i=datasets/"+image;

            try {
                makeRequest(new URL(url), UUID.randomUUID().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
