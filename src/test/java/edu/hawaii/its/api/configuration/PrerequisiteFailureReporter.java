package edu.hawaii.its.api.configuration;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * Repeats a failed integration-test prerequisite after all tests finish, so it is
 * not buried under Spring's startup stack traces. Registered in
 * META-INF/services/org.junit.platform.launcher.TestExecutionListener.
 */
public class PrerequisiteFailureReporter implements TestExecutionListener {

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        String message = PropertiesFileOverrideReporter.prerequisiteFailure();
        if (message != null) {
            System.out.println(PropertiesFileOverrideReporter.banner(message));
        }
    }
}
