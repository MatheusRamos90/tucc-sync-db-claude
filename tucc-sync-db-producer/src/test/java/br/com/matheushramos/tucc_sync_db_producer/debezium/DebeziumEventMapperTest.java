package br.com.matheushramos.tucc_sync_db_producer.debezium;

import br.com.matheushramos.tucc_sync_db_producer.payload.SyncEventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DebeziumEventMapperTest {

    private DebeziumEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DebeziumEventMapper(new ObjectMapper());
    }

    // --- PRODUTO ---

    @Test
    void deveMapearProdutoInsert() {
        String json = """
            {"op":"c","source":{"ts_ms":1713000000000},
             "before":null,
             "after":{"ID":1,"NOME":"Produto A","VALOR":"99.99","DESCONTO":"5.00",
                      "DT_CRIACAO":1713000000000,"DT_ATUALIZACAO":1713000000000}}
            """;
        Optional<SyncEventPayload> result = mapper.map("tucc.TUCC.PRODUTO", json);

        assertThat(result).isPresent();
        assertThat(result.get().table()).isEqualTo("PRODUTO");
        assertThat(result.get().operation()).isEqualTo("INSERT");
        assertThat(result.get().before()).isEmpty();
        assertThat(result.get().after()).containsKey("ID");
        assertThat(result.get().after().get("NOME")).isEqualTo("Produto A");
        assertThat(result.get().capturedAt().toEpochMilli()).isEqualTo(1713000000000L);
    }

    @Test
    void deveMapearProdutoUpdate() {
        String json = """
            {"op":"u","source":{"ts_ms":1713000001000},
             "before":{"ID":1,"NOME":"Produto A","VALOR":"99.99","DESCONTO":"5.00",
                       "DT_CRIACAO":1713000000000,"DT_ATUALIZACAO":1713000000000},
             "after":{"ID":1,"NOME":"Produto A Atualizado","VALOR":"109.99","DESCONTO":"5.00",
                      "DT_CRIACAO":1713000000000,"DT_ATUALIZACAO":1713000001000}}
            """;
        Optional<SyncEventPayload> result = mapper.map("tucc.TUCC.PRODUTO", json);

        assertThat(result).isPresent();
        assertThat(result.get().operation()).isEqualTo("UPDATE");
        assertThat(result.get().before()).containsKey("NOME");
        assertThat(result.get().after().get("NOME")).isEqualTo("Produto A Atualizado");
    }

    @Test
    void deveMapearProdutoDelete() {
        String json = """
            {"op":"d","source":{"ts_ms":1713000002000},
             "before":{"ID":1,"NOME":"Produto A","VALOR":"99.99","DESCONTO":"5.00",
                       "DT_CRIACAO":1713000000000,"DT_ATUALIZACAO":1713000000000},
             "after":null}
            """;
        Optional<SyncEventPayload> result = mapper.map("tucc.TUCC.PRODUTO", json);

        assertThat(result).isPresent();
        assertThat(result.get().operation()).isEqualTo("DELETE");
        assertThat(result.get().before().get("ID")).isEqualTo(1);
        assertThat(result.get().after()).isEmpty();
    }

    // --- EMPRESA ---

    @Test
    void deveMapearEmpresaInsert() {
        String json = """
            {"op":"c","source":{"ts_ms":1713000000000},
             "before":null,
             "after":{"ID":10,"NOME":"Empresa X","CNPJ":"12345678000199",
                      "DT_CRIACAO":1713000000000,"DT_ATUALIZACAO":1713000000000}}
            """;
        Optional<SyncEventPayload> result = mapper.map("tucc.TUCC.EMPRESA", json);

        assertThat(result).isPresent();
        assertThat(result.get().table()).isEqualTo("EMPRESA");
        assertThat(result.get().operation()).isEqualTo("INSERT");
        assertThat(result.get().after().get("CNPJ")).isEqualTo("12345678000199");
    }

    @Test
    void deveMapearEmpresaDelete() {
        String json = """
            {"op":"d","source":{"ts_ms":1713000003000},
             "before":{"ID":10,"NOME":"Empresa X","CNPJ":"12345678000199",
                       "DT_CRIACAO":1713000000000,"DT_ATUALIZACAO":1713000000000},
             "after":null}
            """;
        Optional<SyncEventPayload> result = mapper.map("tucc.TUCC.EMPRESA", json);

        assertThat(result).isPresent();
        assertThat(result.get().operation()).isEqualTo("DELETE");
        assertThat(result.get().before().get("ID")).isEqualTo(10);
    }

    // --- PRODUTO_EMPRESA (sem campos de data) ---

    @Test
    void deveMapearProdutoEmpresaInsert() {
        String json = """
            {"op":"c","source":{"ts_ms":1713000000000},
             "before":null,
             "after":{"ID":100,"PRODUTO_ID":1,"EMPRESA_ID":10}}
            """;
        Optional<SyncEventPayload> result = mapper.map("tucc.TUCC.PRODUTO_EMPRESA", json);

        assertThat(result).isPresent();
        assertThat(result.get().table()).isEqualTo("PRODUTO_EMPRESA");
        assertThat(result.get().operation()).isEqualTo("INSERT");
        assertThat(result.get().after()).doesNotContainKey("DT_CRIACAO");
        assertThat(result.get().after().get("PRODUTO_ID")).isEqualTo(1);
    }

    @Test
    void deveMapearProdutoEmpresaDelete() {
        String json = """
            {"op":"d","source":{"ts_ms":1713000004000},
             "before":{"ID":100,"PRODUTO_ID":1,"EMPRESA_ID":10},
             "after":null}
            """;
        Optional<SyncEventPayload> result = mapper.map("tucc.TUCC.PRODUTO_EMPRESA", json);

        assertThat(result).isPresent();
        assertThat(result.get().operation()).isEqualTo("DELETE");
        assertThat(result.get().before().get("ID")).isEqualTo(100);
    }

    // --- Casos especiais ---

    @Test
    void tombstoneDeveRetornarEmpty() {
        Optional<SyncEventPayload> result = mapper.map("tucc.TUCC.PRODUTO", null);
        assertThat(result).isEmpty();
    }

    @Test
    void snapshotReadDeveSerMapeadoComoInsert() {
        String json = """
            {"op":"r","source":{"ts_ms":1713000000000},
             "before":null,
             "after":{"ID":1,"NOME":"Produto A","VALOR":"99.99","DESCONTO":"5.00",
                      "DT_CRIACAO":1713000000000,"DT_ATUALIZACAO":1713000000000}}
            """;
        Optional<SyncEventPayload> result = mapper.map("tucc.TUCC.PRODUTO", json);

        assertThat(result).isPresent();
        assertThat(result.get().operation()).isEqualTo("INSERT");
    }
}
