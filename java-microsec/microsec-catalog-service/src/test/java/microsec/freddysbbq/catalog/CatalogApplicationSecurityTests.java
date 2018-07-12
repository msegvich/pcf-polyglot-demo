package microsec.freddysbbq.catalog;

import java.math.BigDecimal;
import java.util.Arrays;

import microsec.freddysbbq.catalog.model.v1.Product;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import microsec.test.SecurityIntegrationTest;
import microsec.test.UaaJwtToken;
import microsec.test.UaaJwtToken.UaaJwtTokenBuilder;

@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "security.require-ssl=true")
public class CatalogApplicationSecurityTests extends SecurityIntegrationTest {

    @Test
    public void testProductsSecurity() throws Exception {
        checkHttpsRedirect("/products");

        HttpResponse response = httpsRequest("/products");
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());

        UaaJwtTokenBuilder tokenBuilder = UaaJwtToken.builder();
        UaaJwtToken token = tokenBuilder.build();
        response = httpsRequest("/products", token);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        token.setAud(Arrays.asList("catalog"));
        token.setScope(Arrays.asList("foo.bar"));
        response = httpsRequest("/products", token);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        token.setScope(Arrays.asList("catalog.read"));
        response = httpsRequest("/products", token);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        // test for write scope

        Product product = new Product();
        product.setName("test");
        product.setPrice(new BigDecimal(1));
        String body = new ObjectMapper().writeValueAsString(new Product());

        response = httpsRequest(HttpMethod.POST, "/products", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.PUT, "/products/1", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.DELETE, "/products/1", token, null, null);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        // set the correct scope
        token.setScope(Arrays.asList("catalog.write"));

        response = httpsRequest(HttpMethod.POST, "/products", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.PUT, "/products/1", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(204, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.DELETE, "/products/1", token, null, null);
        Assert.assertEquals(204, response.getStatusLine().getStatusCode());

    }

}