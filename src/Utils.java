import com.sun.management.OperatingSystemMXBean;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;

public class Utils {

    public static int calculMemoire() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        long freeMemory = osBean.getFreePhysicalMemorySize();
        return (int) (freeMemory / (1024 * 1024));
    }

    public static int calculEspaceDisque() {
        File file = new File("C:\\");
        long espaceDisque = file.getFreeSpace() / (1024 * 1024);
        return (int) espaceDisque;
    }

    public static String ajoutInformationHTML(String html, int memoire, int espaceDisque, String nbProcess) throws IOException {
        html = html.replace("{{MEMORY}}", memoire + " MB");
        html = html.replace("{{DISK}}", espaceDisque + " MB");
        html = html.replace("{{PROCESSES}}", nbProcess);
        return html;
    }
}