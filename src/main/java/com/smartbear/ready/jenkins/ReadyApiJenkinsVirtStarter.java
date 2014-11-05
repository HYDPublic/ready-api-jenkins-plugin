package com.smartbear.ready.jenkins;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ReadyApiJenkinsVirtStarter extends Builder {

    private final String virtNames;
    private final String pathToProjectFile;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ReadyApiJenkinsVirtStarter(String virtNames, String pathToProjectFile) {
        this.virtNames = virtNames;
        this.pathToProjectFile = pathToProjectFile;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getVirtNames() {
        return virtNames;
    }

    public String getPathToProjectFile() {
        return pathToProjectFile;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws AbortException {
        listener.getLogger().println("Path to project file: " + pathToProjectFile + "");

        URL readyApiLibs = ReadyApiJenkinsVirtStarter.class.getResource("/ready-api-libs/ready-api-runners.jar");

        if (readyApiLibs == null) {
            listener.getLogger().println("ReadyApi Libs not found!");
        } else {
            Process process = null;
            try {
                process = new ProcessRunner()
                        .run(listener.getLogger(),
                                pathToProjectFile, virtNames, getDescriptor().getJavaHome());
                new ProcessKiller(process, getDescriptor().getVirtRunnerTimeout() * 1000L, listener.getLogger())
                        .killAfterTimeout();
            } catch (Exception e) {
                e.printStackTrace();
                listener.getLogger().println("Could not run virts! Problem: " + e);
                if (process != null) {
                    process.destroy();
                }
                throw new AbortException("Could not start ServiceV Virt(s).");
            }
        }
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link ReadyApiJenkinsVirtStarter}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         * <p/>
         * <p/>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String javaHome;

        private long virtRunnerTimeout = 600; // in seconds

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field.
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         * <p/>
         * Note that returning {@link FormValidation#error(String)} does not
         * prevent the form from being saved. It just means that a message
         * will be displayed to the user.
         */
        public FormValidation doCheckJavaHome(@QueryParameter String value)
                throws IOException, ServletException {
            value = value.trim();
            if (value.isEmpty()) {
                return FormValidation.ok();
            } else if (value.length() < 4) {
                return FormValidation.warning("Isn't the path too short?");
            } else if (!new File(value).isDirectory()) {
                return FormValidation.warning("This directory does not exist");
            } else if (!new File(value, ProcessRunner.JAVA_PATH_FROM_JAVA_HOME).isFile()) {
                return FormValidation.warning("This directory does not seem to contain a java executable");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckVirtRunnerTimeout(@QueryParameter long value)
                throws IOException, ServletException {
            if (value < 10L) {
                return FormValidation.error("Timeout is too short");
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Start ServiceV Virts";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            javaHome = formData.getString("javaHome");
            virtRunnerTimeout = formData.getLong("virtRunnerTimeout");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)

            save();
            return super.configure(req, formData);
        }

        public String getJavaHome() {
            return javaHome;
        }

        public long getVirtRunnerTimeout() {
            return virtRunnerTimeout;
        }
    }

}
