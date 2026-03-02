import config.AppConfig;
import network.Server;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        new Server(AppConfig.PORT).start();
    }
}