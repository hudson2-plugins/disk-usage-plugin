package hudson.plugins.disk_usage;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.plugins.disk_usage.DiskUsageProperty.DiskUsageDescriptor;
import hudson.util.ColorPalette;
import hudson.util.Graph;
import hudson.util.graph.ChartUtil;
import hudson.util.graph.DataSet;
import hudson.util.graph.GraphSeries;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Disk usage of a project
 * 
 * @author dvrzalik
 */
public class ProjectDiskUsageAction extends DiskUsageAction {

    AbstractProject<? extends AbstractProject, ? extends AbstractBuild> project;

    public ProjectDiskUsageAction(AbstractProject<? extends AbstractProject, ? extends AbstractBuild> project) {
        this.project = project;
    }

    @Override
    public String getUrlName() {
        return "disk-usage";
    }

    /**
     * @return Disk usage for all builds
     */
    public DiskUsage getDiskUsage() {
        DiskUsage du = new DiskUsage(0, 0);

        if (project != null) {
            BuildDiskUsageAction action = null;
            Iterator<? extends AbstractBuild> buildIterator = project.getBuilds().iterator();
            while ((action == null) && buildIterator.hasNext()) {
                action = buildIterator.next().getAction(BuildDiskUsageAction.class);
            }
            if (action != null) {
                DiskUsage bdu = action.getDiskUsage();
                //Take last available workspace size
                du.wsUsage = bdu.getWsUsage();
                du.buildUsage += bdu.getBuildUsage();
            }

            while (buildIterator.hasNext()) {
                action = buildIterator.next().getAction(BuildDiskUsageAction.class);
                if (action != null) {
                    du.buildUsage += action.getDiskUsage().getBuildUsage();
                }
            }
        }

        return du;
    }

    public BuildDiskUsageAction getLastBuildAction() {
        Run run = project.getLastBuild();
        if (run != null) {
            return run.getAction(BuildDiskUsageAction.class);
        }

        return null;
    }

    /**
     * Generates a graph with disk usage trend
     * 
     */
    public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (ChartUtil.awtProblemCause != null) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }

        Graph graph = new Graph(-1, 500, 200);
        setGraphDataSet(graph);
        graph.doPng(req, rsp);
    }

    private void setGraphDataSet(Graph graph) {

        //TODO if(nothing_changed) return;

        DataSet dataSet = new DataSet();

        GraphSeries<String> xSeries = new GraphSeries<String>("Build No.");
        dataSet.setXSeries(xSeries);

        GraphSeries<Number> ySeriesWorkspace = new GraphSeries<Number>(GraphSeries.TYPE_LINE, "Workspace", ColorPalette.BLUE, false, false);
        ySeriesWorkspace.setStacked(false);
        dataSet.addYSeries(ySeriesWorkspace);

        GraphSeries<Number> ySeriesBuild = new GraphSeries<Number>(GraphSeries.TYPE_LINE, "Build", ColorPalette.RED, false, false);
        ySeriesWorkspace.setStacked(false);
        dataSet.addYSeries(ySeriesBuild);

        List<Object[]> usages = new ArrayList<Object[]>();
        long maxValue = 0;
        //First iteration just to get scale of the y-axis
        for (AbstractBuild build : project.getBuilds()) {
            BuildDiskUsageAction dua = build.getAction(BuildDiskUsageAction.class);
            if (dua != null) {
                DiskUsage usage = dua.getDiskUsage();
                maxValue = Math.max(maxValue, Math.max(usage.wsUsage, usage.getBuildUsage()));
                usages.add(new Object[]{build, usage.wsUsage, usage.getBuildUsage()});
            }
        }

        int floor = (int) DiskUsage.getScale(maxValue);
        
        String unit = DiskUsage.getUnitString(floor);
        graph.setYAxisLabel("Disk Usage (" + unit + ")");
        
        double base = Math.pow(1024, floor);

        for (Object[] usage : usages) {
            String buildNo = ((AbstractBuild) usage[0]).getDisplayName();
            xSeries.add(buildNo);

            double workspaceSize = ((Long) usage[1]) / base;
            double buildSize = ((Long) usage[2]) / base;
            ySeriesWorkspace.add(workspaceSize);
            ySeriesBuild.add(buildSize);
        }

        graph.setData(dataSet);
    }

    /** Shortcut for the jelly view */
    public boolean showGraph() {
        return Hudson.getInstance().getDescriptorByType(DiskUsageDescriptor.class).isShowGraph();
    }
}
