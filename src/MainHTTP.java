import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
//Pour le calcul de mémoire

import com.sun.management.OperatingSystemMXBean;



public class MainHTTP {

    public static String port = "";
    public static String link = "";
    public static String index = "";
    public static String accept = null;
    public static String reject = null;

    public static String accesLog = "";
    public static String errorLog = "";

    public static String numProcess;

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        // path Fichier de configuration
        String filename = "src/protocol.xml";
        // Appel de la méthode pour prendre les éléments par rapport au fichier de configuration
        prendreElements(filename);

        //A partir des éléments du fichier de configuration, on configure le server
        // Création du Socket pour établir un contact avec le client plus tard
        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
        System.out.println("🟢 Le serveur est fonctionnel. En l'attente d'une connexion...");

        // S'occupe de prendre le PID (Process ID) du programme Java
        RuntimeMXBean runMX = ManagementFactory.getRuntimeMXBean();
        // String stockant le PID (Ce dernier étant noté avec "PID@Ubuntu20", nous devons effectuer un split pour uniquement garder le PID)
        String numeroProcess = runMX.getName().split("@LAPTOP-VN0G0AMG")[0];
        numProcess = numeroProcess;
        // Création d'un fichier qui se chargera de stocker le PID courant
        File filePID = new File("./var/run/myweb.pid");
        // Création d'un PrintWriter chargé d'écrire les données dans le fichier donné
        PrintWriter pwID = new PrintWriter(filePID);
        // Ecriture des données présentes dans le String précédent
        pwID.println(numeroProcess);
        // Fermeture du PrintWriter
        pwID.close();
        System.out.println("Numéro de process " + numeroProcess);

        // Boucle While permettant de charger le serveur indéfiniment
        while (true) {
            run(serverSocket, link);
            System.out.println("Nouvelle requête :");
        }
    }

    /**
     * Méthode permettant de savoir si l'adresse IP mise en paramètre est acceptée par le serveur en fonction du fichier XML
     *
     * @param ip l'adresse IP en question
     * @return boolean true si l'adresse est acceptée, false sinon
     */
    private static boolean estAutorise(String ip) {
        String[] ipRejected = reject.split("\"");
        String[] ipSeparee = ip.split("\"");
        return ipSeparee[0].compareTo(ipRejected[0]) != 0;
    }


    /**
     * Méthode s'occupant de l'excution du serveur
     *
     * @param serverSocket le socket du serveur
     * @param link         le lien du dossier racine
     * @throws IOException
     */
    public static void run(ServerSocket serverSocket, String link) throws IOException {
        try {
            // Création d'un serveur socket pour le client dans le but de l'accepter
            Socket clientSocket = serverSocket.accept();

            //On prend son adresse IP (IPv4)
            String ipString = clientSocket.getLocalAddress().getHostAddress();
            System.out.println("🟢 Connexion d'un client : " + ipString);

            //Si l'adresse IP n'est pas autorisée dans le fichier XML, on refuse la connexion
            ecritureLogConnexion("Tentative de connexion par : " + ipString);
            if (!estAutorise(ipString)) {
                ecritureLogConnexion("Connexion refusée par : " + ipString);
                System.out.println("🔴 Connexion refusée : " + ipString);
                clientSocket.close();
            } else {
                // Création d'un flux d'entrée pour le client
                InputStream inputStream = clientSocket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                // Création d'un outputstream et d'un PrintWriter pour le client (envoi de ses réponses)

                OutputStream outputStream = clientSocket.getOutputStream();
                // Création d'un flux de sortie pour le client

                PrintWriter printWriter = new PrintWriter(outputStream, true);
                // Read the request from the client
                String verifReq = bufferedReader.readLine();
                String request = "";
                while (verifReq != null && !verifReq.isEmpty()) {
                    request += "\n" + verifReq;
                    verifReq = bufferedReader.readLine();
                }
                System.out.println("🟢 Connexion acceptée.");



                // Lecture de la requête du client non nulle
                System.out.println("💡 Requête reçue : " + request);
                String[] parts = request.split(" ");

                // Coupe en plusieurs morceaux la requête dans des variables pour plus de clarté
                String method = parts[0];
                System.out.println(method);
                String path = parts[1];
                System.out.println(path);
                String version = parts[2];
                System.out.println(version);

                System.out.println(MainHTTP.link + path);
                System.out.println(request.split(" ")[1]);
                System.out.println(MainHTTP.link);

                // Vérifie si le chemin existe, et traite s'il n'y a pas de chemin écrit
                if (path.equals(MainHTTP.link) || path.equals("/")) {
                    request = method + " " + MainHTTP.link + "/status.html " + version;
                    System.out.println("ok");
                } else {
                    request = method + " " + MainHTTP.link + path + " " + version;
                }

                System.out.println("Request: " + request);
                path = request.split(" ")[1];
                link = path.substring(1);

                System.out.println(link);

                // Vérifie si le fichier existe, et si cela est le cas, il le confirme. Si ce n'est pas le cas, il charge la page HTML de l'erreur 404
                if (new File(link).exists()) {
                    System.out.println("Fichier existant");
                } else {
                    System.out.println("Fichier inexistant");
                    link = MainHTTP.link.substring(1) + "/error404.html";
                }

                printWriter.flush();
                //Ecriture de la réponse au client
                printWriter.println("HTTP/1.1 200 OK");

                // Choix du content-type pour le codage de la réponse
                String contentType = "";
                if (link.endsWith(".html")) {
                    contentType = "text/html";
                }
                if (link.endsWith(".css")) {
                    contentType = "text/css";
                } else if (link.endsWith(".js")) {
                    contentType = "application/javascript";
                } else if (link.endsWith(".png")) {
                    contentType = "image/png";
                } else if (link.endsWith(".jpg")) {
                    contentType = "image/jpg";
                } else if (link.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (link.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (link.endsWith(".ico")) {
                    contentType = "image/x-icon";
                }
                printWriter.println("Content-Type: " + contentType);
                printWriter.print("");
                printWriter.println("responseType:'arraybuffer'");
                printWriter.println("");

                // S'occupe d'écrire les données
                File file = new File(link);
                DataInputStream bf = new DataInputStream(new FileInputStream(file));
                int memoire = calculMemoire(); // Appel de la fonction calculMemoire pour obtenir la mémoire disponible
                int espaceDisque = calculEspaceDisque();
                String htmlWithInfo = ajoutInformationHTML(memoire, espaceDisque,numProcess); // Ajout de la mémoire disponible au HTML
                byte[] dataRead = null;
                dataRead = htmlWithInfo.getBytes();
                clientSocket.getOutputStream().write(dataRead);
                printWriter.flush();
                clientSocket.close();
                bufferedReader.close();
                bf.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public static String ajoutInformationHTML(int memoire, int espaceDisqueDispo, String nbProcess) throws IOException {
        String link = "root/status.html";
        File file = new File(link);
        String html = new String(Files.readAllBytes(file.toPath()));
        html = html.replace("{{MEMORY}}", memoire + " MB");
        html = html.replace("{{DISK}}", espaceDisqueDispo + " MB");
        html = html.replace("{{PROCESSES}}", nbProcess);
        return html;
    }


    private static int calculMemoire() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long freeMemory = osBean.getFreePhysicalMemorySize(); // Mémoire physique disponible en octets
        int freeMemoryMB = (int) (freeMemory / (1024 * 1024)); // Conversion en mégaoctets
        return freeMemoryMB;
    }

    private static int calculEspaceDisque() {
        File file = new File("C:\\");
        long espaceDisque = file.getFreeSpace() / (1024 * 1024); // Espace disque disponible en octets
        System.out.println("espace disque = " + espaceDisque);
        int espaceDisqueMB = (int) espaceDisque; // Conversion en mégaoctets
        return espaceDisqueMB;
    }

    public static void ecritureLogConnexion(String ipConnexion){
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(accesLog,true));
            pw.println(ipConnexion + " " + java.time.LocalDateTime.now());
            System.out.println("Ecriture faite");
            pw.close();

        } catch (IOException e) {
            ecritureError(e.getMessage());
        }
    }

    public static void ecritureError(String s){
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(errorLog,true));
            pw.println(s + " " + java.time.LocalDateTime.now());
            pw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}