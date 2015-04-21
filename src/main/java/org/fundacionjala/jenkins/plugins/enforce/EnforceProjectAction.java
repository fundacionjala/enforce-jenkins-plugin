/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.jenkins.plugins.enforce;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Actionable;
import hudson.model.ProminentProjectAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Creates an additional URL subspace and exposes a link in the left hand side menu for Enforce jenkins plugin.
 */
public class EnforceProjectAction extends Actionable implements ProminentProjectAction {

    private final AbstractProject<?, ?> project;
    private final String jsonFileName;
    private final double minimumCoverage;
    private PieChartData pieChartData;

    /**
     * Creates an additional action for Enforce jenkins plugin
     *
     * @param project base implementation of Jobs
     */
    public EnforceProjectAction(AbstractProject<?, ?> project) {
        this.project = project;
        EnforcePublisher enforcePublisher = (EnforcePublisher) project.getPublishersList().get(EnforcePublisher.DESCRIPTOR);
        this.jsonFileName = enforcePublisher.getJsonFileName();
        this.minimumCoverage = enforcePublisher.getMinimumCoverage();
        this.pieChartData = new PieChartData();
    }

    /**
     * Gets the data to render the pie chart in floating box
     *
     * @param build the current build to get the workspace
     * @return a new pie chart instance
     */
    public PieChartData getPieChartData(AbstractBuild<?, ?> build) {
        try {
            String jsonFilePath = Paths.get(build.getWorkspace().toURI()).resolve(jsonFileName).toString();
            pieChartData = PieChartData.newInstance(jsonFilePath);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return pieChartData;
    }

    /**
     * Gets percentage coverage color
     *
     * @return a string that represents a color name for css file
     */
    public String getPercentageColor() {
        String percentageColor = Constants.COLOR_GREEN;
        if (isCoverageVisibleOnProjectDashboard()) {
            if (pieChartData.getPercentage() < this.minimumCoverage) {
                percentageColor = Constants.COLOR_RED;
            }
        }
        return percentageColor;
    }

    /**
     * Gets percentage for not covered lines
     *
     * @return a float with value of not covered lines
     */
    public double getPercentageNotCovered() {
        double coveragePercentage = pieChartData.getRoundedPercentage(2);
        return pieChartData.getRoundedValue(100 - coveragePercentage, 2);
    }

    /**
     * Gets coverage percentage
     *
     * @return a float with value of coverage percentage
     */
    public double getPercentage() {
        return pieChartData.getRoundedPercentage(2);
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return "";
    }

    public String getUrlName() {
        return Constants.ACTION_URL_NAME;
    }

    public String getSearchUrl() {
        return getUrlName();
    }

    /**
     * Gets true if pie chart has data to show it on the project dashboard
     */
    public boolean isCoverageVisibleOnProjectDashboard() {
        return pieChartData.coverageDataExists() && pieChartData.dataExists();
    }

    /**
     * Redirects to an empty page
     */
    public void doIndex(StaplerRequest staplerRequest, StaplerResponse staplerResponse) throws IOException {
        staplerResponse.sendRedirect2(Constants.ACTION_PAGE_COVERAGE_DETAILS);
    }

}
