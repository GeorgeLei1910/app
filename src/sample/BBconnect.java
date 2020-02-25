package sample;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BBconnect {
    private static Socket socket= null;
    // Connect Port
    private static int port1 = 5090;
    // Start Port
    private static int port2 = 5091;
    // Stop Port
    private static int port3 = 5092;
    // Download Port
    private static int port4 = 5093;
    InetSocketAddress  endPoint1;
    InetSocketAddress  endPoint2;
    InetSocketAddress  endPoint3;
    InetSocketAddress  endPoint4;
    private static BufferedReader input = null;
    private static BufferedWriter writer = null;
    private static String serverIP = "192.168.6.2";


    private static BBconnect single_instance = null;

    // Constructor
    private BBconnect(){
            endPoint1 = new InetSocketAddress(serverIP, port1);
            endPoint2 = new InetSocketAddress(serverIP, port2);
            endPoint3 = new InetSocketAddress(serverIP, port3);
            endPoint4 = new InetSocketAddress(serverIP, port4);
    }

    //If instance is not created, then create instance
    public static BBconnect getInstance(){
        if (single_instance == null)
            single_instance = new BBconnect();
        return single_instance;
    }

    // connects to the Beaglebone
    public String connect(int typeOfConn){
        InetSocketAddress endPoint = null;
        switch (typeOfConn){
            // Connected
            case 1: {
                endPoint = endPoint1;
                break;
            }
            // Start
            case 2: {
                endPoint = endPoint2;
                break;
            }
            // Stop
            case 3: {
                endPoint = endPoint3;
                break;
            }
            // Download
            case 4: {
                endPoint = endPoint4;
                break;
            }
        }
        String response = "Error";
        try {
            socket = new Socket();


            socket.connect(endPoint, 8000);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            if(typeOfConn == 4){
                getResponseDownload(input);

            }else{
                response = getResponse(input);
            }
            socket.close();

        }catch(IOException exception){
            exception.printStackTrace();
        }
        return response;
    }

    public int getState(){
        int [] ports = new int [] {port2, port3, port4};
        Socket testSocket;
        int i = 0;
        while(i < ports.length) {
            try {
                testSocket = new Socket(serverIP, ports[i]);
                testSocket.close();
            } catch (IOException ioe) {
                System.out.println("Port " + ports[i] + " Closed");
                i++;
            }finally{
                System.out.println("Port Open " + ports[i]);
                return i;
            }
        }
        return 0;
    }






    private static String getResponse(BufferedReader input) throws IOException{
        String response = input.readLine();
        while(response == null){
            response = input.readLine();

        }
        System.out.println("---->"+response);
        return response;

    }


    private static void getResponseDownload(BufferedReader input) throws IOException{
        String response = input.readLine();
        String path = Controller.getCurDataFolder();
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path+"/dummy.txt", true)));
        out.close();
        File dummyFile = new File(path+"/dummy.txt");
        dummyFile.delete();
        while(!response.startsWith("END")){
            if(response.startsWith("Filename")) {
                out.close();
                String segments[] = response.split(": ");
                String seg = segments[1];
                out = new PrintWriter(new BufferedWriter(new FileWriter(path +"/"+seg, true)));

                System.out.println("=======================================================>>>>>>>" + response + "\r\n");

            }else{
                System.out.println("--->"+response+ "\n");
                out.write(response+ "\r\n");
            }
            response = input.readLine();
        }
        out.close();
        System.out.println("---->"+response+ "\r\n");

    }

}
