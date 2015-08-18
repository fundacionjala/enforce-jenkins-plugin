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
 * Provides the ENFORCE_COVERAGE_STATUS macro with the generated values by the unit test execution.
 * @autor Navor Nuñez
 */
@Extension
public class EnforceCoverageStatusTokenMacro extends DataBoundTokenMacro {

    @Override
    public String evaluate(AbstractBuild<?, ?> build, TaskListener listener, String macroName) throws MacroEvaluationException, IOException, InterruptedException {
        EnforcePublisher enforcePublisher = (EnforcePublisher) build.getProject().getPublishersList().get(EnforcePublisher.DESCRIPTOR);
        PieChartData pieChartData = enforcePublisher.getPieChartData(build, listener);
        if (null == pieChartData) {
            return "";
        }
        StringBuilder coverageStatus = new StringBuilder();
        if (pieChartData.dataExists()) {
            coverageStatus.append("Coverage Status: ");
            coverageStatus.append(pieChartData.getFileCoverageStatus());
        }
        return coverageStatus.toString();
    }

    @Override
    public boolean acceptsMacroName(String macroName) {
        return Constants.ENFORCE_COVERAGE_STATUS.equalsIgnoreCase(macroName);
    }
}