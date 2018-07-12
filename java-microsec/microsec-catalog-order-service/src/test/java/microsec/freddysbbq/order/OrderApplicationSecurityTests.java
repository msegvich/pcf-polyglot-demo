package microsec.freddysbbq.order;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;

import microsec.freddysbbq.catalog.model.v1.Product;
import microsec.freddysbbq.catalog.model.v1.Order;
import microsec.freddysbbq.catalog.model.v1.OrderItem;
import microsec.test.SecurityIntegrationTest;
import microsec.test.UaaJwtToken;
import microsec.test.UaaJwtToken.UaaJwtTokenBuilder;
import microsec.uaa.model.v2.UserInfo;

@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "security.require-ssl=true")
public class OrderApplicationSecurityTests extends SecurityIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    private OAuth2RestTemplate mockRestTemplate = mock(OAuth2RestTemplate.class);

    @Autowired
    private CustomerOrderController controller;

    @Before
    public void before() {
        reset(mockRestTemplate);
    }

    @PostConstruct
    public void init() {
        if (orderRepository.count() == 0) {
            Order order = orderFixture();
            orderRepository.save(order);
        }
        controller.setLoadBalancedTokenRelayingRestTemplate(mockRestTemplate);
        controller.setTokenRelayingRestTemplate(mockRestTemplate);
    }

    private Order orderFixture() {
        Order order = new Order();
        order.setCustomerId("12345");
        order.setEmail("frank@whitehouse.gov");
        order.setFirstName("Frank");
        order.setLastName("Underwood");
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProductId(1);
        orderItem.setName("full rack of ribs");
        orderItem.setPrice(new BigDecimal(20));
        orderItem.setQuantity(1);
        order.setOrderItems(Collections.singleton(orderItem));
        return order;
    }

    @Test
    public void testOrdersSecurity() throws Exception {
        checkHttpsRedirect("/orders");

        HttpResponse response = httpsRequest("/orders");
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());

        UaaJwtTokenBuilder tokenBuilder = UaaJwtToken.builder();
        UaaJwtToken token = tokenBuilder.build();
        response = httpsRequest("/orders", token);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        token.setAud(Arrays.asList("order"));
        response = httpsRequest("/orders", token);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        // test for admin scope

        String body = new ObjectMapper().writeValueAsString(orderFixture());

        response = httpsRequest(HttpMethod.POST, "/orders", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.PUT, "/orders/1", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.DELETE, "/orders/1", token, null, null);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        // set the correct scope
        token.setScope(Arrays.asList("order.admin"));

        response = httpsRequest("/orders", token);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.POST, "/orders", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.PUT, "/orders/1", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(204, response.getStatusLine().getStatusCode());

        response = httpsRequest(HttpMethod.DELETE, "/orders/1", token, null, null);
        Assert.assertEquals(204, response.getStatusLine().getStatusCode());

    }

    @Test
    public void testCustomerOrdersSecurity() throws Exception {
        checkHttpsRedirect("/mycatalog-orders");

        HttpResponse response = httpsRequest("/mycatalog-orders");
        Assert.assertEquals(401, response.getStatusLine().getStatusCode());

        UaaJwtToken token = UaaJwtToken.builder()
                .user(UUID.randomUUID().toString(), "test", "test@test.com")
                .build();
        response = httpsRequest("/mycatalog-orders", token);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        token.setAud(Arrays.asList("order"));
        response = httpsRequest("/mycatalog-orders", token);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        String body = new ObjectMapper().writeValueAsString(Collections.singletonMap(1, 1));
        response = httpsRequest(HttpMethod.POST, "/mycatalog-orders", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(403, response.getStatusLine().getStatusCode());

        // set the correct scope
        token.setScope(Arrays.asList("order.me"));

        response = httpsRequest("/mycatalog-orders", token);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        // set up mock behaviours on OAuth2RestTemplate
        when(mockRestTemplate.getForObject(anyString(), eq(UserInfo.class))).thenReturn(new UserInfo());
        Product product = new Product();
        product.setName("test");
        product.setPrice(new BigDecimal(1));
        when(mockRestTemplate.getForObject(anyString(), eq(Product.class), anyString()))
                .thenReturn(product);

        response = httpsRequest(HttpMethod.POST, "/mycatalog-orders", token, ContentType.APPLICATION_JSON, body);
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

    }

}