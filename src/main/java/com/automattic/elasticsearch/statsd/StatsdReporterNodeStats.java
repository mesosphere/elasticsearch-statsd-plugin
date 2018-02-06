package com.automattic.elasticsearch.statsd;

import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.http.HttpStats;
import org.elasticsearch.monitor.fs.FsInfo;
import org.elasticsearch.monitor.jvm.JvmStats;
import org.elasticsearch.monitor.os.OsStats;
import org.elasticsearch.monitor.process.ProcessStats;
import org.elasticsearch.threadpool.ThreadPoolStats;
import org.elasticsearch.transport.TransportStats;

public class StatsdReporterNodeStats extends StatsdReporter {

    private final NodeStats nodeStats;
    private final String nodeName;
    private final Boolean statsdReportFsDetails;

    public StatsdReporterNodeStats(NodeStats nodeStats, String nodeName, Boolean statsdReportFsDetails) {
        this.nodeStats = nodeStats;
        this.nodeName = nodeName;
        this.statsdReportFsDetails = statsdReportFsDetails;
    }

    public void run() {
        try {
            this.sendNodeFsStats(this.nodeStats.getFs());
            this.sendNodeJvmStats(this.nodeStats.getJvm());
            this.sendNodeOsStats(this.nodeStats.getOs());
            this.sendNodeProcessStats(this.nodeStats.getProcess());
            this.sendNodeHttpStats(this.nodeStats.getHttp());
            this.sendNodeTransportStats(this.nodeStats.getTransport());
            this.sendNodeThreadPoolStats(this.nodeStats.getThreadPool());
        } catch (Exception e) {
            this.logException(e);
        }
    }

    private void sendNodeThreadPoolStats(ThreadPoolStats threadPoolStats) {
        String prefix = this.getPrefix("thread_pool");
        for (ThreadPoolStats.Stats stats : threadPoolStats) {
            String threadPoolType = prefix + "." + stats.getName();

            this.sendGauge(threadPoolType, "threads", stats.getThreads());
            this.sendGauge(threadPoolType, "queue", stats.getQueue());
            this.sendGauge(threadPoolType, "active", stats.getActive());
            this.sendGauge(threadPoolType, "rejected", stats.getRejected());
            this.sendGauge(threadPoolType, "largest", stats.getLargest());
            this.sendGauge(threadPoolType, "completed", stats.getCompleted());
        }
    }

    private void sendNodeTransportStats(TransportStats transportStats) {
        String prefix = this.getPrefix("transport");
        this.sendGauge(prefix, "server_open", transportStats.serverOpen());
        this.sendGauge(prefix, "rx_count", transportStats.rxCount());
        this.sendGauge(prefix, "rx_size_in_bytes", transportStats.rxSize().getBytes());
        this.sendGauge(prefix, "tx_count", transportStats.txCount());
        this.sendGauge(prefix, "tx_size_in_bytes", transportStats.txSize().getBytes());
    }

    private void sendNodeProcessStats(ProcessStats processStats) {
        String prefix = this.getPrefix("process");

        this.sendGauge(prefix, "open_file_descriptors", processStats.getOpenFileDescriptors());

        if (processStats.getCpu() != null) {
            this.sendGauge(prefix + ".cpu", "percent", processStats.getCpu().getPercent());
            this.sendGauge(prefix + ".cpu", "total_in_millis", processStats.getCpu().getTotal().millis());
        }

        if (processStats.getMem() != null) {
            this.sendGauge(prefix + ".mem", "total_virtual_in_bytes", processStats.getMem().getTotalVirtual().getBytes());
        }
    }

    private void sendNodeOsStats(OsStats osStats) {
        String prefix = this.getPrefix("os");

        this.sendGauge(prefix + ".load_average", "1m", osStats.getCpu().getLoadAverage()[0]);
        this.sendGauge(prefix + ".load_average", "5m", osStats.getCpu().getLoadAverage()[1]);
        this.sendGauge(prefix + ".load_average", "15m", osStats.getCpu().getLoadAverage()[2]);

        this.sendGauge(prefix, "cpu_percent", osStats.getCpu().getPercent());

        if (osStats.getMem() != null) {
            this.sendGauge(prefix + ".mem", "free_in_bytes", osStats.getMem().getFree().getBytes());
            this.sendGauge(prefix + ".mem", "used_in_bytes", osStats.getMem().getUsed().getBytes());
            this.sendGauge(prefix + ".mem", "free_percent", osStats.getMem().getFreePercent());
            this.sendGauge(prefix + ".mem", "used_percent", osStats.getMem().getUsedPercent());
        }

        if (osStats.getSwap() != null) {
            this.sendGauge(prefix + ".swap", "free_in_bytes", osStats.getSwap().getFree().getBytes());
            this.sendGauge(prefix + ".swap", "used_in_bytes", osStats.getSwap().getUsed().getBytes());
        }

        if (osStats.getCgroup() != null) {
            this.sendGauge(prefix + ".cgroup", "cpuacct.usage", osStats.getCgroup().getCpuAcctUsageNanos());
            this.sendGauge(prefix + ".cgroup", "cpu.cfs_period_micros", osStats.getCgroup().getCpuCfsPeriodMicros());
            this.sendGauge(prefix + ".cgroup", "cpu.cfs_quota_micros", osStats.getCgroup().getCpuCfsQuotaMicros());

            if (osStats.getCgroup().getCpuStat() != null) {
                this.sendGauge(prefix + ".cgroup.cpu.stat", "number_of_elapsed_periods", osStats.getCgroup().getCpuStat().getNumberOfElapsedPeriods());
                this.sendGauge(prefix + ".cgroup.cpu.stat", "number_of_times_throttled", osStats.getCgroup().getCpuStat().getNumberOfTimesThrottled());
                this.sendGauge(prefix + ".cgroup.cpu.stat", "time_throttled_nanos", osStats.getCgroup().getCpuStat().getTimeThrottledNanos());
            }
        }
    }

    private void sendNodeJvmStats(JvmStats jvmStats) {
        String prefix = this.getPrefix("jvm");

        // mem
        this.sendGauge(prefix + ".mem", "heap_used_percent", jvmStats.getMem().getHeapUsedPercent());
        this.sendGauge(prefix + ".mem", "heap_used_in_bytes", jvmStats.getMem().getHeapUsed().getBytes());
        this.sendGauge(prefix + ".mem", "heap_committed_in_bytes", jvmStats.getMem().getHeapCommitted().getBytes());
        this.sendGauge(prefix + ".mem", "non_heap_used_in_bytes", jvmStats.getMem().getNonHeapUsed().getBytes());
        this.sendGauge(prefix + ".mem", "non_heap_committed_in_bytes", jvmStats.getMem().getNonHeapCommitted().getBytes());
        for (JvmStats.MemoryPool memoryPool : jvmStats.getMem()) {
            String memoryPoolType = prefix + ".mem.pools." + memoryPool.getName();

            this.sendGauge(memoryPoolType, "max_in_bytes", memoryPool.getMax().getBytes());
            this.sendGauge(memoryPoolType, "used_in_bytes", memoryPool.getUsed().getBytes());
            this.sendGauge(memoryPoolType, "peak_used_in_bytes", memoryPool.getPeakUsed().getBytes());
            this.sendGauge(memoryPoolType, "peak_max_in_bytes", memoryPool.getPeakMax().getBytes());
        }

        // threads
        this.sendGauge(prefix + ".threads", "count", jvmStats.getThreads().getCount());
        this.sendGauge(prefix + ".threads", "peak_count", jvmStats.getThreads().getPeakCount());

        // garbage collectors
        for (JvmStats.GarbageCollector collector : jvmStats.getGc()) {
            String gcCollectorType = prefix + ".gc.collectors." + collector.getName();

            this.sendGauge(gcCollectorType, "collection_count", collector.getCollectionCount());
            this.sendGauge(gcCollectorType, "collection_time_in_millis", collector.getCollectionTime().millis());
        }

        // TODO: buffer pools
    }

    private void sendNodeHttpStats(HttpStats httpStats) {
        if( httpStats != null ) {
            String prefix = this.getPrefix("http");
            this.sendGauge(prefix, "current_open", httpStats.getServerOpen());
            this.sendGauge(prefix, "total_opened", httpStats.getTotalOpen());
        }
    }

    private void sendNodeFsStats(FsInfo fs) {
        // Send total
        String prefix = this.getPrefix("fs");
        this.sendNodeFsStatsInfo(prefix + ".total", fs.getTotal());

        // Maybe send details
        if (this.statsdReportFsDetails) {
            for (FsInfo.Path info : fs) {
                this.sendNodeFsStatsInfo(prefix + ".data", info);
            }
        }
    }

    private void sendNodeFsStatsInfo(String prefix, FsInfo.Path info) {
        // Construct detailed path
        String prefixAppend = "";
        if (info.getPath() != null)
            prefixAppend += "." + info.getPath();
        if (info.getMount() != null)
            prefixAppend += "." + info.getMount();

        if (info.getAvailable().getBytes() != -1)
            this.sendGauge(prefix + prefixAppend, "available_in_bytes", info.getAvailable().getBytes());
        if (info.getTotal().getBytes() != -1)
            this.sendGauge(prefix + prefixAppend, "total_in_bytes", info.getTotal().getBytes());
        if (info.getFree().getBytes() != -1)
            this.sendGauge(prefix + prefixAppend, "free_in_bytes", info.getFree().getBytes());
    }

    private String getPrefix(String prefix) {
        return this.buildMetricName("node." + this.nodeName + "." + prefix);
    }
}
