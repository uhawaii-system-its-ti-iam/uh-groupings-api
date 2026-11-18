package edu.hawaii.its.api.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.mock.env.MockEnvironment;

public class PropertiesFileOverrideReporterPrerequisiteTest {

    private static final String OVERRIDES_PATH =
            "C:/Users/dev/.dev-conf/uh-groupings-api-overrides.properties";
    private static final String USER = "jdoe";
    private static final String ADMIN = "jdoe";
    private static final String PASSWORD = "s3cret-password";
    private static final String GROUPING_MANY = "tmp:jdoe:jdoe-many";
    private static final String GROUPING_SINGLE = "tmp:jdoe:jdoe-single";
    private static final String GROUPING_LARGE = "tmp:jdoe:jdoe-large";
    private static final String GROUPING_TRUE_EMPTY = "tmp:jdoe:jdoe-true-empty";
    private static final String OWNER_GROUPING = "tmp:jdoe:jdoe-complex";
    private static final List<String> REQUIRED_GROUPINGS =
            List.of(GROUPING_MANY, GROUPING_SINGLE, GROUPING_LARGE, GROUPING_TRUE_EMPTY, OWNER_GROUPING);

    @AfterEach
    public void clearRecordedFailure() {
        PropertiesFileOverrideReporter.clearPrerequisiteFailure();
    }

    @Test
    public void missingOverridesFileIsReportedBeforeOtherBeansAreCreated() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton(AbstractApplicationContext.ENVIRONMENT_BEAN_NAME, new MockEnvironment());
        PropertiesFileOverrideReporter.OverridesFileCheck check = new PropertiesFileOverrideReporter.OverridesFileCheck();

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> check.postProcessBeanFactory(beanFactory));

        String message = failure.getMessage();
        assertTrue(message.contains("The overrides file is missing or still the skeleton:"));
        assertTrue(message.contains(System.getProperty("user.home") + "/."
                + System.getProperty("user.name") + "-conf/uh-groupings-api-overrides.properties"));
        assertTrue(message.contains("properties.override.result=CONFIGURED"));
        assertFalse(message.contains(PASSWORD));
    }

    @Test
    public void failedCheckIsRecordedForTheEndOfRunBanner() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton(AbstractApplicationContext.ENVIRONMENT_BEAN_NAME, new MockEnvironment());

        assertThrows(IllegalStateException.class,
                () -> new PropertiesFileOverrideReporter.OverridesFileCheck().postProcessBeanFactory(beanFactory));

        String recorded = PropertiesFileOverrideReporter.prerequisiteFailure();
        assertTrue(recorded.startsWith("Integration tests did not run."));
        String banner = PropertiesFileOverrideReporter.banner(recorded);
        assertTrue(banner.contains("\u001B[31m"));
        assertTrue(banner.contains(recorded));
        assertTrue(banner.contains("\u001B[0m"));
    }

    @Test
    public void configuredOverridesFileDoesNotStopStartup() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("properties.override.result", "CONFIGURED")
                .withProperty("groupings.api.localhost.user", USER)
                .withProperty("groupings.api.test.admin_user", ADMIN)
                .withProperty("grouperClient.webService.password", PASSWORD);
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton(AbstractApplicationContext.ENVIRONMENT_BEAN_NAME, environment);
        PropertiesFileOverrideReporter.OverridesFileCheck check = new PropertiesFileOverrideReporter.OverridesFileCheck();

        assertDoesNotThrow(() -> check.postProcessBeanFactory(beanFactory));
    }

    @Test
    public void defaultOverrideNamesTheSkeletonFileAndDoesNotCheckGroupings() {
        String message = prerequisiteMessage("DEFAULT", path -> fail("path check should not run"));

        assertSkeletonMessage(message);
    }

    @Test
    public void templateOverrideNamesTheSkeletonFileAndDoesNotCheckGroupings() {
        String message = prerequisiteMessage(" TEMPLATE ", path -> fail("path check should not run"));

        assertSkeletonMessage(message);
    }

    @Test
    public void blankUserIsNamedAndDoesNotCheckGroupings() {
        String message = prerequisiteMessage(
                "CONFIGURED",
                "",
                ADMIN,
                PASSWORD,
                path -> fail("path check should not run"));

        assertTrue(message.contains("groupings.api.localhost.user is blank."));
        assertFalse(message.contains(PASSWORD));
    }

    @Test
    public void placeholderUserIsNamedAndPasswordIsNotPrinted() {
        String message = prerequisiteMessage(
                "CONFIGURED",
                "test_user",
                ADMIN,
                PASSWORD,
                path -> fail("path check should not run"));

        assertTrue(message.startsWith("Integration tests did not run."));
        assertTrue(message.contains("groupings.api.localhost.user is still test_user."));
        assertFalse(message.contains(PASSWORD));
        assertFalse(message.contains("grouperClient.webService.password"));
    }

    @Test
    public void blankAdminAndBlankPasswordAreNamed() {
        String message = prerequisiteMessage(
                "CONFIGURED",
                USER,
                "  ",
                "",
                path -> fail("path check should not run"));

        assertTrue(message.contains("groupings.api.test.admin_user is blank."));
        assertTrue(message.contains("grouperClient.webService.password is blank."));
        assertFalse(message.contains(PASSWORD));
        assertFalse(message.contains("groupings.api.localhost.user is"));
    }

    @Test
    public void placeholderAdminIsNamed() {
        String message = prerequisiteMessage(
                "CONFIGURED",
                USER,
                "test_admin",
                PASSWORD,
                path -> fail("path check should not run"));

        assertTrue(message.contains("groupings.api.test.admin_user is still test_admin."));
        assertFalse(message.contains(PASSWORD));
    }

    @Test
    public void missingGroupingManyNamesThatPathAndTheUsername() {
        List<String> checked = new ArrayList<>();
        String message = prerequisiteMessage("CONFIGURED", USER, ADMIN, PASSWORD, path -> {
            checked.add(path);
            return !GROUPING_MANY.equals(path);
        });

        assertTrue(message.contains("groupings.api.localhost.user is " + USER));
        assertTrue(message.contains(GROUPING_MANY));
        assertFalse(message.contains(GROUPING_SINGLE));
        assertFalse(message.contains(PASSWORD));
        assertTrue(checked.contains(GROUPING_MANY));
        assertTrue(checked.contains(GROUPING_SINGLE));
    }

    @Test
    public void missingGroupingSingleNamesThatPathAndTheUsername() {
        String message = prerequisiteMessage("CONFIGURED", USER, ADMIN, PASSWORD, path -> !GROUPING_SINGLE.equals(path));

        assertTrue(message.contains("groupings.api.localhost.user is " + USER));
        assertTrue(message.contains(GROUPING_SINGLE));
        assertFalse(message.contains(GROUPING_MANY));
    }

    @Test
    public void missingBothGroupingsNamesBothPaths() {
        String message = prerequisiteMessage("CONFIGURED", USER, ADMIN, PASSWORD, path -> false);

        assertTrue(message.contains(GROUPING_MANY));
        assertTrue(message.contains(GROUPING_SINGLE));
        assertTrue(message.contains("groupings.api.localhost.user is " + USER));
    }

    @Test
    public void missingGroupingOtherThanManyOrSingleIsNamed() {
        String message = prerequisiteMessage("CONFIGURED", USER, ADMIN, PASSWORD, path -> !OWNER_GROUPING.equals(path));

        assertTrue(message.contains("groupings.api.localhost.user is " + USER));
        assertTrue(message.contains(OWNER_GROUPING));
        assertFalse(message.contains(GROUPING_MANY));
        assertFalse(message.contains(GROUPING_LARGE));
    }

    @Test
    public void configuredEnvironmentReturnsNoMessage() {
        List<String> checked = new ArrayList<>();
        String message = prerequisiteMessage("CONFIGURED", USER, ADMIN, PASSWORD, path -> {
            checked.add(path);
            return true;
        });

        assertNull(message);
        assertTrue(checked.containsAll(REQUIRED_GROUPINGS));
    }

    @Test
    public void unitTestFailuresAreNotLeftForTheEndOfRunBanner() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton(AbstractApplicationContext.ENVIRONMENT_BEAN_NAME, new MockEnvironment());
        assertThrows(IllegalStateException.class,
                () -> new PropertiesFileOverrideReporter.OverridesFileCheck().postProcessBeanFactory(beanFactory));

        PropertiesFileOverrideReporter.clearPrerequisiteFailure();

        assertNull(PropertiesFileOverrideReporter.prerequisiteFailure());
    }

    private static void assertSkeletonMessage(String message) {
        assertTrue(message.startsWith("Integration tests did not run."));
        assertTrue(message.contains("The overrides file is missing or still the skeleton:"));
        assertTrue(message.contains(OVERRIDES_PATH));
        assertTrue(message.contains("uh-groupings-api-overrides.skeleton.properties"));
        assertTrue(message.contains("groupings.api.localhost.user"));
        assertTrue(message.contains("groupings.api.test.admin_user"));
        assertTrue(message.contains("properties.override.result=CONFIGURED"));
        assertTrue(message.contains("groupings.api.test.required_groupings"));
        assertFalse(message.contains(PASSWORD));
    }

    private static String prerequisiteMessage(String overrideResult, Predicate<String> pathExists) {
        return prerequisiteMessage(overrideResult, "test_user", "test_admin", PASSWORD, pathExists);
    }

    private static String prerequisiteMessage(
            String overrideResult,
            String localhostUser,
            String adminUser,
            String password,
            Predicate<String> pathExists) {
        return PropertiesFileOverrideReporter.prerequisiteMessage(
                overrideResult,
                OVERRIDES_PATH,
                localhostUser,
                adminUser,
                password,
                REQUIRED_GROUPINGS,
                pathExists);
    }

}
