package sample;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;



public class FlightPlanning {


    private static FlightPlanning single_instance = null;
    static private StackPane layout;
    private static Button btnShowBlkPlan, buttonExport, applyBtn, showFlightPlan;
    static private GraphicsContext graphics_context;
    private static String planSettingsFile;
    private static Text currentFlight, textKml, applied, txtTie;
    private static TextField lineFromTxt, lineToTxt, lineTieFromTxt, lineTieToTxt, seperateLines;
    private static CheckBox cbUseSeperateLines;
    static private Path kmlFilePath;

    private static ObservableList<String> items = FXCollections.observableArrayList ();

    static ComboBox listFlights, listBlocks, listSurveys;
    static Button createSurvey, createBlock, createFlight;
    static Button editSurvey, editBlock, editFlight;
    static Button showSurvey, showBlock, showFlight;

    private static Button showSurveyTieLines, showSurveyTieFlight, showBlockTie;

    //FUnction to build page
    private FlightPlanning(StackPane layout) {
        // Setup for Layout
        FlightPlanning.layout = layout;
        
        listSurveys = new ComboBox(Controller.getSurveys());
        listBlocks = new ComboBox(Controller.getBlocks());
        listFlights = new ComboBox(Controller.getFlights());
        //New Stuff
        createSurvey = new Button("Create New Survey");
        createBlock = new Button("Create New Block");
        createFlight = new Button("Create New Flight");
        editSurvey = new Button("Edit Selected Survey");
        editBlock = new Button("Edit Selected Block");
        editFlight = new Button("Edit Selected Flight");
        showSurvey = new Button("Show Flight Lines");
        showBlock = new Button("Show Block Flight Plan");
        showFlight = new Button("Show Selected Flight");

        showBlockTie = new Button("Show Block Tie Plan");
        
        showSurveyTieLines = new Button("Show Tie Lines");
        showSurveyTieFlight = new Button("Show Flight and Tie Lines");

        listSurveys.setTranslateX(-200);
        listBlocks.setTranslateX(0);
        listFlights.setTranslateX(200);
        listSurveys.setTranslateY(-180);
        listBlocks.setTranslateY(-180);
        listFlights.setTranslateY(-180);
        createSurvey.setTranslateX(-200);
        createBlock.setTranslateX(0);
        createFlight.setTranslateX(200);
        createSurvey.setTranslateY(-150);
        createBlock.setTranslateY(-150);
        createFlight.setTranslateY(-150);

        editSurvey.setTranslateX(-200);
        editBlock.setTranslateX(0);
        editFlight.setTranslateX(200);
        editSurvey.setTranslateY(-120);
        editBlock.setTranslateY(-120);
        editFlight.setTranslateY(-120);

        showSurvey.setTranslateX(-200);
        showBlock.setTranslateX(0);
        showFlight.setTranslateX(200);
        showSurvey.setTranslateY(-90);
        showBlock.setTranslateY(-90);
        showFlight.setTranslateY(-90);

        showSurveyTieLines.setTranslateX(-200);
        showSurveyTieLines.setTranslateY(-60);
        showSurveyTieFlight.setTranslateX(-200);
        showSurveyTieFlight.setTranslateY(-30);

        showBlockTie.setTranslateX(0);
        showBlockTie.setTranslateY(-60);
        
        
        //
        btnShowBlkPlan = new Button("Show Block Plan");
        buttonExport = new Button("Export File");

        btnShowBlkPlan.setTranslateX(-200);
        btnShowBlkPlan.setTranslateY(-120);
        btnShowBlkPlan.setStyle("-fx-font: 13 Courier;");

        currentFlight = new Text("No Flight Exists   ");
        currentFlight.setTranslateX(-220);
        currentFlight.setTranslateY(-70);
        currentFlight.setStyle("-fx-font: 13 Courier;");

        buttonExport.setTranslateX(200);
        buttonExport.setTranslateY(-30);
        buttonExport.setStyle("-fx-font: 13 Courier;");

        lineFromTxt = new TextField(); lineToTxt = new TextField();
        lineTieFromTxt = new TextField(); lineTieToTxt = new TextField();
        cbUseSeperateLines = new CheckBox("Use Single Line\n(Separate Line)"); seperateLines = new TextField();




        showFlightPlan = new Button("Show Flight Plan");
        showFlightPlan.setTranslateY(0);
        showFlightPlan.setTranslateX(-200);

        applyBtn = new Button("apply");
        applyBtn.setTranslateX(150); applyBtn.setTranslateY(-70); applyBtn.setMaxWidth(60);

        applied = new Text("Not Applied");
        applied.setTranslateX(270);
        applied.setTranslateY(-70);
        applied.setStyle("-fx-font: 13 Courier;");

        txtTie = new Text("Ties:");
        txtTie.setTranslateX(-45);
        txtTie.setTranslateY(-70);
        txtTie.setStyle("-fx-font: 13 Courier;");

        //Bind Buttons to Activation
        editSurvey.disableProperty().bind(listSurveys.valueProperty().isNull());
        showSurvey.disableProperty().bind(listSurveys.valueProperty().isNull());
        showSurveyTieFlight.disableProperty().bind(listSurveys.valueProperty().isNull());
        showSurveyTieLines.disableProperty().bind(listSurveys.valueProperty().isNull());
        listBlocks.disableProperty().bind(listSurveys.valueProperty().isNull());
        createBlock.disableProperty().bind(listSurveys.valueProperty().isNull());
        editBlock.disableProperty().bind(listBlocks.valueProperty().isNull());
        showBlock.disableProperty().bind(listBlocks.valueProperty().isNull());
        showBlockTie.disableProperty().bind(listBlocks.valueProperty().isNull());
        listFlights.disableProperty().bind(listBlocks.valueProperty().isNull());
        createFlight.disableProperty().bind(listBlocks.valueProperty().isNull());
        editFlight.disableProperty().bind(listFlights.valueProperty().isNull());
        showFlight.disableProperty().bind(listFlights.valueProperty().isNull());
        buttonExport.disableProperty().bind(listFlights.valueProperty().isNull());

        listSurveys.setOnAction(event -> {
            String curSurv = listSurveys.getValue().toString();
            Controller.setCurSurveyFolder(curSurv);
        });

        listBlocks.setOnAction(event -> {
            try{
                String str = listBlocks.getValue().toString();
                String num = "0";
                for(int i = 0; i < str.length(); i++){
                    char ch2 = str.charAt(i);
                    if(ch2 == ','){
                        num = str.substring(1, i);
                        break;
                    }
                }
                Controller.setCurBlockFolder(Integer.parseInt(num), str);
            }catch(NullPointerException e){

            }
        });
        listFlights.setOnAction(event -> {
            try{
                String str = listFlights.getValue().toString();
                String num = "0";
                if (!str.equals(""))
                    num = str.substring(1);

                Controller.setCurFlightFolder(Integer.parseInt(num));

            }catch(NullPointerException e){

            }
        });


        createSurvey.setOnAction(event -> {
            final Stage dialog = new Stage();

            TextField name = new TextField();
            name.setMaxWidth(200);
            name.setPrefWidth(200);
            name.setTranslateX(0);
            name.setTranslateY(-10);

            Button ok = new Button("OK");
            ok.setTranslateX(40);
            ok.setTranslateY(50);
            ok.setPrefWidth(60);
            Button cancel = new Button("Cancel");
            cancel.setTranslateX(-40);
            cancel.setTranslateY(50);

            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(Controller.getPrimaryStage());
            StackPane popUpLayout =  new StackPane();
            popUpLayout.setStyle("-fx-background-color: #474747;");
            Scene popUpScene = new Scene(popUpLayout, 400, 150);
            dialog.setScene(popUpScene);
            dialog.setTitle("Name The Survey");
            dialog.show();

            popUpLayout.getChildren().add(name);
            popUpLayout.getChildren().add(ok);
            popUpLayout.getChildren().add(cancel);

            ok.setOnAction(event1 -> {
                String nameOfSurvey = name.getText();
                if(!nameOfSurvey.equals("")) listSurveys.setValue(Controller.addSurvey(nameOfSurvey));
                dialog.close();
                editSurvey.fire();
            });
            cancel.setOnAction(event1 -> {
                dialog.close();
            });
        });
        editSurvey.setOnAction(event -> {
            File filePath = new File(System.getProperty("user.dir")+ Controller.getPathToSurvey() + "/FlightPlan" + Controller.getPrefixToSurvey() + "-plan_settings.txt");
            final Stage dialog = new Stage();
            items.clear();

            Text txtOvershoot = new Text("Overshoot Survey");
            Text txtOvershootBlock = new Text("Overshoot Block");
            TextField fieldOvershoot = new TextField();
            TextField fieldOvershootBlock = new TextField();
            txtOvershoot.setTranslateX(-205);
            txtOvershoot.setTranslateY(-100);
            txtOvershootBlock.setTranslateX(-210);
            txtOvershootBlock.setTranslateY(-70);
            fieldOvershoot.setTranslateX(-100);
            fieldOvershoot.setTranslateY(-100);
            fieldOvershootBlock.setTranslateX(-100);
            fieldOvershootBlock.setTranslateY(-70);
            fieldOvershoot.setMaxWidth(50);
            fieldOvershootBlock.setMaxWidth(50);

            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(Controller.getPrimaryStage());
            Button btnShowFlight = new Button("choose start");
            Text txtSpacing = new Text("Waypoint Spacing (m)");
            Text txtLineSpacing = new Text("Line Spacing (m)");
            TextField txtFieldSpacing = new TextField();
            TextField txtFieldLineSpacing = new TextField();
            txtFieldSpacing.setMaxWidth(50);
            txtFieldLineSpacing.setMaxWidth(50);
            txtSpacing.setTranslateX(-200);
            txtSpacing.setTranslateY(-170);
            txtFieldSpacing.setTranslateX(-110);
            txtFieldSpacing.setTranslateY(-170);
            txtFieldLineSpacing.setTranslateX(-110);
            txtFieldLineSpacing.setTranslateY(-140);
            txtLineSpacing.setTranslateX(-220);
            txtLineSpacing.setTranslateY(-140);
            btnShowFlight.setTranslateX(15);
            btnShowFlight.setTranslateY(45);
            StackPane popUplayout =  new StackPane();
            Scene dialogScene = new Scene(popUplayout, 600, 500);
            Text textPosition = new Text("Position: ");
            Text textPositionStart = new Text("Starting Position: ");
            TextField posLon = new TextField();
            TextField posLat = new TextField();
            TextField posLatStart = new TextField();
            TextField posLonStart = new TextField();
            posLon.setPromptText("Lon");
            posLat.setPromptText("Lat");
            posLon.setPrefWidth(50);
            posLon.setMaxWidth(50);
            posLat.setPrefWidth(50);
            posLat.setMaxWidth(50);
            posLat.setTranslateX(-180);
            posLat.setTranslateY(-30);
            posLon.setTranslateX(-120);
            posLon.setTranslateY(-30);
            textPosition.setTranslateX(-240);
            textPosition.setTranslateY(-30);



            posLonStart.setPromptText("Lon");
            posLatStart.setPromptText("Lat");
            posLonStart.setPrefWidth(50);
            posLonStart.setMaxWidth(50);
            posLatStart.setPrefWidth(50);
            posLatStart.setMaxWidth(50);
            posLatStart.setTranslateX(-120);
            posLatStart.setTranslateY(45);
            posLonStart.setTranslateX(-60);
            posLonStart.setTranslateY(45);
            textPositionStart.setTranslateX(-210);
            textPositionStart.setTranslateY(45);


            Text txtArrow = new Text("--->");
            txtArrow.setTranslateX(-60);
            txtArrow.setTranslateY(-30);

            Button btnDelete = new Button("Delete Selected");
            Button btnAddPos = new Button("ADD");
            btnAddPos.setTranslateX(-20);
            btnAddPos.setTranslateY(-30);
            ListView<String> list = new ListView<String>();
            list.setMaxHeight(160);
            list.setMaxWidth(150);
            list.setItems(items);
            list.setTranslateX(165);
            list.setTranslateY(50);
            btnDelete.setTranslateX(165);
            btnDelete.setTranslateY(145);
            Text text = new Text("Direction in degrees");
            text.setTranslateX(-170);
            text.setTranslateY(-210);
            Button btnOK = new Button("OK");
            Button btnCancel = new Button("Cancel");
            TextField dir = new TextField();
            dir.setTranslateX(-70);
            dir.setTranslateY(-210);
            dir.setPrefWidth(50);
            dir.setMaxWidth(50);
            btnOK.setTranslateX(60);
            btnOK.setTranslateY(210);
            btnCancel.setTranslateX(-60);
            btnCancel.setTranslateY(210);
            dir.getText();

            CheckBox chbDir = new CheckBox("Clockwise planning");
            CheckBox chbGoogleApi = new CheckBox("Google API for elevation");
            CheckBox chbTwoWay = new CheckBox("Render Both Ways");

            chbDir.setTranslateX(-200);
            chbDir.setTranslateY(80);

            chbGoogleApi.setTranslateX(-190);
            chbGoogleApi.setTranslateY(100);

            chbTwoWay.setTranslateX(-190);
            chbTwoWay.setTranslateY(120);

            Button useKMLFile = new Button("Load KML file");
            useKMLFile.setTranslateX(165);
            useKMLFile.setTranslateY(-50);

            Text elevText = new Text("Elevation Buffer");
            elevText.setTranslateX(-200);
            elevText.setTranslateY(160);

            TextField elevTxtField = new TextField();
            elevTxtField.setTranslateX(-120);
            elevTxtField.setTranslateY(160);
            elevTxtField.setMaxWidth(50);


            Rectangle rectangle = new Rectangle(300, -100, 255, 160);
            rectangle.setFill(Color.TRANSPARENT);

            rectangle.setArcWidth(15);

            rectangle.setStroke(Color.BLACK);
            rectangle.setStrokeWidth(1.5);
            rectangle.setTranslateX(160);
            rectangle.setTranslateY(-150);


            Text tieLineTxt = new Text("Tie-Line Spacing (m)");
            tieLineTxt.setTranslateX(100);
            tieLineTxt.setTranslateY(-190);
            TextField tieSpaceField = new TextField();
            tieSpaceField.setTranslateX(190);
            tieSpaceField.setTranslateY(-190);
            tieSpaceField.setMaxWidth(50);

            Text tieLineTxtStart = new Text("Start Position");
            tieLineTxtStart.setTranslateX(90);
            tieLineTxtStart.setTranslateY(-135);
            TextField tieSpaceFieldStartLon = new TextField();
            tieSpaceFieldStartLon.setTranslateX(165);
            tieSpaceFieldStartLon.setTranslateY(-135);
            TextField tieSpaceFieldStartLat = new TextField();
            tieSpaceFieldStartLat.setTranslateX(210);
            tieSpaceFieldStartLat.setTranslateY(-135);
            tieSpaceFieldStartLat.setMaxWidth(40);
            tieSpaceFieldStartLon.setMaxWidth(40);

            Button btnChooseStartTie = new Button("choose start");
            btnChooseStartTie.setTranslateX(135);
            btnChooseStartTie.setTranslateY(-100);


            try{

                String s;
                InputStream ins = new FileInputStream(filePath);
                Reader r = new InputStreamReader(ins, StandardCharsets.UTF_8); // leave charset out for default
                BufferedReader br = new BufferedReader(r);

                //Read everything to the settings
                while ((s = br.readLine()) != null) {
                    if(s.startsWith("Direction:")){
                        System.out.println(s);

                        String[] segments = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            Integer angle = (Integer.parseInt(seg.trim()) - 90 )* -1;
                            dir.setText(angle.toString());
                        }
                    }if(s.startsWith("Points:")){
                        System.out.println(s);

                        String[] segments = s.split(":");
                        for(int i = 1; i < segments.length ; i++)
                            items.add(segments[i]);
                    }if(s.startsWith("Start:")){
                        System.out.println(s);

                        String[] segments = s.split(":");
                        System.out.println(segments.length);
                        if(segments.length > 1){
                            String[] newSegs = segments[1].split(",");
                            if(newSegs.length > 0){
                                System.out.println(newSegs.length);
                                posLonStart.setText(newSegs[0]);
                                posLatStart.setText(newSegs[1]);
                            }
                        }

                    }
                    if(s.startsWith("Spacing:")) {
                        System.out.println(s);
                        String[] segments = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            txtFieldSpacing.setText(seg);
                        }

                    }if(s.startsWith("LineSpacing:")){
                        System.out.println(s);
                        String[] segments = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            txtFieldLineSpacing.setText(seg);}

                    }
                    if(s.startsWith("OvershootSurvey:")){
                        System.out.println(s);
                        String[] segments = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            fieldOvershoot.setText(seg);}

                    }
                    if(s.startsWith("OvershootBlock:")){
                        System.out.println(s);
                        String[] segments = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            fieldOvershootBlock.setText(seg);}

                    }
                    if(s.startsWith("ElevationBuffer:")){
                        //System.out.println(s);
                        String[] segments = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            elevTxtField.setText(seg);}
                    }
                    if(s.startsWith("Clockwise:")) {
                        System.out.println(s);
                        String[] segments = s.split(":");
                        if (segments.length > 1) {
                            String seg = segments[1];
                            if (seg.equals("1"))
                                chbDir.setSelected(true);
                        }
                    }
                    if(s.startsWith("Elevation:")){
                        System.out.println(s);
                        String[] segments = s.split(":");
                        String seg = segments[1];
                        System.out.println("-----)))))>"+seg.equals("1"));
                        if(seg.equals("1"))
                            chbGoogleApi.setSelected(true);

                    }if(s.startsWith("TieLineSpacing:")) {
                        System.out.println(s);
                        String[] segments = s.split(":");
                        String seg = segments[1];
                        tieSpaceField.setText(seg);
                    }if(s.startsWith("TieStart:")){
                        System.out.println(s);
                        String[] segments = s.split(":");
                        System.out.println(segments.length);
                        if(segments.length > 1){
                            String[] newSegs = segments[1].split(",");
                            if(newSegs.length > 0){
                                System.out.println(newSegs.length);
                                tieSpaceFieldStartLon.setText(newSegs[0]);
                                tieSpaceFieldStartLat.setText(newSegs[1]);
                            }
                        }
                        break;
                    }if(s.startsWith("TwoWay:")){
                        System.out.println(s);
                        String[] segments = s.split(":");
                        String seg = segments[1];
                        System.out.println("-----)))))>"+seg.equals("1"));
                        if(seg.equals("1"))
                            chbTwoWay.setSelected(true);
                    }
                }
            }catch (Exception e) {
                System.err.println(e.getMessage()); // handle exception
            }


            btnChooseStartTie.setOnAction(event14 -> {
                if(items.size() < 3){
                    AllAlerts.noSurveyBoundary();
                }else {
                    CanvasFlightPlan canvasFlightPlan = new CanvasFlightPlan(-3);
                    canvasFlightPlan.setInitPosition(tieSpaceFieldStartLon, tieSpaceFieldStartLat);
                }
            });
            btnShowFlight.setOnAction(event13 -> {
                if(items.size() < 3){
                    AllAlerts.noSurveyBoundary();
                }else {
                    CanvasFlightPlan canvasFlightPlan = new CanvasFlightPlan(-3);
                    canvasFlightPlan.setInitPosition(posLonStart, posLatStart);
                }
            });

            btnCancel.setOnAction(event15 -> dialog.close());
            btnDelete.setOnAction(event16 -> {
                items.remove(list.getSelectionModel().getSelectedItem());
                list.setItems(items);
            });
            btnOK.setOnAction(event17 -> {
                try {
                    PrintWriter out = new PrintWriter(filePath);
                    out.flush();
                    System.out.println(filePath.getAbsolutePath());
                    out.write("Direction:" + (Integer.parseInt(dir.getText().trim()) - 90) * -1+"\r\n");
                    out.write("Points:");
                    for(String itm : items){
                        out.write( itm + ":");
                        System.out.println(itm);
                    }
                    out.write(   "\r\n");
                    if(posLonStart.getText().length() > 0 && posLatStart.getText().length() > 0){
                        out.write("Start:"+posLonStart.getText().trim()+","+ posLatStart.getText().trim() + "\r\n");
                    }else{
                        out.write(   "Start:\r\n");
                    }
                    out.write("Spacing:"+txtFieldSpacing.getText().trim());
                    out.write(   "\r\n");
                    out.write("LineSpacing:"+txtFieldLineSpacing.getText().trim());
                    out.write(   "\r\n");
                    out.write("OvershootSurvey:"+fieldOvershoot.getText().trim());
                    out.write(   "\r\n");
                    out.write("OvershootBlock:"+fieldOvershootBlock.getText().trim());
                    out.write(   "\r\n");
                    out.write("ElevationBuffer:"+elevTxtField.getText().trim());
                    out.write(   "\r\n");
                    out.write("Clockwise:"+((chbDir.isSelected()) ? "1" : "-1"));
                    out.write(   "\r\n");
                    out.write("Elevation:"+((chbGoogleApi.isSelected()) ? "1" : "0"));
                    out.write(   "\r\n");
                    out.write("TwoWay:"+((chbTwoWay.isSelected()) ? "1" : "0"));
                    out.write(   "\r\n");
                    out.write("TieLineSpacing:"+tieSpaceField.getText().trim());
                    out.write(   "\r\n");
                    if(tieSpaceFieldStartLat.getText().length() > 0 && tieSpaceFieldStartLon.getText().length() > 0){
                        out.write("TieStart:"+tieSpaceFieldStartLon.getText()+","+ tieSpaceFieldStartLat.getText() + "\r\n");
                    }else{
                        out.write(   "TieStart:\r\n");
                    }
                    out.close();
                    String path = System.getProperty("user.dir").replace('\\', '/') ;
                    String pathPython = path.replace('\\', '/') + "/Package/pythontest.py";
                    String command = "python " +pathPython+" -m FlightPlan" + " -f "+ path + "/Data/"+Controller.getCurSurvey() +
                            "/FlightPlan" + Controller.getPrefixToSurvey() + "-plan_settings.txt";
                    System.out.println(command);
                    try {
                        Process p = Runtime.getRuntime().exec(command);
                        Controller.pythonConsole(p);
//                    p.waitFor();
                        //Python console log
                    }catch(Exception e){

                    }
                    System.out.println("P ran");

                }catch(Exception e){

                }
                dialog.close();
            });

            btnAddPos.setOnAction(event18 -> {
                if((posLon.getText().matches("^-?[0-9]\\d*(\\.\\d+)?$")) &&
                    posLat.getText().matches("^-?[0-9]\\d*(\\.\\d+)?$")){
                    items.add(posLat.getText() +","+ posLon.getText());
                }
            });

            useKMLFile.setOnAction(event12 -> {
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(Main.getStage());
                if (file != null) {
                    kmlFilePath = file.toPath();
//                    textKml.setText(kmlFilePath.getFileName().toString());
                }
                if(kmlFilePath != null){
                    System.out.println("ergerv");
                    try{
                        file = new File(kmlFilePath.toString());
                        String s;
                        InputStream ins = new FileInputStream(file);
                        Reader r = new InputStreamReader(ins, StandardCharsets.UTF_8); // leave charset out for default
                        BufferedReader br = new BufferedReader(r);

                        while ((s = br.readLine()) != null) {
                            s = s.trim();
                            if(s.contains("coordinates")){
                                s = br.readLine();
                                list.getItems().clear();
                                items.clear();
                                while(!s.contains("coordinates")) {
                                    s = s.trim();
                                    String[] cords = s.split(",0");
                                    for (String str : cords) {
                                        String[] segs = str.split(",");
                                        //System.out.println(segs[1].trim()+" "+ segs[0].trim()+ " 0");
                                        posLon.setText(segs[0].trim());
                                        posLat.setText(segs[1].trim());
                                        btnAddPos.fire();
                                    }
                                    s = br.readLine();
                                }
                                break;
                            }
                        }
                    }catch (IOException e){
                    }
                }
            });

            btnOK.setPrefWidth(80);
            btnCancel.setPrefWidth(80);
            popUplayout.getChildren().add(text);
            popUplayout.getChildren().add(btnCancel);
            popUplayout.getChildren().add(btnOK);
            popUplayout.getChildren().add(dir);
            popUplayout.getChildren().add(list);
            popUplayout.getChildren().add(textPosition);
            popUplayout.getChildren().add(posLat);
            popUplayout.getChildren().add(posLon);
            popUplayout.getChildren().add(btnDelete);
            popUplayout.getChildren().add(txtArrow);
            popUplayout.getChildren().add(btnAddPos);
            popUplayout.getChildren().add(btnShowFlight);
            popUplayout.getChildren().add(posLonStart);
            popUplayout.getChildren().add(posLatStart);
            popUplayout.getChildren().add(textPositionStart);
            popUplayout.getChildren().add(txtSpacing);
            popUplayout.getChildren().add(txtFieldSpacing);
            popUplayout.getChildren().add(fieldOvershoot);
            popUplayout.getChildren().add(fieldOvershootBlock);
            popUplayout.getChildren().add(txtOvershoot);
            popUplayout.getChildren().add(txtOvershootBlock);
            popUplayout.getChildren().add(chbGoogleApi);
            popUplayout.getChildren().add(chbDir);
            popUplayout.getChildren().add(chbTwoWay);
            popUplayout.getChildren().add(txtFieldLineSpacing);
            popUplayout.getChildren().add(txtLineSpacing);
            popUplayout.getChildren().add(useKMLFile);
            popUplayout.getChildren().add(elevText);
            popUplayout.getChildren().add(elevTxtField);
            popUplayout.getChildren().add(rectangle);
            popUplayout.getChildren().add(tieLineTxt);
            popUplayout.getChildren().add(tieSpaceField);
            popUplayout.getChildren().add(tieLineTxtStart);
            popUplayout.getChildren().add(tieSpaceFieldStartLat);
            popUplayout.getChildren().add(tieSpaceFieldStartLon);
            popUplayout.getChildren().add(btnChooseStartTie);

            posLat.getParent().requestFocus();
            posLon.getParent().requestFocus();
            dialog.setScene(dialogScene);
            dialog.setTitle("Survey Plan Settings " + Controller.getCurSurvey());
            dialog.show();
        });

        showSurvey.setOnAction(event -> {
            CanvasFlightPlan canvasFlightPlan = new CanvasFlightPlan(1);
        });
        showSurveyTieLines.setOnAction(event -> {
            CanvasFlightPlan canvasFlightPlan = new CanvasFlightPlan(2);
        });
        showSurveyTieFlight.setOnAction(event -> {
            CanvasFlightPlan canvasFlightPlan = new CanvasFlightPlan(3);
        });

        createBlock.setOnAction(event -> {
            listBlocks.setValue(Controller.addBlocks());
        });
        editBlock.setOnAction(event -> {
            CanvasFlightPlan canvasFlightPlan = new CanvasFlightPlan(0);
        });
        showBlock.setOnAction(event -> {
            CanvasFlightPlan canvasFlightPlan = new CanvasFlightPlan(-1);
        });
        showBlockTie.setOnAction(event -> {
            CanvasFlightPlan canvasFlightPlan = new CanvasFlightPlan(4);
        });

        createFlight.setOnAction(event -> {
            listFlights.setValue(Controller.addFlight());
        });

        editFlight.setOnAction(event -> {
//            updateFlightPlanInfo();
            final Stage dialog = new Stage();
//                downloadFiles.disableProperty().bind(downloadableFiles.valueProperty().isNull());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(Controller.getPrimaryStage());
            StackPane popUpLayout = new StackPane();
            popUpLayout.setStyle("-fx-background-color: #474747;");
            Scene popUpScene = new Scene(popUpLayout, 400, 200);
            dialog.setScene(popUpScene);
            dialog.setTitle("Edit Flight: " + Controller.getCurFlight());
            dialog.show();

            Text txtFlight, txtTie, from, to;
//            TextField lineFromTxt, lineToTxt, lineTieFromTxt, lineTieToTxt, seperateLines;
            Button efOK, efCancel;
//            CheckBox cbUseSeperateLines;

            txtFlight = new Text("Flight Lines"); txtTie = new Text("Tie Lines");
            from = new Text("From"); to = new Text("To");

            efOK = new Button("OK (Apply)"); efCancel = new Button("Cancel");

            txtFlight.setFill(Color.WHITE); txtTie.setFill(Color.WHITE); from.setFill(Color.WHITE); to.setFill(Color.WHITE);
            cbUseSeperateLines.setTextFill(Color.WHITE);

            from.setTranslateX(-50); from.setTranslateY(-80); to.setTranslateX(50); to.setTranslateY(-80);
            txtFlight.setTranslateX(-150); txtFlight.setTranslateY(-40); txtTie.setTranslateX(-150);
            lineFromTxt.setTranslateX(-50); lineFromTxt.setTranslateY(-40); lineToTxt.setTranslateX(50); lineToTxt.setTranslateY(-40);
            lineTieFromTxt.setTranslateX(-50); lineTieToTxt.setTranslateX(50);
            cbUseSeperateLines.setTranslateX(-100); cbUseSeperateLines.setTranslateY(40);
            seperateLines.setTranslateX(50); seperateLines.setTranslateY(40);

            lineFromTxt.setMaxWidth(50); lineToTxt.setMaxWidth(50);
            lineTieFromTxt.setMaxWidth(50); lineTieToTxt.setMaxWidth(50); seperateLines.setMaxWidth(50);

            efOK.setTranslateX(-50); efOK.setTranslateY(80); efCancel.setTranslateX(50); efCancel.setTranslateY(80);
            efOK.setMaxWidth(90); efCancel.setMaxWidth(90);

            popUpLayout.getChildren().add(from);
            popUpLayout.getChildren().add(to);
            popUpLayout.getChildren().add(txtFlight);
            popUpLayout.getChildren().add(txtTie);
            popUpLayout.getChildren().add(lineFromTxt);
            popUpLayout.getChildren().add(lineToTxt);
            popUpLayout.getChildren().add(lineTieFromTxt);
            popUpLayout.getChildren().add(lineTieToTxt);
            popUpLayout.getChildren().add(cbUseSeperateLines);
            popUpLayout.getChildren().add(seperateLines);
            popUpLayout.getChildren().add(efOK);
            popUpLayout.getChildren().add(efCancel);

            efCancel.setOnAction(event1 -> {
                dialog.close();
            });
            efOK.setOnAction(event1 -> {
                applyBtn.fire();
                dialog.close();
            });
        });
        showFlight.setOnAction(event -> {
            CanvasFlightPlan canvasFlightPlan = new CanvasFlightPlan(-2);
        });


        // Sets function of Buttons
        // Show Block Plan of the flight
        btnShowBlkPlan.setOnAction((event) -> {
            CanvasFlightPlan canvasFlightPlanBlock = new CanvasFlightPlan(0);
        });

        // Show Flight Plan after Line and Tie from tos are Applied
        showFlightPlan.setOnAction((event) -> {
            CanvasFlightPlan canvasFlightPlanBlock = new CanvasFlightPlan(-1);
        });

        // Exports Flight Planner into .txt file in Export folder
        buttonExport.setOnAction((event) -> {
            String curPath = System.getProperty("user.dir").replace('\\', '/')+"/Exports";
            File  exportedFolder = new File(curPath);
            exportedFolder.mkdirs();
            String srcPath = Controller.getCurDataFolder();
            File folder = new File(srcPath);
            String parFolder = folder.getParentFile().getName();
            String s = "", from= "", to= "", isSeparate= "", line= "", isApplied= "";
            String filePath = folder.getParent() + "/flight_plan/"+ Controller.getPrefixToFlight() +"-waypoints.txt";
            InputStream ins = null;
            try{
                String fpPath = System.getProperty("user.dir")+ Controller.getPathToFlight() +"/flight_plan"+ Controller.getPrefixToFlight() + "-flightPlan.txt";
                System.out.println(fpPath);
                ins = new FileInputStream(fpPath);
                Reader r = new InputStreamReader(ins, StandardCharsets.UTF_8); // leave charset out for default
                BufferedReader br = new BufferedReader(r);
                System.out.println(filePath);
                while ((s = br.readLine()) != null) {
                    if(s.startsWith("From:")){
                        String[] segments = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            from = seg;
                        }
                    }
                    if(s.startsWith("To:")) {
                        String[] segments = s.split(":");
                        if(segments.length > 1) {
                            String seg = segments[1];
                            to = seg;
                        }
                    }if(s.startsWith("useSeperateLines:")){
                        String[] segments = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            isSeparate = seg;
                        }
                    }if(s.startsWith("seperateLines:")){
                        String[] segments = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            line = seg;
                        }
                    }
                    if(s.startsWith("applyOrNot:")){
                        String[] segments = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            isApplied = seg;
                        }
                    }
                }
                br.close();
            }catch (Exception e){
                lineFromTxt.setText("");
                lineToTxt.setText("");
                lineTieFromTxt.setText("");
                lineTieToTxt.setText("");
                applied.setText("not applied");
                e.printStackTrace();
            }

            if(parFolder.startsWith("Flight")) {
                Path src = Paths.get(filePath);
                System.out.println(src);
                String[] surveyName = Controller.getCurSurvey().split("_");

                String destPath = "";
                if(isSeparate.equals("0")){
                    destPath = curPath + Controller.getPrefixToFlight() + "-L"+ from + "L"+ to +"-waypoints.txt";
                }else{
                    destPath = curPath + Controller.getPrefixToFlight()+ "-L"+ line + "-waypoints.txt";
                }
                Path dest = Paths.get(destPath);
                System.out.println(dest);
            try{
                Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);

                }catch (Exception e){

                }
            }
        });

        cbUseSeperateLines.setOnAction((event) -> {
            if(cbUseSeperateLines.isSelected()){
                seperateLines.setDisable(false);
                lineToTxt.setDisable(true);
                lineFromTxt.setDisable(true);
            }else{
                seperateLines.setDisable(true);
                lineToTxt.setDisable(false);
                lineFromTxt.setDisable(false);
            }
        });

        //Apply button runs python createFlight
        applyBtn.setOnAction((event) -> {
            applied.setText("Applied");
            String toUseSepereatLines = "0";
            if(cbUseSeperateLines.isSelected()){
                toUseSepereatLines = "1";
            }
            String lft = lineFromTxt.getText().trim(), ltt = lineToTxt.getText().trim(), ltft = lineTieFromTxt.getText().trim(), lttt = lineTieToTxt.getText().trim();
            Controller.updateFilePlanSetting(lft, ltt, ltft, lttt, toUseSepereatLines, seperateLines.getText().trim(),
                    "Applied",
                    Controller.getCurFlight());

            String path = System.getProperty("user.dir").replace('\\', '/');
            String createFlightFilePath = path + Controller.getPathToFlight() +"/flight_plan" + Controller.getPrefixToFlight() + "-flightPlan.txt";

            String pathPython = path + "/Package/pythontest.py";
            String command = "python " + pathPython + " -m CreateFlight -f " + createFlightFilePath;
            System.out.println(command);
            try {
                //Python code runs here
                Process p = Runtime.getRuntime().exec(command);
                Controller.pythonConsole(p);
//                p.waitFor();
                //Python console log
            }catch(Exception e){

            }
        });

    }
    // Gets the single instance of Flight Planning page
    public static FlightPlanning getInstance(StackPane layout) {
        if (single_instance == null)
            single_instance = new FlightPlanning(layout);
        return single_instance;
    }

    //Updates FlightPlanInfo when Flight is switched over
    public static void updateFlightPlanInfo(){
        lineToTxt.setDisable(false);
        lineFromTxt.setDisable(false);
        applied.setDisable(false);
        showFlightPlan.setDisable(false );
        applyBtn.setDisable(false);
        lineTieFromTxt.setDisable(false);
        lineTieToTxt.setDisable(false);
        btnShowBlkPlan.setDisable(false);
        cbUseSeperateLines.setDisable(false);

        if(Controller.getCurBlock() == 0){
            btnShowBlkPlan.setDisable(true);
        }
        if(Controller.getCurFlight() == 0){
            currentFlight.setText("Flight Not Chosen     ");
            lineToTxt.setText("");
            lineFromTxt.setText("");
            applied.setText("Not applied");
            lineToTxt.setDisable(true);
            lineFromTxt.setDisable(true);
            cbUseSeperateLines.setDisable(true);
            seperateLines.setDisable(true);
            applied.setDisable(true);
            showFlightPlan.setDisable(true);
            applyBtn.setDisable(true);
            lineTieFromTxt.setDisable(true);
            lineTieToTxt.setDisable(true);
            return;
        }else{
            currentFlight.setText("Flight"+Controller.getCurFlight()+ "    ,Lines: ");
        }

        try{
            String filePath = System.getProperty("user.dir")+"/Data/" + Controller.getCurSurvey()+"/Block" +
                    Controller.getCurBlock() + "/Flight"+ Controller.getCurFlight()+"/flight_plan" + Controller.getPrefixToFlight() +"-flightPlan.txt";
            System.out.println(filePath);
            String s;
            InputStream ins = new FileInputStream(filePath);
            Reader r = new InputStreamReader(ins, StandardCharsets.UTF_8); // leave charset out for default
            BufferedReader br = new BufferedReader(r);
            System.out.println(filePath);
            while ((s = br.readLine()) != null) {
                if(s.startsWith("From:")){
                    String[] segments = s.split(":");
                    if(segments.length > 1){
                        String seg = segments[1];
                        lineFromTxt.setText(seg);
                    }else{
                        lineFromTxt.setText("");
                    }
                }
                if(s.startsWith("To:")) {
                    String[] segments = s.split(":");
                    if(segments.length > 1) {
                        String seg = segments[1];
                        lineToTxt.setText(seg);
                    }else{
                        lineToTxt.setText("");
                    }


                }if(s.startsWith("fromTie:")) {
                    String[] segments = s.split(":");
                    if(segments.length > 1){
                        String seg = segments[1];
                        lineTieFromTxt.setText(seg);
                    }else{
                        lineTieFromTxt.setText("");
                    }

                }if(s.startsWith("toTie:")) {
                    String[] segments = s.split(":");
                    if (segments.length > 1) {
                        String seg = segments[1];
                        lineTieToTxt.setText(seg);
                    } else {
                        lineTieToTxt.setText("");
                    }

                }if(s.startsWith("useSeperateLines:")){
                    String[] segments = s.split(":");
                    if(segments.length > 1){
                        String seg = segments[1];
                        System.out.println(seg);
                        if (seg.equals("1")){
                            cbUseSeperateLines.setSelected(true);
                            seperateLines.setDisable(false);
                            lineFromTxt.setDisable(true);
                            lineToTxt.setDisable(true);
                        }else {
                            cbUseSeperateLines.setSelected(false);
                            seperateLines.setDisable(true);
                            lineFromTxt.setDisable(false);
                            lineToTxt.setDisable(false);
                        }
                    }
                }if(s.startsWith("seperateLines:")){
                    String[] segments = s.split(":");
                    if(segments.length > 1){
                        String seg = segments[1];
                        seperateLines.setText(seg);
                    }else {
                        seperateLines.setText("");
                    }
                }
                if(s.startsWith("applyOrNot:")){
                    String[] segments = s.split(":");
                    if(segments.length > 1){
                        String seg = segments[1];
                        applied.setText(seg);
                    }else{
                        applied.setText("Not Applied");
                    }
                }
            }
            br.close();
        }catch (Exception e){
            lineFromTxt.setText("");
            lineToTxt.setText("");
            lineTieFromTxt.setText("");
            lineTieToTxt.setText("");
            applied.setText("not applied");
            e.printStackTrace();
        }
    }
    // Puts elements when Flight Planning tab is selected
    public void showElements(){
        layout.getChildren().add(listFlights);
        layout.getChildren().add(listBlocks);
        layout.getChildren().add(listSurveys);
        layout.getChildren().add(createFlight);
        layout.getChildren().add(createBlock);
        layout.getChildren().add(createSurvey);
        layout.getChildren().add(editFlight);
        layout.getChildren().add(editBlock);
        layout.getChildren().add(editSurvey);
        layout.getChildren().add(showFlight);
        layout.getChildren().add(showBlock);
        layout.getChildren().add(showSurvey);
        layout.getChildren().add(showSurveyTieFlight);
        layout.getChildren().add(showSurveyTieLines);
        layout.getChildren().add(buttonExport);
        layout.getChildren().add(showBlockTie);
    }
    // Removes the buttons and stuff when Pane transitions
    public void removeElements(){
        layout.getChildren().remove(listFlights);
        layout.getChildren().remove(listBlocks);
        layout.getChildren().remove(listSurveys);
        layout.getChildren().remove(createFlight);
        layout.getChildren().remove(createBlock);
        layout.getChildren().remove(createSurvey);
        layout.getChildren().remove(editFlight);
        layout.getChildren().remove(editBlock);
        layout.getChildren().remove(editSurvey);
        layout.getChildren().remove(showFlight);
        layout.getChildren().remove(showBlock);
        layout.getChildren().remove(showSurvey);
        layout.getChildren().remove(showSurveyTieFlight);
        layout.getChildren().remove(showSurveyTieLines);
        layout.getChildren().remove(buttonExport);
        layout.getChildren().remove(showBlockTie);
    }

    public static ObservableList getCoordinates(){return items;}

}
