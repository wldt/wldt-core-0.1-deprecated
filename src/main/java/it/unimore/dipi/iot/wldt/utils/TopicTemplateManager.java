package it.unimore.dipi.iot.wldt.utils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class TopicTemplateManager {

    private static final String DEVICE_TELEMETRY_DEVICE_TOPIC = "telemetry/{{device_id}}";
    private static final String DEVICE_TELEMETRY_RESOURCE_TOPIC = "telemetry/{{device_id}}/resource/{{resource_id}}";
    private static final String DEVICE_EVENT_TOPIC = "events/{{device_id}}";
    private static final String DEVICE_COMMAND_REQUEST_TOPIC = "commands/{{device_id}}/request";
    private static final String DEVICE_COMMAND_RESPONSE_TOPIC = "commands/{{device_id}}/response";

    private static final String DEVICE_ID_ATTRIBUTE = "device_id";
    private static final String RESOURCE_ID_ATTRIBUTE = "resource_id";

    private static final Logger logger = LoggerFactory.getLogger(TopicTemplateManager.class);

    public static void main(String[] args)  {

        try{

            logger.info("Topic Template Manger Tester started  ... ");

            String testDevice = "uuid:dummydevice:test";
            String testResource = "uuid:resource1";

            logger.info("Device -> DEVICE_TELEMETRY_DEVICE_TOPIC Topic: {}", getTopicForDevice(DEVICE_TELEMETRY_DEVICE_TOPIC, testDevice));
            logger.info("Device Resource -> DEVICE_TELEMETRY_RESOURCE_TOPIC Topic: {}", getTopicForDeviceResource(DEVICE_TELEMETRY_RESOURCE_TOPIC, testDevice, testResource));
            logger.info("Device -> DEVICE_EVENT_TOPIC Topic: {}", getTopicForDevice(DEVICE_EVENT_TOPIC, testDevice));
            logger.info("Device -> DEVICE_COMMAND_REQUEST_TOPIC Topic: {}", getTopicForDevice(DEVICE_COMMAND_REQUEST_TOPIC, testDevice));
            logger.info("Device -> DEVICE_COMMAND_RESPONSE_TOPIC Topic: {}", getTopicForDevice(DEVICE_COMMAND_RESPONSE_TOPIC, testDevice));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getTopicForDevice(String topicTemplate, String deviceId) throws IOException {

        Map<String, Object> context = new HashMap<>();
        context.put(DEVICE_ID_ATTRIBUTE, deviceId);

        return applyTemplateToTopic(topicTemplate, context);
    }

    public static String getTopicForDeviceResource(String topicTemplate, String deviceId, String resourceId) throws IOException {

        Map<String, Object> context = new HashMap<>();
        context.put(DEVICE_ID_ATTRIBUTE, deviceId);
        context.put(RESOURCE_ID_ATTRIBUTE, resourceId);

        return applyTemplateToTopic(topicTemplate, context);
    }

    private static String applyTemplateToTopic(String topicTemplate, Map<String, Object> context) throws IOException {

        Writer writer = new StringWriter();

        MustacheFactory mustacheFactory = new DefaultMustacheFactory();
        Mustache mustache = mustacheFactory.compile(new StringReader(topicTemplate),"topic");

        mustache.execute(writer, context);
        writer.flush();
        return writer.toString();
    }

}
