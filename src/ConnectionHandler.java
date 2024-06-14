import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class ConnectionHandler implements Runnable {
    private Socket clientSocket;
    private ConfigServ config;
    private Logger logger;
    private String numProcess;

    public ConnectionHandler(Socket clientSocket, ConfigServ config, Logger logger, String numProcess) {
        this.clientSocket = clientSocket;
        this.config = config;
        this.logger = logger;
        this.numProcess = numProcess;
    }

    @Override
    public void run() {
        try {
            gererConnection();
        } catch (IOException e) {
            logger.logError(e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean ipEstAutorisee(String ip) {
        // Vérifie si l'ip fait partie des IP autorisées
        String ipReject = config.getReject().split("/")[0];
        return !(ipReject.compareTo(ip) == 0);
    }

    private void gererConnection() throws IOException {
        String ipConnection = clientSocket.getInetAddress().getHostAddress();
        logger.logAccess("Tentative de connexion par l'ip : " + ipConnection);

        System.out.println("Connection recue " + ipConnection);

        // On lit la connexion du client
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        // On se prépare pour écrire la réponse à sa connexion
        OutputStream outputStream = clientSocket.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream, true);

        // On récupère l'adresse demandée par le client
        String request = bufferedReader.readLine();
        if (request == null) {
            clientSocket.close();
            return;
        }
        String[] parts = request.split(" ");

        // On récupère la page demandée
        String path = parts[1];

        boolean status = false;
        if (path.equals(config.getLink()) || path.equals("/")) {
            // Si rien n'est précisé, on renvoie l'index
            path = config.getLink() + "/index.html";
        } else {
            if (path.compareTo("/status.html") == 0) {
                // Si c'est le status, le booléen passe à true
                status = true;
            }
            // On ne rajoute pas .html à la page demandée
            path = config.getLink() + path;
        }

        // Si l'ip ne fait pas partie des IP autorisées, on le renvoie vers l'erreur 403
        if (!ipEstAutorisee(ipConnection)) {
            path = config.getLink() + "/error403.html";
            logger.logAccess("Connection refusée " + ipConnection);
            System.out.println("Connection refusée, ip non autorisée : " + ipConnection);
        }

        // Si la page demandée n'existe pas, on le renvoie vers l'erreur 404
        File file = new File(path.substring(1));
        if (!file.exists()) {
            file = new File(config.getLink().substring(1) + "/error404.html");
        }

        String contentType = getContent(String.valueOf(file.toPath()));

        if (contentType.startsWith("text/")) {
            // Traitement pour les fichiers texte
            String html = new String(Files.readAllBytes(file.toPath()));
            // Si status est à true, on remplace dans le HTML les informations demandées
            if (status) {
                int memoire = Utils.calculMemoire();
                int espaceDisque = Utils.calculEspaceDisque();
                html = Utils.ajoutInformationHTML(html, memoire, espaceDisque, numProcess);
            }
            // On vérifie si du code est présent dans le HTML
            html = InterpreteurCode.presenceCode(html);
            // On renvoie la réponse au client
            ecrirePage(printWriter, contentType, html);
        } else {
            // Traitement pour les fichiers binaires
            ecrireFichierBinaire(outputStream, contentType, file);
        }

        printWriter.flush();
        clientSocket.close();
        bufferedReader.close();
    }

    /**
     * Ecrit la page
     * @param printWriter output
     * @param contentType type de contenu
     * @param content contenu
     */

    private void ecrirePage(PrintWriter printWriter, String contentType, String content) {
        // Renvoie de la réponse au client
        printWriter.println("HTTP/1.1 200 OK");
        printWriter.println("Content-Type: " + contentType);
        printWriter.println("Content-Length: " + content.length());
        printWriter.println();
        printWriter.print(content);
    }

    /**
     * Ecris la page si fichier
     * @param outputStream output
     * @param contentType type de contenu
     * @param file fichier
     * @throws IOException
     */

    private void ecrireFichierBinaire(OutputStream outputStream, String contentType, File file) throws IOException {
        PrintWriter printWriter = new PrintWriter(outputStream, true);
        printWriter.println("HTTP/1.1 200 OK");
        printWriter.println("Content-Type: " + contentType);
        printWriter.println("Content-Length: " + file.length());
        printWriter.println();
        printWriter.flush();

        Files.copy(file.toPath(), outputStream);
        outputStream.flush();
    }

    public String getContent(String filePath){
        String extension = "";
        String res = "application/octet-stream";
        int i = filePath.lastIndexOf('.');
        if(i > 0){
            extension = filePath.substring(i+1);
        }

        switch(extension){
            case "html":
                res = "text/html";
                break;
            case "jpg":
                res = "image/jpeg";
                break;
            case "png":
                res = "image/png";
                break;
        }
        return res;
    }
}
