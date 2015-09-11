package org.fundacionjala.jenkins.plugins.enforce.tokens;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import org.fundacionjala.jenkins.plugins.enforce.Constants;
import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;

import java.io.IOException;

/**
 * Provides the ENFORCE_TEST_RESULT macro with the generated values by the unit test execution.
 * @autor Navor Nuñez
 */
@Extension
public class EnforceTestResultTokenMacro extends DataBoundTokenMacro {

    @Override
    public String evaluate(AbstractBuild<?, ?> build, TaskListener listener, String macroName) throws MacroEvaluationException, IOException, InterruptedException {
        AbstractTestResultAction<?> testResultContainer = build.getAction(AbstractTestResultAction.class);
        StringBuilder testResult = new StringBuilder();
        if (testResultContainer != null) {
            String testResultDescription = testResultContainer.getBuildHealth().getDescription();
            testResult.append(testResultDescription);
            Integer failedUnitTests = testResultContainer.getFailedTests().size();
            if (failedUnitTests > 0) {
                testResult.append("\nFailures:");
                Integer processedUnitTests = 0;
                for (TestResult testResultItem : testResultContainer.getFailedTests()) {
                    testResult.append(Constants.LINE_SEPARATOR).append("\n").append(testResultItem.getFullName());
                    if ((null != testResultItem.getErrorDetails()) && !testResultItem.getErrorDetails().trim().isEmpty()) {
                        testResult.append("\n-------- Message --------\n").append(testResultItem.getErrorDetails().trim());
                    }
                    if ((null != testResultItem.getStderr()) && !testResultItem.getStderr().trim().isEmpty()) {
                        testResult.append("\n-------- Stacktrace --------\n").append(testResultItem.getStderr().trim());
                    }
                    processedUnitTests++;
                    if (processedUnitTests.equals(failedUnitTests)) {
                        testResult.append(Constants.LINE_SEPARATOR);
                    }
                }
            }
        }
        return testResult.toString();
    }

    @Override
    public boolean acceptsMacroName(String macroName) {
        return Constants.ENFORCE_TEST_RESULT.equalsIgnoreCase(macroName);
    }
}
