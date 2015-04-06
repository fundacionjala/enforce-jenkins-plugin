/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.jenkins.plugins.enforce;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.nio.file.Paths;

/**
 * Represents the post build Enforce that run after the build is completed.
 */
public class EnforcePublisher extends Recorder {

    private final String jsonFileName;
    private final float minimumCoverage;

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
        String jsonFilePath = "";
        PieChartData pieChartData = null;

        try {
            jsonFilePath = Paths.get(build.getWorkspace().toURI()).resolve(jsonFileName).toString();
            message = new StringBuilder();
            if (!new File(jsonFilePath).exists()) {
                listener.getLogger().println(message.append(jsonFilePath).append(" was not found"));
                return true;
            }
            pieChartData = PieChartData.newInstance(jsonFilePath);
        } catch (Exception exception) {
            build.setResult(Result.FAILURE);
            exception.printStackTrace(listener.fatalError("Unable to find coverage data"));
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
