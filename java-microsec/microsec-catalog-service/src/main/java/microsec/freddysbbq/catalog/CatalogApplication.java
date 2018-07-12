package microsec.freddysbbq.catalog;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

import microsec.common.CatalogBootstrap;
import microsec.freddysbbq.catalog.model.v1.Product;

@SpringBootApplication
@EntityScan(basePackageClasses = Product.class)
@EnableResourceServer
@EnableDiscoveryClient
public class CatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }

    @Autowired
    private ProductRepository productRepository;

    @Bean
    public CatalogBootstrap catalogBootstrap() {
        return new CatalogBootstrap();
    }

    @PostConstruct
    public void bootstrap() {
        if (productRepository.count() == 0) {
            productRepository.save(catalogBootstrap().getProducts());
        }
    }

    @Configuration
    public static class RepositoryConfig extends RepositoryRestConfigurerAdapter {
        @Override
        public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
            config.exposeIdsFor(Product.class);
        }
    }

    @Bean
    public ResourceServerConfigurer resourceServer(SecurityProperties securityProperties) {
        return new ResourceServerConfigurerAdapter() {
            @Override
            public void configure(ResourceServerSecurityConfigurer resources) {
                resources.resourceId("catalog");
            }

            @Override
            public void configure(HttpSecurity http) throws Exception {
                if (securityProperties.isRequireSsl()) {
                    http.requiresChannel().anyRequest().requiresSecure();
                }
                http
                        .authorizeRequests()
                        .antMatchers(HttpMethod.GET, "/**").access("#oauth2.hasScope('catalog.read')")
                        .antMatchers(HttpMethod.POST, "/**").access("#oauth2.hasScope('catalog.write')")
                        .antMatchers(HttpMethod.PUT, "/**").access("#oauth2.hasScope('catalog.write')")
                        .antMatchers(HttpMethod.DELETE, "/**").access("#oauth2.hasScope('catalog.write')");
            }
        };
    }
}