import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class InterpreteurCode {

    public static String presenceCode(String html) {
        //Tant que des balises interpreteur sont présentes,
        while (html.indexOf("<code interpreteur=") != -1) {
            //On récupère les parties qui nous intéressent (path de l'interpreteur, code entre balise)
            String htmlCoupe = html.substring(html.indexOf("<code interpreteur="), (html.indexOf("</code>") + 7));
            String interpreteur = htmlCoupe.substring(0, (htmlCoupe.indexOf(">") + 1));
            String code = htmlCoupe.substring((htmlCoupe.indexOf(">") + 1), htmlCoupe.indexOf("</code>"));

            //Tout le code doit etre sur une ligne, les \n pouvant
            code = code.replace("\n", " ");

            String partieRemplacee = htmlCoupe.substring(0, htmlCoupe.indexOf("</code>"));
            //On execute le code
            String resultat = executerCode(interpreteur, code);

            //Et on le remplace dans le HTML
            html = html.replace((partieRemplacee + ("</code>")), resultat);
        }
        return html;
    }

    public static String executerCode(String interpreteur, String code) {
        //on récupère le path exact de la balise
        String interpreteurPath = interpreteur.substring((interpreteur.indexOf("«") + 1), interpreteur.indexOf("»"));
        //Si c'est Python on exe en Python
        if (interpreteurPath.contains("python")) {
            code = codeExecPython(code, interpreteurPath);
            //Si c'est en bash on exe en Bash
        } else if (interpreteurPath.contains("bash")) {
            code = codeExecBash(code, interpreteurPath);
        }
        //On retourne le code, si l'interpreteur est pas existant on retourne la formation du code
        return code;
    }

    public static String codeExecPython(String code, String interpreteur) {
        String res = "";
        try {
            //On créer un fichier temporaire comportant le script python
            Path tempScript = Files.createTempFile("script", ".py");
            //On écris le code dedans
            Files.write(tempScript, code.getBytes());

            //On compose la commande, avec le script et l'interpreteur
            String[] command = {interpreteur, tempScript.toString()};

            //On créer le processus
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();



            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            //On récupère la réponse du code
            while (line != null){
                System.out.println(line);
                res += line;
                line = reader.readLine();
            }

            //On supprime le fichier qui contenait le script, celui ci étant maintenant inutile
            Files.delete(tempScript);

        } catch (IOException e) {
            res = "erreur";
            throw new RuntimeException(e);
        }
        return res;
    }

    public static String codeExecBash(String code, String interpreteur) {
        String res = "64";
        return res;
    }
}
