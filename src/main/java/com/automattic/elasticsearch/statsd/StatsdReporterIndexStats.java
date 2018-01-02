package com.automattic.elasticsearch.statsd;

import org.elasticsearch.index.cache.query.QueryCacheStats;
import org.elasticsearch.index.cache.request.RequestCacheStats;
import org.elasticsearch.index.engine.SegmentsStats;
import org.elasticsearch.index.fielddata.FieldDataStats;
import org.elasticsearch.index.flush.FlushStats;
import org.elasticsearch.index.get.GetStats;
import org.elasticsearch.index.merge.MergeStats;
import org.elasticsearch.index.refresh.RefreshStats;
import org.elasticsearch.index.search.stats.SearchStats;
import org.elasticsearch.index.shard.DocsStats;
import org.elasticsearch.index.shard.IndexingStats;
import org.elasticsearch.index.store.StoreStats;
import org.elasticsearch.index.warmer.WarmerStats;
import org.elasticsearch.search.suggest.completion.CompletionStats;

public abstract class StatsdReporterIndexStats extends StatsdReporter {

    protected void sendDocsStats(String name, DocsStats docsStats) {
        if (null == docsStats) return;
        this.sendGauge(name, "count", docsStats.getCount());
        this.sendGauge(name, "deleted", docsStats.getDeleted());
    }

    protected void sendStoreStats(String name, StoreStats storeStats) {
        if (null == storeStats) return;
        this.sendGauge(name, "size_in_bytes", storeStats.sizeInBytes());
    }

    protected void sendIndexingStats(String name, IndexingStats indexingStats) {
        if (null == indexingStats) return;
        IndexingStats.Stats totalStats = indexingStats.getTotal();
        this.sendIndexingStatsStats(name, totalStats);

        // TODO: Maybe print out stats to shards level?
    }

    protected void sendGetStats(String name, GetStats getStats) {
        if (null == getStats) return;
        this.sendGauge(name, "total", getStats.getCount());
        this.sendGauge(name, "time_in_millis", getStats.getTimeInMillis());
        this.sendGauge(name, "exists_total", getStats.getExistsCount());
        this.sendGauge(name, "exists_time_in_millis", getStats.getExistsTimeInMillis());
        this.sendGauge(name, "missing_total", getStats.getMissingCount());
        this.sendGauge(name, "missing_time_in_millis", getStats.getMissingTimeInMillis());
        this.sendGauge(name, "current", getStats.current());
    }

    protected void sendSearchStats(String name, SearchStats searchStats) {
        if (null == searchStats) return;
        this.sendGauge(name, "open_contexts", searchStats.getOpenContexts());
        SearchStats.Stats totalSearchStats = searchStats.getTotal();
        this.sendSearchStatsStats(name, totalSearchStats);

        // TODO: Maybe print out stats to shards level?
    }

    protected void sendMergeStats(String name, MergeStats mergeStats) {
        if (null == mergeStats) return;
        this.sendGauge(name, "current", mergeStats.getCurrent());
        this.sendGauge(name, "current_docs", mergeStats.getCurrentNumDocs());
        this.sendGauge(name, "current_size_in_bytes", mergeStats.getCurrentSizeInBytes());
        this.sendGauge(name, "total", mergeStats.getTotal());
        this.sendGauge(name, "total_time_in_millis", mergeStats.getTotalTimeInMillis());
        this.sendGauge(name, "total_docs", mergeStats.getTotalNumDocs());
        this.sendGauge(name, "total_size_in_bytes", mergeStats.getTotalSizeInBytes());
    }

    protected void sendRefreshStats(String name, RefreshStats refreshStats) {
        if (null == refreshStats) return;
        this.sendGauge(name, "total", refreshStats.getTotal());
        this.sendGauge(name, "total_time_in_millis", refreshStats.getTotalTimeInMillis());
    }

    protected void sendFlushStats(String name, FlushStats flushStats) {
        if (null == flushStats) return;
        this.sendGauge(name, "total", flushStats.getTotal());
        this.sendGauge(name, "total_time_in_millis", flushStats.getTotalTimeInMillis());
    }

    protected void sendWarmerStats(String name, WarmerStats warmerStats) {
        if (null == warmerStats) return;
        this.sendGauge(name, "current", warmerStats.current());
        this.sendGauge(name, "total", warmerStats.total());
        this.sendGauge(name, "total_time_in_millis", warmerStats.totalTimeInMillis());
    }

    protected void sendFielddataCacheStats(String name, FieldDataStats fielddataStats) {
        if (null == fielddataStats) return;
        this.sendGauge(name, "memory_size_in_bytes", fielddataStats.getMemorySizeInBytes());
        this.sendGauge(name, "evictions", fielddataStats.getEvictions());
    }

    protected void sendCompletionStats(String name, CompletionStats completionStats) {
        if (null == completionStats) return;
        this.sendGauge(name, "size_in_bytes", completionStats.getSizeInBytes());
    }

    protected void sendSegmentsStats(String name, SegmentsStats segmentsStats) {
        if (null == segmentsStats) return;
        this.sendGauge(name, "count", segmentsStats.getCount());
        this.sendGauge(name, "memory_in_bytes", segmentsStats.getMemoryInBytes());
    }

    protected void sendIndexingStatsStats(String name, IndexingStats.Stats indexingStatsStats) {
        if (null == indexingStatsStats) return;
        this.sendGauge(name, "index_total", indexingStatsStats.getIndexCount());
        this.sendGauge(name, "index_time_in_millis", indexingStatsStats.getIndexTime().millis());
        this.sendGauge(name, "index_current", indexingStatsStats.getIndexCount());
        this.sendGauge(name, "delete_total", indexingStatsStats.getDeleteCount());
        this.sendGauge(name, "delete_time_in_millis", indexingStatsStats.getDeleteTime().millis());
        this.sendGauge(name, "delete_current", indexingStatsStats.getDeleteCurrent());
        this.sendGauge(name, "throttle_time_in_millis", indexingStatsStats.getThrottleTime().millis());
    }

    protected void sendSearchStatsStats(String name, SearchStats.Stats searchStatsStats) {
        if (null == searchStatsStats) return;
        this.sendGauge(name, "query_total", searchStatsStats.getQueryCount());
        this.sendGauge(name, "query_time_in_millis", searchStatsStats.getQueryTimeInMillis());
        this.sendGauge(name, "query_current", searchStatsStats.getQueryCurrent());
        this.sendGauge(name, "fetch_total", searchStatsStats.getFetchCount());
        this.sendGauge(name, "fetch_time_in_millis", searchStatsStats.getFetchTimeInMillis());
        this.sendGauge(name, "fetch_current", searchStatsStats.getFetchCurrent());
    }

    protected void sendQueryCacheStats(String name, QueryCacheStats queryCacheStats) {
        if (null == queryCacheStats) {
            return;
        }
        this.sendGauge(name, "cache_count", queryCacheStats.getCacheCount());
        this.sendGauge(name, "hit_count", queryCacheStats.getHitCount());
        this.sendGauge(name, "miss_count", queryCacheStats.getMissCount());
        this.sendGauge(name, "cache_size", queryCacheStats.getCacheSize());
        this.sendGauge(name, "memory_size_in_bytes", queryCacheStats.getMemorySizeInBytes());
        this.sendGauge(name, "evictions", queryCacheStats.getEvictions());
    }

    protected void sendRequestCacheStats(String name, RequestCacheStats requestCacheStats) {
        if (null == requestCacheStats) {
            return;
        }
        this.sendGauge(name, "hit_count", requestCacheStats.getHitCount());
        this.sendGauge(name, "miss_count", requestCacheStats.getMissCount());
        this.sendGauge(name, "evictions", requestCacheStats.getEvictions());
        this.sendGauge(name, "memory_size_in_bytes", requestCacheStats.getMemorySizeInBytes());
    }
}
