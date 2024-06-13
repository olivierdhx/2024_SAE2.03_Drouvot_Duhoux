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
            String ipString = clientSocket.getLocalAddress().getHostAddress();
            logger.logAccess("Tentative de connexion par : " + ipString);
            if (!isAuthorized(ipString)) {
                logger.logAccess("Connexion refusée par : " + ipString);
                clientSocket.close();
                return;
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream, true);
            String request = bufferedReader.readLine();
            handleRequest(request, printWriter, outputStream);
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.logError(e.getMessage());
        }
    }

    private boolean isAuthorized(String ip) {
        String[] ipRejected = config.getReject().split("\"");
        String[] ipSeparee = ip.split("\"");
        return ipSeparee[0].compareTo(ipRejected[0]) != 0;
    }

    private void handleRequest(String request, PrintWriter printWriter, OutputStream outputStream) throws IOException {
        String[] parts = request.split(" ");
        //On sépare la requete en plusieurs partie
        String method = parts[0];
        String path = parts[1];
        String version = parts[2];
        //Si rien n'est précisé dans l'url, alors c'est l'index
        if(path.equals(config.getLink()) || path.equals("/")) {
            path = config.getLink() + "/index.html";
        }else{
            //Sinon c'est la page demandée
            path = config.getLink() + path + ".html";
        }

        //Et on recherche le fichier en fonction du path
        File file = new File(path.substring(1));
        if (!file.exists()) {
            //Si le fichier n'existe pas, alors on renvoie le error404
            file = new File(config.getLink().substring(1) + "/error404.html");
        }
    
        String contentType = Files.probeContentType(file.toPath());

        if (contentType.startsWith("text/")) {
            int memoire = Utils.calculMemoire();
            int espaceDisque = Utils.calculEspaceDisque();
            String htmlWithInfo = Utils.ajoutInformationHTML(file, memoire, espaceDisque, numProcess);
            String htmlWithExecutedCode = executeCodeInHTML(htmlWithInfo);
            sendTextResponse(printWriter, contentType, htmlWithExecutedCode);
        } else {
            sendBinaryResponse(outputStream, contentType, file);
        }
    }

    private void sendTextResponse(PrintWriter printWriter, String contentType, String content) {
        printWriter.println("HTTP/1.1 200 OK");
        printWriter.println("Content-Type: " + contentType);
        printWriter.println("");
        printWriter.println(content);
        printWriter.flush();
    }

    private void sendBinaryResponse(OutputStream outputStream, String contentType, File file) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeBytes("HTTP/1.1 200 OK\r\n");
        dataOutputStream.writeBytes("Content-Type: " + contentType + "\r\n");
        dataOutputStream.writeBytes("Content-Length: " + file.length() + "\r\n");
        dataOutputStream.writeBytes("\r\n");

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytesRead);
        }
        fileInputStream.close();
        dataOutputStream.flush();
    }

    private String executeCodeInHTML(String html) throws IOException {
        Pattern pattern = Pattern.compile("<code interpreteur=\"([^\"]+)\">(.*?)</code>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String interpreter = matcher.group(1);
            String code = matcher.group(2);
            String output = executeCode(interpreter, code);
            matcher.appendReplacement(result, output);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String executeCode(String interpreter, String code) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(interpreter);
        Process process = processBuilder.start();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        writer.write(code);
        writer.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        reader.close();

        return output.toString();
    }
}
