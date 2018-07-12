package microsec.common;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import microsec.freddysbbq.catalog.model.v1.Product;

@ConfigurationProperties("catalogBootstrap")
public class CatalogBootstrap {
    private List<Product> products;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
