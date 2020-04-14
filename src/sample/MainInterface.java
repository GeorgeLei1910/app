package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;


public class MainInterface {

    private static MainInterface single_instance = null;
    static private Button buttonLogData, buttonDataQuality, buttonFlightPlan, buttonBlock, buttonFlight;
    static private Rectangle rectangle = new Rectangle(0, 140, 700, 450);

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

        buttonFlight.setTranslateX(-240);
        buttonFlight.setTranslateY(-260);
        buttonFlight.setMaxWidth(80);
        buttonFlight.setMaxHeight(10);

        listSurveys = new ComboBox(Controller.getSurveys());
        listBlocks = new ComboBox(Controller.getBlocks());
        listFlights = new ComboBox(Controller.getFlights());
        translateNode(listSurveys, -200, -180);
        translateNode(listBlocks, 0, -180);
        translateNode(listFlights, 200, -180);

        curSurveyName.setFill(Color.WHITE);
        translateNode(curSurveyName, 0, -270);
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
        layout.getChildren().add(curSurveyName);
        layout.getChildren().add(listFlights);
        layout.getChildren().add(listBlocks);
        layout.getChildren().add(listSurveys);

        LoggingData loggingData =  LoggingData.getInstance(layout);
        loggingData.showElements();
    }

    public void removeElements(StackPane layout){
        layout.getChildren().remove(rectangle);
        layout.getChildren().remove(buttonLogData);
        layout.getChildren().remove(buttonDataQuality);
        layout.getChildren().remove(buttonFlightPlan);
        layout.getChildren().remove(curSurveyName);
        layout.getChildren().remove(listFlights);
        layout.getChildren().remove(listBlocks);
        layout.getChildren().remove(listSurveys);
    }

    public static void translateNode(Node n, float x, float y){
        n.setTranslateX(x);
        n.setTranslateY(y);
    }
}
