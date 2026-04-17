package br.com.matheushramos.tucc_monolito.dao;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Fábrica de conexões Oracle via Apache Commons DBCP.
 * Configuração via variáveis de ambiente (compatível com Docker).
 */
public class ConnectionFactory {

    private static final BasicDataSource dataSource;

    static {
        String host     = env("ORACLE_HOST",     "localhost");
        String port     = env("ORACLE_PORT",     "1521");
        String db       = env("ORACLE_DB",       "XEPDB1");
        String user     = env("ORACLE_USER",     "tucc");
        String password = env("ORACLE_PASSWORD", "tucc");

        String url = "jdbc:oracle:thin:@//" + host + ":" + port + "/" + db;

        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setInitialSize(2);
        dataSource.setMaxActive(10);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("SELECT 1 FROM DUAL");
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }
}
