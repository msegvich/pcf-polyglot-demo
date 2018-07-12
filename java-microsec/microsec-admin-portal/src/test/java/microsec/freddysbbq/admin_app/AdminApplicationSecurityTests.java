package microsec.freddysbbq.admin_app;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2SsoProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import microsec.test.SecurityIntegrationTest;

@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "security.require-ssl=true")
public class AdminApplicationSecurityTests extends SecurityIntegrationTest {

    @Autowired
    private OAuth2SsoProperties ssoProperties;

    @Test
    public void testHomepageSecurity() throws Exception {
        checkRequiresHttpsAndOauthSso("/", ssoProperties.getLoginPath());
    }

    @Test
    public void testMenuSecurity() throws Exception {
        checkRequiresHttpsAndOauthSso("/menuItems", ssoProperties.getLoginPath());
        checkRequiresHttpsAndOauthSso("/menuItems/new", ssoProperties.getLoginPath());
        checkRequiresHttpsAndOauthSso("/menuItems/1", ssoProperties.getLoginPath());
        checkRequiresHttpsAndOauthSso("/menuItems/1/delete", ssoProperties.getLoginPath());
    }

    @Test
    public void testOrderSecurity() throws Exception {
        checkRequiresHttpsAndOauthSso("/orders/", ssoProperties.getLoginPath());
        checkRequiresHttpsAndOauthSso("/orders/1/delete", ssoProperties.getLoginPath());
    }

}