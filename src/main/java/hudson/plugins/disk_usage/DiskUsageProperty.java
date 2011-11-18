package hudson.plugins.disk_usage;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.Extension;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

//(basically nothing to see here)
/**
 * This Property sets DiskUsage action. 
 * 
 * @author dvrzalik
 */
public class DiskUsageProperty extends JobProperty<Job<?, ?>> {
    
     @Override
    public Action getJobAction(Job<?, ?> job) {
        return new ProjectDiskUsageAction((AbstractProject) job);//??
    }

    @Extension
    public static final class DiskUsageDescriptor extends JobPropertyDescriptor {

        public DiskUsageDescriptor() {
            load();
        }

        //Show graph on the project page?
        private boolean showGraph;

        @Override
        public String getDisplayName() {
            return Messages.DisplayName();
        }


        @Override
        public DiskUsageProperty newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             return new DiskUsageProperty();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            showGraph = req.getParameter("disk_usage.showGraph") != null;
            save();
            return super.configure(req, formData);
        }

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return true;
        }

        public boolean isShowGraph() {
            //The graph is shown by default
            return showGraph;
        }

        public void setShowGraph(Boolean showGraph) {
            this.showGraph = showGraph;
        }
    }
}

    
