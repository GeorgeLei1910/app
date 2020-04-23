package sample;

import javax.naming.ldap.Control;
import java.io.*;
import java.net.*;

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

    private byte[] buf = new byte[256];
    InetSocketAddress  endPoint1;
    InetSocketAddress  endPoint2;
    InetSocketAddress  endPoint3;
    InetSocketAddress  endPoint4;
    private static BufferedReader input = null;
    private static PrintWriter writer = null;
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
    public String connect(int typeOfConn) throws SocketTimeoutException, ConnectException {
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
            writer = new PrintWriter(socket.getOutputStream(), true);

            switch (typeOfConn) {
                case 1:
                    response = getStatus(input);
                    break;
                case 2:
                    response = getResponse(input);
                    writer.print("Start" + "\n");
                    break;
                case 3:
                    response = getResponse(input);
                    break;
            }
            socket.close();
        }catch(ConnectException ce){
            throw new ConnectException();
        }catch(SocketTimeoutException ste){
            ste.printStackTrace();
            response = "Disconnected";
            System.out.println(response);
            throw new SocketTimeoutException();
        }catch(IOException exception){
            exception.printStackTrace();
        }
        System.out.println(response);
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
        return response;
    }

    public String getIPAddress(){
        return serverIP;
    }
    public void setIPAddress(String newIP){
        serverIP = newIP;
    }
}
