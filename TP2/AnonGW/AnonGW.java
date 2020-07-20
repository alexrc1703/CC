import java.util.ArrayList;
import java.util.List;

public class AnonGW {

    public static void main(final String[] args) {

        int size = args.length;
        String protectIp = "";
        int port = 0;
        List<String> brothers = new ArrayList<String>();

        for (int i = 0; i <= 5; i += 2) {
            switch (args[i]) {
                case "target-server":
                    protectIp = args[i + 1];
                    break;
                case "port":
                    port = Integer.parseInt(args[i + 1]);
                    break;
                case "overlay-peers":
                    i++;
                    for (; i < size; i++) {
                        brothers.add(args[i]);
                    }
                    break;
                default:
                    System.out.println("Error Command");
            }
        }

        // Thread listen TCP
        AnonGWTCP anonTCP = new AnonGWTCP(port, brothers);
        new Thread(anonTCP).start();

        // Thread listen UDP
        AnonGWUDP anonUDP = new AnonGWUDP(protectIp, brothers);
        new Thread(anonUDP).start();

    }
}
