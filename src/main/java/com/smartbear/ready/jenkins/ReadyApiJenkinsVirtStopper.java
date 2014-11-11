package com.smartbear.ready.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class ReadyApiJenkinsVirtStopper extends Recorder {

    private final boolean breakBuildIfNoVirtStopped;

    @DataBoundConstructor
    public ReadyApiJenkinsVirtStopper(boolean breakBuildIfNoVirtStopped) {
        this.breakBuildIfNoVirtStopped = breakBuildIfNoVirtStopped;
    }

    public boolean isBreakBuildIfNoVirtStopped() {
        return breakBuildIfNoVirtStopped;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        listener.getLogger().println("Trying to stop ServiceV Virts");
        boolean stoppedAnyVirt = ProcessKeeper.killProcess(build.getId(), listener.getLogger());
        if (!stoppedAnyVirt && breakBuildIfNoVirtStopped) {
            listener.getLogger().println("FAILURE: Did not find any ServiceV Virt to stop!");
            build.setResult(Result.FAILURE);
        }
        listener.getLogger().println("Successfully stopped all ServiceV Virts");
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Stop ServiceV Virts";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }

    }

}
