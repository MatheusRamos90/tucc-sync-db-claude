package br.com.matheushramos.tucc_sync_db_producer.debezium;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "debezium.enabled", havingValue = "true", matchIfMissing = true)
public class DebeziumEngineRunner implements InitializingBean, DisposableBean {

    private final Properties debeziumConnectorProperties;
    private final DebeziumEventHandler eventHandler;

    private DebeziumEngine<ChangeEvent<String, String>> engine;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void afterPropertiesSet() {
        engine = DebeziumEngine.create(Json.class)
                .using(debeziumConnectorProperties)
                .notifying(eventHandler)
                .build();
        executor.submit(engine);
        log.info("Debezium engine iniciado.");
    }

    @Override
    public void destroy() throws Exception {
        log.info("Encerrando Debezium engine...");
        if (engine != null) {
            engine.close();
        }
        executor.shutdown();
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        log.info("Debezium engine encerrado.");
    }
}
