package it.unimore.dipi.iot.wldt.worker.mqtt;

import it.unimore.dipi.iot.wldt.engine.WldtEngine;
import it.unimore.dipi.iot.wldt.engine.WldtConfiguration;
import it.unimore.dipi.iot.wldt.exception.*;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
import it.unimore.dipi.iot.wldt.utils.WldtUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class Mqtt2MqttWorker extends WldtWorker<Mqtt2MqttConfiguration, Void, Void> {

    private static final Logger logger = LoggerFactory.getLogger(Mqtt2MqttWorker.class);

    private static final String TAG = "[WLDT-MQTT-WORKER]";

    private static final String CONF_FILE_NAME = "mqtt.yaml";

    private String wldtId;

    private Mqtt2MqttManager mqtt2MqttManager;

    private Mqtt2MqttWorker(){
        super();
    }

    public Mqtt2MqttWorker(String wldtId, Mqtt2MqttConfiguration mqtt2MqttConfiguration) {
        super(mqtt2MqttConfiguration);
        this.wldtId = wldtId;
    }

    public Mqtt2MqttWorker(String wldtId, WldtConfiguration wldtConfiguration) throws WldtConfigurationException {
        this.wldtId = wldtId;
        this.setWldtWorkerConfiguration((Mqtt2MqttConfiguration) WldtUtils.readConfigurationFile(WldtEngine.WLDT_CONFIGURATION_FOLDER, CONF_FILE_NAME, Mqtt2MqttConfiguration.class));

        logger.info("MQTT Module Configuration Loaded {} {}", TAG, this.getWldtWorkerConfiguration());
    }

    @Override
    public void startWorkerJob() throws WldtConfigurationException, WldtRuntimeException {

        if(this.getWldtWorkerConfiguration() == null
                || this.getWldtWorkerConfiguration().getDestinationBrokerAddress() == null
                || this.getWldtWorkerConfiguration().getDestinationBrokerPort() <= 0
                || this.getWldtWorkerConfiguration().getBrokerAddress() == null
                || this.getWldtWorkerConfiguration().getBrokerPort() <= 0
                || (this.getWldtWorkerConfiguration().getDeviceTelemetryTopic() == null
                    && this.getWldtWorkerConfiguration().getResourceTelemetryTopic() == null
                    && this.getWldtWorkerConfiguration().getEventTopic() == null
                    && this.getWldtWorkerConfiguration().getCommandRequestTopic() == null)
        )
            throw new WldtConfigurationException("WldtCoapWorker -> Worker Configuration = null or required parameters are missing!");

        try{
            startMqttProtocolManagement();
        }catch (Exception | WldtMqttModuleException e){
            e.printStackTrace();
            String errorMsg = String.format("MQTT PROTOCOL MANAGER Runtime Error: %s", e.getLocalizedMessage());
            logger.debug("{} {}", TAG, errorMsg);
            throw new WldtRuntimeException(errorMsg);
        }
    }

    public void startMqttProtocolManagement() throws IOException, WldtMqttModuleException, MqttException {

        logger.info("{} STARTING MQTT PROTOCOL MANAGEMENT ...", TAG);

        if(mqtt2MqttManager == null){

            this.mqtt2MqttManager = new Mqtt2MqttManager(this.wldtId, this.getWldtWorkerConfiguration());

            logger.debug("{} Starting MQTT Manager for Broker at: {}:{} ", TAG, mqtt2MqttManager.getMqtt2MqttConfiguration().getBrokerAddress(), mqtt2MqttManager.getMqtt2MqttConfiguration().getBrokerPort());

            this.mqtt2MqttManager.init();
            this.mqtt2MqttManager.initTelemetryMessageManagement();
            this.mqtt2MqttManager.initEventMessageManagement();

            logger.info("{} MQTT PROTOCOL MANAGEMENT [STARTED]", TAG);
        }
        else
            logger.error("MQTT Manager already associated to an existing MQTT Device ({}:{}) ! No Action !", this.mqtt2MqttManager.getMqtt2MqttConfiguration().getBrokerAddress(), this.mqtt2MqttManager.getMqtt2MqttConfiguration().getBrokerPort());

    }

    public Mqtt2MqttManager getMqtt2MqttManager() {
        return mqtt2MqttManager;
    }

    public void setMqtt2MqttManager(Mqtt2MqttManager mqtt2MqttManager) {
        this.mqtt2MqttManager = mqtt2MqttManager;
    }

    public String getWldtId() {
        return wldtId;
    }

    public void setWldtId(String wldtId) {
        this.wldtId = wldtId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WldtMqttWorker{");
        sb.append("wldtId='").append(wldtId).append('\'');
        sb.append(", wldtMqttManager=").append(mqtt2MqttManager);
        sb.append('}');
        return sb.toString();
    }
}
