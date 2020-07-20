import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class AnonGWTCP implements Runnable {

    private ServerSocket anonServer;
    private int listenPort;
    private List<String> brothers;
    private int brothersIt;

    public AnonGWTCP(int listenPort, List<String> brothers) {
        this.listenPort = listenPort;
        this.brothers = brothers;
        this.brothersIt = 0;
    }

    @Override
    public void run() {
        try {
            System.out.println("------------- AnonGW TCP -------------");

            this.anonServer = new ServerSocket(this.listenPort);
            System.out.println(this.anonServer);

            while (true) {
                System.out.println("AnonGW Listening ...");

                // Wait for new Client
                Socket socket = anonServer.accept();
                // Create new clieant handler
                ClientHandler threadClient = new ClientHandler(socket, this.brothers.get(this.brothersIt));

                // Start client handler
                new Thread(threadClient).start();

                // Select next brother for next client connection
                this.brothersIt++;
                if (this.brothersIt>=this.brothers.size()) this.brothersIt=0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}