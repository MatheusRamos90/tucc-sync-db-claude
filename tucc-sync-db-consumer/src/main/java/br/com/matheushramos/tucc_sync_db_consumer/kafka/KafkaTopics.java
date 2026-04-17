package br.com.matheushramos.tucc_sync_db_consumer.kafka;

public final class KafkaTopics {

    public static final String SYNC_EVENT = "tucc-sync-event";
    public static final String DLQ        = "tucc-sync-event.dlq";
    public static final String RETRY      = "tucc-sync-event.retry";

    private KafkaTopics() {}
}
