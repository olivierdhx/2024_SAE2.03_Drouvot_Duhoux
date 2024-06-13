import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

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
            e.printStackTrace();
        }
    }

    private void handleRequest() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream outputStream = clientSocket.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream, true);

        String request = bufferedReader.readLine();
        System.out.println(request);
        String[] parts = request.split(" ");


        String method = parts[0];
        String path = parts[1];
        String version = parts[2];

        if(path.equals(config.getLink()) || path.equals("/")){
            path = config.getLink() + "/index.html";
        }else{
            path = config.getLink() + path;
        }


        File file = new File(path.substring(1));
        if (!file.exists()) {
            file = new File(config.getLink().substring(1) + "/error404.html");
        }

        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        if (contentType.startsWith("text/")) {
            int memoire = Utils.calculMemoire();
            int espaceDisque = Utils.calculEspaceDisque();
            String htmlWithInfo = Utils.ajoutInformationHTML(file, memoire, espaceDisque, numProcess);
            sendTextResponse(printWriter, contentType, htmlWithInfo);
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

    private void sendErrorResponse(PrintWriter printWriter, String status, String message) {
        printWriter.println("HTTP/1.1 " + status);
        printWriter.println("Content-Type: text/plain");
        printWriter.println("Content-Length: " + message.length());
        printWriter.println();
        printWriter.print(message);
        printWriter.flush();
    }
}
