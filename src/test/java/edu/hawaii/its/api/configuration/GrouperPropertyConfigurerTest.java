package edu.hawaii.its.api.configuration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import edu.hawaii.its.api.service.BatchExecutor;
import edu.hawaii.its.api.service.GrouperApiService;
import edu.hawaii.its.api.service.GrouperService;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

@ActiveProfiles("localTest")
@SpringBootTest(classes = {SpringBootWebApplication.class})
@TestPropertySource(properties = {"grouperClient.webService.url=test-url-b"})
public class GrouperPropertyConfigurerTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private GrouperPropertyConfigurer grouperPropertyConfigurer;

    @Value("${groupings.api.grouper.lookup-batch-size}")
    private int lookupBatchSize;

    @Value("${groupings.api.grouper.update-batch-size}")
    private int updateBatchSize;

    @Value("${groupings.api.grouper.max-concurrent-requests}")
    private int maxConcurrentRequests;

    @Value("${groupings.api.grouper.retry-delays-millis}")
    private List<Long> retryDelaysMillis;

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

    @Test
    public void testGrouperApiService() {
        GrouperService service = context.getBean("grouperService", GrouperService.class);
        assertNotNull(service);
        assertTrue(service instanceof GrouperApiService);
    }

    @Test
    public void grouperApiServiceSendsBulkRequestsWithTheConfiguredSettings() {
        GrouperService service = context.getBean("grouperService", GrouperService.class);
        BatchExecutor batchExecutor = context.getBean(BatchExecutor.class);
        assertSame(batchExecutor, ReflectionTestUtils.getField(service, "batchExecutor"));

        assertEquals(lookupBatchSize, ReflectionTestUtils.getField(batchExecutor, "lookupBatchSize"));
        assertEquals(updateBatchSize, ReflectionTestUtils.getField(batchExecutor, "updateBatchSize"));
        assertEquals(maxConcurrentRequests, ReflectionTestUtils.getField(batchExecutor, "maxConcurrentRequests"));
        assertEquals(retryDelaysMillis, ReflectionTestUtils.getField(batchExecutor, "retryDelaysMillis"));
        assertEquals(List.of(5000L, 15000L), retryDelaysMillis);

        // The concurrent batches run on a pool of maxConcurrentRequests threads.
        ThreadPoolExecutor requestPool =
                (ThreadPoolExecutor) ReflectionTestUtils.getField(batchExecutor, "requestPool");
        assertEquals(maxConcurrentRequests, requestPool.getMaximumPoolSize());
    }
}