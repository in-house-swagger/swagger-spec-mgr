package io.swagger;

import io.swagger.configuration.SwaggerDocumentationConfig;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import me.suwash.swagger.spec.manager.infra.config.ApplicationProperties;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableConfigurationProperties(ApplicationProperties.class)
@ComponentScan(basePackages = {
    "io.swagger", "me.suwash.swagger.spec.manager"
})
public class Swagger2SpringBoot implements CommandLineRunner {

    @Override
    public void run(String... arg0) throws Exception {
        if (arg0.length > 0 && arg0[0].equals("exitcode")) {
            throw new ExitException();
        }
    }

    public static void main(String[] args) throws Exception {
        new SpringApplication(Swagger2SpringBoot.class).run(args);
    }

    @Bean
    public Docket swaggerSpringMvcPlugin() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("all")
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build()
            .directModelSubstitute(org.joda.time.LocalDate.class, java.sql.Date.class)
            .directModelSubstitute(org.joda.time.DateTime.class, java.util.Date.class)
            .apiInfo(SwaggerDocumentationConfig.apiInfo());
    }

    @Bean
    protected Validator validator() {
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }

    class ExitException extends RuntimeException implements ExitCodeGenerator {
        private static final long serialVersionUID = 1L;

        @Override
        public int getExitCode() {
            return 10;
        }
    }
}
