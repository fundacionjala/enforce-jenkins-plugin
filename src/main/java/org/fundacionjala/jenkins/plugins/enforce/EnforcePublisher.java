/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.jenkins.plugins.enforce;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the post build Enforce that run after the build is completed.
 */
public class EnforcePublisher extends Recorder {

    private final String jsonFileName;
    private final float minimumCoverage;
    private PieChartData pieChartData;

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    @DataBoundConstructor
    public EnforcePublisher(String jsonFileName, float minimumCoverage) {
        this.jsonFileName = jsonFileName;
        this.minimumCoverage = minimumCoverage;
    }


    public String getJsonFileName() {
        return jsonFileName;
    }

    public float getMinimumCoverage() {
        return minimumCoverage;
    }

    public PieChartData getPieChartData(AbstractBuild<?, ?> build, TaskListener listener) {
        return this.getPieChartData(build, listener, null);
    }

    public PieChartData getPieChartData(AbstractBuild<?, ?> build, TaskListener listener, StringBuilder message) {
        if (null == this.pieChartData) {
            try {
                String jsonFilePath = Paths.get(build.getWorkspace().toURI()).resolve(jsonFileName).toString();
                if (!new File(jsonFilePath).exists()) {
                    String msg = jsonFilePath + " was not found";
                    listener.getLogger().println(msg);
                    if (null != message) {
                        message.append(msg);
                    }
                }
                pieChartData = PieChartData.newInstance(jsonFilePath);
            } catch (Exception exception) {
                build.setResult(Result.FAILURE);
                exception.printStackTrace(listener.fatalError("Unable to find coverage data"));
            }
        }
        return this.pieChartData;
    }

    /**
     * Runs the step over the given build and reports the progress to the listener.
     *
     * @param build    the current build.
     * @param launcher It is responsible for inheriting environment variables.
     * @param listener It receives events that happen during a build.
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        StringBuilder message = new StringBuilder();
        listener.getLogger().println(message.append("Minimum Coverage:").append(minimumCoverage).append("%"));
        if (null == this.getPieChartData(build, listener, message)) {
            return true;
        }
        double coveragePercentage = pieChartData != null ? pieChartData.getRoundedPercentage(2) : 0;
        if (pieChartData != null && pieChartData.coverageDataExists() && coveragePercentage < minimumCoverage) {
            message = new StringBuilder();
            listener.getLogger().println(message.append("Percentage coverage (").append(coveragePercentage)
                    .append("%)")
                    .append(" is less than minimum coverage(")
                    .append(minimumCoverage).append("%)"));
            build.setResult(Result.FAILURE);
        } else {
            build.setResult(Result.SUCCESS);
        }

        message = new StringBuilder();
        listener.getLogger().println(message.append(Constants.PUBLISHER_MESSAGE).append(":").append(jsonFileName).toString());

        return true;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new EnforceProjectAction(project);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }


    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {


        DescriptorImpl() {
            super(EnforcePublisher.class);
        }

        public String getDisplayName() {
            return Constants.PUBLISHER_DISPLAY_NAME;
        }

        /**
         * Configures parameters of coverage publisher
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindParameters(this, Constants.PUBLISHER_PARAMETER + ".");
            save();
            return super.configure(req, formData);
        }

        /**
         * Creates new instance of coverage publisher
         */
        @Override
        public EnforcePublisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            EnforcePublisher instance = req.bindJSON(EnforcePublisher.class, formData);
            return instance;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }


}
