package sample;

import com.bbn.openmap.proj.Ellipsoid;
import com.bbn.openmap.proj.coords.LatLonGCT;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;


import java.lang.Math;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.proj.coords.UTMPoint;

public class CanvasFlightPlan  {

    private String Current_Survey = Controller.getCurSurvey();
    private int Current_Block = Controller.getCurBlock();
    private int Current_Flight = Controller.getCurFlight();
    private String planSettingsFile;
    private String wayPointsFile;
    private String wayPointsTieFile;

    private Color color;
    private double totalDistance = 0;
    private int type;
    private String planSettingsFileBlock;
    private String wayPointsFileBlock;

    private String planSettingsFileFlight;
    private String wayPointsFileFlight;
    private String wayPointsFileBlockTie;
    private final Stage STAGE;

    private String planSettings;
    private String wayPoints;
    private String blockPoints;
    double aCanvas  = 1500.0f, bCanvas  = 1500.0f;
    double a = 800.0, b = 800.0;
    double offsetX = 50, offsetY = 50;
    double osX = 0, osY = 0;
    private Scene SCENE;
    private Canvas canvas;
    private ArrayList<Position> polygon = new ArrayList<>(), blockPolygon = new ArrayList<>();
    private GraphicsContext graphics_context;
    private Integer index_color;
    private HashMap<Integer, ArrayList<Position>> polsPositions = new HashMap<>();
    private Position minPosition, maxPosition;

    public enum COLORS {
        A(Color.GOLD), B(Color.DARKCYAN),C(Color.DARKGOLDENROD),D(Color.CYAN),
        E(Color.DARKGREEN), F(Color.DARKKHAKI), G(Color.DARKMAGENTA), H(Color.CHOCOLATE),
        I(Color.BURLYWOOD), J(Color.BLUEVIOLET);

        private Color numVal;

        COLORS(Color numVal) {
            this.numVal = numVal;
        }

        public Color getColor() {
            return numVal;
        }
    }

    /*
        Canvas Flight Types:
        -3 = Survey Settings select Start
        -2 = Show Flight Plan
        -1 = Show Block Plan
        0 = Show Block Edit Plan
        1 = Show Survey Plan (Flight Lines only)
        2 = Show Survey Plan (Tie Lines only)
        3 = Show Survey Plan (Flight and Tie Lines)
        4 = Show Block Tie Lines
     */

    public  CanvasFlightPlan(int type) {

        this.type = type;
        String pathPython = System.getProperty("user.dir").replace('\\', '/') + "/Package/pythontest.py";
        String path = System.getProperty("user.dir").replace('\\', '/') + "/Data/" + Current_Survey;
        // All in the Survey/FlightPlan Folder
        planSettingsFile = path +"/FlightPlan" + Controller.getPrefixToSurvey() + "-plan_settings.txt";
        wayPointsFile = path + "/FlightPlan" + Controller.getPrefixToSurvey() +"-waypointsData.txt";
        wayPointsTieFile = path + "/FlightPlan"+ Controller.getPrefixToSurvey() + "-waypointsDataTieLine.txt";

        path += "/Block" + Current_Block;
        planSettingsFileBlock = path + "/flight_plan" + Controller.getPrefixToBlock() + "-flightPalnBlock.txt";
        wayPointsFileBlock = path + "/flight_plan" + Controller.getPrefixToBlock() + "-waypointsDataBlock.txt";
        wayPointsFileBlockTie = path + "/flight_plan" + Controller.getPrefixToBlock() + "-waypointsDataBlockTieLines.txt";
        planSettingsFileFlight = path + "/flight_plan" + Controller.getPrefixToBlock() + "-flightPalnBlock.txt";

        wayPointsFileFlight = path + "/Flight"+Current_Flight+"/flight_plan"+ Controller.getPrefixToFlight() +"-waypointsDataFlight.txt";

        switch(type){
            case -2:
                color = Color.NAVY;
                planSettings = planSettingsFileFlight;
                wayPoints = wayPointsFileFlight;
                break;
            case -1:
                color = Color.NAVY;
                planSettings = planSettingsFileBlock;
                wayPoints = wayPointsFileBlock;
            break;
            case 0:
                color = Color.RED;
                planSettings = planSettingsFile;
                wayPoints = wayPointsFile;
                blockPoints = planSettingsFileBlock;
            break;
            case 1:
                planSettings = planSettingsFile;
                wayPoints = wayPointsFile;
                color = Color.BLUE;
            break;
            case 2:
                color = Color.GREEN;
                wayPoints = wayPointsTieFile;
                planSettings = planSettingsFile;
            break;
            case 3:
                color = Color.BLACK;
                planSettings = planSettingsFile;
                try {
                    wayPoints = mergeTwoFiles(wayPointsTieFile, wayPointsFile);
                }catch(IOException e){
                    e.printStackTrace();
                }
                break;
            case 4:
                color = Color.DARKCYAN;
                planSettings = planSettingsFileBlock;
                wayPoints = wayPointsFileBlockTie;
            break;
            default: planSettings = planSettingsFile;
            break;
        }

        System.out.println(Current_Survey);
        STAGE = new Stage();
        STAGE.initModality(Modality.APPLICATION_MODAL);
        STAGE.initOwner(Controller.getPrimaryStage());

        if(type != 1 && type != 2 && type != 3){
            aCanvas = 1500.0f;
            bCanvas = 1500.0f;
            a = 800.0;
            b = 800.0;
            offsetX = 40;
            offsetY = 40;
        }
        //fpMap.setTitle("FlightPlan Map");
        this.canvas = new Canvas(aCanvas, bCanvas);
        // graphics context
        GraphicsContext graphics_context = canvas.getGraphicsContext2D();
        this.graphics_context = graphics_context;
        graphics_context.setFill(Color.RED);
        // create a Group
        Group group = new Group(canvas);
        // create a scene
        Scene scene = new Scene(group, a, b);
        this.SCENE = scene;
        // set the scene
        STAGE.setScene(scene);
        //fpMap.setTitle("Flight Plan Settings");
        STAGE.show();

        // New Window pop up for block planning
        if(type == 0) {
            final Stage partitionStage = new Stage();
            StackPane layout = new StackPane();
            Scene scenePartition = new Scene(layout, 200, 100);
            partitionStage.setScene(scenePartition);
            javafx.geometry.Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            double x = bounds.getMinX() + (bounds.getWidth() - scene.getWidth()) * 0.3;
            double y = bounds.getMinY() + (bounds.getHeight() - scene.getHeight()) * 0.7;
            partitionStage.setX(x);
            partitionStage.setY(y);
            partitionStage.show();
            partitionStage.setResizable(true);
            Button btnPartition = new Button("Partition into Blocks");
            btnPartition.setPrefWidth(200);
            btnPartition.setPrefHeight(100);
            layout.getChildren().add(btnPartition);
            btnPartition.setOnAction((event) -> {
                partitionStage.setWidth(400);
                partitionStage.setHeight(320);
                layout.getChildren().remove(btnPartition);
                partitionSettings(layout, scene);
            });
        }
        System.out.println("After MinMax");

        showWaypoints();

        System.out.println("After showWayPoints");
    }

    private void showWaypoints() {
        ArrayList<Position> posList = new ArrayList<>();
        double x = 0;
        double y = 0;
        Position minPosition = new Position(0,0);
        Position maxPosition = new Position(0,0);
        System.out.println("showWayPoint Setup Complete");
        try {
            ObservableList<String> coords = FlightPlanning.getCoordinates();
            String start = "UTM:";
            if(type != 1 && type != 2 && type != 3 && type != 0)
                start = "Points:";
            String[] segments = new String[0];
            String s = "";

            if(this.type != -3){
                InputStream ins = new FileInputStream(planSettings);
                Reader r = new InputStreamReader(ins, StandardCharsets.UTF_8); // leave charset out for default
                BufferedReader br = new BufferedReader(r);
                while ((s = br.readLine()) != null) {
                    if (s.startsWith(start)){
                        s = s.substring(start.length());
                        segments = new String[s.split(":").length];
                        segments = s.split(":");
                        break;
                    }
                }
            }else{
                // Convert Lat Lon to UTM in shit
                segments = new String[coords.size()];
                int zoneno = 0;
                for(int i = 0; i < segments.length; i++){
                    String [] ss = coords.get(i).split(",");
                    segments[i] = ss[1] + "," + ss[0];
                }
            }

            System.out.println("Number of segments = " + segments.length);
            for(int i = 0; i < segments.length; i++){
                String posX = segments[i].split(",")[0];
                String posY = segments[i].split(",")[1];
                System.out.println(posX + "   " + posY);
                //Dunno what this is for but ok.
//                if(type != 1 && type != 2 && type != 3){
//                    posX = posX.substring(1, posX.length());
//                    posY = posY.substring(0, posY.length()-1);
//                }
                System.out.println("-------> "+posX +","+ posY);
                double posx = Double.parseDouble(posX);
                double posy = Double.parseDouble(posY);
                if(minPosition.getX() == 0 & minPosition.getY() == 0){
                    minPosition.setX(posx);
                    minPosition.setY(posy);
                }
                if(maxPosition.getX() == 0 & maxPosition.getY() == 0){
                    maxPosition.setX(posx);
                    maxPosition.setY(posy);
                }
                if(minPosition.getX() > posx)
                    minPosition.setX(posx);
                if(minPosition.getY() > posy)
                    minPosition.setY(posy);
                if(maxPosition.getX() < posx)
                    maxPosition.setX(posx);
                if(maxPosition.getY() < posy)
                    maxPosition.setY(posy);
                posList.add(new Position(posx, posy));
            }
            this.minPosition = minPosition;
            this.maxPosition = maxPosition;
            if(type == -3){
                //This means that if this is in Lat Lon and the longitude of survey area spans for more than 180 degrees longitude
                // Fix this by making all coordinates to one side
                if((this.maxPosition.getX() - this.minPosition.getX()) > 180){
                    double midpoint = (this.maxPosition.getX() + this.minPosition.getX()) / 2.0;
                    for (int i = 0; i < posList.size(); i++){
                        Position p = posList.get(i);
                        if(p.getX() < midpoint){
                            posList.set(i, new Position(p.getX() + 360, p.getY()));
                        }
                    }
                }
                //Get new max and min Position after conversion
                for(int i = 0; i < posList.size(); i++){
                    double posx = posList.get(i).getX();
                    double posy = posList.get(i).getY();
                    if (i == 0){
                        minPosition.setX(posx);
                        minPosition.setY(posy);
                        maxPosition.setX(posx);
                        maxPosition.setY(posy);
                        continue;
                    }
                    if(minPosition.getX() > posx)
                        minPosition.setX(posx);
                    if(minPosition.getY() > posy)
                        minPosition.setY(posy);
                    if(maxPosition.getX() < posx)
                        maxPosition.setX(posx);
                    if(maxPosition.getY() < posy)
                        maxPosition.setY(posy);
                }
            }


            double scale = convertPosition(posList, minPosition, maxPosition);
            int size = polygon.size();
            for(int i = 0; i < polygon.size(); i++) {
                graphics_context.setStroke(Color.GREEN);
                graphics_context.setLineWidth(5);
                graphics_context.strokeLine(polygon.get(i % size).getX(), polygon.get(i % size).getY(),
                        polygon.get((i+1) % size).getX(), polygon.get((i+1) % size).getY());
            }
            for(Position p : polygon){
                graphics_context.setFill(Color.BLUE);
                System.out.println(p.getX()+","+p.getY());
                graphics_context.fillOval( p.getX() - 5 ,p.getY() - 5, 10, 10 );
            }
            if(this.type == -1 || this.type == -2 || this.type == 4){
                InputStream ins = new FileInputStream(planSettingsFile);
                Reader r = new InputStreamReader(ins, StandardCharsets.UTF_8); // leave charset out for default
                BufferedReader br = new BufferedReader(r);
                segments = new String[0];
                start = "UTM:";
                while ((s = br.readLine()) != null) {
                    if (s.startsWith(start)){
                        s = s.substring(start.length());
                        segments = new String[s.split(":").length];
                        segments = s.split(":");
                        break;
                    }
                }

                ArrayList<Position> survList = new ArrayList<>();

                for(int i = 0; i < segments.length; i++) {
                    String posX = segments[i].split(",")[0];
                    String posY = segments[i].split(",")[1];
                    double posx = Double.parseDouble(posX);
                    double posy = Double.parseDouble(posY);
                    survList.add(new Position(posx, posy));
                }
                convertPosition(survList, minPosition, maxPosition, scale);
                size = polygon.size();
                for(int i = 0; i < polygon.size(); i++) {
                    graphics_context.setStroke(Color.MAGENTA);
                    graphics_context.setLineDashes(10);
                    graphics_context.setLineWidth(3);
                    graphics_context.strokeLine(polygon.get(i % size).getX(), polygon.get(i % size).getY(),
                            polygon.get((i+1) % size).getX(), polygon.get((i+1) % size).getY());
                }
            }
            if(this.type == 0){
                //TODO: Implement Show Current BLock Plan
                try {
                    ArrayList<Position> blockPosList = new ArrayList<>();
                    InputStream bis = new FileInputStream(blockPoints);
                    Reader blockr = new InputStreamReader(bis, StandardCharsets.UTF_8); // leave charset out for default
                    BufferedReader blockbr = new BufferedReader(blockr);
                    s = "";
                    while ((s = blockbr.readLine()) != null) {
                        String[] ss = s.split(":");
                        for (int i = 1; i < ss.length; i++) {
                            String[] sss = ss[i].split(",");
                            System.out.println(Double.parseDouble(sss[0]) + "  " + Double.parseDouble(sss[1]));
                            blockPosList.add(new Position(Double.parseDouble(sss[0]), Double.parseDouble(sss[1])));
                        }
                    }
                    convertBlockPosition(blockPosList, minPosition, maxPosition);
                    size = blockPosList.size();
                    for(int i = 0; i < size; i++) {
                        graphics_context.setStroke(Color.PURPLE);
                        graphics_context.setLineWidth(7);
                        graphics_context.strokeLine(blockPolygon.get(i % size).getX(), blockPolygon.get(i % size).getY(),
                                blockPolygon.get((i+1) % size).getX(), blockPolygon.get((i+1) % size).getY());
                    }
//            graphics_context.strokeLine(polygon.get(i % size).getX(), polygon.get(i % size).getY(),
//                    polygon.get((i+1) % size).getX(), polygon.get((i+1) % size).getY());
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(this.type != -3){
                drawWaypoints(graphics_context, minPosition,  maxPosition );
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        //Y then X because Lat is Y and Lon is X
    }


    private double convertPosition(ArrayList<Position> list, Position minPosition, Position maxPosition){
        polygon.clear();
        double h = maxPosition.getX() - minPosition.getX();
        double v = maxPosition.getY() - minPosition.getY();
        double scale = (h > v)? h : v;
        osX = (h > v)? 0 : (a-2*offsetX) * (1 - (h / v)) / 2;
        osY = (h < v)? 0 : (b-offsetY*2) * (1 - (v / h)) / 2;
        double scalex = ((a-2*offsetX)/scale);
        double scaley = ((b-offsetY*2)/scale);
        // Centers the Polygon
        canvas.setTranslateX(osX);
        canvas.setTranslateY(osY * -1.0);
        for(Position p : list){
            polygon.add(new Position(scalex*(p.getX()-minPosition.getX()) + offsetX,
                    b-(scaley*(p.getY()-minPosition.getY()) + offsetY)));
        }
        return scale;
    }
    private void convertPosition(ArrayList<Position> list, Position minPosition, Position maxPosition, double scale){
        polygon.clear();
        double h = maxPosition.getX() - minPosition.getX();
        double v = maxPosition.getY() - minPosition.getY();
        double scalex = ((a-2*offsetX)/scale);
        double scaley = ((b-offsetY*2)/scale);
        for(Position p : list){
            polygon.add(new Position(scalex*(p.getX()-minPosition.getX())+ offsetX,
                    b-(scaley*(p.getY()-minPosition.getY())+ offsetY)));
        }
    }
    private void convertBlockPosition(ArrayList<Position> list, Position minPosition, Position maxPosition){
        blockPolygon.clear();
        double h = maxPosition.getX() - minPosition.getX();
        double v = maxPosition.getY() - minPosition.getY();
        double scale = (h > v)? h : v;
        double scalex = ((a-2*offsetX)/scale);
        double scaley = ((b-offsetY*2)/scale);
        for(Position p : list){
            blockPolygon.add(new Position(scalex*(p.getX()-minPosition.getX())+ offsetX,
                    b-(scaley*(p.getY()-minPosition.getY())+offsetY)));
        }
    }


    private void drawWaypoints(GraphicsContext gc, Position minPosition, Position maxPosition ){
        double h = maxPosition.getX() - minPosition.getX();
        double v = maxPosition.getY() - minPosition.getY();
        double scale = (h > v)? h : v;
        double minX = minPosition.getX();
        double minY = minPosition.getY();
        DecimalFormat numberFormat = new DecimalFormat("#.0");
        try {
            String s = "";
            InputStream ins = new FileInputStream(wayPoints);
            Reader r = new InputStreamReader(ins, StandardCharsets.UTF_8); // leave charset out for default
            BufferedReader br = new BufferedReader(r);
            Position prevPos = new Position(0,0);
            Position newPos = new Position(0,0);
            double x_init = 0;
            double y_init = 0;

            if((s = br.readLine()) != null){
                // Fill the Starting point
                String[] segments = s.split(" ");
                gc.setFill(Color.RED);
                double x = ((a-2*offsetX)/scale) * (Double.parseDouble(segments[0])-minX)+offsetX;
                double y = b-(((b-2*offsetY)/scale)*(Double.parseDouble(segments[1])-minY)+offsetY);
                prevPos = new Position(Double.parseDouble(segments[0]), Double.parseDouble(segments[1]));
                gc.fillRect( x - 2.5, y - 2.5, 5, 5 );
                x_init = x;
                y_init = y;
            }
            while ((s = br.readLine()) != null){
                String[] segments = s.split(" ");
                gc.setFill(Color.BLACK);
                double posX1 = Double.parseDouble(segments[0]);
                double posY2 = Double.parseDouble(segments[1]);
                newPos = new Position(posX1, posY2);
                totalDistance += newPos.distance(prevPos);
                prevPos = newPos;
                double x = ((a-2*offsetX)/scale) * (posX1-minX)+offsetX;
                double y = b-(((b-2*offsetY)/scale)*(posY2-minY)+offsetY);
                gc.fillOval( x - 2.5, y - 2.5, 5, 5 );
                gc.setStroke(color);
                gc.setLineWidth(1);
                gc.setLineDashes(2);
                gc.strokeLine(x_init, y_init, x, y);
                x_init = x;
                y_init = y;
            }
            br.close();
            String modeOfCanvas = "";
            switch(type){
                case 4: modeOfCanvas = "Block Map of " + Controller.getPrefixToBlock() + " With Tie Lines";
                break;
                case 3: modeOfCanvas = "Survey Map of " + Controller.getCurSurvey() + " With Flight and Tie Lines";
                break;
                case 2: modeOfCanvas = "Survey Map of " + Controller.getCurSurvey() + " With Tie Lines";
                break;
                case 1: modeOfCanvas = "Survey Map of " + Controller.getCurSurvey() + " With Flight Lines";
                break;
                case 0: modeOfCanvas = "Survey Map of " + Controller.getCurSurvey() + " Edit Block " + Controller.getCurBlock();
                break;
                case -1: modeOfCanvas = "Block Map of " + Controller.getPrefixToBlock();
                break;
                case -2: modeOfCanvas = "Flight Map of " + Controller.getPrefixToFlight();
                break;
                case -3: modeOfCanvas = Controller.getCurSurvey() + ": Select Starting Point";
                break;
                default:
            }
            STAGE.setTitle(modeOfCanvas+"   --    Total distance travel : "+ numberFormat.format(totalDistance/1000)+ " KM");
        }catch (IOException e){

        }


    }

    private void partitionSettings(StackPane layout, Scene scene){
        Button createBlock_mode_on = new Button("CreateBlock Mode ON");
        Button createBlock_mode_off = new Button("CreateBlock Mode OFF");
        Button showBlocksPlan = new Button("Show Block Plans");
        Button createBlocksPlan = new Button("Create Block Plans");
        Button rerenderBlocksPlan = new Button("Re-Render Block Plans");
        Button undoPoints = new Button("Undo");
        createBlock_mode_on.setTranslateY(100);
        createBlock_mode_on.setTranslateX(-80);
        createBlock_mode_off.setTranslateY(100);
        createBlock_mode_off.setTranslateX(80);
        createBlock_mode_off.setDisable(true);
        showBlocksPlan.setTranslateY(70);
        showBlocksPlan.setTranslateX(-80);
        createBlocksPlan.setTranslateY(70);
        createBlocksPlan.setTranslateX(80);

        undoPoints.setTranslateY(40);
        undoPoints.setTranslateX(-80);
        undoPoints.setDisable(true);
        rerenderBlocksPlan.setTranslateY(40);
        rerenderBlocksPlan.setTranslateX(80);

        ComboBox comboBox = new ComboBox(Controller.getBlocks());
        comboBox.setDisable(true);
        comboBox.setTranslateY(-110);

        comboBox.setValue(Controller.getBlocks().get(Controller.getCurBlock()));
        System.out.println(comboBox.getValue().toString());
        String str = comboBox.getValue().toString();

        try{
            for(int i = 0; i < str.length(); i++) {
                char ch2 = str.charAt(i);
                if (ch2 == ',') {
                    str = str.substring(1, i);
                    index_color = Integer.parseInt(str);
                    break;
                }
            }
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        layout.getChildren().add(createBlock_mode_on);
        layout.getChildren().add(createBlock_mode_off);
        layout.getChildren().add(comboBox);
        layout.getChildren().add(createBlocksPlan);
        layout.getChildren().add(showBlocksPlan);
        layout.getChildren().add(rerenderBlocksPlan);
        layout.getChildren().add(undoPoints);
        showBlocksPlan.setDisable(true);
        createBlocksPlan.setDisable(true);
        EventHandler<javafx.scene.input.MouseEvent> eventHandlerScene =
                e -> {
                showBlocksPlan.setDisable(false);
                undoPoints.setDisable(false);
                    try{
                        Color c =  COLORS.values()[(index_color-1) % 10].numVal;
                        graphics_context.setFill(c);
                        Position newPos = produceZappedPoint(e.getX() - osX, e.getY() + osY);
                        System.out.println(newPos.getX() +" "+ newPos.getY());
                        graphics_context.fillOval(newPos.getX() - 5,newPos.getY() - 5, 10, 10);
                        if(!polsPositions.containsKey(index_color)){
                            polsPositions.put(Controller.getCurBlock(), new ArrayList<>());
                            polsPositions.get(Controller.getCurBlock()).add(newPos);
                            System.out.println("-->");
                        }else{
                            polsPositions.get(index_color).add(newPos);
                            System.out.println("---");
                        }
                        }catch(NullPointerException nullE){

                    }
                };

        createBlock_mode_on.setOnAction((event) -> {
            createBlock_mode_on.setDisable(true);
            createBlock_mode_off.setDisable(false);
            rerenderBlocksPlan.setDisable(true);
            scene.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, eventHandlerScene);
        });
        undoPoints.setOnAction(event -> {
            ArrayList<Position> todel = polsPositions.get(Controller.getCurBlock());
            if(todel.size() > 0){
            Position delete = todel.remove(todel.size() - 1);
            graphics_context.setFill(Color.WHITE);
            graphics_context.fillOval(delete.getX() - 5,delete.getY() - 5, 10, 10);
            }
        });
        rerenderBlocksPlan.setOnAction(event -> {
            renderBlock(Controller.getCurBlock());
        });


        createBlock_mode_off.setOnAction((event) -> {
            scene.removeEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, eventHandlerScene);
            createBlock_mode_on.setDisable(false);
            createBlock_mode_off.setDisable(true);
            createBlocksPlan.setDisable(true);
            showBlocksPlan.setDisable(true);
            rerenderBlocksPlan.setDisable(false);
        });
        //TODO: fix null pointer
        double h = maxPosition.getX() - minPosition.getX();
        double v = maxPosition.getY() - minPosition.getY();
        double scale = (h > v)? h : v;


        showBlocksPlan.setOnAction((event) -> {
            showBlocksPlan.setDisable(true);
            createBlocksPlan.setDisable(false);
            Iterator it = polsPositions.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<Integer, ArrayList<Position>> pair = (Map.Entry) it.next();

                int size = pair.getValue().size();

                for (int i = 0 ; i < size ; i++){
                    graphics_context.setStroke(COLORS.values()[(pair.getKey()-1) % 10].numVal);
                    graphics_context.setLineWidth(2);
                    graphics_context.strokeLine(pair.getValue().get(i % size).getX(), pair.getValue().get(i % size).getY(),
                            pair.getValue().get((i+1) % size).getX(), pair.getValue().get((i+1) % size).getY());
                }
            }
        });
        //Create BLock Plan on the
        createBlocksPlan.setOnAction((event) -> {
            createBlocksPlan.setDisable(true);
            int blockno = 0;
            Iterator it = polsPositions.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<Integer, ArrayList<Position>> pair = (Map.Entry) it.next();
                blockno = pair.getKey();
                try{
                    String fileFlightPlanBlock = System.getProperty("user.dir")+"/Data/"+ Current_Survey+"/Block"+ blockno +"/flight_plan" + Controller.getPrefixToSurvey() +
                            "-B" + pair.getKey() +"-flightPalnBlock.txt";
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileFlightPlanBlock,false)));
                    out.flush();
                    out.write("Points:");
                    for(Position itm : pair.getValue()){
                        double xP = minPosition.getX() + (itm.getX() - offsetX)*scale/(a-2*offsetX);
                        double yP = (b - itm.getY() - offsetY)*scale/(b-2*offsetY)+ minPosition.getY();
                        out.write( xP+","+yP+ ":");
                    }
                    out.write("\n");
                    out.close();
                }catch(Exception e) {

                }
                renderBlock(blockno);
            }
        });

    }
    private void renderBlock(int blockno){
        String path = System.getProperty("user.dir");
        String pathPython = path + "/Package/pythontest.py";
        String command = "python " + pathPython + " -m FlightPlanBlocks -f " + planSettingsFile + " -b " + blockno;
        System.out.println(command);
        try {
            Process p = Runtime.getRuntime().exec(command);
            Controller.pythonConsole(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Position produceZappedPoint(double x, double y){
        double constant = 6;
        double constant2 = 0.5;
        double constant3 = 0.2;

        Position mousePos = new Position(x, y);

        for(Position p :  polygon){
            if(mousePos.distance(p) <= constant){
                return p;
            }
        }
        for(ArrayList<Position> pList :  polsPositions.values()){
            for(Position p : pList){
                System.out.println(p.getX());
                if(mousePos.distance(p) <= constant){
                    return p;
                }
            }
        }
        int sizePolygon = polygon.size();
        for(int i = 0; i < sizePolygon; i++){
            int size = polygon.size();
            Position p1 = polygon.get(i % size);
            Position p2 = polygon.get((i+1) % size);
            if(mousePos.distance2TwoPoints(p1, p2) <= constant2){
                return mousePos.closestPoint(p1, p2);
            }

        }


        for(ArrayList<Position> pList :  polsPositions.values()){
            int size = pList.size();
            for(int i = 0; i < size; i++){
                Position p1 = pList.get(i % size);
                Position p2 = pList.get((i+1) % size);
                if(mousePos.distance2TwoPoints(p1, p2) <= constant3)
                    return mousePos.closestPoint(p1, p2);
            }
        }
        return mousePos;
    }

    public void setInitPosition(TextField f1, TextField f2){
        double h = maxPosition.getX() - minPosition.getX();
        double v = maxPosition.getY() - minPosition.getY();

        double scale = (h > v)? h : v;

        graphics_context.setFill(Color.PURPLE);

        if(!f1.getText().isEmpty() || !f2.getText().isEmpty()){
            System.out.println(f1.getText() + "   " + f2.getText());
            graphics_context.setFill(Color.PURPLE);
            double xPos = Double.parseDouble(f1.getText());
            double yPos = Double.parseDouble(f2.getText());
            System.out.println(xPos + "     " + yPos);
            double xP = (xPos - minPosition.getX()) * (a - 2 * offsetX) / scale + offsetX;
            double yP = b - (yPos - minPosition.getY()) * (b -2 * offsetY) / scale - offsetY;
            graphics_context.fillOval(xP - 7.5,yP - 7.5, 15, 15);
            System.out.println(xP + "   " + yP);
        }

        EventHandler<javafx.scene.input.MouseEvent> eventHandlerScene =
                e -> {
                    try{
                        graphics_context.setFill(Color.RED);

                        Position newPos = produceZappedPoint(e.getX(),e.getY());
                         double xPos = newPos.x - osX;
                         double yPos = newPos.y + osY;
                        System.out.println(newPos.getX() +" "+ newPos.getY());
                        graphics_context.fillRect(xPos,yPos, 10, 10);
                        //TODO: there is a zero divider here.
                         double xP = minPosition.getX() + (xPos - offsetX)*scale/(a-2*offsetX);
                         double yP = (b - yPos - offsetY)*scale/(b-2*offsetY) + minPosition.getY();

                         if(this.type == -3){
                             if(xP > 180.0)  xP -= 360.0;
                             if(xP < -180.0) xP += 360.0;
                         }
                        f1.setText(Double.toString(xP));
                        f2.setText(Double.toString(yP));
                        System.out.println("Current Starting Point: " + yP + "     " + xP);
                        System.out.println("Current Starting Point: " + yPos + "     " + xPos);

                    }catch(NullPointerException nullE){

                    }
                };

        this.SCENE.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, eventHandlerScene);
    }

    private String mergeTwoFiles(String file1, String file2) throws IOException{
        String outputFile = System.getProperty("user.dir")+"/Data/"+Current_Survey+"/FlightPlan" + Controller.getPrefixToSurvey() + "-waypointsMerged.txt";
        PrintWriter pw = new PrintWriter(outputFile);
        // BufferedReader object for file1.txt
        BufferedReader br = new BufferedReader(new FileReader(file1));
        String line = br.readLine();
        // loop to copy each line of
        // file1.txt to  file3.txt
        while (line != null){
            pw.println(line);
            line = br.readLine();
        }
        br = new BufferedReader(new FileReader(file2));
        line = br.readLine();
        // loop to copy each line of
        // file2.txt to  file3.txt
        while(line != null){
            pw.println(line);
            line = br.readLine();
        }

        pw.flush();
        // closing resources
        br.close();
        pw.close();

        return outputFile;
    }

    private class Position{

        double x;
        double y;

        public Position(double x, double y){
            this.x = x;
            this.y = y;
        }
        public double getX(){

            return x;
        }

        public double getY(){ return y; }
        public void setX(double x){ this.x = x; }
        public void setY(double y){ this.y = y; }

        public double distance(Position p){
           double dist = Math.sqrt(Math.pow((p.getX()-x),2) +Math.pow((p.getY()-y),2));
            //System.out.println("EWFWEFWEV#VRVEVE     -->   "+dist);
           return dist;
        }

        public double distance2TwoPoints(Position p1, Position p2){
            double dist1 = Math.sqrt(Math.pow((p1.getX()-x),2) +Math.pow((p1.getY()-y),2));
            double dist2 = Math.sqrt(Math.pow((p2.getX()-x),2) +Math.pow((p2.getY()-y),2));
            double distanceBetweenTwo = Math.sqrt(Math.pow((p2.getX()-p1.getX()),2) +Math.pow((p2.getY()-p1.getY()),2));
            return ((dist1 + dist2) - distanceBetweenTwo) ;
        }
        public Position closestPoint(Position p1, Position p2){
            double m = ((p1.getY() - p2.getY())/(p1.getX() - p2.getX()));

            double xNew = (x + m*y + Math.pow(m,2)*p1.getX()-m*p1.getY())/(1+Math.pow(m,2));
            double yNew = m*(xNew - p1.getX())+p1.getY();
            Position newPosition = new Position(xNew, yNew);
            return newPosition;
        }


    }

}
