package br.com.matheushramos.tucc_core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tuccOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TUCC Core API")
                        .description("Consulta dos dados sincronizados Oracle → PostgreSQL")
                        .version("1.0.0"));
    }
}
