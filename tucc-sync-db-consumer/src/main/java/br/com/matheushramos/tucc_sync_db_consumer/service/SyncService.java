package br.com.matheushramos.tucc_sync_db_consumer.service;

import br.com.matheushramos.tucc_sync_db_consumer.entity.EmpresaEntity;
import br.com.matheushramos.tucc_sync_db_consumer.entity.ProdutoEmpresaEntity;
import br.com.matheushramos.tucc_sync_db_consumer.entity.ProdutoEntity;
import br.com.matheushramos.tucc_sync_db_consumer.payload.SyncEventPayload;
import br.com.matheushramos.tucc_sync_db_consumer.repository.EmpresaRepository;
import br.com.matheushramos.tucc_sync_db_consumer.repository.ProdutoEmpresaRepository;
import br.com.matheushramos.tucc_sync_db_consumer.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final ProdutoRepository produtoRepository;
    private final EmpresaRepository empresaRepository;
    private final ProdutoEmpresaRepository produtoEmpresaRepository;

    @Transactional
    public void process(SyncEventPayload payload) {
        switch (payload.table()) {
            case "PRODUTO"         -> processProduto(payload);
            case "EMPRESA"         -> processEmpresa(payload);
            case "PRODUTO_EMPRESA" -> processProdutoEmpresa(payload);
            default -> log.warn("Tabela desconhecida no payload: {}", payload.table());
        }
    }

    private void processProduto(SyncEventPayload payload) {
        switch (payload.operation()) {
            case "INSERT", "UPDATE" -> produtoRepository.save(toProdutoEntity(payload.after()));
            case "DELETE" -> {
                Long id = extractId(payload.before());
                if (produtoRepository.existsById(id)) {
                    produtoRepository.deleteById(id);
                } else {
                    log.warn("DELETE idempotente: PRODUTO id={} não encontrado, ignorando.", id);
                }
            }
            default -> log.warn("Operação desconhecida para PRODUTO: {}", payload.operation());
        }
    }

    private void processEmpresa(SyncEventPayload payload) {
        switch (payload.operation()) {
            case "INSERT", "UPDATE" -> empresaRepository.save(toEmpresaEntity(payload.after()));
            case "DELETE" -> {
                Long id = extractId(payload.before());
                if (empresaRepository.existsById(id)) {
                    empresaRepository.deleteById(id);
                } else {
                    log.warn("DELETE idempotente: EMPRESA id={} não encontrada, ignorando.", id);
                }
            }
            default -> log.warn("Operação desconhecida para EMPRESA: {}", payload.operation());
        }
    }

    private void processProdutoEmpresa(SyncEventPayload payload) {
        switch (payload.operation()) {
            case "INSERT", "UPDATE" ->
                    produtoEmpresaRepository.save(toProdutoEmpresaEntity(payload.after()));
            case "DELETE" -> {
                Long id = extractId(payload.before());
                if (produtoEmpresaRepository.existsById(id)) {
                    produtoEmpresaRepository.deleteById(id);
                } else {
                    log.warn("DELETE idempotente: PRODUTO_EMPRESA id={} não encontrado, ignorando.", id);
                }
            }
            default ->
                    log.warn("Operação desconhecida para PRODUTO_EMPRESA: {}", payload.operation());
        }
    }

    private ProdutoEntity toProdutoEntity(Map<String, Object> data) {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(toLong(data.get("ID")));
        entity.setNome((String) data.get("NOME"));
        entity.setValor(toBigDecimal(data.get("VALOR")));
        entity.setDesconto(toBigDecimal(data.get("DESCONTO")));
        entity.setCreatedAt(toInstant(data.get("DT_CRIACAO")));
        entity.setUpdatedAt(toInstant(data.get("DT_ATUALIZACAO")));
        return entity;
    }

    private EmpresaEntity toEmpresaEntity(Map<String, Object> data) {
        EmpresaEntity entity = new EmpresaEntity();
        entity.setId(toLong(data.get("ID")));
        entity.setNome((String) data.get("NOME"));
        entity.setCnpj((String) data.get("CNPJ"));
        entity.setCreatedAt(toInstant(data.get("DT_CRIACAO")));
        entity.setUpdatedAt(toInstant(data.get("DT_ATUALIZACAO")));
        return entity;
    }

    private ProdutoEmpresaEntity toProdutoEmpresaEntity(Map<String, Object> data) {
        ProdutoEmpresaEntity entity = new ProdutoEmpresaEntity();
        entity.setId(toLong(data.get("ID")));
        entity.setProdutoId(toLong(data.get("PRODUTO_ID")));
        entity.setEmpresaId(toLong(data.get("EMPRESA_ID")));
        return entity;
    }

    private Long extractId(Map<String, Object> data) {
        return toLong(data.get("ID"));
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString().trim());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        return new BigDecimal(value.toString().trim());
    }

    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return Instant.ofEpochSecond(0, n.longValue() * 1000L);
        String s = value.toString().trim();
        try {
            // Debezium Oracle sends TIMESTAMP as epoch microseconds
            return Instant.ofEpochSecond(0, Long.parseLong(s) * 1000L);
        } catch (NumberFormatException e) {
            return Instant.parse(s);
        }
    }
}
