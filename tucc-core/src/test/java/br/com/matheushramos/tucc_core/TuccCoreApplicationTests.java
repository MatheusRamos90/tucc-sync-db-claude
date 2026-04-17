package br.com.matheushramos.tucc_core;

import br.com.matheushramos.tucc_core.repository.EmpresaRepository;
import br.com.matheushramos.tucc_core.repository.ProdutoEmpresaRepository;
import br.com.matheushramos.tucc_core.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TuccCoreApplicationTests {

	@MockitoBean ProdutoRepository produtoRepository;
	@MockitoBean EmpresaRepository empresaRepository;
	@MockitoBean ProdutoEmpresaRepository produtoEmpresaRepository;

	@Test
	void contextLoads() {
	}
}
