package br.com.matheushramos.tucc_sync_db_consumer;

import br.com.matheushramos.tucc_sync_db_consumer.repository.EmpresaRepository;
import br.com.matheushramos.tucc_sync_db_consumer.repository.ProdutoEmpresaRepository;
import br.com.matheushramos.tucc_sync_db_consumer.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TuccSyncDbConsumerApplicationTests {

	@MockitoBean ProdutoRepository produtoRepository;
	@MockitoBean EmpresaRepository empresaRepository;
	@MockitoBean ProdutoEmpresaRepository produtoEmpresaRepository;

	@Test
	void contextLoads() {
	}
}
