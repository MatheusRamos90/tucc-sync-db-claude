package br.com.matheushramos.tucc_sync_db_consumer.service;

import br.com.matheushramos.tucc_sync_db_consumer.entity.EmpresaEntity;
import br.com.matheushramos.tucc_sync_db_consumer.entity.ProdutoEmpresaEntity;
import br.com.matheushramos.tucc_sync_db_consumer.entity.ProdutoEntity;
import br.com.matheushramos.tucc_sync_db_consumer.payload.SyncEventPayload;
import br.com.matheushramos.tucc_sync_db_consumer.repository.EmpresaRepository;
import br.com.matheushramos.tucc_sync_db_consumer.repository.ProdutoEmpresaRepository;
import br.com.matheushramos.tucc_sync_db_consumer.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock ProdutoRepository produtoRepository;
    @Mock EmpresaRepository empresaRepository;
    @Mock ProdutoEmpresaRepository produtoEmpresaRepository;

    @InjectMocks SyncService syncService;

    // --- PRODUTO ---

    @Test
    void produtoInsert_deveChamarSave() {
        SyncEventPayload payload = new SyncEventPayload(
                "PRODUTO", "INSERT", Instant.now(),
                Map.of(),
                Map.of("ID", 1, "NOME", "Produto A", "VALOR", "99.99",
                        "DESCONTO", "5.00", "DT_CRIACAO", 1713000000000L,
                        "DT_ATUALIZACAO", 1713000000000L));

        syncService.process(payload);

        ArgumentCaptor<ProdutoEntity> captor = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(produtoRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getNome()).isEqualTo("Produto A");
    }

    @Test
    void produtoUpdate_deveChamarSave() {
        SyncEventPayload payload = new SyncEventPayload(
                "PRODUTO", "UPDATE", Instant.now(),
                Map.of("ID", 1, "NOME", "Produto A"),
                Map.of("ID", 1, "NOME", "Produto Atualizado", "VALOR", "109.99",
                        "DESCONTO", "0.00", "DT_CRIACAO", 1713000000000L,
                        "DT_ATUALIZACAO", 1713000001000L));

        syncService.process(payload);

        verify(produtoRepository).save(any(ProdutoEntity.class));
    }

    @Test
    void produtoDelete_idExistente_deveChamarDeleteById() {
        when(produtoRepository.existsById(1L)).thenReturn(true);
        SyncEventPayload payload = new SyncEventPayload(
                "PRODUTO", "DELETE", Instant.now(),
                Map.of("ID", 1, "NOME", "Produto A", "VALOR", "99.99",
                        "DESCONTO", "5.00", "DT_CRIACAO", 1713000000000L,
                        "DT_ATUALIZACAO", 1713000000000L),
                Map.of());

        syncService.process(payload);

        verify(produtoRepository).deleteById(1L);
    }

    @Test
    void produtoDelete_idAusente_naoDeveLancarExcecao() {
        when(produtoRepository.existsById(99L)).thenReturn(false);
        SyncEventPayload payload = new SyncEventPayload(
                "PRODUTO", "DELETE", Instant.now(),
                Map.of("ID", 99),
                Map.of());

        syncService.process(payload);

        verify(produtoRepository, never()).deleteById(any());
    }

    // --- EMPRESA ---

    @Test
    void empresaInsert_deveChamarSave() {
        SyncEventPayload payload = new SyncEventPayload(
                "EMPRESA", "INSERT", Instant.now(),
                Map.of(),
                Map.of("ID", 10, "NOME", "Empresa X", "CNPJ", "12345678000199",
                        "DT_CRIACAO", 1713000000000L, "DT_ATUALIZACAO", 1713000000000L));

        syncService.process(payload);

        ArgumentCaptor<EmpresaEntity> captor = ArgumentCaptor.forClass(EmpresaEntity.class);
        verify(empresaRepository).save(captor.capture());
        assertThat(captor.getValue().getCnpj()).isEqualTo("12345678000199");
    }

    @Test
    void empresaDelete_idAusente_naoDeveLancarExcecao() {
        when(empresaRepository.existsById(99L)).thenReturn(false);
        SyncEventPayload payload = new SyncEventPayload(
                "EMPRESA", "DELETE", Instant.now(),
                Map.of("ID", 99),
                Map.of());

        syncService.process(payload);

        verify(empresaRepository, never()).deleteById(any());
    }

    // --- PRODUTO_EMPRESA ---

    @Test
    void produtoEmpresaInsert_deveChamarSave() {
        SyncEventPayload payload = new SyncEventPayload(
                "PRODUTO_EMPRESA", "INSERT", Instant.now(),
                Map.of(),
                Map.of("ID", 100, "PRODUTO_ID", 1, "EMPRESA_ID", 10));

        syncService.process(payload);

        ArgumentCaptor<ProdutoEmpresaEntity> captor =
                ArgumentCaptor.forClass(ProdutoEmpresaEntity.class);
        verify(produtoEmpresaRepository).save(captor.capture());
        assertThat(captor.getValue().getProdutoId()).isEqualTo(1L);
        assertThat(captor.getValue().getEmpresaId()).isEqualTo(10L);
    }

    @Test
    void produtoEmpresaDelete_idExistente_deveChamarDeleteById() {
        when(produtoEmpresaRepository.existsById(100L)).thenReturn(true);
        SyncEventPayload payload = new SyncEventPayload(
                "PRODUTO_EMPRESA", "DELETE", Instant.now(),
                Map.of("ID", 100, "PRODUTO_ID", 1, "EMPRESA_ID", 10),
                Map.of());

        syncService.process(payload);

        verify(produtoEmpresaRepository).deleteById(100L);
    }

    // --- Casos especiais ---

    @Test
    void tabelaDesconhecida_naoDeveChamarNenhumRepository() {
        SyncEventPayload payload = new SyncEventPayload(
                "DESCONHECIDA", "INSERT", Instant.now(), Map.of(), Map.of("ID", 1));

        syncService.process(payload);

        verifyNoInteractions(produtoRepository, empresaRepository, produtoEmpresaRepository);
    }

    @Test
    void operacaoDesconhecida_naoDeveChamarNenhumRepository() {
        SyncEventPayload payload = new SyncEventPayload(
                "PRODUTO", "TRUNCATE", Instant.now(), Map.of(), Map.of("ID", 1));

        syncService.process(payload);

        verifyNoInteractions(produtoRepository);
    }
}
