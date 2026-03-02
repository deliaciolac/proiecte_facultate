package config;

public final class AppConfig {
    private AppConfig() {}

    public static final int PORT = 5050;

    public static final String DB_URL =
            "jdbc:mysql://localhost:3306/autoservice_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DB_USER = "autoservice_user";
    public static final String DB_PASS = "autoservice123";
}
