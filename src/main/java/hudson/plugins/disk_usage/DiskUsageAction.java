package hudson.plugins.disk_usage;

import hudson.model.ProminentProjectAction;

/**
 * Disk usage information holder
 * @author dvrzalik
 */
public abstract class DiskUsageAction implements ProminentProjectAction {

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return Messages.DisplayName();
    }

    public String getUrlName() {
        return Messages.UrlName();
    }
}
