package sample;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataQuality {
    private static DataQuality single_instance = null;
    static private CheckBox cb1, cb2, cb3, cb4, cb5, cb6, cb7, cb8, cbUseRaw;
    static private Button buttonGraph, buttonGraphTrim;
    static private List<graphType> listOfGraphs = new ArrayList<graphType>();
    static private StackPane layout;
    static private Text txtRange;
    static private TextField r1;
    static private TextField r2;


     enum graphType {
        FourthDiff,
         FourthDiffBMag,
        MagProfile,
         BaseMagProfile,
        LaserProfile,
         FlightMap,
         FlightMapPiksivsMav,
         MavAltvsPiksiAlt,
    }



    private DataQuality(StackPane layout){
        this.layout = layout;
        txtRange = new Text("Range");
        r1 = new TextField();
        r2 = new TextField();
        r1.setMaxWidth(50);
        r2.setMaxWidth(50);
         cb1 = new CheckBox("4th Difference");
         cb2 = new CheckBox("4th Difference of Base Mag");
         cb3 = new CheckBox("Mag Profile");
         cb4 = new CheckBox("Laser Profile");
         cb5 = new CheckBox("Base Mag Profile");
         cb6 = new CheckBox("Flight Map");
         cb7 = new CheckBox("Mav Alt vs Piksi Alt");
        cb8 = new CheckBox("Flight Map Piksi vs Mav");

        buttonGraph = new Button("Graph");
        buttonGraphTrim = new Button("trimmed Graph");
        cb1.setTranslateX(-240);
        cb1.setTranslateY(-150);
        txtRange.setTranslateX(-150);
        txtRange.setTranslateY(-150);
        r1.setTranslateX(-100);
        r1.setTranslateY(-150);
        r2.setTranslateX(-40);
        r2.setTranslateY(-150);
        r1.setText("0");
        r2.setText("0");
        cb2.setTranslateX(-200);
        cb2.setTranslateY(-120);
        cb3.setTranslateX(-249);
        cb3.setTranslateY(-90);
        cb4.setTranslateX(-246);
        cb4.setTranslateY(-60);
        cb5.setTranslateX(-232);
        cb5.setTranslateY(-30);
        cb6.setTranslateX(-252);
        cb6.setTranslateY(0);
        cb7.setTranslateX(-228);
        cb7.setTranslateY(30);
        cb8.setTranslateX(-214);
        cb8.setTranslateY(60);

        cbUseRaw = new CheckBox("Use Raw Data");
        cbUseRaw.setTranslateX(-200);
        cbUseRaw.setTranslateY(-200);


        buttonGraph.setTranslateX(200);
        buttonGraph.setTranslateY(-100);

        buttonGraphTrim.setTranslateX(200);
        buttonGraphTrim.setTranslateY(-70);

        buttonGraph.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                fillGraphList();
                try{
                    String path = System.getProperty("user.dir");
                    String pathPython = path + "/Package/pythontest.py";
                    Iterator<graphType> listInterator = listOfGraphs.iterator();
                    String param = "";
                   while(listInterator.hasNext()){
                        param = listInterator.next().toString();
                        System.out.println(">>>>>>>>>>>>>>>>> Graphing "+ param + " Attempted");
                        String command = "python " +pathPython+" -m ";
                        String args = " -r1 "+r1.getText() + " -r2 "+r2.getText();
                        String  pathFolder = Controller.getCurDataFolder();
                        command = command + param + " -f "+pathFolder+ args;
                        System.out.println(command);
                        GraphingThread graphingThread = new GraphingThread(command);
                        graphingThread.showGraph();

                    }

                }catch(Exception e){

                }
            }
        });



    }

    private static void fillGraphList(){
        listOfGraphs.clear();
        if(cb1.isSelected())
            listOfGraphs.add(graphType.FourthDiff);
        if(cb2.isSelected())
            listOfGraphs.add(graphType.FourthDiffBMag);
        if(cb3.isSelected())
            listOfGraphs.add(graphType.MagProfile);
        if(cb4.isSelected())
            listOfGraphs.add(graphType.LaserProfile);
        if(cb5.isSelected())
            listOfGraphs.add(graphType.BaseMagProfile);
        if(cb6.isSelected())
            listOfGraphs.add(graphType.FlightMap);
        if(cb7.isSelected())
            listOfGraphs.add(graphType.MavAltvsPiksiAlt);
        if(cb8.isSelected())
            listOfGraphs.add(graphType.FlightMapPiksivsMav);

    }

    public static DataQuality getInstance(StackPane layout)
    {
        if (single_instance == null)
            single_instance = new DataQuality(layout);


        return single_instance;
    }

    public void showElems(){
        this.layout.getChildren().add(cb1);
        this.layout.getChildren().add(cb2);
        this.layout.getChildren().add(cb3);
        this.layout.getChildren().add(cb4);
        this.layout.getChildren().add(cb5);
        this.layout.getChildren().add(cb6);
        this.layout.getChildren().add(cb7);
        this.layout.getChildren().add(cb8);
        this.layout.getChildren().add(buttonGraph);
        this.layout.getChildren().add(buttonGraphTrim);

        this.layout.getChildren().add(txtRange);
        this.layout.getChildren().add(r1);
        this.layout.getChildren().add(r2);
        this.layout.getChildren().add(cbUseRaw);

    }


    public void removeElements(){
        this.layout.getChildren().remove(cb1);
        this.layout.getChildren().remove(cb2);
        this.layout.getChildren().remove(cb3);
        this.layout.getChildren().remove(cb4);
        this.layout.getChildren().remove(cb5);
        this.layout.getChildren().remove(cb6);
        this.layout.getChildren().remove(cb7);
        this.layout.getChildren().remove(cb8);

        this.layout.getChildren().remove(buttonGraph);
        this.layout.getChildren().remove(buttonGraphTrim);

        this.layout.getChildren().remove(txtRange);
        this.layout.getChildren().remove(r1);
        this.layout.getChildren().remove(r2);
        this.layout.getChildren().remove(cbUseRaw);


    }



}
