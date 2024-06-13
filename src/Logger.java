import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    private String accesLog;
    private String errorLog;

    public Logger(String accesLog, String errorLog) {
        this.accesLog = accesLog;
        this.errorLog = errorLog;
    }

    public void logAccess(String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(accesLog, true))) {
            //Permet de récupérer la date précise du message
            pw.println(message + " " + java.time.LocalDateTime.now());
        } catch (IOException e) {
            logError(e.getMessage());
        }
    }

    public void logError(String message) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(errorLog, true))) {
            //Permet de récupérer la date précise du message
            pw.println(message + " " + java.time.LocalDateTime.now());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
