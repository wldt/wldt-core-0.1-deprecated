package it.unimore.dipi.iot.wldt.metrics;

import com.codahale.metrics.*;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
import it.unimore.dipi.iot.wldt.worker.coap.Coap2CoapWorker;
import it.unimore.dipi.iot.wldt.worker.mqtt.Mqtt2MqttWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 30/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Singleton used to monitor application metrics. Current implementation using Dropwizard Metrics Library
 */
public class WldtMetricsManager {

    public static String METRICS_FOLDER = "metrics";

    private static final Logger logger = LoggerFactory.getLogger(WldtMetricsManager.class);

    public static final String DTDP_DISCOVERY_TIME = "dtdp_discovery";

    public static final String MQTT_INCOMING_CLIENT_SETUP_TIME = "incoming_mqtt_client_setup_time";

    public static final String MQTT_OUTGOING_CLIENT_SETUP_TIME = "outgoing_mqtt_client_setup_time";

    public static final String MQTT_EVENT_FORWARD_TIME = "event_forward_time";

    public static final String MQTT_FORWARD_TIME = "mqtt_forward_time";

    public static final String MQTT_COMMAND_REQUEST_FORWARD_TIME = "command_request_forward_time";

    public static final String MQTT_COMMAND_RESPONSE_FORWARD_TIME = "command_response_forward_time";

    public static final String MQTT_OUTGOING_PUBLISH_DATA_TIME = "mqtt_outgoing_publish_time";

    public static final String MQTT_INCOMING_PAYLOAD_SIZE = "incoming_mqtt_payload_size";

    public static final String MQTT_INCOMING_TELEMETRY_PAYLOAD_SIZE = "incoming_telemetry_mqtt_payload_size";

    public static final String MQTT_INCOMING_EVENT_PAYLOAD_SIZE = "incoming_event_mqtt_payload_size";

    public static final String MQTT_INCOMING_COMMAND_PAYLOAD_SIZE = "incoming_command_mqtt_payload_size";

    public static final String MQTT_TOPIC_REGISTRATION_TIME = "mqtt_topics_registration_time";

    public static final String COAP_SERVER_SETUP_TIME = "coap_server_setup_time";

    public static final String COAP_GET_FORWARD_TIME = "coap_get_forward_time";

    public static final String COAP_GET_OBSERVE_FORWARD_TIME = "coap_get_observe_forward_time";

    public static final String COAP_POST_FORWARD_TIME = "coap_post_forward_time";

    public static final String COAP_PUT_FORWARD_TIME = "coap_put_forward_time";

    public static final String COAP_DELETE_FORWARD_TIME = "coap_delete_forward_time";

    public static final String COAP_GET_OBSERVE_RESPONSE_TIME = "coap_get_observe_response_time";

    public static final String COAP_GET_RESPONSE_TIME = "coap_get_response_time";

    public static final String COAP_POST_RESPONSE_TIME = "coap_post_response_time";

    public static final String COAP_PUT_RESPONSE_TIME = "coap_put_response_time";

    public static final String COAP_DELETE_RESPONSE_TIME = "coap_delete_response_time";

    public static final String COAP_OVERALL_FORWARD_TIME = "coap_overall_forward_time";

    public static final String COAP_OVERALL_RESPONSE_TIME = "coap_overall_response_time";

    public static final String COAP_INCOMING_PAYLOAD_SIZE = "coap_incoming_payload_size";

    public static final String COAP_OUTGOING_PAYLOAD_SIZE = "coap_outgoing_payload_size";

    public static final String COAP_RESOURCE_DISCOVERY_TIME = "coap_resource_discovery_time";

    public static final String COAP_WLDT_RESOURCE_LIST_CREATION_TIME = "coap_wldt_resource_list_creation_time";

    public static final String COAP_WLDT_RESOURCE_CREATION_TIME = "coap_wldt_resource_creation_time";

    public static final String METRICS_GRAPHITE_PREFIX = "wldt";

    private static WldtMetricsManager instance = null;

    private MetricRegistry metricsRegistry = null;

    private CsvReporter reporter = null;

    private boolean isMonitoringActive = false;

    private Graphite graphite = null;

    private GraphiteReporter graphiteReporter = null;

    private WldtMetricsManager(){
        metricsRegistry = new MetricRegistry();
    }

    public static WldtMetricsManager getInstance(){
        if(instance == null)
            instance = new WldtMetricsManager();

        return instance;
    }

    public void enableCsvReporter(){

        try{

            checkOrCreateBasicMetricsFolder();

            reporter = CsvReporter.forRegistry(metricsRegistry)
                    .formatFor(Locale.US)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build(checkOrCreateMetricsFolder());

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void enableGraphiteReporter(String host, int port, String prefix){

        try{
            graphite = new Graphite(new InetSocketAddress(host, port));
            graphiteReporter = GraphiteReporter.forRegistry(metricsRegistry)
                    .prefixedWith(prefix)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .filter(MetricFilter.ALL)
                    .build(graphite);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void startMonitoring(int period){

        try{

            if(reporter != null) {
                reporter.start(period, TimeUnit.SECONDS);
                logger.info("[STARTED] METRICS CSV REPORTER");
            }

            if(graphiteReporter != null){
                graphiteReporter.start(period, TimeUnit.SECONDS);
                logger.info("[STARTED] METRICS GRAPHITE REPORTER");
            }

            this.isMonitoringActive = true;

            logger.info("[STARTED] APPLICATION METRICS");

        }catch (Exception e){
            e.printStackTrace();
            logger.error("[ERROR] Error Starting Application Metrics Module ! Exception: {}", e.getLocalizedMessage());
        }
    }

    /**
     * Check and create if the target metrics folder (identified by the start timestamp) exists or needs to be created
     * @return
     */
    private File checkOrCreateMetricsFolder(){
        File metricsFolder = new File(String.format("%s/%s", METRICS_FOLDER, String.valueOf(System.currentTimeMillis())));

        if(!metricsFolder.exists())
            metricsFolder.mkdir();

        return metricsFolder;
    }

    /**
     * Check if the basic metrics folder exists or needs to be created
     * @return
     */
    private void checkOrCreateBasicMetricsFolder(){

        File metricsFolder = new File(METRICS_FOLDER);

        if(!metricsFolder.exists()) {
            if(metricsFolder.mkdir())
               logger.info("LOGGER FOLDER -> Correctly created !");
            else
                logger.error("LOGGER FOLDER -> Error creating folder: {}", METRICS_FOLDER);
        }
        else
            logger.info("LOGGER FOLDER -> Already exists !");
    }

    public Timer.Context getTimer(String metricIdentifier , String timerKey){
        if(isMonitoringActive)
            return metricsRegistry.timer(name(metricIdentifier, timerKey)).time();
        else
            return null;
    }

    public void measureValue(String metricIdentifier, String key, int value){
        if(isMonitoringActive) {
            Histogram histogram = metricsRegistry.histogram(name(metricIdentifier, key));
            histogram.update(value);
        }
    }

    public Timer.Context getTimer(WldtWorker wldtWorker, String timerKey){
        if(isMonitoringActive)
            return metricsRegistry.timer(name(wldtWorker.getClass(), timerKey)).time();
        else
            return null;
    }

    public void measureValue(WldtWorker wldtWorker, String key, int value){
        if(isMonitoringActive) {
            Histogram histogram = metricsRegistry.histogram(name(wldtWorker.getClass(), key));
            histogram.update(value);
        }
    }

    public void measureValue(WldtWorker wldtWorker, String key, long value){
        if(isMonitoringActive) {
            Histogram histogram = metricsRegistry.histogram(name(wldtWorker.getClass(), key));
            histogram.update(value);
        }
    }

    public Timer.Context getMqttModuleTimerContext(String timerKey){
        if(isMonitoringActive)
            return metricsRegistry.timer(name(Mqtt2MqttWorker.class, timerKey)).time();
        else
            return null;
    }

    public Timer.Context getCoapModuleTimerContext(String timerKey){
        if(isMonitoringActive)
            return metricsRegistry.timer(name(Coap2CoapWorker.class, timerKey)).time();
        else
            return null;
    }

    public void measureMqttIncomingPayloadSizeMetric(int packetSize){
        if(isMonitoringActive) {
            Histogram histogram = metricsRegistry.histogram(name(Mqtt2MqttWorker.class, MQTT_INCOMING_PAYLOAD_SIZE));
            histogram.update(packetSize);
        }
    }

    public void measureMqttIncomingTelemetryPayload(int packetSize){
        if(isMonitoringActive) {
            Histogram histogram = metricsRegistry.histogram(name(Mqtt2MqttWorker.class, MQTT_INCOMING_TELEMETRY_PAYLOAD_SIZE));
            histogram.update(packetSize);
        }
    }

    public void measureMqttIncomingEventPayloadSizeMetric(int packetSize){
        if(isMonitoringActive) {
            Histogram histogram = metricsRegistry.histogram(name(Mqtt2MqttWorker.class, MQTT_INCOMING_EVENT_PAYLOAD_SIZE));
            histogram.update(packetSize);
        }
    }

    public void measureMqttIncomingCommandPayloadSizeMetric(int packetSize){
        if(isMonitoringActive) {
            Histogram histogram = metricsRegistry.histogram(name(Mqtt2MqttWorker.class, MQTT_INCOMING_COMMAND_PAYLOAD_SIZE));
            histogram.update(packetSize);
        }
    }

    public void measureCoapIncomingPayloadSizeMetric(int packetSize){
        if(isMonitoringActive) {
            Histogram histogram = metricsRegistry.histogram(name(Coap2CoapWorker.class, COAP_INCOMING_PAYLOAD_SIZE));
            histogram.update(packetSize);
        }
    }

    public void measureCoapOutgoingPayloadSizeMetric(int packetSize){
        if(isMonitoringActive) {
            Histogram histogram = metricsRegistry.histogram(name(Coap2CoapWorker.class, COAP_OUTGOING_PAYLOAD_SIZE));
            histogram.update(packetSize);
        }
    }

    public MetricRegistry getMetricsRegistry() {
        return metricsRegistry;
    }

    public void setMetricsRegistry(MetricRegistry metricsRegistry) {
        this.metricsRegistry = metricsRegistry;
    }
}
