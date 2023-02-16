package ServerSide;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A server class responsible for handling multiple client request at the same time via
 * multi-threading. This class can start new thread for multiple client requests
 */
public class serverThreaded {
    /**
     * Connects the client to the server on a port, then establish the connection
     * via a socket object and creates a thread to handle the request
     */

    private void connectionToClients() {
        System.out.println("Server is starting.");
        try(ServerSocket serverSocket = new ServerSocket(2000)){

            while (true){
                System.out.println("Server: Waiting for connecting client...");

                try{
                    Socket socket = serverSocket.accept();

                    threadHandler threadHandler = new threadHandler(socket);
                    Thread connectionThread = new Thread(threadHandler);
                    connectionThread.start();
                }catch (IOException ex){
                    Logger.getLogger(serverThreaded.class.getName()).log(Level.SEVERE,null,ex);
                    System.out.println("Server: closed could not start a connection with the client");
                    System.out.println(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }catch (IOException ex){
            Logger.getLogger(serverThreaded.class.getName()).log(Level.SEVERE,null,ex);
            System.out.println("Server: Closed down socket see msgg below");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main (String[] args) throws IOException {
        serverThreaded simpleConnection = new serverThreaded();
        simpleConnection.connectionToClients();
    }
}
