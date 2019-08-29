package blackjackclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class BlackJackClient {

        public static void main(String[] args) {
        String hostname ="localhost";
        int serverPort = 2121;

        try (
                Socket echoSocket = new Socket(hostname, serverPort);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream());
                Scanner in = new Scanner(echoSocket.getInputStream());
                Scanner stdIn = new Scanner(System.in);    
            )
        {
                WriterToServer wts = new WriterToServer(echoSocket, out, stdIn);
                ReaderFromServer rfs = new ReaderFromServer(echoSocket, in);
                wts.start();
                rfs.start();
                
                
                try{
                   wts.join(); 
                   rfs.join(); 
                }catch(InterruptedException e) {
                    System.out.println("InterruptedException");
                }   
                
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ReaderFromServer extends Thread {
    private final Socket socket;
    private final Scanner input;

    public ReaderFromServer(Socket socket, Scanner input) {
        this.socket = socket;
        this.input = input;
    }

    @Override
    public void run() {
        while(input.hasNextLine()) {
            String line = input.nextLine();
            System.out.println(line);
            
            
            }
        }        
}
          

class WriterToServer extends Thread {
    private final Socket socket;
    private final PrintWriter output;
    private final Scanner fromconsole;

    public WriterToServer(Socket socket, PrintWriter output, Scanner fromconsole) {
        this.socket = socket;
        this.output = output;
        this.fromconsole = fromconsole;
    }

    @Override
    public void run() {
        while(true) {
            String msg = fromconsole.nextLine();
            output.println(msg);
            output.flush();
        }   
    }
        
}   

    
