package edu.hawaii.its.api.configuration;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import edu.hawaii.its.api.service.BatchExecutor;
import edu.hawaii.its.api.service.ExecutorService;
import edu.hawaii.its.api.service.GrouperApiService;
import edu.hawaii.its.api.service.GrouperService;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

@Configuration
public class GrouperPropertyConfigurer {

    public static final Log log = LogFactory.getLog(GrouperPropertyConfigurer.class);

    @Value("${grouperClient.webService.url:}")
    private String webServiceUrl;

    @Value("${grouperClient.webService.login:}")
    private String webServiceLogin;

    @Value("${grouperClient.webService.password:}")
    private String webServicePassword;

    @PostConstruct
    public void init() {
        GrouperClientConfig config = GrouperClientConfig.retrieveConfig();

        setOverride(config, "grouperClient.webService.url", webServiceUrl);
        setOverride(config, "grouperClient.webService.login", webServiceLogin);
        setOverride(config, "grouperClient.webService.password", webServicePassword);
    }

    private void setOverride(GrouperClientConfig config, String key, String value) {
        if (value != null) {
            config.propertiesOverrideMap().put(key, value);
        }
    }

    @Bean(name = "grouperService")
    @ConditionalOnProperty(name = "grouping.api.server.type", havingValue = "GROUPER", matchIfMissing = true)
    public GrouperService grouperApiService(ExecutorService executorService, BatchExecutor batchExecutor) {
        log.debug("REAL Grouper Api Service Started");
        return new GrouperApiService(executorService, batchExecutor);
    }

    /**
     * Sends the bulk lookups, adds and removes (e.g. of an import) to Grouper in batches. The concurrent batches of
     * every lookup and removal share one pool of maxConcurrentRequests threads, which caps the bulk requests this API
     * instance sends at once. Idle threads end after a minute.
     */
    @Bean
    public BatchExecutor batchExecutor(ExecutorService executorService,
            @Value("${groupings.api.grouper.lookup-batch-size}") int lookupBatchSize,
            @Value("${groupings.api.grouper.update-batch-size}") int updateBatchSize,
            @Value("${groupings.api.grouper.max-concurrent-requests}") int maxConcurrentRequests,
            @Value("${groupings.api.grouper.retry-delays-millis}") List<Long> retryDelaysMillis) {
        CustomizableThreadFactory threadFactory = new CustomizableThreadFactory("grouper-request-");
        threadFactory.setDaemon(true);
        ThreadPoolExecutor requestPool = new ThreadPoolExecutor(maxConcurrentRequests, maxConcurrentRequests,
                1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), threadFactory);
        requestPool.allowCoreThreadTimeOut(true);
        return new BatchExecutor(executorService, requestPool, lookupBatchSize, updateBatchSize,
                maxConcurrentRequests, retryDelaysMillis);
    }
}