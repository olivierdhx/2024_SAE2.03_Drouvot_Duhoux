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
            html = processCodeTags(html);
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

    private String processCodeTags(String html) {
        Pattern pattern = Pattern.compile("<code interpreteur=\"([^\"]+)\">(.+?)</code>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        StringBuffer processedHtml = new StringBuffer();

        while (matcher.find()) {
            String interpreter = matcher.group(1);
            String code = matcher.group(2);
            String result = executeCode(interpreter, code);
            matcher.appendReplacement(processedHtml, "<pre>" + result + "</pre>");
        }
        matcher.appendTail(processedHtml);
        return processedHtml.toString();
    }

    private String executeCode(String interpreter, String code) {
        try {
            File tempFile = File.createTempFile("code", null);
            try (PrintWriter writer = new PrintWriter(tempFile)) {
                writer.print(code);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(interpreter, tempFile.getAbsolutePath());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            tempFile.delete();
            return output.toString();
        } catch (IOException e) {
            logger.logError("Error executing code: " + e.getMessage());
            e.printStackTrace();
            return "Error executing code: " + e.getMessage();
        }
    }
}
