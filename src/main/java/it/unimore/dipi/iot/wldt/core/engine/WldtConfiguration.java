package it.unimore.dipi.iot.wldt.core.engine;

import java.util.List;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class WldtConfiguration {

    private int threadPoolSize = 5;

    private String deviceNameSpace;

    private String wldtBaseIdentifier;

    private int wldtStartupTimeSeconds = 0;

    private List<String> activeProtocolList = null;

    private boolean applicationMetricsEnabled = false;

    private int applicationMetricsReportingPeriodSeconds = 60;

    private List<String> metricsReporterList = null;

    private String graphiteReporterAddress = "127.0.0.1";

    private int graphiteReporterPort = 2003;

    private String graphitePrefix = "wldt";

    public WldtConfiguration() {
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public String getDeviceNameSpace() {
        return deviceNameSpace;
    }

    public void setDeviceNameSpace(String deviceNameSpace) {
        this.deviceNameSpace = deviceNameSpace;
    }

    public String getWldtBaseIdentifier() {
        return wldtBaseIdentifier;
    }

    public void setWldtBaseIdentifier(String wldtBaseIdentifier) {
        this.wldtBaseIdentifier = wldtBaseIdentifier;
    }

    public boolean getApplicationMetricsEnabled() {
        return applicationMetricsEnabled;
    }

    public void setApplicationMetricsEnabled(boolean applicationMetricsEnabled) {
        this.applicationMetricsEnabled = applicationMetricsEnabled;
    }

    public int getApplicationMetricsReportingPeriodSeconds() {
        return applicationMetricsReportingPeriodSeconds;
    }

    public void setApplicationMetricsReportingPeriodSeconds(int applicationMetricsReportingPeriodSeconds) {
        this.applicationMetricsReportingPeriodSeconds = applicationMetricsReportingPeriodSeconds;
    }

    public int getWldtStartupTimeSeconds() {
        return wldtStartupTimeSeconds;
    }

    public void setWldtStartupTimeSeconds(int wldtStartupTimeSeconds) {
        this.wldtStartupTimeSeconds = wldtStartupTimeSeconds;
    }

    public List<String> getActiveProtocolList() {
        return activeProtocolList;
    }

    public void setActiveProtocolList(List<String> activeProtocolList) {
        this.activeProtocolList = activeProtocolList;
    }

    public List<String> getMetricsReporterList() {
        return metricsReporterList;
    }

    public void setMetricsReporterList(List<String> metricsReporterList) {
        this.metricsReporterList = metricsReporterList;
    }

    public String getGraphiteReporterAddress() {
        return graphiteReporterAddress;
    }

    public void setGraphiteReporterAddress(String graphiteReporterAddress) {
        this.graphiteReporterAddress = graphiteReporterAddress;
    }

    public int getGraphiteReporterPort() {
        return graphiteReporterPort;
    }

    public void setGraphiteReporterPort(int graphiteReporterPort) {
        this.graphiteReporterPort = graphiteReporterPort;
    }

    public String getGraphitePrefix() {
        return graphitePrefix;
    }

    public void setGraphitePrefix(String graphitePrefix) {
        this.graphitePrefix = graphitePrefix;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WldtConfiguration{");
        sb.append("threadPoolSize=").append(threadPoolSize);
        sb.append(", deviceNameSpace='").append(deviceNameSpace).append('\'');
        sb.append(", wldtBaseIdentifier='").append(wldtBaseIdentifier).append('\'');
        sb.append(", wldtStartupTimeSeconds=").append(wldtStartupTimeSeconds);
        sb.append(", activeProtocolList=").append(activeProtocolList);
        sb.append(", applicationMetricsEnabled=").append(applicationMetricsEnabled);
        sb.append(", applicationMetricsReportingPeriodSeconds=").append(applicationMetricsReportingPeriodSeconds);
        sb.append(", metricsReporterList=").append(metricsReporterList);
        sb.append(", graphiteReporterAddress='").append(graphiteReporterAddress).append('\'');
        sb.append(", graphiteReporterPort=").append(graphiteReporterPort);
        sb.append(", graphitePrefix='").append(graphitePrefix).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
