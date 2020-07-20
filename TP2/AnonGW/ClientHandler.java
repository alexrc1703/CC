import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

public class ClientHandler implements Runnable {
	private Socket clientSocket;
    private String brother;
    private DataInputStream inFromClient;                   // Input from server
    private DataOutputStream outToClient;                   // Output to server
    private List<byte[]> finalres;

    public ClientHandler(Socket clientSocket, String bro) {
        this.clientSocket = clientSocket;
        this.brother = bro;
        this.finalres = new ArrayList<>();
    }

    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

    @Override
    public void run() {

        System.out.println("NEW CLIENT SENDING DATA!");

        try {
            this.outToClient = new DataOutputStream(clientSocket.getOutputStream());
            this.inFromClient = new DataInputStream(clientSocket.getInputStream());

            System.out.println("--- Send Port to Brother ---");
            // Send port to brother

            // Create the socket object for carrying the data.
            DatagramSocket s = new DatagramSocket();
            Integer port = s.getLocalPort();

            //byte[] portS = AESencrp.encrypt(port.toString().getBytes());  
            byte[] portS = port.toString().getBytes();

            // Create the datagramPacket for sending  the data.
	        DatagramPacket dpSend = null;

            System.out.println("Port: " + port);

            dpSend = new DatagramPacket(portS, portS.length, InetAddress.getByName(this.brother), 6666);
            s.send(dpSend);

            System.out.println("--------------------------------");

            System.out.println("--- Wait for Brother ---");

            // Read from cliente request
            byte[] bytes = new byte[16000];
            byte[] buf = new byte[16000];

            DatagramPacket receive = null;
            boolean wait = true;

            // Wait for server side anonGW connection
            while (wait) {
            	// Create a DatagramPacket to receive the data.
            	receive = new DatagramPacket(buf, buf.length);
               
                s.receive(receive);

            	if (data(buf).toString().equals("connected")) {
                    //System.out.println("Done received");
                    wait = false;
                    break;
                }
            }

            System.out.println("--------------------------------");

            System.out.println("--- Send Request to Brother ---");

            int count = inFromClient.read(bytes);
            System.out.println("Read");

            // Write request to brother
            System.out.write(bytes, 0, count);
            dpSend = new DatagramPacket(bytes, count, InetAddress.getByName(this.brother), port);
            s.send(dpSend);

            // signal end of request
            byte[] done = "done".getBytes();
            dpSend = new DatagramPacket(done, done.length, InetAddress.getByName(this.brother), port);
            s.send(dpSend);

            System.out.println("Done sent\n");
            System.out.println("--------------------------------");


        	System.out.println("---Receive Response---");
            // Receive response from brother
            // Receive from brother
            buf = new byte[16000];
            wait = true;
            while (wait) {
            	// Create a DatagramPacket to receive the data.
                receive = new DatagramPacket(buf, buf.length);

                s.receive(receive);
                //System.out.println("Packet received");

            	if (data(buf).toString().equals("done")) {
                    //System.out.println("Done received");
                    wait = false;
                    break;
                }

                /* Signal packet reception */
                byte[] rec = "rec".getBytes();
                dpSend = new DatagramPacket(rec, rec.length, InetAddress.getByName(this.brother), port);
                s.send(dpSend);
                //System.out.println("Signal Sent");
                
                byte[] b = new byte[receive.getLength()];
                b = Arrays.copyOfRange(buf, 0, receive.getLength());
            	//byte[] desBuf = AESencrp.decrypt(buf);
                this.finalres.add(b);
                buf = new byte[16000];
            }
            s.close();
            //System.out.println(message);
        } catch (Exception e) {
                e.printStackTrace();
        }

        System.out.println("--------------------------------");


        // Send to Client
        try {
            System.out.println("---Send Data to Client---");
        	for(byte[] res : this.finalres) {
                this.outToClient.write(res);
        		//this.outToClient.write(mes.getBytes());
        		//System.out.println(mes);
        	}
            // Closes
        	this.outToClient.close();
        	this.inFromClient.close();
            this.clientSocket.close();
            
            System.out.println("--------------------------------");
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
}