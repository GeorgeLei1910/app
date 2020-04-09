package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;


public class MainInterface {

    private static MainInterface single_instance = null;
    static private Button buttonLogData, buttonDataQuality, buttonFlightPlan, buttonBlock, buttonSettings, buttonFlight;
    static private Rectangle rectangle = new Rectangle(0, 140, 700, 450);
    static private ComboBox comboBox;
    static private ComboBox comboBoxFlights;

    public ComboBox listSurveys, listBlocks, listFlights;

    static private Text curSurveyName;

    private MainInterface(StackPane layout){

        rectangle.setFill(Color.DARKGRAY);
        rectangle.setArcHeight(15);
        rectangle.setArcWidth(15);

        rectangle.setStroke(Color.WHITE);
        rectangle.setStrokeWidth(2.3);


        buttonLogData = new Button("Logging Data");
        buttonDataQuality = new Button("Data Quality");
        buttonFlightPlan = new Button("Flight Planning");
        buttonBlock = new Button("New Block");
        buttonSettings = new Button("Settings");
        buttonFlight = new Button("New Flight");
        curSurveyName = new Text(Controller.getCurSurvey());
        buttonBlock.setStyle("-fx-font-size:16");
        buttonFlight.setStyle("-fx-font-size:10");
        buttonLogData.setTranslateX(0);
        buttonLogData.setTranslateY(-230);
        buttonDataQuality.setTranslateX(110);
        buttonDataQuality.setTranslateY(-230);
        buttonFlightPlan.setTranslateX(-100);
        buttonFlightPlan.setTranslateY(-230);
        buttonBlock.setTranslateX(0);
        buttonBlock.setTranslateY(250);
        buttonSettings.setTranslateX(-330);
        buttonSettings.setTranslateY(-260);

        buttonFlight.setTranslateX(-240);
        buttonFlight.setTranslateY(-260);
        buttonFlight.setMaxWidth(80);
        buttonFlight.setMaxHeight(10);

        listSurveys = new ComboBox(Controller.getSurveys());
        listBlocks = new ComboBox(Controller.getBlocks());
        listFlights = new ComboBox(Controller.getFlights());

        listSurveys.setTranslateX(-200);
        listBlocks.setTranslateX(0);
        listFlights.setTranslateX(200);
        listSurveys.setTranslateY(-180);
        listBlocks.setTranslateY(-180);
        listFlights.setTranslateY(-180);

        curSurveyName.setFill(Color.WHITE);
        curSurveyName.setTranslateX(0);
        curSurveyName.setTranslateY(-270);
        curSurveyName.setStyle("-fx-font: 20 Courier;");

        buttonLogData.setStyle("-fx-border-color: #484848; -fx-border-width: 2; " +
                "-fx-border-radius: 7 7 7 7;" +
                "-fx-background-radius: 6.5  6.5 6.5 6.5;");
        buttonFlightPlan.setStyle("-fx-border-color: #484848; -fx-border-width: 2; " +
                "-fx-border-radius: 7 7 7 7;" +
                "-fx-background-radius: 6.5  6.5 6.5 6.5;");
        buttonDataQuality.setStyle("-fx-border-color: #484848; -fx-border-width: 2; " +
                "-fx-border-radius: 7 7 7 7;" +
                "-fx-background-radius: 6.5  6.5 6.5 6.5;");
        buttonSettings.setStyle("-fx-border-color: #484848; -fx-border-width: 2; " +
                "-fx-border-radius: 20 20 20 20;" +
                "-fx-background-radius: 20  20 20 20;");

        comboBox = new ComboBox(Controller.getBlocks());
        comboBoxFlights = new ComboBox(Controller.getFlights());
        comboBox.setTranslateX(300);
        comboBox.setTranslateY(-260);
        comboBox.setPrefWidth(134);
        comboBox.setMaxWidth(134);

        comboBoxFlights.setTranslateX(200);
        comboBoxFlights.setTranslateY(-260);
        comboBoxFlights.setPrefWidth(50);
        comboBoxFlights.setMaxWidth(50);



        comboBox.setOnAction(event -> {
            try{
                String str = comboBox.getValue().toString();
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


        comboBoxFlights.setOnAction(event -> {
            try{
                String str = comboBoxFlights.getValue().toString();
                String num = "0";
                if (!str.equals(""))
                    num = str.substring(1);

                Controller.setCurFlightFolder(Integer.parseInt(num));

            }catch(NullPointerException e){

            }
        });


        buttonDataQuality.setOnAction(event -> {
            try{
            LoggingData loggingData = LoggingData.getInstance(layout);
            FlightPlanning flightPlanning = FlightPlanning.getInstance(layout);
            loggingData.removeElements();
            flightPlanning.removeElements();

            DataQuality dataQuality =  DataQuality.getInstance(layout);
            dataQuality.showElems();
            }catch (IllegalArgumentException e){

            }

        });

        buttonLogData.setOnAction(event -> {
            try{
            DataQuality dataQuality =  DataQuality.getInstance(layout);
            FlightPlanning flightPlanning = FlightPlanning.getInstance(layout);
            dataQuality.removeElements();
            flightPlanning.removeElements();
            LoggingData loggingData = LoggingData.getInstance(layout);
            loggingData.showElements();
            }catch (IllegalArgumentException e){

            }

        });

        buttonFlightPlan.setOnAction(event -> {
            try{
            DataQuality dataQuality =  DataQuality.getInstance(layout);
            LoggingData loggingData = LoggingData.getInstance(layout);
            dataQuality.removeElements();
            loggingData.removeElements();
            FlightPlanning flightPlanning = FlightPlanning.getInstance(layout);
            flightPlanning.showElements();
            }catch(IllegalArgumentException e){

            }

        });

        buttonBlock.setOnAction(new EventHandler<ActionEvent>(){

            @Override
            public void handle(ActionEvent event) {

                comboBox.setValue(Controller.addBlocks());
                //comboBoxFlights.setValue(controller.addFlightLines());

            }

        });


        buttonFlight.setOnAction(new EventHandler<ActionEvent>(){

            @Override
            public void handle(ActionEvent event) {
                //comboBoxFlights.setValue("kkk");
                comboBoxFlights.setValue(Controller.addFlight());
            }

        });


        buttonSettings.setOnAction(new EventHandler<ActionEvent>(){

            @Override
            public void handle(ActionEvent event) {

                removeElements(layout);
                DataQuality dataQuality =  DataQuality.getInstance(layout);
                LoggingData loggingData = LoggingData.getInstance(layout);
                FlightPlanning flightPlanning = FlightPlanning.getInstance(layout);
                dataQuality.removeElements();
                loggingData.removeElements();
                flightPlanning.removeElements();
                Settings settings = Settings.getInstance(layout);
                settings.showElements(layout);
            }

        });

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






    }

    public static MainInterface getInstance(StackPane layout)
    {
        if (single_instance == null)
            single_instance = new MainInterface(layout);

        return single_instance;
    }

    public static void updateMainInterface(String Survey){
        curSurveyName.setText(Survey);
    }


    public void showElements(StackPane layout){
        layout.getChildren().add(rectangle);
        layout.getChildren().add(buttonLogData);
        layout.getChildren().add(buttonDataQuality);
        layout.getChildren().add(buttonFlightPlan);
//        layout.getChildren().add(buttonBlock);
        layout.getChildren().add(comboBox);
        layout.getChildren().add(comboBoxFlights);
        layout.getChildren().add(buttonSettings);
        layout.getChildren().add(curSurveyName);
        layout.getChildren().add(listFlights);
        layout.getChildren().add(listBlocks);
        layout.getChildren().add(listSurveys);

        //        layout.getChildren().add(buttonFlight);

        LoggingData loggingData =  LoggingData.getInstance(layout);
        loggingData.showElements();


    }


    public void removeElements(StackPane layout){
        layout.getChildren().remove(rectangle);
        layout.getChildren().remove(buttonLogData);
        layout.getChildren().remove(buttonDataQuality);
        layout.getChildren().remove(buttonFlightPlan);
//        layout.getChildren().remove(buttonBlock);
        layout.getChildren().remove(comboBox);
        layout.getChildren().remove(buttonSettings);
        layout.getChildren().remove(comboBoxFlights);
        layout.getChildren().remove(curSurveyName);
//        layout.getChildren().remove(buttonFlight);
        layout.getChildren().remove(listFlights);
        layout.getChildren().remove(listBlocks);
        layout.getChildren().remove(listSurveys);

    }


}
