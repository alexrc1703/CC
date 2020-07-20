import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ServerHandler implements Runnable {
	private DatagramSocket socket;
	private byte[] buf = new byte[1024];
    private List<String> message;
    private Integer port;
    private String serverIp;
    private InetAddress brother;

    public ServerHandler(Integer port, String serverIp, InetAddress brother) throws SocketException {
        this.socket = new DatagramSocket(port);            // creates a new socket in the port received from client
        this.port = port;
        this.message = new ArrayList<>();
        this.serverIp = serverIp;
        this.brother = brother;
		System.out.println("Create done");
    }

    public static StringBuilder data(byte[] a)
    {
        if (a == null) return null;

        StringBuilder ret = new StringBuilder();
        int i = 0;

        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

    public void run() {
        try {
            System.out.println("SERVER HANDLER: " + this.port + "\n\n");

            
            System.out.println("--- Signal Client ---");

                byte[] signal = "connected".getBytes();
	            //byte[] encBytes = AESencrp.encrypt(signal);
	            DatagramPacket dpSignal = new DatagramPacket(signal, signal.length, this.brother, this.port);
	            this.socket.send(dpSignal);

            System.out.println("--------------------------------");

            DatagramPacket receive = null;
            boolean receiving = true;

            System.out.println("--- Receiving Client Request ---");
            while (receiving) {
                // Create a DatagramPacket to receive the data.
                receive = new DatagramPacket(buf, buf.length);
                this.socket.receive(receive);
                // System.out.println(data(buf));
                String data = data(buf).toString();
                if (data.equals("done"))
                    receiving = false;
                else {
                    this.message.add(data);
                }
                this.buf = new byte[16000];
            }

            System.out.println("--------------------------------");
            // Send to Server
	        try {

	        	System.out.println("--- Sending Request to Server ---");

	            Socket socketServer = new Socket(this.serverIp, 80);

	            /* Alterar para bytes */
	            PrintWriter outToServer = new PrintWriter(socketServer.getOutputStream(), true);
	            DataInputStream inFromServer = new DataInputStream(socketServer.getInputStream());

	            for (String mes : message) {
	                outToServer.println(mes);
	                // System.out.println(mes);
	            }

	            System.out.println("--------------------------------");

	            System.out.println("--------------------------------\nReceive from Server\n          &&          \n Send to Client\n--------------------------------");

	            byte[] bytes = new byte[16000];

	            // Receive response from server
	            int count;
	            DatagramPacket toBrother = null;

				while ((count = inFromServer.read(bytes)) > 0) {
	                // System.out.write(bytes, 0, count);
	                // Send response to brother
                    // byte[] encByte = AESencrp.encrypt(bytes);
	                toBrother = new DatagramPacket(bytes, count, this.brother, this.port);
                    this.socket.send(toBrother);
                    //System.out.println("Packet Sent");
                    
                    /* Wait confirmation from clientHandler */
                    receive = new DatagramPacket(buf, buf.length);
                    //System.out.println("Signal Received");
	            }
	            bytes = "done".getBytes();
	            //byte[] encBytes = AESencrp.encrypt(bytes);
	            toBrother = new DatagramPacket(bytes, bytes.length, this.brother, this.port);
	            this.socket.send(toBrother);

	            outToServer.close();
	            inFromServer.close();
	            socketServer.close();
	            this.socket.close();

	            System.out.println("--------------------------------");
	            System.out.println("SERVER HANDLER END");
        	} catch (IOException e) {
            		e.printStackTrace();
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}