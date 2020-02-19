package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class HelpSection {


    private static HelpSection single_instance = null;
    private static Text passage;
    private static StackPane layout;
    static private Button buttonSettings;

    private HelpSection(StackPane layout){
        setText();
        buttonSettings = new Button("Settings");
        buttonSettings.setTranslateX(-330);
        buttonSettings.setTranslateY(-260);
        this.layout = layout;
        buttonSettings.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                removeElems();
                Settings settings = Settings.getInstance(layout);
                settings.showElements(layout);
            }
        });
    }


    public static HelpSection getInstance(StackPane layout){
        if (single_instance == null)
            single_instance = new HelpSection(layout);
        return single_instance;
    }

    private void setText(){

        passage = new Text("Software User Guide;");
        passage.setFill(Color.WHITE);
        passage.setStyle("-fx-font: 13 Courier;");
        passage.setX(-100);
        passage.setY(0);
    }


    public void showElems(){
        this.layout.getChildren().add(passage);
        this.layout.getChildren().add(buttonSettings);


    }
    public void removeElems(){
        this.layout.getChildren().remove(passage);
        this.layout.getChildren().remove(buttonSettings);

    }




}
