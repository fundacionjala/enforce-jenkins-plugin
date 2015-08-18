package org.fundacionjala.jenkins.plugins.enforce.tokens;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import org.fundacionjala.jenkins.plugins.enforce.Constants;
import org.fundacionjala.jenkins.plugins.enforce.EnforcePublisher;
import org.fundacionjala.jenkins.plugins.enforce.PieChartData;
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
                testResult.append("\nFailures");
                Integer processedUnitTests = 0;
                for (TestResult testResultItem : testResultContainer.getFailedTests()) {
                    processedUnitTests++;
                    testResult.append("\n").append(testResultItem.getFullName());
                    testResult.append("\n\tMessage: ").append(testResultItem.getErrorDetails());
                    testResult.append("\n\tStacktrace: ").append(testResultItem.getErrorStackTrace());
                    if (processedUnitTests < failedUnitTests) {
                        testResult.append("\n");
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