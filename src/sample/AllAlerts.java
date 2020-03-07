package sample;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.File;
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
    public static void downloadFailed(String orig, String dest){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Download Failed");
        alert.setHeaderText("Download Unsuccessful");
        alert.setContentText("No files were downloaded.\nEither Beaglebone is disconnected, or files were not found");
        alert.showAndWait();
    }
}
