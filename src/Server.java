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

    /**
     * Créer un server a partir de la config et des logs
     * @param config
     * @param logger
     */

    public Server(ConfigServ config, Logger logger) {
        this.config = config;
        this.logger = logger;
        setupPidFile();
    }

    /**
     * Créer le fichier et met le numero de processeur
     */

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

    /**
     * Start le serveur
     * @throws IOException
     */

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
