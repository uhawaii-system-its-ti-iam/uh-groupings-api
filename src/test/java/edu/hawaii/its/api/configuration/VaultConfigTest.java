package edu.hawaii.its.api.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

@SpringBootTest(classes = { SpringBootWebApplication.class })
@TestPropertySource(properties = { "grouperClient.webService.password=grouperPassword" })
@ActiveProfiles("dockerhost")
public class VaultConfigTest {

    @Autowired
    private VaultConfig vaultConfig;

    @Test
    public void construction() {
        assertNotNull(vaultConfig);
    }

    @Test
    public void testingOverrideProperty() {
        GrouperClientConfig config = GrouperClientConfig.retrieveConfig();

        String key = "grouperClient.webService.password";
        String testUrl = "grouperPassword";

        vaultConfig.init();

        String overriddenValue = config.propertiesOverrideMap().get(key);
        assertThat(overriddenValue, equalTo(testUrl));
    }

    @Test
    public void testEnvironmentVariableOverride() {
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("grouperClient.webService.password", "grouperPassword");

        GrouperPropertyConfigurer configurerWithMockEnv = new GrouperPropertyConfigurer();
        ReflectionTestUtils.setField(configurerWithMockEnv, "webServicePassword",
                mockEnv.getProperty("grouperClient.webService.password"));

        configurerWithMockEnv.init();

        GrouperClientConfig config = GrouperClientConfig.retrieveConfig();

        String overriddenValue = config.propertiesOverrideMap().get("grouperClient.webService.password");
        assertThat(overriddenValue, equalTo("grouperPassword"));
    }
}
