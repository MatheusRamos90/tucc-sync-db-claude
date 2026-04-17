package br.com.matheushramos.tucc_sync_db_producer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@ConditionalOnProperty(name = "debezium.enabled", havingValue = "true", matchIfMissing = true)
public class DebeziumConfig {

    @Value("${debezium.connector-name}")
    private String connectorName;

    @Value("${debezium.database.hostname}")
    private String hostname;

    @Value("${debezium.database.port}")
    private String port;

    @Value("${debezium.database.user}")
    private String user;

    @Value("${debezium.database.password}")
    private String password;

    @Value("${debezium.database.dbname}")
    private String dbname;

    @Value("${debezium.database.pdb-name}")
    private String pdbName;

    @Value("${debezium.database.schema}")
    private String schema;

    @Value("${debezium.offset.storage-file}")
    private String offsetStorageFile;

    @Value("${debezium.offset.flush-interval-ms}")
    private String offsetFlushIntervalMs;

    @Value("${debezium.schema-history-file}")
    private String schemaHistoryFile;

    @Bean
    public Properties debeziumConnectorProperties() {
        Properties props = new Properties();
        props.setProperty("name", connectorName);
        props.setProperty("connector.class", "io.debezium.connector.oracle.OracleConnector");

        props.setProperty("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore");
        props.setProperty("offset.storage.file.filename", offsetStorageFile);
        props.setProperty("offset.flush.interval.ms", offsetFlushIntervalMs);

        props.setProperty("database.hostname", hostname);
        props.setProperty("database.port", port);
        props.setProperty("database.user", user);
        props.setProperty("database.password", password);
        props.setProperty("database.dbname", dbname);
        props.setProperty("database.pdb.name", pdbName);

        props.setProperty("schema.history.internal",
                "io.debezium.storage.file.history.FileSchemaHistory");
        props.setProperty("schema.history.internal.file.filename", schemaHistoryFile);

        props.setProperty("topic.prefix", "tucc");
        props.setProperty("table.include.list",
                schema + ".PRODUTO," + schema + ".EMPRESA," + schema + ".PRODUTO_EMPRESA");
        props.setProperty("decimal.handling.mode", "string");

        return props;
    }
}
