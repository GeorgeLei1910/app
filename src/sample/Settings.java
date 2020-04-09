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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class Settings {

    private static Settings single_instance = null;
    static private Button buttonBack;
    static Button buttonSurvey;
    static private Rectangle rectangle = new Rectangle(0, 140, 700, 450);
    static private ComboBox comboBox;
    static public Button btnSettings;
    static private FileChooser fileChooser;
    static private Button fileChooseBtn;
    static private Path kmlFilePath;
    static private Text textKml, textElevation, text;
    static private Button showPlanOfTieAndFlight, fileChooseBtnElevation, showPlanOfTieFlight, showPlanOfSurvey;
    static private Path elevFilePath;
    static private Button Help;


    ObservableList<String> items = FXCollections.observableArrayList ();



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
        btnSettings = new Button("Survey Plan Settings");
        btnSettings.setTranslateX(-240);
        btnSettings.setTranslateY(-50);

        showPlanOfSurvey = new Button("Create and Show Survey Plan");
        showPlanOfSurvey.setTranslateX(-270);
        showPlanOfSurvey.setTranslateY(0);

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


        fileChooseBtn = new Button("....");

        fileChooseBtn.setOnAction((event) -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(Main.getStage());
            if (file != null) {
                kmlFilePath = file.toPath();
                textKml.setText(kmlFilePath.getFileName().toString());
            }
        });

        fileChooseBtnElevation = new Button("....");
        fileChooseBtnElevation.setOnAction((event) -> {
            FileChooser fileChooser2 = new FileChooser();
            File file = fileChooser2.showOpenDialog(Main.getStage());
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
        buttonBack.setOnAction(event -> {
           removeElements(layout);
           MainInterface mainInterface = MainInterface.getInstance(layout);
            mainInterface.showElements(layout);
        });


        comboBox.setOnAction(event -> {

            String str = comboBox.getValue().toString();
            //str = str.substring(6, str.length());
            Controller.setCurSurveyFolder(str);
            System.out.println(Controller.getCurSurvey());

        });

        Help.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                removeElements(layout);
                HelpSection helpSection = HelpSection.getInstance(layout);
                helpSection.showElems();

            }
        });
    }


    public void removeElements(StackPane layout){
        layout.getChildren().remove(rectangle);
        layout.getChildren().remove(buttonBack);
        layout.getChildren().remove(comboBox);
        layout.getChildren().remove(buttonSurvey);
        layout.getChildren().remove(btnSettings);
        layout.getChildren().remove(text);
        layout.getChildren().remove(fileChooseBtn);
        layout.getChildren().remove(showPlanOfSurvey);
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
