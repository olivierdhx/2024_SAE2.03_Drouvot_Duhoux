import com.sun.management.OperatingSystemMXBean;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;

public class Utils {
    /**
     *  Calcul la mémoire sur le serveur
     * @return la mémoire
     */

    public static int calculMemoire() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long freeMemory = osBean.getFreePhysicalMemorySize();
        return (int) (freeMemory / (1024 * 1024));
    }

    /**
     * Calcul l'espace dispo sur le serveur
     * @return l'espace disque
     */

    public static int calculEspaceDisque() {
        File file = new File("C:\\");
        long espaceDisque = file.getFreeSpace() / (1024 * 1024);
        return (int) espaceDisque;
    }

    /**
     * Ajoute les informations dans le String de l'HTML
     * @param html l'html
     * @param memoire la mémoire dispo
     * @param espaceDisque l'espace disque disponible
     * @param nbProcess le numéro de processeur
     * @return le HTML modifié
     * @throws IOException
     */
    public static String ajoutInformationHTML(String html, int memoire, int espaceDisque, String nbProcess) throws IOException {
        html = html.replace("{{MEMORY}}", memoire + " MB");
        html = html.replace("{{DISK}}", espaceDisque + " MB");
        html = html.replace("{{PROCESSES}}", nbProcess);
        return html;
    }
}