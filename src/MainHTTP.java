public class MainHTTP {
    public static void main(String[] args) {
        try {
            ConfigServ config = new ConfigServ("src/protocol.xml");
            Logger logger = new Logger(config.getAccesLog(), config.getErrorLog());
            Server server = new Server(config, logger);
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
