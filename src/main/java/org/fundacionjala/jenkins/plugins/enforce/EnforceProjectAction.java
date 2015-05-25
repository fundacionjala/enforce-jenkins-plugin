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
    private int[] range;
    private String[] color;

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

        this.range =  new int[]{0, 75, 80, 95, 100};
        this.color = new String[]{"#d2322d","#ed9c28","#2aabd2","#5cb85c"};

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
        String percentageColor = "#5cb85c";
        for (int i = 0; i < range.length - 1; i++) {
            if(this.range[i] <= pieChartData.getPercentage() && this.range[i+1] > pieChartData.getPercentage() ) {
                percentageColor = this.color[i];
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
        return Constants.ACTION_ICON_FILE_NAME;
    }

    public String getDisplayName() {
        return Constants.ACTION_DISPLAY_NAME;
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
