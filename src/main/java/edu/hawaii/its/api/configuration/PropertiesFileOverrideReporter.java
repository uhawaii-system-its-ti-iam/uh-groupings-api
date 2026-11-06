package edu.hawaii.its.api.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import jakarta.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import edu.hawaii.its.api.service.GroupPathService;

@Configuration
public class PropertiesFileOverrideReporter {

    private static final Log logger = LogFactory.getLog(PropertiesFileOverrideReporter.class);
    private static final String PLACEHOLDER_USER = "test_user";
    private static final String PLACEHOLDER_ADMIN = "test_admin";
    private static final String RED = "[31m";
    private static final String RESET = "[0m";
    private static final String RULE = "=".repeat(80);

    /* Set when an integration-test prerequisite check fails, so it can be repeated at the end of the run. */
    private static volatile String prerequisiteFailure;

    private final String message;

    public PropertiesFileOverrideReporter(@Value("${properties.override.result:DEFAULT}") String result) {
        this.message = "Properties override result: " + result;
    }

    @PostConstruct
    public void checkOverridesStatus() {
        logger.info(this.message);
    }

    public String getMessage() {
        return this.message;
    }

    /**
     * The prerequisite message recorded during this JVM's startup, or null if every check passed.
     */
    public static String prerequisiteFailure() {
        return prerequisiteFailure;
    }

    /**
     * Wraps the message in a red banner so it stands out at the end of the test output.
     */
    public static String banner(String message) {
        return String.join(System.lineSeparator(),
                "", RED + RULE, message, RULE + RESET, "");
    }

    private static void fail(String message) {
        prerequisiteFailure = message;
        throw new IllegalStateException(message);
    }

    /**
     * Describes why integration tests cannot run, or returns null when prerequisites are met.
     * {@code pathExists} is consulted only after the overrides file and credentials look usable.
     */
    public static String prerequisiteMessage(
            String overrideResult,
            String overridesPath,
            String localhostUser,
            String adminUser,
            String password,
            String groupingMany,
            String groupingSingle,
            Predicate<String> pathExists) {
        String blocker = overridesFileBlocker(overrideResult, overridesPath, localhostUser, adminUser, password);
        if (blocker != null) {
            return blocker;
        }
        return missingGroupingMessage(localhostUser, groupingMany, groupingSingle, pathExists);
    }

    /**
     * File and credential problems that can be reported before other beans are created.
     * Returns null when those prerequisites look usable.
     */
    static String overridesFileBlocker(Environment environment) {
        return overridesFileBlocker(
                environment.getProperty("properties.override.result", "DEFAULT"),
                overridesFilePath(),
                environment.getProperty("groupings.api.localhost.user", ""),
                environment.getProperty("groupings.api.test.admin_user", ""),
                environment.getProperty("grouperClient.webService.password", ""));
    }

    private static String overridesFileBlocker(
            String overrideResult,
            String overridesPath,
            String localhostUser,
            String adminUser,
            String password) {
        if (isUnconfiguredOverride(overrideResult)) {
            return skeletonMessage(overridesPath);
        }
        return credentialMessage(localhostUser, adminUser, password);
    }

    @Bean
    @Profile("integrationTest")
    public static BeanFactoryPostProcessor integrationTestOverridesFileCheck() {
        return new OverridesFileCheck();
    }

    private static String overridesFilePath() {
        return System.getProperty("user.home") + "/."
                + System.getProperty("user.name") + "-conf/uh-groupings-api-overrides.properties";
    }

    @Bean
    @Profile("integrationTest")
    public IntegrationTestPrerequisiteCheck integrationTestPrerequisiteCheck(
            @Value("${properties.override.result:DEFAULT}") String overrideResult,
            @Value("${groupings.api.localhost.user:}") String localhostUser,
            @Value("${groupings.api.test.admin_user:}") String adminUser,
            @Value("${grouperClient.webService.password:}") String password,
            @Value("${groupings.api.test.grouping_many:}") String groupingMany,
            @Value("${groupings.api.test.grouping_single:}") String groupingSingle,
            GroupPathService groupPathService) {
        return new IntegrationTestPrerequisiteCheck(
                overrideResult,
                overridesFilePath(),
                localhostUser,
                adminUser,
                password,
                groupingMany,
                groupingSingle,
                groupPathService);
    }

    private static boolean isUnconfiguredOverride(String overrideResult) {
        if (overrideResult == null) {
            return false;
        }
        String result = overrideResult.trim();
        return "DEFAULT".equals(result) || "TEMPLATE".equals(result);
    }

    private static String skeletonMessage(String overridesPath) {
        return String.join(System.lineSeparator(),
                "Integration tests did not run.",
                "The overrides file is missing or still the skeleton:",
                "  " + overridesPath,
                "Copy uh-groupings-api-overrides.skeleton.properties there, set",
                "groupings.api.localhost.user and groupings.api.test.admin_user to your UH username,",
                "set the Grouper test password, and set properties.override.result=CONFIGURED.",
                "The suite expects these groupings to already exist in Grouper test:",
                "  tmp:<user>:<user>-many",
                "  tmp:<user>:<user>-single");
    }

    /**
     * Credential problems that can be reported before other beans are created.
     * Returns null when those prerequisites look usable.
     */
    private static String credentialMessage(String localhostUser, String adminUser, String password) {
        List<String> problems = new ArrayList<>();
        if (isBlank(localhostUser)) {
            problems.add("groupings.api.localhost.user is blank.");
        } else if (PLACEHOLDER_USER.equals(localhostUser.trim())) {
            problems.add("groupings.api.localhost.user is still test_user.");
        }
        if (isBlank(adminUser)) {
            problems.add("groupings.api.test.admin_user is blank.");
        } else if (PLACEHOLDER_ADMIN.equals(adminUser.trim())) {
            problems.add("groupings.api.test.admin_user is still test_admin.");
        }
        if (isBlank(password)) {
            problems.add("grouperClient.webService.password is blank.");
        }
        if (problems.isEmpty()) {
            return null;
        }
        List<String> lines = new ArrayList<>();
        lines.add("Integration tests did not run.");
        lines.addAll(problems);
        return String.join(System.lineSeparator(), lines);
    }

    private static String missingGroupingMessage(
            String localhostUser,
            String groupingMany,
            String groupingSingle,
            Predicate<String> pathExists) {
        List<String> missing = new ArrayList<>();
        if (!pathExists.test(groupingMany)) {
            missing.add(groupingMany);
        }
        if (!pathExists.test(groupingSingle)) {
            missing.add(groupingSingle);
        }
        if (missing.isEmpty()) {
            return null;
        }
        List<String> lines = new ArrayList<>();
        lines.add("Integration tests did not run.");
        lines.add("groupings.api.localhost.user is " + localhostUser
                + ", but these groupings were not found in Grouper test:");
        for (String path : missing) {
            lines.add("  " + path);
        }
        return String.join(System.lineSeparator(), lines);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /* First Check - Overrides File Check. Checks if overrides file is present and credentials are valid. */
    static class OverridesFileCheck implements BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
            String message = overridesFileBlocker(beanFactory.getBean(Environment.class));
            if (message != null) {
                fail(message);
            }
        }
    }

    static class IntegrationTestPrerequisiteCheck {

        private final String overrideResult;
        private final String overridesPath;
        private final String localhostUser;
        private final String adminUser;
        private final String password;
        private final String groupingMany;
        private final String groupingSingle;
        private final GroupPathService groupPathService;

        IntegrationTestPrerequisiteCheck(
                String overrideResult,
                String overridesPath,
                String localhostUser,
                String adminUser,
                String password,
                String groupingMany,
                String groupingSingle,
                GroupPathService groupPathService) {
            this.overrideResult = overrideResult;
            this.overridesPath = overridesPath;
            this.localhostUser = localhostUser;
            this.adminUser = adminUser;
            this.password = password;
            this.groupingMany = groupingMany;
            this.groupingSingle = groupingSingle;
            this.groupPathService = groupPathService;
        }

        /* Second Check */
        @PostConstruct
        public void check() {
            String message = prerequisiteMessage(
                    overrideResult,
                    overridesPath,
                    localhostUser,
                    adminUser,
                    password,
                    groupingMany,
                    groupingSingle,
                    groupPathService::isValidPath);
            if (message != null) {
                fail(message);
            }
        }
    }

}
