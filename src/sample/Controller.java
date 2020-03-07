package sample;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

public class Controller {
    private static Controller single_instance = null;
    private static LocalDate date = LocalDate.now(); // Create a date object
    private static String curFlightFolder = System.getProperty("user.dir");
    static private HashMap<String , HashMap<String, ArrayList<String>>> mapOfFiles = new HashMap<>();
    static private ObservableList<String> flights, surveys, blocks;
    private static File workingFolder = new File(curFlightFolder);
    private static Path dirPackagePath = Paths.get(curFlightFolder + "/Package");
    private static Path fileFlightTracker = Paths.get(curFlightFolder + "/Package" + "/Flights.txt");
    private static String Current_Survey = "";
    private static int Current_Flight = 0, Current_Block = 0;
    private static int [] Current_Lines = new int[] {0, 0};
    private static Stage primaryStage;
    private static String Curr_BlockName;
    private static StackPane layout;
    //private HashMap<String, ArrayList<String>> roundsNew = new HashMap<>();


    private Controller(StackPane layout){

        this.layout = layout;
        flights = FXCollections.observableArrayList();
        blocks = FXCollections.observableArrayList();
        surveys = FXCollections.observableArrayList();
        try{
        String s;
        InputStream ins = new FileInputStream(fileFlightTracker.toString());
        Reader r = new InputStreamReader(ins, "UTF-8"); // leave charset out for default
        BufferedReader br = new BufferedReader(r);
            String blockContained = "";
            String surveyContained = "";
            s = br.readLine();
        do{
            if(s.startsWith("Survey")){
                HashMap<String, ArrayList<String>> Blocks = new HashMap<>();
                mapOfFiles.put(s, Blocks);
                //roundsStrings.add(s);
                surveyContained = s;
                System.out.println("**********"+s);
            }else if(s.startsWith("B")){
                ArrayList<String> flights = new ArrayList<>();
                System.out.println("========="+s);
                mapOfFiles.get(surveyContained).put(s,flights);
                blockContained = s;

            }else {
                System.out.println("--------" + blockContained + "-" + s);
                mapOfFiles.get(surveyContained).get(blockContained).add(s);
            }
        }while ((s = br.readLine()) != null);
        if(!mapOfFiles.isEmpty()){
            //System.out.println("sdfsefwfw");
            setCurSurveyFolder(surveyContained);
            loadSurveys();
        }
        }catch (Exception e)
        {
            e.printStackTrace();
            System.err.println(e.getMessage()); // handle exception
        }
        System.out.println(mapOfFiles);
        setUpPackage();

    }

    public static Controller getInstance(StackPane layout){
        if (single_instance == null)
            single_instance = new Controller(layout);
        return single_instance;
    }


    //Add Stuff
    public static String addBlocks(){
        if(Current_Survey == "")
        {
            String s = "Survey Def";
            addSurvey(s);
            //setCurSurveyFolder(s);
        }
        String i = Integer.toString(mapOfFiles.get(Current_Survey).size()+1);
        String block = "B" + i + ", " + date;

        ArrayList<String> flights = new ArrayList<>();
        //flights.add("F0");
        mapOfFiles.get(Current_Survey).put(block, flights);
        Curr_BlockName = block;
        loadBlocks();
        updateFileStructure();
        createBlockFolder(i);
        setCurBlockFolder(Integer.parseInt(i), block);
        return block;
    }



    public static String addFlight(){
        int size = mapOfFiles.get(Current_Survey).get(Curr_BlockName).size();
        String i = Integer.toString(size + 1);
        String flight = "F"+i;
        //System.out.println(rounds.get(Current_Round-1).keySet());
        //String key = "B"+Current_Block+","+" "+"2019-07-31";
        //System.out.println(key);
        mapOfFiles.get(Current_Survey).get(Curr_BlockName).add(flight);
        //flightListLines.add(flight);
        loadFlights();
        updateFileStructure();
        //setCurFlightFolder(Integer.parseInt(i));
        createFlightFolder(i);
        return flight;
    }

    public static String addSurvey(String name){
        String nameOfSurvey =  "Survey_"+ name;
        //String i = Integer.toString(roundsStrings.size()+1);
        HashMap<String, ArrayList<String>> roundsNew = new HashMap<>();
        mapOfFiles.put(nameOfSurvey, roundsNew);
        setCurSurveyFolder(nameOfSurvey);
        //roundsStrings.add(nameOfSurvey);
        updateFileStructure();
        createSurveyFolder();
        setCurSurveyFolder(nameOfSurvey);
        loadSurveys();
        return nameOfSurvey;
    }

    // Load Files
    private static void loadFlights(){
        flights.clear();
        flights.add("");
        if(!Curr_BlockName.equals("")){
            for (String itm : mapOfFiles.get(Current_Survey).get(Curr_BlockName)) {
                flights.add(itm);
                //System.out.println("==>"+itm);
            }
        }
    }

    private static void loadBlocks(){
        blocks.clear();
        HashMap<Integer, String> hashMap = new HashMap<>();
        Set<String> keySet = mapOfFiles.get(Current_Survey).keySet();
        System.out.println("=====>"+keySet);
        String num = "";
        for(String itm : keySet){
            for(int j = 0; j < itm.length(); j++){
                char ch2 = itm.charAt(j);
                if(ch2 == ',') {
                    num = itm.substring(1, j);
                    System.out.println("------> "+num);
                    hashMap.put(Integer.parseInt(num), itm);
                    break;
                }
            }
        }

        blocks.add("");
        for(int i = 1; i < hashMap.keySet().size()+1; i++){
            blocks.add(hashMap.get(i));
            System.out.println(hashMap.get(i));
        }
    }


    static private void loadSurveys(){
        surveys.clear();
        for(String survey : mapOfFiles.keySet()){
            surveys.add(survey);
        }
    }

    static private void setUpPackage() {
        File[] listOfFiles = workingFolder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isDirectory() &&
                    listOfFiles[i].getAbsolutePath().equals(dirPackagePath.toString())) {
                System.out.println(curFlightFolder + "/Package");
                setUpScripts();
                    return;
            }
        }
        new File(dirPackagePath.toString()).mkdirs();
        setUpScripts();

    }


    static private void setUpScripts(){

        File dirPackage = new File(dirPackagePath.toString());
        File[] listOfFiles = dirPackage.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() &&
                    listOfFiles[i].getAbsolutePath().equals(fileFlightTracker.toString())){
                return;
            }
        }
        updateFileStructure();


    }


    static private void updateFileStructure(){

        try {
            File file = new File(fileFlightTracker.toString());
            if (file.exists()){
                file.delete();
            }
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileFlightTracker.toString(), true)));
            out.flush();
            for(String survey : mapOfFiles.keySet()){
                out.write(survey+"\r\n");
                for(String key : mapOfFiles.get(survey).keySet()){
                    out.write(key+"\r\n");
                    for(String flight : mapOfFiles.get(survey).get(key)){
                        out.write(flight+"\r\n");
                    }
                }
            }
            out.close();
        }catch (IOException e) {

        }
    }
    // When apply is clicked on
    static public void updateFilePlanSetting(String From, String To,String FromTie, String ToTie,String useSep, String seperateLines, String applyOrNot,int curFlight){
        File flightPlanFolder = new File(curFlightFolder+"/Data/" + Current_Survey+"/Block" +
                Current_Block + "/Flight"+ getCurFlight()+"/flight_plan"+ getPrefixToFlight() +"-flightPlan.txt");
        try {
//            if (flightPlanFolder.exists()){
//                flightPlanFolder.delete();
//                System.out.println("Deleted " + flightPlanFolder.toString());
//            }else{
//                flightPlanFolder.createNewFile();
//                System.out.println("Created " + flightPlanFolder.toString());
//            }
            // Write the new settings on the Flight's flightPlan.txt
            PrintWriter out = new PrintWriter(flightPlanFolder);
            out.flush();
            out.write("From:");
            out.write(From);
            out.write("\r\n");
            out.write("To:");
            out.write(To);
            out.write("\r\n");
            out.write("fromTie:");
            out.write(FromTie);
            out.write("\r\n");
            out.write("toTie:");
            out.write(ToTie);
            out.write("\r\n");
            out.write("useSeperateLines:");
            out.write(useSep);
            out.write("\r\n");
            out.write("seperateLines:");
            out.write(seperateLines);
            out.write("\r\n");
            out.write("applyOrNot:");
            out.write(applyOrNot);
            out.write("\r\n");
            out.close();
        }catch (IOException e) {

        }
    }



    //Create Stuff
    static private void createFlightFolder(String i){
        File flightFolder = new File(curFlightFolder+"/Data/" + Current_Survey + "/Block" + Current_Block + "/Flight"+i);
        flightFolder.mkdirs();
        File rawDataFolder = new File(curFlightFolder+"/Data/" + Current_Survey+"/Block" + Current_Block + "/Flight"+i+"/raw_data");
        rawDataFolder.mkdirs();
        File figuresFolder = new File(curFlightFolder+"/Data/" + Current_Survey+"/Block" +Current_Block + "/Flight"+ i+"/figures");
        figuresFolder.mkdirs();
        File flightPlanFolder = new File(curFlightFolder+"/Data/" + Current_Survey+"/Block" +Current_Block + "/Flight"+ i+"/flight_plan");
        flightPlanFolder.mkdirs();
        updateFilePlanSetting("", "", "","","" ,"","",Integer.parseInt(i));
    }


    static private void createBlockFolder(String i){
        File flightFolder = new File(curFlightFolder+"/Data/"+ Current_Survey + "/Block" + i);
        flightFolder.mkdirs();
        File rawDataFolder = new File(curFlightFolder+"/Data/" + Current_Survey+"/Block" + i+"/raw_data");
        rawDataFolder.mkdirs();
        File figuresFolder = new File(curFlightFolder+"/Data/" + Current_Survey+"/Block" + i+"/figures");
        figuresFolder.mkdirs();
        File flightPlanFolder = new File(curFlightFolder+"/Data/" + Current_Survey+"/Block" + i+"/flight_plan");
        flightPlanFolder.mkdirs();
    }

    static private void createSurveyFolder(){
        try{
        File  roundFolder = new File(curFlightFolder+"/Data/" + Current_Survey);
        File  baseMagFolder = new File(curFlightFolder+"/Data/"+Current_Survey+"/BaseMag" );
        File  flightPlanJobFolder = new File(curFlightFolder+"/Data/"+Current_Survey+"/FlightPlan" );
        File  rawDataFolderSurvey = new File(curFlightFolder+"/Data/"+Current_Survey+"/raw_data" );
        File  JobPlanFile = new File(curFlightFolder+"/Data/"+Current_Survey+"/FlightPlan/"+ getPrefixToSurvey() +"-plan_settings.txt" );
        roundFolder.mkdirs();
        baseMagFolder.mkdirs();
        flightPlanJobFolder.mkdirs();
        rawDataFolderSurvey.mkdirs();
        JobPlanFile.createNewFile();
        }catch(IOException e){
        }
    }
    //
    static public void setCurSurveyFolder(String name){
        Current_Survey = name;
        setCurBlockFolder(0, "");
        System.out.println( "<<<<<<<<<<<<<<<<<  CURRENTLY AT SURVEY NUMBER (("+ name+ ")) >>>>>>>>>>>>>>>>>");
        MainInterface.getInstance(layout).updateMainInterface(Current_Survey);
        FlightPlanning.getInstance(layout).updateFlightPlanInfo();
        loadBlocks();
    }




    static public void setCurFlightFolder(int i){
        Current_Flight = i;
        if(i != 0)
            FlightPlanning.getInstance(layout).updateFlightPlanInfo();
        else
            FlightPlanning.getInstance(layout).updateFlightPlanInfo();

        System.out.println( "<<<<<<<<<<<<<<<<<  CURRENTLY AT FLIGHT NUMBER (("+ i+ ")) >>>>>>>>>>>>>>>>>");
        FlightPlanning.getInstance(layout).updateFlightPlanInfo();
    }


    static public void setCurBlockFolder(int i, String name){
        Current_Block = i;
        setCurFlightFolder(0);
        System.out.println(mapOfFiles);
        for(String key : mapOfFiles.get(Current_Survey).keySet()){
            if(name.equals(key)){
                Curr_BlockName = key;
                System.out.println(Curr_BlockName);
            }
        }
        if (name.equals(""))
            Curr_BlockName = "";
        System.out.println( "<<<<<<<<<<<<<<<<<  CURRENTLY AT BLOCK NUMBER (("+ i+ ")) >>>>>>>>>>>>>>>>>");
        FlightPlanning.getInstance(layout).updateFlightPlanInfo();
        loadFlights();
    }

    //Accessors
    static public String getCurSurvey(){    return Current_Survey;}
    static public int getCurBlock(){        return Current_Block;}
    static public int getCurFlight(){       return Current_Flight;}

    public static ObservableList<String> getBlocks(){ return blocks; }
    public static ObservableList<String> getFlights(){ return flights; }
    public static ObservableList<String> getSurveys(){ return surveys; }

    static public Stage getPrimaryStage(){ return primaryStage; }

    public static String getCurDataFolder(){
        String curFolder = "";

        if(Current_Flight != 0  && Current_Block != 0){
            curFolder = curFlightFolder + "/Data/" + Current_Survey + "/Block" + Current_Block + "/Flight" + Current_Flight + "/raw_data";
        }else if(Current_Block != 0){
            curFolder = curFlightFolder + "/Data/" + Current_Survey + "/Block" + Current_Block  + "/raw_data";
        }else{
            curFolder = curFlightFolder + "/Data/" + Current_Survey + "/raw_data";
        }
        return  curFolder;

    }
    static public void setPrimaryStage(Stage primaryStageNew){
        primaryStage = primaryStageNew;
    }
    //Gets the file prefix of sth.
    public static String getPrefixToSurvey(){
        String [] surveyName = getCurSurvey().split("_");
        return "/S"+surveyName[1];
    }
    public static String getPrefixToBlock(){
        String [] surveyName = getCurSurvey().split("_");
        return "/S"+surveyName[1] +"-B"+Controller.getCurBlock();
    }
    public static String getPrefixToFlight(){
        String [] surveyName = getCurSurvey().split("_");
        return "/S"+surveyName[1] +"-B"+Controller.getCurBlock() + "-F"+Controller.getCurFlight();
    }

    //Gets the String for the Path to Each folder
    public static String getPathToSurvey(){
        return "/Data/"+getCurSurvey();
    }
    public static String getPathToBlock(){
        return "/Data/"+getCurSurvey()+"/Block"+getCurBlock();
    }
    public static String getPathToFlight(){
        return "/Data/"+getCurSurvey()+"/Block"+getCurBlock()+"/Flight"+getCurFlight();
    }

    public static void pythonConsole(Process p) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
    }


    private static void updateLines(){

    }
}
