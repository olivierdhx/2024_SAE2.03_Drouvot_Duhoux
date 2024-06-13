import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class RequestHandler implements Runnable {
    private Socket clientSocket;
    private ConfigServ config;
    private Logger logger;
    private String numProcess;

    /**
     * Constructeur pour les requetes
     * @param clientSocket Connexion du client
     * @param config Configuration du server
     * @param logger Log du server
     * @param numProcess Numéro de processeur du serveur
     */

    public RequestHandler(Socket clientSocket, ConfigServ config, Logger logger, String numProcess) {
        this.clientSocket = clientSocket;
        this.config = config;
        this.logger = logger;
        this.numProcess = numProcess;
    }

    /**
     * Run la connexion
     */

    @Override
    public void run() {
        try {
            handleRequest();
        } catch (IOException e) {
            logger.logError(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vérifie si l'ip qui se connecte est autorisée grace au fichier de config
     * @param ip ip qui se connecte
     * @return true si autorisée
     */

    public boolean ipEstAutorisee(String ip){
        String ipReject = config.getReject().split("/")[0];

        return !(ipReject.compareTo(ip) == 0);
    }

    /**
     * Gère toute la connexion du client
     * @throws IOException
     */
    private void handleRequest() throws IOException {
        String ipConnection = clientSocket.getInetAddress().getHostAddress();
        logger.logAccess("Tentative de connexion par l'ip : " + ipConnection);
        System.out.println(ipConnection);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream outputStream = clientSocket.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream, true);

        String request = bufferedReader.readLine();
        String[] parts = request.split(" ");


        String method = parts[0];
        String path = parts[1];
        String version = parts[2];
        boolean status = false;
        if(path.equals(config.getLink()) || path.equals("/")){
            path = config.getLink() + "/index.html";
        }else{
            if(path.compareTo("/status") == 0){
                status = true;
            }
            path = config.getLink() + path + ".html";
        }

        if(!ipEstAutorisee(ipConnection)){
            path = config.getLink() + "/error403.html";
        }



        File file = new File(path.substring(1));
        if (!file.exists()) {
            file = new File(config.getLink().substring(1) + "/error404.html");
        }

        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        String html = new String(Files.readAllBytes(file.toPath()));
        if (contentType.startsWith("text/")) {
            if(status){
                int memoire = Utils.calculMemoire();
                int espaceDisque = Utils.calculEspaceDisque();
                html = Utils.ajoutInformationHTML(html, memoire, espaceDisque, numProcess);
            }
            sendTextResponse(printWriter, contentType, html);
        } else {
            sendBinaryResponse(outputStream, contentType, file);
        }

        printWriter.flush();
        clientSocket.close();
        bufferedReader.close();
    }

    private void sendTextResponse(PrintWriter printWriter, String contentType, String content) {
        printWriter.println("HTTP/1.1 200 OK");
        printWriter.println("Content-Type: " + contentType);
        printWriter.println("Content-Length: " + content.length());
        printWriter.println();
        printWriter.print(content);
    }

    private void sendBinaryResponse(OutputStream outputStream, String contentType, File file) throws IOException {
        PrintWriter printWriter = new PrintWriter(outputStream, true);
        printWriter.println("HTTP/1.1 200 OK");
        printWriter.println("Content-Type: " + contentType);
        printWriter.println("Content-Length: " + file.length());
        printWriter.println("responseType:'arraybuffer'");
        printWriter.println();
        printWriter.flush();

        Files.copy(file.toPath(), outputStream);
        outputStream.flush();
    }
}
