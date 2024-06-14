public class TestInterp {
    public static void main(String[] args) {
        String html = "<html>\n" +
                "<body>\n" +
                "<h1> Exemple avec la date </h1>\n" +
                "<h2>en bash</h2>\n" +
                "La date est <code interpreteur=«/bin/bash»>date</code>\n" +
                "<h2>En python</h2>\n" +
                "La date est <code interpreteur=«/usr/bin/python»>\n" +
                "    import time;\n" +
                "    print(time.time())\n" +
                "</code>\n" +
                "</body>\n" +
                "</html>";

        String resultat = InterpreteurCode.presenceCode(html);
        System.out.println("Ca mouline ? ");
        System.out.println(resultat);
    }
}
