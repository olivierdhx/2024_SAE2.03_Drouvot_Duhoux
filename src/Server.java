import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private String numProcess;

    /**
     * Créer un server a partir de la config et des logs
     */

    public Server() {
        ecrireNumProcess();
    }

    /**
     * Créer le fichier et met le numero de processeur
     */

    private void ecrireNumProcess() {
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
     * @param config l'objet ConfigServ que l'on passe en parametre au ConnectionHandler
     * @param logger l'objet Logger que l'on passe en parametre au ConnectionHandler
     * @throws IOException
     */

    public void start(ConfigServ config, Logger logger) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(config.getPort()))) {
            System.out.println("Server allumé, attente connection");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ConnectionHandler(clientSocket, config, logger, numProcess)).start();
            }
        }
    }
}
