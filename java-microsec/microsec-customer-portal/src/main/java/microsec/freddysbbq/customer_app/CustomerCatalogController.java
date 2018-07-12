package microsec.freddysbbq.customer_app;

import microsec.common.CatalogBootstrap;
import microsec.freddysbbq.catalog.model.v1.Product;
import microsec.freddysbbq.catalog.model.v1.Order;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Controller
public class CustomerCatalogController {

    @Autowired
    @Qualifier("loadBalancedOauth2RestTemplate")
    private OAuth2RestTemplate oauth2RestTemplate;

    @Autowired
    private CatalogBootstrap catalogBootstrap;

//    @RequestMapping("/")
//    public String index(Model model, Principal principal) {
//        model.addAttribute("username", principal.getName());
//        return "index";
//    }

    @RequestMapping("/catalog")
    @HystrixCommand(fallbackMethod = "catalogFallback")
    public String catalog(Model model) throws Exception {
        PagedResources<Product> catalog = oauth2RestTemplate
                .exchange(
                        "//catalog-service/products",
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<PagedResources<Product>>() {
                        })
                .getBody();
        model.addAttribute("catalog", catalog.getContent());
        return "catalog";
    }

    public String catalogFallback(Model model) {
        model.addAttribute("catalog", catalogBootstrap.getProducts());
        model.addAttribute("catalogServiceDown", true);
        return "catalog";
    }

    @RequestMapping("/catalog-orders")
    @HystrixCommand(fallbackMethod = "myOrdersFallback")
    public String myOrders(Model model) throws Exception {
        Collection<Order> orders = oauth2RestTemplate
                .exchange(
                        "//catalog-order-service/mycatalog-orders",
                        HttpMethod.GET, null,
                        new ParameterizedTypeReference<Collection<Order>>() {
                        })
                .getBody();
        model.addAttribute("orders", orders);
        return "catalog-orders";
    }

    public String myOrdersFallback(Model model) throws Exception {
        model.addAttribute("orders", Collections.emptySet());
        model.addAttribute("orderServiceDown", true);
        return "catalog-orders";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/catalog-orders")
    @HystrixCommand(fallbackMethod = "placeOrderFallback")
    public String placeOrder(Model model, @ModelAttribute CustomerCatalogController.OrderForm orderForm) throws Exception {
        oauth2RestTemplate
                .postForObject("//catalog-order-service/mycatalog-orders", orderForm.getOrder(), Void.class);
        return "redirect:.";
    }

    public String placeOrderFallback(Model model, CustomerCatalogController.OrderForm orderForm) throws Exception {
        return myOrdersFallback(model);
    }

    public static class OrderForm {
        private LinkedHashMap<Long, Integer> order = new LinkedHashMap<Long, Integer>();

        public LinkedHashMap<Long, Integer> getOrder() {
            return order;
        }

        public void setOrder(LinkedHashMap<Long, Integer> order) {
            this.order = order;
        }
    }
}
