package sample;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
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
    static private Button openStates;
    private LoggingData(StackPane layout){
        this.layout = layout;
        buttonConnect = new Button("Connect UAV");
        buttonDownload = new Button("Download Data");
        buttonProcess = new Button("Process Data");
        ipAddressField = new TextField();
        ipAddressField.setPromptText("IP address");
        buttonStart =  new Button("Start Logging");
        buttonStop = new Button("Stop Logging");
        openStates = new Button("Reconnect");
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

        openStates.setTranslateX(260);
        openStates.setTranslateY(-10);

        buttonStart.setDisable(true);
        buttonStop.setDisable(true);
        buttonDownload.setDisable(true);

        openStates.setOnAction((event) -> {
//            int state = BBconnect.getInstance().getState();
            BBconnect bbconnect = BBconnect.getInstance();
            buttonDownload.setDisable(false);
            buttonStart.setDisable(false);
            buttonStop.setDisable(false);
        });

        // Connects to BeagleBone via Wifi (192.168.8.1) or USB (192.168.7.2)
        buttonConnect.setOnAction((event) -> {
            BBconnect bbConnect = BBconnect.getInstance();
            String connection = null;
            try {
                connection = bbConnect.connect(1);
                if(connection.contains("Logger On")){
                    //Disables and enables different stuff.
                    buttonDownload.setDisable(true);
                    buttonStop.setDisable(false);
                    buttonStart.setDisable(true);
                }else if(connection.contains("Logger Off")){
                    buttonDownload.setDisable(false);
                    buttonStop.setDisable(true);
                    buttonStart.setDisable(false);
                }
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                disconnectedAlert();
            }

        });

        buttonStart.setOnAction((event) -> {

            File directory = new File(Controller.getCurDataFolder());
            if(directory.isDirectory() && directory.list().length > 2){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Files already exist in this folder");
                alert.setContentText("Either Empty this flight or choose or create another flight to download the data :)");
                alert.showAndWait();

            }else if(Controller.getCurFlight() != 0){
//                BBconnect bbConnect = BBconnect.getInstance();
//                bbConnect.connect(4);
                // This is an FTP download, This not only not need the program to run, it can be faster than the method above.
                // All methods are in the Class SFTPClient.
                SFTPClient download = new SFTPClient();
                try {
                    BBconnect bbConnect = BBconnect.getInstance();
                    if(bbConnect.connect(2).equals("Start")){
                        buttonDownload.setDisable(true);
                        buttonStop.setDisable(false);
                        buttonStart.setDisable(true);
                        //To make sure it does not show
                    }else{
                        disconnectedAlert();
                    }
                    buttonConnect.fire();
                    buttonConnect.fire();
                    buttonConnect.fire();
                }catch (SocketTimeoutException ste){
                    buttonStart.setDisable(false);
                    buttonStop.setDisable(true);
                    buttonConnect.setDisable(true);
                }
            }else{
                flightNotChosenAlert();
            }
        });

        buttonStop.setOnAction((event) -> {
            BBconnect bbConnect = BBconnect.getInstance();
            try {
                if(bbConnect.connect(3).equals("Stop")){
                    buttonDownload.setDisable(false);
                    buttonStart.setDisable(false);
                    buttonStop.setDisable(true);
                    //To loop the PlaneServer
                    buttonConnect.fire();
                    buttonConnect.fire();
                }else{
                    disconnectedAlert();
                }
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
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
//                BBconnect bbConnect = BBconnect.getInstance();
//                bbConnect.connect(4);
                // This is an FTP download, This not only not need the program to run, it can be faster than the method above.
                // All methods are in the Class SFTPClient.
                SFTPClient download = new SFTPClient();
                try {
                    long startTime = System.currentTimeMillis();
                    String dest = Controller.getCurDataFolder();
                    download.connect();
                    System.out.println("Connected");
                    System.out.println("Saving to " + dest);
                    download.download("/home/debian/stratus/build/Mag.csv", dest + "\\Mag.csv");
                    long magDone = System.currentTimeMillis();
                    System.out.println("Mag.csv downloaded in " + (magDone - startTime) + " ms");
                    download.download("/home/debian/stratus/build/Mav.csv", dest + "\\Mav.csv");
                    long mavDone = System.currentTimeMillis();
                    System.out.println("Mav.csv downloaded in " + (mavDone - magDone) + " ms");
                    download.download("/home/debian/stratus/build/MavAtt.csv", dest + "\\MavAtt.csv");
                    long mavAttDone = System.currentTimeMillis();
                    System.out.println("MavAtt.csv downloaded in " + (mavAttDone - mavDone) + " ms");
                    download.download("/home/debian/stratus/build/MavLaser.csv", dest + "\\MavLaser.csv");
                    long mavLasDone = System.currentTimeMillis();
                    System.out.println("MavLaser.csv downloaded in " + (mavLasDone - mavAttDone) + " ms");
                    download.download("/home/debian/stratus/build/PiksiGPS.csv", dest + "\\PiksiGPS.csv");
                    long piksiDone = System.currentTimeMillis();
                    System.out.println("PiksiGPS.csv downloaded in " + (piksiDone - mavLasDone) + " ms");
                    download.download("/home/debian/stratus/build/PiksiGPSTime.csv", dest + "\\PiksiGPSTime.csv");
                    long piksiTimeDone = System.currentTimeMillis();
                    System.out.println("PiksiGPSTime.csv downloaded in " + (piksiTimeDone - piksiDone) + " ms");
                    download.disconnect();
                    long endTime = System.currentTimeMillis();
                    System.out.println("Disconnected");
                    System.out.println("Total Time used: " + (endTime - startTime));
                    buttonProcess.fire();
                } catch (JSchException jsche) {
                    jsche.printStackTrace();
                    disconnectedAlert();
                } catch (SftpException sftpe){
                    sftpe.printStackTrace();
                } catch (ConnectException ce){
                    ce.printStackTrace();
                    disconnectedAlert();
                }
//                buttonStart.setDisable(false);
//                buttonStop.setDisable(true);
//                buttonConnect.setDisable(true);

            }else{
                flightNotChosenAlert();
            }
        });

        buttonProcess.setOnAction((event) -> {
            try{
            String path = System.getProperty("user.dir");
            path = path.replace('\\', '/');
            String pathPython = path + "/Package/pythontest.py";
            String  pathFolder =  Controller.getCurDataFolder().replace('\\', '/');
//            String command = "python " + pathPython;
            String command = "python " +pathPython+" -m process " + "-f " +pathFolder;

            // Puts python Package\pythontest.py -m process -f
            System.out.println(command);
            Process p = Runtime.getRuntime().exec(command);

            //Python console print (Can be commented out)
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();

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

    public static LoggingData getInstance(StackPane layout){
        if (single_instance == null)
            single_instance = new LoggingData(layout);
        return single_instance;
    }

    public void showElements(){
        layout.getChildren().add(buttonConnect);
        layout.getChildren().add(buttonDownload);
        layout.getChildren().add(buttonProcess);
        layout.getChildren().add(ipAddressField);
        layout.getChildren().add(buttonStart);
        layout.getChildren().add(buttonStop);
        layout.getChildren().add(openStates);
    }


    public void removeElements(){
        layout.getChildren().remove(buttonConnect);
        layout.getChildren().remove(buttonDownload);
        layout.getChildren().remove(buttonProcess);
        layout.getChildren().remove(ipAddressField);
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
    private void disconnectedAlert(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error");
        alert.setHeaderText("Disconnected");
        alert.setContentText("Your UAV is disconnected. Either:\n 1. Your UAV is off \n 2. Your UAV is on but you are not connected to its wifi");
        alert.showAndWait();
        buttonConnect.setDisable(false);
        buttonStart.setDisable(true);
        buttonStop.setDisable(true);
        buttonDownload.setDisable(true);
    }
    private void flightNotChosenAlert(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Flight Is not Set");
        alert.setHeaderText("Flight is not chosen");
        alert.setContentText("In order to download the data, you should provide a path (Flight number) " +
                "so that we can place it there!");
        alert.showAndWait();
    }




}
