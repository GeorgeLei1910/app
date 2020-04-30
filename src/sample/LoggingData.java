package sample;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.*;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;


public class LoggingData {
//Makes all the buttons
    private static LoggingData single_instance = null;
    static private Button buttonConnect, buttonDownload, buttonProcess, buttonStart, buttonStop, buttonLatest;
    static private TextField ipAddressField;
    static private Text latestTime = new Text(10, 500, "Press Connect to see\n the most recent file");
    static private SFTPClient download = new SFTPClient();
    static private ObservableList<String> listOfFiles = FXCollections.observableArrayList();
    static private ArrayList<String> filenames = new ArrayList<String>(), allfolders = new ArrayList<String>();
    static private boolean shouldLoad = true;

    static private StackPane layout;
    private static int isLogging = 0;
//    static private Button openStates;
    private LoggingData(StackPane layout){
        LoggingData.layout = layout;
        buttonConnect = new Button("Connect UAV");
        buttonDownload = new Button("Download Older Data");
        buttonProcess = new Button("Process Data");
        buttonLatest = new Button("Download Most Recent Data");
        ipAddressField = new TextField();
        ipAddressField.setPromptText("IP address");
        buttonStart =  new Button("Start Logging");
        buttonStop = new Button("Stop Logging");
        buttonConnect.setTranslateX(-100);
        buttonConnect.setTranslateY(-140);
        buttonDownload.setTranslateX(0);
        buttonDownload.setTranslateY(150);
        buttonLatest.setTranslateX(-175);
        buttonLatest.setTranslateY(150);
        buttonProcess.setTranslateX(150);
        buttonProcess.setTranslateY(150);
        ipAddressField.setPrefWidth(100);
        ipAddressField.setMaxWidth(100);
        ipAddressField.setTranslateX(-230);
        ipAddressField.setTranslateY(-140);
        ipAddressField.setText("192.168.8.1");

        buttonStart.setTranslateX(-70);
        buttonStart.setTranslateY(-80);
        buttonStop.setTranslateX(-70);
        buttonStop.setTranslateY(0);

        latestTime.setTranslateX(-175);
        latestTime.setTranslateY(100);
        latestTime.setFont(new Font(12));

        buttonStart.setDisable(true);
        buttonStop.setDisable(true);
        buttonDownload.setDisable(true);
        buttonLatest.setDisable(true);
        buttonProcess.setDisable(false);
        filenames.add("Mag.csv");
        filenames.add("Mav.csv");
        filenames.add("MavAtt.csv");
        filenames.add("MavLaser.csv");
        filenames.add("PiksiGPS.csv");
        filenames.add("PiksiGPSTime.csv");

        // Connects to BeagleBone via Wifi (192.168.8.1) or USB (192.168.7.2)
        buttonConnect.setOnAction((event) -> {
            BBconnect bbConnect = BBconnect.getInstance();
            String connection = null;
            try {
                download.connect(ipAddressField.getText());
                bbConnect.setIPAddress(ipAddressField.getText());
                connection = bbConnect.connect(1);
                if(connection.contains("Logger On")){
                    //Disables and enables different stuff.
                    buttonStop.setDisable(false);
                    buttonStart.setDisable(true);
                    buttonDownload.setDisable(true);
                    buttonLatest.setDisable(true);

                }else if(connection.contains("Logger Off")){
                    buttonStop.setDisable(true);
                    buttonStart.setDisable(false);
                    buttonDownload.setDisable(false);
                    buttonLatest.setDisable(false);
                }
                if(shouldLoad){
                    getFilesFromServer();
                    shouldLoad = false;
                }
                if(listOfFiles.size() == 0){
                    buttonLatest.setDisable(true);
                    buttonDownload.setDisable(true);
                    AllAlerts.noFilesOnBBAlert();
                    latestTime.setText("No Files in Record");
                }
//                buttonConnect.setDisable(true);
            } catch (ConnectException | SocketTimeoutException ce){
                ce.printStackTrace();
                download.disconnect();
                AllAlerts.disconnectedAlert();
                disableDisconnected();
            } catch (JSchException e) {
                e.printStackTrace();
                download.disconnect();
                AllAlerts.disconnectedAlert();
                disableDisconnected();
            }
        });

        buttonStart.setOnAction((event) -> {
            File directory = new File(Controller.getCurDataFolder());

//                BBconnect bbConnect = BBconnect.getInstance();
//                bbConnect.connect(4);
                // This is an FTP download, This not only not need the program to run, it can be faster than the method above.
                // All methods are in the Class SFTPClient.
                try {
                    BBconnect bbConnect = BBconnect.getInstance();
                    if(bbConnect.connect(2).contains("Start")){
                        for(int i = 0; i < 5; i++){
                            buttonConnect.fire();
                            if(buttonStart.isDisabled()){
                                break;
                            }else if (i == 4){

                            }
                        }
                    }else{
                        AllAlerts.disconnectedAlert();
                        disableDisconnected();
                    }

                }catch (ConnectException | SocketTimeoutException ce) {
                    ce.printStackTrace();
                    AllAlerts.disconnectedAlert();
                    disableDisconnected();
                }
        });

        buttonStop.setOnAction((event) -> {
            BBconnect bbConnect = BBconnect.getInstance();
            try {
                if(bbConnect.connect(3).equals("Stop")){
                    shouldLoad = true;
                    for(int i = 0; i < 5; i++){
                        buttonConnect.fire();
                        if(buttonStop.isDisabled()){
                            break;
                        }else if (i == 4){

                        }
                    }
                }else{
                    AllAlerts.disconnectedAlert();
                    disableDisconnected();
                }
            } catch (ConnectException | SocketTimeoutException ce){
                ce.printStackTrace();
                AllAlerts.disconnectedAlert();
                disableDisconnected();
            }
        });

        buttonLatest.setOnAction(event -> {
            if(Controller.getCurFlight() != 0) {
                int write = 0;
                File directory = new File(Controller.getCurDataFolder());
                if (directory.isDirectory() && directory.listFiles().length > 0) {
                    write = AllAlerts.overwriteWarning(directory);
                }else{
                    write = 1;
                }
                if(write != 0) {
                    String orig = "/home/debian/stratus/build/datafiles/" + listOfFiles.get(0);
                    String dest = Controller.getCurDataFolder();
                    ArrayList<String> unsuccessfulFiles = new ArrayList<>();

                        for (String fn : filenames) {
                            System.out.println("Copying from: " + orig + "/" + listOfFiles.get(0) + "-" + fn);
                            System.out.println("Copying to: " + dest + "/" + listOfFiles.get(0) + "-" + fn);
                            try {
                            download.download(orig + "/" + listOfFiles.get(0) + "-" + fn, dest + "/" + listOfFiles.get(0).replace(":", "") + "-" + fn);
                            } catch (JSchException e) {
                                e.printStackTrace();
                                unsuccessfulFiles.add(fn);
                            } catch (SftpException e) {
                                e.printStackTrace();
                                unsuccessfulFiles.add(fn);
                            }
                        }

                    if(unsuccessfulFiles.size() == 0){
                        AllAlerts.downloadSuccessful(orig, dest);
                    }else{
                        AllAlerts.downloadFailed(unsuccessfulFiles);
                    }
                }
            }else{
                AllAlerts.flightNotChosenAlert();
            }
        });

        buttonDownload.setOnAction(event -> {
            File directory = new File(Controller.getCurDataFolder());
            if(Controller.getCurFlight() != 0){
                if(directory.list().length > 0) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Data files exist");
                    alert.setHeaderText("You have data files in this flight");
                    alert.setContentText("Proceed if you want to overwrite the previous data\nOtherwise click \"Cancel\" on the next window");
                    alert.showAndWait();
                }
               //  This is an FTP download, This not only not need the program to run, it can be faster than the method above.
               //   All methods are in the Class SFTPClient.
                final Stage dialog = new Stage();
//                    listOfFiles.setItems();
                Button downloadFiles = new Button("Download Files"), cancel = new Button("Cancel");
                ComboBox downloadableFiles = new ComboBox(listOfFiles);


                downloadableFiles.setTranslateX(-150);
                downloadFiles.setTranslateX(0);
                cancel.setTranslateX(150);

                downloadableFiles.setDisable(false);
                downloadFiles.setDisable(false);
                cancel.setDisable(false);

                downloadFiles.disableProperty().bind(downloadableFiles.valueProperty().isNull());

                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(Controller.getPrimaryStage());
                StackPane popUpLayout = new StackPane();
                popUpLayout.setStyle("-fx-background-color: #474747;");
                Scene popUpScene = new Scene(popUpLayout, 500, 100);
                dialog.setScene(popUpScene);
                dialog.setTitle("Download Older Data");
                dialog.show();

                popUpLayout.getChildren().add(downloadableFiles);
                popUpLayout.getChildren().add(downloadFiles);
                popUpLayout.getChildren().add(cancel);

                cancel.setOnAction(event1 -> {
                    dialog.close();
                });

                downloadFiles.setOnAction(event1 -> {
                    int write = 1;
                    String filename = downloadableFiles.getValue().toString();
                    String orig = "/home/debian/stratus/build/datafiles/" + filename + "/" + filename;
                    String dest = Controller.getCurDataFolder() + "\\" + filename.replace(":", "");
                    if (directory.isDirectory() && directory.listFiles().length > 0) {
                        write = AllAlerts.overwriteWarning(directory);
                    }
                    if (write != 0) {
                        ArrayList<String> unsuccessfulFiles = new ArrayList<>();
                        for (String fn : filenames) {
                            System.out.println("Copying from: " + orig + "-" + fn);
                            System.out.println("Copying to: " + dest + "-" + fn);
                            try {
                                download.download(orig + "-" + fn, dest + "-" + fn);
                            } catch (JSchException e) {
                                e.printStackTrace();
                                unsuccessfulFiles.add(fn);
                            } catch (SftpException e) {
                                e.printStackTrace();
                                unsuccessfulFiles.add(fn);
                            }
                            if(directory.length() > 0){
                                AllAlerts.downloadSuccessful(orig, dest);
                            }else{
                                AllAlerts.downloadFailed(unsuccessfulFiles);
                            }
                        }
                    }

                });
            }else{
                AllAlerts.flightNotChosenAlert();
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
        layout.getChildren().add(buttonLatest);
        layout.getChildren().add(ipAddressField);
        layout.getChildren().add(buttonStart);
        layout.getChildren().add(buttonStop);
        layout.getChildren().add(latestTime);
    }


    public void removeElements(){
        layout.getChildren().remove(buttonConnect);
        layout.getChildren().remove(buttonLatest);
        layout.getChildren().remove(buttonDownload);
        layout.getChildren().remove(buttonProcess);
        layout.getChildren().remove(ipAddressField);
        layout.getChildren().remove(buttonStart);
        layout.getChildren().remove(buttonStop);
        layout.getChildren().remove(latestTime);
    }


    private boolean isDirEmpty(String dir){
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(dir))) {
            return !dirStream.iterator().hasNext();
        }catch (IOException e){
            return false;
        }
    }

    private void disableDisconnected(){
        buttonConnect.setDisable(false);
        buttonStart.setDisable(true);
        buttonStop.setDisable(true);
        buttonDownload.setDisable(true);
        buttonLatest.setDisable(true);
        buttonProcess.setDisable(false);
    }

    private void getFilesFromServer(){
        try {
            allfolders = download.listDataFiles();
            Collections.sort(allfolders);
            int earliest = 0;
            String latestStr;
            for(int i = 1; i < allfolders.size(); i++){
                latestStr = allfolders.get(i - 1).substring(3);
                String compStr = allfolders.get(i).substring(3);
                if (latestStr.compareTo(compStr) > 0) {
                    earliest = i;
                    break;
                }
            }
            System.out.println(earliest);
            System.out.println(allfolders.size());
            listOfFiles.clear();
            if (earliest == 0) {
                for (int i = allfolders.size() - 1; i >= 0; i--) listOfFiles.add(allfolders.get(i));
            }else{
                for(int i = earliest - 1; i >= 0; i--) listOfFiles.add(allfolders.get(i));
                for(int i = allfolders.size() - 1; i >= earliest; i--) listOfFiles.add(allfolders.get(i));
            }
            latestTime.setText("Most Recent File:\n" + listOfFiles.get(0));

        } catch (JSchException e) {
            e.printStackTrace();
            AllAlerts.disconnectedAlert();
        } catch (SftpException e) {
            e.printStackTrace();
            AllAlerts.disconnectedAlert();
        }
    }
}
