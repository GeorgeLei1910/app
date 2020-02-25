package sample;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Path;

public class Settings {

    private static Settings single_instance = null;
    static private Button buttonBack, buttonSurvey;
    static private Rectangle rectangle = new Rectangle(0, 140, 700, 450);
    static private ComboBox comboBox;
    static private Button btnSettings;
    static private FileChooser fileChooser;
    static private Button fileChooseBtn;
    static private Path kmlFilePath;
    static private Text text;
    static private Text textKml;
    static private Text textElevation;
    static private Button showPlanOfTieAndFlight, fileChooseBtnElevation, showPlanOfTieFlight, showPlanOfFlight;
    static private Path elevFilePath;
    static private Button Help;

    static private ListView<String> listOfSurveys;
    static private ListView<String> listOfBlocks;
    static private ListView<String> listOfFlights;

    ObservableList<String> items =FXCollections.observableArrayList ();

    private Settings(StackPane layout){
        text = new Text();
        textElevation = new Text("Upload Elevation file");
        text.setText("Upload KML file");
        text.setFill(Color.WHITE);
        text.setTranslateX(-250);
        text.setTranslateY(-120);



        textElevation.setFill(Color.WHITE);
        textElevation.setStyle("-fx-font: 13 Courier;");
        textElevation.setTranslateX(-227);
        textElevation.setTranslateY(-90);

        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.WHITE);
        rectangle.setArcHeight(15);
        rectangle.setArcWidth(15);
        rectangle.setStrokeWidth(2.3);
        buttonBack = new Button("Back");
        comboBox = new ComboBox(Controller.getSurveys());
        buttonBack.setTranslateX(-330);
        buttonBack.setTranslateY(-260);
        btnSettings = new Button("Flight Plan Settings");
        btnSettings.setTranslateX(-240);
        btnSettings.setTranslateY(-50);

        showPlanOfFlight = new Button("Create and Show Plan");
        showPlanOfFlight.setTranslateX(-270);
        showPlanOfFlight.setTranslateY(0);

        showPlanOfTieFlight = new Button("Show Tie Plan");
        showPlanOfTieFlight.setTranslateX(-270);
        showPlanOfTieFlight.setTranslateY(35);


        showPlanOfTieAndFlight = new Button("Show Together");
        showPlanOfTieAndFlight.setTranslateX(-270);
        showPlanOfTieAndFlight.setTranslateY(70);


        Help = new Button("HELP");
        Help.setTranslateX(-260);
        Help.setTranslateY(-260);
        Help.setStyle("-fx-border-color: #484848; -fx-border-width: 2; " +
                "-fx-border-radius: 0 0 0 0;" +
                "-fx-background-radius: 0 0 0 0;");
        textKml = new Text();
        textKml.setTranslateX(60);
        textKml.setTranslateY(-120);
        textKml.setStyle("-fx-font: 13 Courier;");

        buttonBack.setStyle("-fx-border-color: #484848; -fx-border-width: 2; " +
                "-fx-border-radius: 20 20 20 20;" +
                "-fx-background-radius: 20  20 20 20;");
        buttonBack.setPrefWidth(73);
        comboBox.setTranslateX(300);
        comboBox.setTranslateY(-260);
        comboBox.setPrefWidth(134);
        comboBox.setMaxWidth(134);

        fileChooser = new FileChooser();
        fileChooseBtn = new Button("....");
        fileChooseBtn.setOnAction((event) -> {
            File file = fileChooser.showOpenDialog(Main.getStage());
            if (file != null) {

                kmlFilePath = file.toPath();
                textKml.setText(kmlFilePath.getFileName().toString());
            }

        });
        fileChooseBtnElevation = new Button("....");
        fileChooseBtnElevation.setOnAction((event) -> {
            File file = fileChooser.showOpenDialog(Main.getStage());
            if (file != null) {
                elevFilePath = file.toPath();
            }
        });

        fileChooseBtn.setStyle("-fx-font-size:10");
        fileChooseBtn.setMaxWidth(50);
        fileChooseBtn.setPrefWidth(50);
        fileChooseBtn.setTranslateX(-150);
        fileChooseBtn.setTranslateY(-120);

        fileChooseBtnElevation.setStyle("-fx-font-size:10");
        fileChooseBtnElevation.setMaxWidth(50);
        fileChooseBtnElevation.setPrefWidth(50);
        fileChooseBtnElevation.setTranslateX(-100);
        fileChooseBtnElevation.setTranslateY(-90);


        buttonSurvey = new Button("New Survey");
        buttonSurvey.setStyle("-fx-font-size:16");
        buttonSurvey.setTranslateX(0);
        buttonSurvey.setTranslateY(250);
        buttonBack.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {

               removeElements(layout);
               MainInterface mainInterface = MainInterface.getInstance(layout);
                mainInterface.showElements(layout);


            }
        });


        comboBox.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {

                String str = comboBox.getValue().toString();
                //str = str.substring(6, str.length());
                Controller.setCurSurveyFolder(str);
                System.out.println(Controller.getCurSurvey());

            }
        });

        Help.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                removeElements(layout);
                HelpSection helpSection = HelpSection.getInstance(layout);
                helpSection.showElems();

            }
        });



        buttonSurvey.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
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

                ok.setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event) {
                        String nameOfSurvey = name.getText();
                        if(!nameOfSurvey.equals("")){

                            comboBox.setValue(Controller.addSurvey(nameOfSurvey));
                        }
                        dialog.close();

                    }
                });

                cancel.setOnAction(new EventHandler<ActionEvent>(){
                    @Override
                    public void handle(ActionEvent event) {

                        dialog.close();

                    }
                });







            }
        });


        listOfSurveys = new ListView<>();
        listOfSurveys.setMaxHeight(200);
        listOfSurveys.setMaxWidth(150);
        listOfSurveys.setItems(Controller.getSurveys());
        listOfSurveys.setTranslateX(-35);
        listOfSurveys.setTranslateY(50);

        listOfBlocks = new ListView<>();
        listOfBlocks.setMaxHeight(200);
        listOfBlocks.setMaxWidth(100);
        listOfBlocks.setItems(Controller.getBlocks());
        listOfBlocks.setTranslateX(100);
        listOfBlocks.setTranslateY(50);


        listOfFlights = new ListView<>();
        listOfFlights.setMaxHeight(200);
        listOfFlights.setMaxWidth(60);
        listOfFlights.setItems(Controller.getFlights());
        listOfFlights.setTranslateX(220);
        listOfFlights.setTranslateY(50);

        // Corresponds to Flight Plan Settings
        btnSettings.setOnAction((event) -> {
            File filePath = new File(System.getProperty("user.dir")+"/Data/"+Controller.getCurSurvey()+"/FlightPlan/plan_settings.txt");
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

            chbDir.setTranslateX(-200);
            chbDir.setTranslateY(80);

            chbGoogleApi.setTranslateX(-190);
            chbGoogleApi.setTranslateY(100);


            Button useKMLFile = new Button("Load KML file");
            useKMLFile.setTranslateX(165);
            useKMLFile.setTranslateY(-50);

            Text elevText = new Text("Elevation Buffer");
            elevText.setTranslateX(-200);
            elevText.setTranslateY(140);

            TextField elevTxtField = new TextField();
            elevTxtField.setTranslateX(-120);
            elevTxtField.setTranslateY(140);
            elevTxtField.setMaxWidth(50);


            Rectangle rectangle = new Rectangle(300, -100, 255, 160);
            rectangle.setFill(Color.TRANSPARENT);

            rectangle.setArcWidth(15);

            rectangle.setStroke(Color.BLACK);
            rectangle.setStrokeWidth(1.5);
            rectangle.setTranslateX(160);
            rectangle.setTranslateY(-150);


            Text tieLineTxt = new Text("Tie-Line Spacing");
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
                Reader r = new InputStreamReader(ins, "UTF-8"); // leave charset out for default
                BufferedReader br = new BufferedReader(r);

                //Read everything to the settings
                while ((s = br.readLine()) != null) {
                    if(s.startsWith("Direction:")){
                        System.out.println(s);

                        String segments[] = s.split(":");
                        if(segments.length > 1){
                        String seg = segments[1];
                            Integer angle = (Integer.parseInt(seg.trim()) - 90 )* -1;
                            dir.setText(angle.toString());
                        }
                    }if(s.startsWith("Points:")){
                        System.out.println(s);

                        String segments[] = s.split(":");
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
                        String segments[] = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            txtFieldSpacing.setText(seg);
                        }

                    }if(s.startsWith("LineSpacing:")){
                        System.out.println(s);
                        String segments[] = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            txtFieldLineSpacing.setText(seg);}

                    }
                    if(s.startsWith("OvershootSurvey:")){
                        System.out.println(s);
                        String segments[] = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            fieldOvershoot.setText(seg);}

                    }
                    if(s.startsWith("OvershootBlock:")){
                        System.out.println(s);
                        String segments[] = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            fieldOvershootBlock.setText(seg);}

                    }
                    if(s.startsWith("ElevationBuffer:")){
                        //System.out.println(s);
                        String segments[] = s.split(":");
                        if(segments.length > 1){
                            String seg = segments[1];
                            elevTxtField.setText(seg);}
                    }
                    if(s.startsWith("Clockwise:")) {
                        System.out.println(s);
                        String segments[] = s.split(":");
                        if (segments.length > 1) {
                            String seg = segments[1];
                            if (seg.equals("1"))
                                chbDir.setSelected(true);
                        }
                    }
                    if(s.startsWith("Elevation:")){
                        System.out.println(s);
                        String segments[] = s.split(":");
                        String seg = segments[1];
                        System.out.println("-----)))))>"+seg.equals("1"));
                        if(seg.equals("1"))
                            chbGoogleApi.setSelected(true);

                    }if(s.startsWith("TieLineSpacing:")) {
                        System.out.println(s);
                        String segments[] = s.split(":");
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
                    }


                }
            }catch (Exception e)
            {
                System.err.println(e.getMessage()); // handle exception
            }


            btnChooseStartTie.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {

                    CanvasFlightPlan canvasFlightPlan = new CanvasFlightPlan(-2);
                    canvasFlightPlan.setInitPosition(tieSpaceFieldStartLon, tieSpaceFieldStartLat);

                }
            });


            btnCancel.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {

                    dialog.close();

                }
            });


            btnDelete.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {

                    items.remove(list.getSelectionModel().getSelectedItem());
                    list.setItems(items);

                }
            });


            btnOK.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {

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
                        out.write("TieLineSpacing:"+tieSpaceField.getText().trim());
                        out.write(   "\r\n");
                        if(tieSpaceFieldStartLat.getText().length() > 0 && tieSpaceFieldStartLon.getText().length() > 0){
                            out.write("TieStart:"+tieSpaceFieldStartLon.getText()+","+ tieSpaceFieldStartLat.getText() + "\r\n");
                        }else{
                            out.write(   "TieStart:\r\n");
                        }
                        out.close();
                    }catch(Exception e){

                    }

                    dialog.close();

                }
            });

            btnAddPos.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    if((posLon.getText().matches("^-?[0-9]\\d*(\\.\\d+)?$")) &&
                            posLat.getText().matches("^-?[0-9]\\d*(\\.\\d+)?$")){
                    items.add("(" +posLon.getText() +","+ posLat.getText() +")");
                    }

                }
            });

            btnShowFlight.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {

                    CanvasFlightPlan canvasFlightPlan = new CanvasFlightPlan(-2);
                    canvasFlightPlan.setInitPosition(posLonStart, posLatStart);
                    }


            });


            useKMLFile.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent event) {
                    if(kmlFilePath != null){
                        System.out.println("ergerv");
                        try{
                            File file = new File(kmlFilePath.toString());
                            String s;
                            InputStream ins = new FileInputStream(file);
                            Reader r = new InputStreamReader(ins, "UTF-8"); // leave charset out for default
                            BufferedReader br = new BufferedReader(r);

                            while ((s = br.readLine()) != null) {
                                s = s.trim();
                                if(s.substring(0,4).matches("^-?[0-9]\\d*(\\.\\d+)?$")){
                                    list.getItems().clear();
                                    items.clear();
                                    String[] cords = s.split(",0");
                                    for(String str : cords){

                                        String[] segs = str.split(",");
                                        //System.out.println(segs[1].trim()+" "+ segs[0].trim()+ " 0");
                                        posLon.setText(segs[0].trim());
                                        posLat.setText(segs[1].trim());
                                        btnAddPos.fire();
                                    }
                                    break;
                                }
                            }
                            }catch (IOException e){
                        }
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
            dialog.setTitle("Flight Plan Settings " + Controller.getCurSurvey());
            dialog.show();


        });
        //Corresponds to "Create and Show plan"
        showPlanOfFlight.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                String path = System.getProperty("user.dir");
                String pathPython = path + "/Package/pythontest.py";
                String command = "python " +pathPython+" -m FlightPlan" + " -f "+ path + "/Data/"+Controller.getCurSurvey() +
                        "/FlightPlan/plan_settings.txt";
                try {

                    Process p = Runtime.getRuntime().exec(command);
//                    p.waitFor();
                    //Python console log
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                    reader.close();
                }catch(Exception e){

                }
                System.out.println("P ran");
               CanvasFlightPlan canvasFlightPlan = new CanvasFlightPlan(1);
            }
        });

        showPlanOfTieFlight.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {

                CanvasFlightPlan canvasFlightPlan1 = new CanvasFlightPlan(2);

            }
        });


        showPlanOfTieAndFlight.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {

                CanvasFlightPlan canvasFlightPlan1 = new CanvasFlightPlan(3);

            }
        });



    }


    public static Settings getInstance(StackPane layout)
    {
        if (single_instance == null)
            single_instance = new Settings(layout);


        return single_instance;
    }


    public void showElements(StackPane layout){
        layout.getChildren().add(rectangle);
        layout.getChildren().add(buttonBack);
        layout.getChildren().add(comboBox);
        layout.getChildren().add(buttonSurvey);
        layout.getChildren().add(btnSettings);
        layout.getChildren().add(text);
        layout.getChildren().add(fileChooseBtn);
        layout.getChildren().add(showPlanOfFlight);
        layout.getChildren().add(textElevation);
        layout.getChildren().add(Help);

        layout.getChildren().add(fileChooseBtnElevation);
        layout.getChildren().add(textKml);
        layout.getChildren().add(showPlanOfTieFlight);
        layout.getChildren().add(showPlanOfTieAndFlight);
        /*layout.getChildren().add(listOfSurveys);
        layout.getChildren().add(listOfBlocks);
        layout.getChildren().add(listOfFlights);*/


    }


    public void removeElements(StackPane layout){
        layout.getChildren().remove(rectangle);
        layout.getChildren().remove(buttonBack);
        layout.getChildren().remove(comboBox);
        layout.getChildren().remove(buttonSurvey);
        layout.getChildren().remove(btnSettings);
        layout.getChildren().remove(text);
        layout.getChildren().remove(fileChooseBtn);
        layout.getChildren().remove(showPlanOfFlight);
        layout.getChildren().remove(textElevation);
        layout.getChildren().remove(Help);

        layout.getChildren().remove(fileChooseBtnElevation);
        layout.getChildren().remove(textKml);
        layout.getChildren().remove(showPlanOfTieFlight);
        layout.getChildren().remove(showPlanOfTieAndFlight);
       /* layout.getChildren().remove(listOfSurveys);
        layout.getChildren().remove(listOfBlocks);
        layout.getChildren().remove(listOfFlights);*/

    }







}
