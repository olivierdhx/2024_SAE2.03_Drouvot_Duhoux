import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    private final String accessLog;
    private final String errorLog;

    /**
     * Constructeur logger avec le chemin spécifié par la config
     * @param accesLog path vers le fichier qui contiendra les logs de connexion
     * @param errorLog path vers le fichier qui contiendra les logs d'erreurs
     */

    public Logger(String accesLog, String errorLog) {
        this.accessLog = accesLog;
        this.errorLog = errorLog;
    }

    /**
     * Permet d'écrire dans le fichier de connexion
     * @param message message de connexion
     */

    public void logAccess(String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(accessLog, true))) {
            //Permet de récupérer la date précise du message
            pw.println(java.time.LocalDateTime.now() + " : " + message);
        } catch (IOException e) {
            logError(e.getMessage());
        }
    }

    /**
     * Permet d'écrire dans le fichier d'erreurs
     * @param message message d'erreur
     */

    public void logError(String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(errorLog, true))) {
            //Permet de récupérer la date précise du message
            pw.println(java.time.LocalDateTime.now() + " : " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
