import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestHandler implements Runnable {
    private Socket clientSocket;
    private ConfigServ config;
    private Logger logger;
    private String numProcess;

    public RequestHandler(Socket clientSocket, ConfigServ config, Logger logger, String numProcess) {
        this.clientSocket = clientSocket;
        this.config = config;
        this.logger = logger;
        this.numProcess = numProcess;
    }

    @Override
    public void run() {
        try {
            handleRequest();
        } catch (IOException e) {
            logger.logError(e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean ipEstAutorisee(String ip){
        String ipReject = config.getReject().split("/")[0];
        return !(ipReject.compareTo(ip) == 0);
    }

    private void handleRequest() throws IOException {
        String ipConnection = clientSocket.getInetAddress().getHostAddress();
        System.out.println("IP récupérée");
        logger.logAccess("Tentative de connexion par l'ip : " + ipConnection);
        System.out.println(ipConnection);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream outputStream = clientSocket.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream, true);
        ;

        String request = bufferedReader.readLine();
        String[] parts = request.split(" ");

        String method = parts[0];
        String path = parts[1];
        String version = parts[2];
        boolean status = false;
        if (path.equals(config.getLink()) || path.equals("/")) {
            path = config.getLink() + "/fichierTest.html";
        } else {
            if (path.compareTo("/status") == 0) {
                status = true;
            }
            path = config.getLink() + path + ".html";
        }

        if (!ipEstAutorisee(ipConnection)) {
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
            if (status) {
                int memoire = Utils.calculMemoire();
                int espaceDisque = Utils.calculEspaceDisque();
                html = Utils.ajoutInformationHTML(html, memoire, espaceDisque, numProcess);
            }
            html = InterpreteurCode.presenceCode(html);
            sendTextResponse(printWriter, contentType, html);
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
        System.out.println("J'utilise le sendTextResponse");
    }
}
