import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ConfigServ config;
    private Logger logger;
    private String numProcess;

    public Server(ConfigServ config, Logger logger) {
        this.config = config;
        this.logger = logger;
        setupPidFile();
    }

    private void setupPidFile() {
        RuntimeMXBean runMX = ManagementFactory.getRuntimeMXBean();
        numProcess = runMX.getName().split("@")[0];
        File filePID = new File("./var/run/myweb.pid");
        try (PrintWriter pwID = new PrintWriter(filePID)) {
            pwID.println(numProcess);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(config.getPort()))) {
            System.out.println("🟢 Le serveur est fonctionnel. En l'attente d'une connexion...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new RequestHandler(clientSocket, config, logger, numProcess)).start();
            }
        }
    }
}
