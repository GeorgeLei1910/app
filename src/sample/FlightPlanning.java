package sample;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.io.*;
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
    private static Text currentFlight, arrow, applied, txtTie;
    private static TextField lineFromTxt, lineToTxt, lineTieFromTxt, lineTieToTxt, seperateLines;
    private static CheckBox cbUseSeperateLines;

    //FUnction to build page
    private FlightPlanning(StackPane layout) {
        // Setup for Layout
        this.layout = layout;
        btnShowBlkPlan = new Button("Show Block Plan");
        buttonExport = new Button("Export File");

        btnShowBlkPlan.setTranslateX(-200);
        btnShowBlkPlan.setTranslateY(-120);
        btnShowBlkPlan.setStyle("-fx-font: 13 Courier;");

        currentFlight = new Text("No Flight Exists   ");
        currentFlight.setTranslateX(-220);
        currentFlight.setTranslateY(-70);
        currentFlight.setStyle("-fx-font: 13 Courier;");

        buttonExport.setTranslateX(-200);
        buttonExport.setTranslateY(40);
        buttonExport.setStyle("-fx-font: 13 Courier;");

        lineFromTxt = new TextField();
        lineToTxt = new TextField();

        lineFromTxt.setTranslateX(-120);
        lineFromTxt.setTranslateY(-70);
        lineFromTxt.setMaxWidth(30);
        lineFromTxt.setText("From Line");

        lineToTxt.setTranslateX(-90);
        lineToTxt.setTranslateY(-70);
        lineToTxt.setMaxWidth(30);
        lineToTxt.setText("To Line");


        lineTieFromTxt = new TextField();
        lineTieFromTxt.setTranslateX(-10);
        lineTieFromTxt.setTranslateY(-70);
        lineTieFromTxt.setMaxWidth(30);
        lineTieFromTxt.setText("");


        lineTieToTxt = new TextField();
        lineTieToTxt.setTranslateX(20);
        lineTieToTxt.setTranslateY(-70);
        lineTieToTxt.setMaxWidth(30);


        showFlightPlan = new Button("Show Flight Plan");
        showFlightPlan.setTranslateY(0);
        showFlightPlan.setTranslateX(-200);


        seperateLines = new TextField();
        seperateLines.setMaxWidth(130);
        seperateLines.setTranslateX(-40);
        seperateLines.setTranslateY(-30);
        cbUseSeperateLines = new CheckBox("Seperate lines");
        cbUseSeperateLines.setTranslateX(-220);
        cbUseSeperateLines.setTranslateY(-30);

        arrow = new Text("--->");
        arrow.setTranslateX(65);
        arrow.setTranslateY(-70);

        applyBtn = new Button("apply");
        applyBtn.setTranslateX(150);
        applyBtn.setTranslateY(-70);
        applyBtn.setMaxWidth(60);

        applied = new Text("Not Applied");
        applied.setTranslateX(270);
        applied.setTranslateY(-70);
        applied.setStyle("-fx-font: 13 Courier;");

        txtTie = new Text("Ties:");
        txtTie.setTranslateX(-45);
        txtTie.setTranslateY(-70);
        txtTie.setStyle("-fx-font: 13 Courier;");

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
            String filePath = folder.getParent() + "/flight_plan/waypoints.txt";
            if(parFolder.startsWith("Flight")) {
                Path src = Paths.get(filePath);
                System.out.println(src);
                String[] surveyName = Controller.getCurSurvey().split("_");
                String destPath = curPath+"/S"+surveyName[1]+"-B"+Controller.getCurBlock()+"-F"+Controller.getCurFlight()+ "-waypoints.txt";
                Path dest = Paths.get(destPath);
                System.out.println(dest);
            try{
                Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);

                }catch (IOException e){

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

            String path = System.getProperty("user.dir");
            String createFlightFilePath = path + "/Data/"+Controller.getCurSurvey()+"/Block"+Controller.getCurBlock()+
                    "/Flight"+Controller.getCurFlight()+"/flight_plan/flightPlan.txt";

            String pathPython = path + "/Package/pythontest.py";
            String command = "python " + pathPython + " -m CreateFlight -f " + createFlightFilePath;
            System.out.println(command);
            try {
                //Python code runs here
                Process p = Runtime.getRuntime().exec(command);
                p.waitFor();
                //Python code
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
                    Controller.getCurBlock() + "/Flight"+ Controller.getCurFlight()+"/flight_plan/flightPlan.txt";
            System.out.println(filePath);
            String s;
            InputStream ins = new FileInputStream(filePath);
            Reader r = new InputStreamReader(ins, "UTF-8"); // leave charset out for default
            BufferedReader br = new BufferedReader(r);
            System.out.println(filePath);
            while ((s = br.readLine()) != null) {
                if(s.startsWith("From:")){
                    String segments[] = s.split(":");
                    if(segments.length > 1){
                        String seg = segments[1];
                        lineFromTxt.setText(seg);
                    }else{
                        lineFromTxt.setText("");
                    }
                }
                if(s.startsWith("To:")) {
                    String segments[] = s.split(":");
                    if(segments.length > 1) {
                        String seg = segments[1];
                        lineToTxt.setText(seg);
                    }else{
                        lineToTxt.setText("");
                    }


                }if(s.startsWith("fromTie:")) {
                    String segments[] = s.split(":");
                    if(segments.length > 1){
                        String seg = segments[1];
                        lineTieFromTxt.setText(seg);
                    }else{
                        lineTieFromTxt.setText("");
                    }

                }if(s.startsWith("toTie:")) {
                    String segments[] = s.split(":");
                    if (segments.length > 1) {
                        String seg = segments[1];
                        lineTieToTxt.setText(seg);
                    } else {
                        lineTieToTxt.setText("");
                    }

                }if(s.startsWith("useSeperateLines:")){
                    String segments[] = s.split(":");
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
                    String segments[] = s.split(":");
                    if(segments.length > 1){
                        String seg = segments[1];
                        seperateLines.setText(seg);
                    }else {
                        seperateLines.setText("");
                    }
                }
                if(s.startsWith("applyOrNot:")){
                    String segments[] = s.split(":");
                    if(segments.length > 1){
                        String seg = segments[1];
                        applied.setText(seg);
                    }else{
                        applied.setText("Not Applied");
                    }
                }
            }
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
        layout.getChildren().add(btnShowBlkPlan);
        layout.getChildren().add(currentFlight);
        layout.getChildren().add(lineFromTxt);
        layout.getChildren().add(lineToTxt);
        layout.getChildren().add(applyBtn);
        layout.getChildren().add(arrow);
        layout.getChildren().add(applied);
        layout.getChildren().add(showFlightPlan);
        layout.getChildren().add(buttonExport);
        layout.getChildren().add(lineTieFromTxt);
        layout.getChildren().add(lineTieToTxt);
        layout.getChildren().add(txtTie);
        layout.getChildren().add(cbUseSeperateLines);
        layout.getChildren().add(seperateLines);
    }
    // Removes the buttons and stuff when Pane transitions
    public void removeElements(){
        layout.getChildren().remove(btnShowBlkPlan);
        layout.getChildren().remove(currentFlight);
        layout.getChildren().remove(lineFromTxt);
        layout.getChildren().remove(lineToTxt);
        layout.getChildren().remove(applyBtn);
        layout.getChildren().remove(arrow);
        layout.getChildren().remove(applied);
        layout.getChildren().remove(showFlightPlan);
        layout.getChildren().remove(buttonExport);
        layout.getChildren().remove(lineTieFromTxt);
        layout.getChildren().remove(lineTieToTxt);
        layout.getChildren().remove(txtTie);
        layout.getChildren().remove(cbUseSeperateLines);
        layout.getChildren().remove(seperateLines);

    }

}
