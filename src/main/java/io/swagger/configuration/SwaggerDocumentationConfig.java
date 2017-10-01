package io.swagger.configuration;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen",
    date = "2017-08-08T21:14:16.911+09:00")
@Configuration
public class SwaggerDocumentationConfig {

  @SuppressWarnings("unchecked")
  @Bean
  protected Docket customImplementation() {
    return new Docket(DocumentationType.SWAGGER_2).select()
        .paths(or(regex("/users.*"), regex("/specs.*"), regex("/branches.*"), regex("/switch.*"),
            regex("/merge.*"), regex("/tags.*")))
        .build().directModelSubstitute(org.joda.time.LocalDate.class, java.sql.Date.class)
        .directModelSubstitute(org.joda.time.DateTime.class, java.util.Date.class)
        .apiInfo(SwaggerDocumentationConfig.apiInfo());
  }

  /**
   * swagger specification api情報。
   *
   * @return api情報
   */
  public static ApiInfo apiInfo() {
    return new ApiInfoBuilder().title("Swagger Specification Manager")
        .description("Swagger Specification Management APIs. ").license("Apache 2.0")
        .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html").termsOfServiceUrl("")
        .version(SwaggerDocumentationConfig.class.getPackage().getImplementationVersion())
        .contact(new Contact("suwa-sh", "http://suwa-sh.github.io/profile", "suwash01@gmail.com"))
        .build();
  }
}
