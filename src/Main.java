public class Main {
    public static void main(String[] args) {
        try {
            //Lis la config dans le chemin spécifié
            ConfigServ config = new ConfigServ("src/protocol.xml");
            //Créer les logs a partir du chemin spécifié dans la config
            Logger logger = new Logger(config.getAccesLog(), config.getErrorLog());
            //A partir de cela, créer le server et le start
            Server server = new Server();
            server.start(config, logger);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
