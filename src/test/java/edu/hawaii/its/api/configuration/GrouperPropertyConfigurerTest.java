package edu.hawaii.its.api.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

@SpringBootTest(classes = {SpringBootWebApplication.class})
@TestPropertySource(properties = {"grouperClient.webService.url=test-url-b"})
public class GrouperPropertyConfigurerTest {

    @Autowired
    private GrouperPropertyConfigurer grouperPropertyConfigurer;

    @Test
    public void construction() {
        assertNotNull(grouperPropertyConfigurer);
    }

    @Test
    public void testingOverrideProperty() {
        // Retrieve the current configuration instance
        GrouperClientConfig config = GrouperClientConfig.retrieveConfig();

        // Define the key and the expected test value
        String key = "grouperClient.webService.url";
        String testUrl = "test-url-b";

        // Now perform the initialization that should override the value
        grouperPropertyConfigurer.init();

        // Check that the override worked as expected
        String overriddenValue = config.propertiesOverrideMap().get(key);
        assertThat(overriddenValue, equalTo(testUrl));
    }

    @Test
    public void testEnvironmentVariableOverride() {
        // Create a MockEnvironment with the specific property
        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("grouperClient.webService.url", "test-url-b2");

        // Create a new instance of the class being tested, with the mock environment
        GrouperPropertyConfigurer configurerWithMockEnv = new GrouperPropertyConfigurer();
        ReflectionTestUtils.setField(configurerWithMockEnv, "webServiceUrl", mockEnv.getProperty("grouperClient.webService.url"));

        // Call the init method to trigger the override logic
        configurerWithMockEnv.init();

        // Retrieve the current configuration instance
        GrouperClientConfig config = GrouperClientConfig.retrieveConfig();

        // Verify that the value has been overridden correctly
        String overriddenValue = config.propertiesOverrideMap().get("grouperClient.webService.url");
        assertThat(overriddenValue, equalTo("test-url-b2"));
    }
}