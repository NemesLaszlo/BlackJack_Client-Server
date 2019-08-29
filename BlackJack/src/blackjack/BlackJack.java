package blackjack;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class BlackJack {

    public static void main(String[] args) throws InterruptedException, IOException {
            Accepter server = new Accepter();
            server.start();
    }
}

class Accepter extends Thread {

    public ServerSocket serverSocket;
    public ArrayList<Client> clients;
    
    public Accepter() throws IOException {
        this.serverSocket = new ServerSocket(2121);
        this.clients = new ArrayList<>();
    }

    @Override
    public void run() {
        System.out.println("Starting accepter");
        while (true) {
            try {
                Socket s = serverSocket.accept();
                Client client = new Client(s, clients);
                client.start();
                synchronized (clients) {
                    clients.add(client);
                }
                System.out.println(s.getRemoteSocketAddress());
                Game game = new Game(s,clients);
                game.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Game extends Thread {
    private Socket s;
    private ArrayList<Client> clients;
    private Scanner in;
    private PrintWriter out;
    public boolean isGameOver = false;
    
    public Game(Socket socket, ArrayList<Client> clients) throws IOException {
        this.s = socket;
        this.clients = clients;
        this.in = new Scanner(socket.getInputStream());
        this.out = new PrintWriter(socket.getOutputStream());
    }
    
    @Override
    public void run() {
        while(!isGameOver) {
            ArrayList<Client> Gameclients;
            synchronized(clients) {
                Gameclients = clients;
            }
            
            if( Gameclients.size() >= 1) {
                int count = 0;
                for(Client client : Gameclients) {
                    if( client.bust == false && client.stick == false ) {
                        count++;
                    }
                }
            
                if(count == 0) {
                    isGameOver = true;
                    int max = Gameclients.get(0).startValue;
                    if( max > 21 ) {
                        max = 0;
                    }
                    for(Client x : Gameclients) {
                        if( x.startValue > max && x.startValue <= 21 ) {
                            max = x.startValue;
                        }
                    }
                    String winner = "";
                    for(Client y : Gameclients) {
                        if( y.startValue == max ) {
                            winner += " " + y.name + " ";
                        }
                    }
                    
                    for( Client z : Gameclients) {
                        z.out.println("A gyoztes(ek): " + winner);
                        z.out.flush();
                         try {
                            z.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    
                }
            }         
        }              
    }               
}
        
class Client extends Thread {

    private Socket s;
    private ArrayList<Client> clients;
    public String name;
    private Scanner in;
    public PrintWriter out;
    public boolean stick = false;
    public boolean bust = false;
    public int startValue = 0;
    Random rand = new Random();

    public Client(Socket socket, ArrayList<Client> clients) throws IOException {
        this.s = socket;
        this.clients = clients;
        this.name = "";
        this.in = new Scanner(socket.getInputStream());
        this.out = new PrintWriter(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            out.println("Eloszor add meg a neved!");
            out.flush();
            while (in.hasNextLine()) {
                String[] cmd = in.nextLine().split(" ");
                if(cmd.length == 1 && !cmd[0].equals("hit") && !cmd[0].equals("stick") ) {
                    name = cmd[0];
                    System.out.println("Name: " + name);
                    startValue += drawNumber() + drawNumber();
                    out.println("OK");
                    out.println("Lapjaid osszege: " + startValue);
                    out.println("Hit - lapkeres | Stick - megallok");
                    out.flush(); 
                }else {
                    switch(cmd[0]) {
                        case "hit":
                            if( stick || bust ) {
                                out.print("Nem Kell tobb lapot huznod! - vard meg a jatek veget");
                                out.flush();
                                break;
                            }
                            int cardValue = drawNumber();
                            int actualValue = startValue;
                            int result = actualValue + cardValue;
                            startValue = result;
                            System.out.println(result);
                            out.println("Lapjaid osszege: " + startValue);
                            out.flush();

                            if(result == 21) {
                                this.bust = true;
                                this.stick = true;
                                out.println("21-ed van!");
                                out.flush();
                            } else if(result > 21) {
                                this.bust = true;
                                this.stick = false;
                                out.println("Tobb mint 21-ed van! - BURST - Nem kell több lapot kerned!");
                                out.flush();
                            }else {
                                out.println("Kerhetsz meg lapot ha szeretnel!");
                                out.flush();
                            }  
                            break;
                        case "stick":  
                                if( stick || bust ) {
                                    out.print("Nem Kell tobb lapot huznod! - vard meg a jatek veget");
                                    out.flush();
                                    break;
                                }
                                this.bust = false;
                                this.stick = true;
                                out.println("Nem kersz tobbet!");
                                out.flush();
                            break;
                        default:
                            System.out.println("Unknown command");
                            break;
                    }
                }
            }
        System.out.println("Client disconnected");
        try {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void close() throws Exception {
        out.close();
        in.close();
        s.close();
    }
    
    private int drawNumber() {
            return rand.nextInt(9) + 2;
    }    
}
