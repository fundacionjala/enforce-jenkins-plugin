package org.fundacionjala.jenkins.plugins.enforce.tokens;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import org.fundacionjala.jenkins.plugins.enforce.Constants;
import org.fundacionjala.jenkins.plugins.enforce.EnforcePublisher;
import org.fundacionjala.jenkins.plugins.enforce.PieChartData;
import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;

import java.io.IOException;

/**
 * Provides the ENFORCE_COVERAGE_RESULT macro with the generated values by the unit test execution.
 * @autor Navor Nuñez
 */
@Extension
public class EnforceCoverageResultTokenMacro extends DataBoundTokenMacro {

    @Override
    public String evaluate(AbstractBuild<?, ?> build, TaskListener listener, String macroName) throws MacroEvaluationException, IOException, InterruptedException {
        EnforcePublisher enforcePublisher = (EnforcePublisher) build.getProject().getPublishersList().get(EnforcePublisher.DESCRIPTOR);
        PieChartData pieChartData = enforcePublisher.getPieChartData(build, listener);
        if (null == pieChartData) {
            return "";
        }
        StringBuilder coverageResult = new StringBuilder();
        if (pieChartData.coverageDataExists()) {
            coverageResult.append("Coverage Result: ");
            coverageResult.append(pieChartData.getRoundedPercentage(2));
            coverageResult.append("% of code coverage, ");
            coverageResult.append(pieChartData.getCoveragePercentStatus());
            coverageResult.append(" status.");
        }
        return coverageResult.toString();
    }

    @Override
    public boolean acceptsMacroName(String macroName) {
        return Constants.ENFORCE_COVERAGE_RESULT.equalsIgnoreCase(macroName);
    }
}
