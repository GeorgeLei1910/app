package sample;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class LoggingData {
//Makes all the buttons
    private static LoggingData single_instance = null;
    static private Button buttonConnect, buttonDownload, buttonProcess, buttonStart, buttonStop;
    static private TextField ipAddressField;
    static private StackPane layout;
    static private CheckBox cbMag;
    static private CheckBox cbPiksi;
    static private CheckBox cbMav;
    static private Button openStates;
    private LoggingData(StackPane layout){
        this.layout = layout;
        buttonConnect = new Button("Connect UAV");
        buttonDownload = new Button("Download Data");
        buttonProcess = new Button("Process Data");
        ipAddressField = new TextField();
        ipAddressField.setPromptText("IP address");
        buttonStart =  new Button("Start");
        buttonStop = new Button("Stop");
        openStates = new Button("Open States");
        buttonConnect.setTranslateX(-100);
        buttonConnect.setTranslateY(-140);
        buttonDownload.setTranslateX(-150);
        buttonDownload.setTranslateY(150);
        buttonProcess.setTranslateX(150);
        buttonProcess.setTranslateY(150);
        ipAddressField.setPrefWidth(100);
        ipAddressField.setMaxWidth(100);
        ipAddressField.setTranslateX(-230);
        ipAddressField.setTranslateY(-140);

        buttonStart.setTranslateX(-70);
        buttonStart.setTranslateY(-80);
        buttonStop.setTranslateX(-70);
        buttonStop.setTranslateY(0);

        cbMag = new CheckBox("Magnetometer");
        cbPiksi = new CheckBox("Piksi");
        cbMav = new CheckBox("PixHawk");
        cbMag.setTranslateX(-210);
        cbPiksi.setTranslateX(-241);
        cbMav.setTranslateX(-229);
        cbMag.setTranslateY(-80);
        cbPiksi.setTranslateY(-40);
        cbMav.setTranslateY(0);
        openStates.setTranslateX(260);
        openStates.setTranslateY(-10);


        buttonStart.setDisable(true);
        buttonStop.setDisable(true);
        buttonDownload.setDisable(true);

        openStates.setOnAction((event) -> {
            buttonStop.setDisable(false);
            buttonStart.setDisable(false);
            buttonDownload.setDisable(false);
            buttonConnect.setDisable(false);


        });

        // Connects to BeagleBone via Wifi (192.168.8.1) or USB (192.168.7.2)
        buttonConnect.setOnAction((event) -> {
            BBconnect bbConnect = BBconnect.getInstance();
            if(bbConnect.connect(1).equals("Connected")){
                //Disables and enables different stuff.
                buttonDownload.setDisable(true);
                buttonStop.setDisable(true);
                buttonStart.setDisable(false);
                buttonConnect.setDisable(true);
            }

        });

        buttonStart.setOnAction((event) -> {
            BBconnect bbConnect = BBconnect.getInstance();
            if(bbConnect.connect(2).equals("Start")){
                buttonDownload.setDisable(true);
                buttonConnect.setDisable(true);
                buttonStop.setDisable(false);
                buttonStart.setDisable(true);
            }

        });

        buttonStop.setOnAction((event) -> {
            BBconnect bbConnect = BBconnect.getInstance();
            if(bbConnect.connect(3).equals("Stop")){
                buttonDownload.setDisable(false);
                buttonConnect.setDisable(true);
                buttonStart.setDisable(true);
                buttonStop.setDisable(true);
            }

        });

        buttonDownload.setOnAction((event) -> {
            File directory = new File(Controller.getCurDataFolder());
            if(directory.isDirectory() && directory.list().length > 2){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Files already exist in this folder");
                alert.setContentText("Either Empty this flight or choose or create another flight to download the data :)");
                alert.showAndWait();

            }else if(Controller.getCurFlight() != 0){
                BBconnect bbConnect = BBconnect.getInstance();
                bbConnect.connect(4);
                buttonStart.setDisable(true);
                buttonStop.setDisable(true);
                buttonDownload.setDisable(true);
                buttonConnect.setDisable(false);
//                buttonProcess.fire();

            }else{
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Flight Is not Set");
                alert.setHeaderText("Flight is not chosen");
                alert.setContentText("In order to download the data, you should provide a path (Flight number) " +
                        "so that we can place it there!");

                alert.showAndWait();


            }


        });




        buttonProcess.setOnAction((event) -> {
            try{
            String path = System.getProperty("user.dir");
            String pathPython = path+ "\\Package\\hellotest.py";
            String  pathFolder =  Controller.getCurDataFolder().replace("/", "\\");
            String command = "python " + pathPython;
//            String command = "python " +pathPython+" -m process " + "-f " +pathFolder;
            System.out.println(command);
            // Puts python Package\pythontest.py -m process -f

            Process p = Runtime.getRuntime().exec(command);
            OutputStream rtm = p.getOutputStream();
            PrintStream prntstrm = new PrintStream(rtm);
            prntstrm.println();

//            System.out.println(pyos.);
            }catch(IOException e){
                System.out.println("IO exception occurred");

            }
            System.out.println(">>>>>>>>>>>>>>>>> The Raw data processed and Saved Successfully");


        });





    }

    public static LoggingData getInstance(StackPane layout)
    {
        if (single_instance == null)
            single_instance = new LoggingData(layout);


        return single_instance;
    }








    public void showElements(){
        layout.getChildren().add(buttonConnect);
        layout.getChildren().add(buttonDownload);
        layout.getChildren().add(buttonProcess);
        layout.getChildren().add(ipAddressField);
        layout.getChildren().add(cbMag);
        layout.getChildren().add(cbPiksi);
        layout.getChildren().add(cbMav);
        layout.getChildren().add(buttonStart);
        layout.getChildren().add(buttonStop);
        layout.getChildren().add(openStates);


    }


    public void removeElements(){
        layout.getChildren().remove(buttonConnect);
        layout.getChildren().remove(buttonDownload);
        layout.getChildren().remove(buttonProcess);
        layout.getChildren().remove(ipAddressField);
        layout.getChildren().remove(cbMag);
        layout.getChildren().remove(cbPiksi);
        layout.getChildren().remove(cbMav);
        layout.getChildren().remove(buttonStart);
        layout.getChildren().remove(buttonStop);
        layout.getChildren().remove(openStates);


    }


    private boolean isDirEmpty(String dir){
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(dir))) {
            return !dirStream.iterator().hasNext();
        }catch (IOException e){

            return false;
        }

    }




}
