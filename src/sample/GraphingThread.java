package sample;
import javafx.scene.text.Text;

import java.lang.Thread;


public class GraphingThread extends Thread {

    static  private Text text;
    String command;


    public GraphingThread(String command){
        super("my Graphing thread");

        this.command =  command;


    }

    public void run()
    {
        try {
            //ProcessBuilder pb = new ProcessBuilder("python", "/Users/faridfaraji/Desktop/side_projects/Stratus_project/SoftwareFolder/Package/pythontest.py" ,"-m", "FourthDiff");
            //Process process = pb.start();
            //process.waitFor();

            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
        }catch(Exception e){

        }

    }

    public void showGraph(){
        start();


    }



}