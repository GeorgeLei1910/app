package sample;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class AllAlerts {
    public static void disconnectedAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Disconnected");
        alert.setContentText("Your UAV is disconnected. Either:\n 1. Your UAV is off \n 2. Your UAV is on but you are not connected to its wifi");
        alert.showAndWait();
    }
    public static void flightNotChosenAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Flight Is not Set");
        alert.setHeaderText("Flight is not chosen");
        alert.setContentText("In order to download the data, you should provide a path (Flight number) " +
                "so that we can place it there!");
        alert.showAndWait();
    }
    public static void folderNotChosenAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No Folder Chosen");
        alert.setHeaderText("Folder");
        alert.setContentText("In order to download the data, you should select the folder on the drop down bar");
        alert.showAndWait();
    }
    public static void noFilesOnBBAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No Files on UAV");
        alert.setHeaderText("Nothing is recorded yet");
        alert.setContentText("There are no records of Data Yet.\n To make new data logging sessions, click on Start Logging in the main interface");
        alert.showAndWait();
    }
    public static int overwriteWarning(File directory){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Overwrite");
        alert.setHeaderText("The flight you chose already has data.");
        alert.setContentText("Do you want to overwrite that data?");

        ButtonType buttonOverwrite = new ButtonType("Overwrite");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonOverwrite, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonOverwrite){
            for(File delf : directory.listFiles()) delf.delete();
            return 1;
        }
        return 0;
    }
    public static void downloadSuccessful(String orig, String dest){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Download Complete");
        alert.setHeaderText("Download Successful");
        alert.setContentText("All files from " + orig + "\nis downloaded into " + dest);
        alert.showAndWait();
    }
    public static void downloadFailed(ArrayList<String> s){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Download Failed");
        alert.setHeaderText("Download Unsuccessful");
        String listString = "";
        for (String add: s){
            listString.concat(add + " \n");
        }
        alert.setContentText("The following files are not downloaded:\n" + listString);
        alert.showAndWait();
    }

    public static void noSurveyBoundary(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Not Enough Points");
        alert.setHeaderText("You don't have enough coordinates in your survey");
        alert.setContentText("Add at least Three points in the \"Position\" section and try again");
        alert.showAndWait();
    }
    public static void createError(String p){
        TextArea textArea = new TextArea("Try running the following command in Command Line:\n" + p + "\nTo see the exception thrown");
        textArea.setEditable(false);
        textArea.setWrapText(true);
        GridPane gridPane = new GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(textArea, 0, 0);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Render Error");
        alert.setHeaderText("Your Python Script is throwing an exception");
        alert.getDialogPane().setContent(gridPane);
        alert.showAndWait();
    }
    public static void renderBlockSuccessful(String survey, String block){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Render Complete");
        alert.setHeaderText("Render Successful");
        alert.setContentText(survey + " Block" + block + " has rendered successfully");
        alert.showAndWait();
    }
    public static void surveyNameError(int s){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Survey Name Error");
        String headerOption = "";
        String bodyOption = "";
        switch(s){
            case 0: headerOption = "empty";
                    bodyOption = "Type in a name for your survey and press ok";
                break;
            case 1: headerOption = "has a space or (back)slash";
                    bodyOption = "Make sure your survey name does not have a space, or (back)slash";
                break;
        }
        alert.setHeaderText("Your survey is " + headerOption);
        alert.setContentText(bodyOption);
        alert.showAndWait();
    }
}
