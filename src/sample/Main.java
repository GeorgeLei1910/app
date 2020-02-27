package sample;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;

import java.io.File;


public class Main extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        System.out.println("Inside init() method! Perform necessary initializations here.");
    }
    static private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        stage = primaryStage;
        StackPane layout = new StackPane();

        Controller.getInstance(layout).setPrimaryStage(stage);
        Controller controller = Controller.getInstance(layout);
        Scene scene = new Scene(layout, 800, 600);
        layout.setStyle("-fx-background-color: #474747;");

        MainInterface mainInterface =  MainInterface.getInstance(layout);
        mainInterface.showElements(layout);
        primaryStage.setTitle("Interface");
        primaryStage.setScene(scene);
        primaryStage.show();
        String curFolder = System.getProperty("user.dir");
        File dataFolder = new File(curFolder + "/Data");
        dataFolder.mkdirs();
    }
    static public Stage getStage(){
        return stage;
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        launch(args);
    }
}
