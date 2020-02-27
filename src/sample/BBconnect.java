package sample;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

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
    private static String serverIP = "192.168.8.1";


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
    public String connect(int typeOfConn) throws SocketTimeoutException {
        InetSocketAddress endPoint = null;
        switch (typeOfConn){
            // Status if Logging or not.
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
            socket.connect(endPoint, 3000);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));


            if(typeOfConn == 1){
                response = getStatus(input);
            }else{
                response = getResponse(input);
            }
            socket.close();

        }catch(SocketTimeoutException ste){
            ste.printStackTrace();
            response = "Disconnected";
            System.out.println(response);
            throw new SocketTimeoutException();
        }
        catch(IOException exception){
            exception.printStackTrace();
        }
        return response;
    }


    private static String getResponse(BufferedReader input) throws IOException{
        String response = input.readLine();
        while(response == null){
            response = input.readLine();
        }
        System.out.println("---->"+response);
        return response;
    }
    private static String getStatus(BufferedReader input) throws IOException{
        String response = input.readLine();
        System.out.println(response.contains("Logger On"));
        return response;
    }

    public String getIPAddress(){
        return serverIP;
    }
    public void setIPAddress(String newIP){
        serverIP = newIP;
    }
}
