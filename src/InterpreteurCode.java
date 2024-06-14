import java.util.ArrayList;

public class InterpreteurCode {

    public static String presenceCode(String html){
        while(html.indexOf("<code interpreteur=") != -1){
            System.out.println("Ca boucle ? ");
            String htmlCoupe = html.substring(html.indexOf("<code interpreteur="),(html.indexOf("</code>")+7));
            String interpreteur = htmlCoupe.substring(0,(htmlCoupe.indexOf(">") + 1));
            String code = htmlCoupe.substring((htmlCoupe.indexOf(">")+1),htmlCoupe.indexOf("</code>"));
            String partieRemplacee = htmlCoupe.substring(0,htmlCoupe.indexOf("</code>"));
            String resultat = executerCode(interpreteur, code);
            html = html.replace((partieRemplacee+("</code>")),resultat);
            html.contains((partieRemplacee+("</code>")));
            System.out.println("Ca mouline je crois");
        }
        System.out.println("Ca boucle ? ");
        return html;
    }

    public static String executerCode(String interpreteur, String code){
            String interpreteurPath = interpreteur.substring(interpreteur.indexOf("«"),interpreteur.indexOf("»"));
            if(interpreteurPath.contains("python")){
                code = codeExecPython(code);
            }else if(interpreteurPath.contains("bash")){
                code = codeExecBash(code);
            }
        return code;
    }

    public static String codeExecPython(String code){
        String res = "42";
        return res;
    }

    public static String codeExecBash(String code){
        String res = "64";
        return res;
    }
}
