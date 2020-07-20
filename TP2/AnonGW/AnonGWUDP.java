import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;

public class AnonGWUDP implements Runnable {

    private String protectedIp;
    private List<String> brothers;

    public AnonGWUDP(String protectedIp, List<String> brothers) {
        this.protectedIp = protectedIp;
        this.brothers = brothers;
    }

    @Override

    public void run() {
        try {
            System.out.println("------------- AnonGW UDP -------------");

            // Create a socket to listen at port 6666
            DatagramSocket ds = new DatagramSocket(6666);
            byte[] receive = new byte[1024];


            // Listening to port 6666
            while (true)
            {
                DatagramPacket dpReceive = null;

                // create a DatagramPacket to receive the data.
                dpReceive = new DatagramPacket(receive, receive.length);

                // reveice datagrams
                ds.receive(dpReceive);

                // get the address from the sending brother
                String address = dpReceive.getAddress().toString().replace("/","");

                // recieve the data in byte buffer.
                if(this.brothers.contains(address)) { // checks if connection is from another anonGW

                    System.out.println("\nNew UDP connection\n");
                    // convert the data to int(port)
                    // String portSt = new String(AESencrp.decrypt(dpReceive.getData()));
                    String portSt = new String(dpReceive.getData());
                    int port = Integer.parseInt(portSt.trim());
                    System.out.println("Port received " + port + "\n");

                    // Create and start new server handler
                    ServerHandler serverHandler = new ServerHandler(port, this.protectedIp, dpReceive.getAddress());
                    new Thread(serverHandler).start();

                    receive = new byte[1024];
                } else {
                    System.out.println("Ilegal connection" + address);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}