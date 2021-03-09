package it.unimore.dipi.iot.wldt.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.unimore.dipi.iot.wldt.exception.WldtConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class WldtUtils {

    private static final Logger logger = LoggerFactory.getLogger(WldtUtils.class);

    public static String generateRandomWldtId(String namespace, String wlMainIdentifier){
        return String.format("%s:%s:%s", namespace, wlMainIdentifier, UUID.randomUUID());
    }

    public static Object readConfigurationFile(String confFolder, String confFilename, Class<? extends Object> targetClass) throws WldtConfigurationException {

        try{

            //ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            //File file = new File(classLoader.getResource(WLDT_CONFIGURATION_FILE).getFile());
            File file = new File(String.format("%s/%s", confFolder, confFilename));

            ObjectMapper om = new ObjectMapper(new YAMLFactory());

            return om.readValue(file, targetClass);

        }catch (Exception e){
            String errorMessage = String.format("ERROR LOADING CONFIGURATION FILE (%s) ! Error: %s", confFilename, e.getLocalizedMessage());
            logger.error("{}", errorMessage);
            throw new WldtConfigurationException(errorMessage);
        }

    }

}
