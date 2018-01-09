package com.automattic.elasticsearch.statsd.test;

import com.automattic.elasticsearch.plugin.StatsdPlugin;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import com.google.common.collect.Iterables;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.InternalTestCluster;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Predicates.containsPattern;
import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_REPLICAS;
import static org.elasticsearch.cluster.metadata.IndexMetaData.SETTING_NUMBER_OF_SHARDS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
@ESIntegTestCase.ClusterScope(maxNumDataNodes = 3, minNumDataNodes = 3, numClientNodes = 0, numDataNodes = 3, transportClientRatio = -1)
public class StatsdPluginIntegrationTest extends ESIntegTestCase {

    public static final int STATSD_SERVER_PORT = 12345;

    private String index;
    private String type = RandomStringGenerator.randomAlphabetic(6).toLowerCase();

    private static StatsdMockServer statsdMockServer;

    @BeforeClass
    public static void startMockStatsdServer() {
        statsdMockServer = new StatsdMockServer(STATSD_SERVER_PORT);
        statsdMockServer.start();
    }

    @AfterClass
    public static void stopMockStatsdServer() throws Exception {
        System.out.println("Waiting for cleanup");
        Thread.sleep(10000);
        statsdMockServer.close();
    }

    @Override
    public Settings indexSettings() {
        return Settings.builder().put(super.indexSettings()).put(SETTING_NUMBER_OF_SHARDS, 4).put(SETTING_NUMBER_OF_REPLICAS, 1).build();
    }

    @Before
    public void prepareForTest(){
        index = RandomStringGenerator.randomAlphabetic(6).toLowerCase();
        logger.info("Creating index " + index);
        super.createIndex(index);
        statsdMockServer.resetContents();
    }

    // Add StatsdPlugin to test cluster
    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        Collection<Class<? extends Plugin>> plugins = new ArrayList<>();
        plugins.add(StatsdPlugin.class);
        return plugins;
    }

    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return Settings.builder().put(super.nodeSettings(nodeOrdinal))
        .put("metrics.statsd.host", "localhost")
        .put("metrics.statsd.port", STATSD_SERVER_PORT)
        .put("metrics.statsd.prefix", "myhost"+nodeOrdinal)
        .put("metrics.statsd.every", "1s")
        .put("metrics.statsd.test_mode", true).build();
    }


    @Test
    public void testThatIndexingResultsInMonitoring() throws Exception {
        IndexResponse indexResponse = indexElement(index, type, "value");
        assertThat(indexResponse.getId(), is(notNullValue()));

        //Index some more docs
        this.indexSomeDocs(101);
        this.flushAndRefresh(index);
        Thread.sleep(2000);

        ensureValidKeyNames();
        assertStatsdMetricIsContained("index." + index + ".total.indexing.index_total:102|g");
        assertStatsdMetricIsContained(".jvm.threads.peak_count:");
    }

    @Test
    public void masterFailOverShouldWork() throws Exception {
        IndexResponse indexResponse = indexElement(index, type, "value");
        assertThat(indexResponse.getId(), is(notNullValue()));
        super.flushAndRefresh(index);

        InternalTestCluster testCluster = (InternalTestCluster) ESIntegTestCase.cluster();
        testCluster.stopCurrentMasterNode();
        testCluster.startNode();
        Thread.sleep(4000);
        statsdMockServer.resetContents();
        System.out.println("stopped master");

        indexResponse = indexElement(index, type, "value");
        assertThat(indexResponse.getId(), is(notNullValue()));

        // wait for master fail over and writing to graph reporter
        Thread.sleep(4000);
        assertStatsdMetricIsContained("index."+index+".total.indexing.index_total:2|g");
    }

    // the stupid hamcrest matchers have compile erros depending whether they run on java6 or java7, so I rolled my own version
    // yes, I know this sucks... I want power asserts, as usual
    private void assertStatsdMetricIsContained(final String id) {
        // defensive copy as contents are modified by the mock server thread
        Collection<String> contents = new ArrayList<>(statsdMockServer.content);
        assertThat(Iterables.any(contents, containsPattern(id)), is(true));
    }

    // Make sure no elements with a chars [] are included
    private void ensureValidKeyNames() {
        // defensive copy as contents are modified by the mock server thread
        Collection<String> contents = new ArrayList<>(statsdMockServer.content);
        assertThat(Iterables.any(contents, containsPattern("\\.\\.")), is(false));
        assertThat(Iterables.any(contents, containsPattern("\\[")), is(false));
        assertThat(Iterables.any(contents, containsPattern("\\]")), is(false));
        assertThat(Iterables.any(contents, containsPattern("\\(")), is(false));
        assertThat(Iterables.any(contents, containsPattern("\\)")), is(false));
    }

    private IndexResponse indexElement(String index, String type, String fieldValue) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("field", fieldValue);
        return super.index(index, type, RandomStringGenerator.randomAlphabetic(16), doc);
    }

    private void indexSomeDocs(int docs) {
        while (docs > 0) {
            indexElement(index, type, "value " + docs);
            docs--;
        }
    }
}
