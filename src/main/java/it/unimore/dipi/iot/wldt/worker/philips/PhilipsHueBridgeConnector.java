package it.unimore.dipi.iot.wldt.worker.philips;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 12/06/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class PhilipsHueBridgeConnector {

    private static final Logger logger = LoggerFactory.getLogger(PhilipsHueBridgeConnector.class);

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static PhilipsHueBridgeConnector instance = null;

    private ObjectMapper objectMapper = null;

    private OkHttpClient client = null;

    private PhilipsHueBridgeConnector() {

        objectMapper = new ObjectMapper();

        client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .build();
    }

    public static PhilipsHueBridgeConnector getInstance(){

        if(instance == null)
            instance = new PhilipsHueBridgeConnector();

        return instance;
    }

    public boolean changeLightState(String bridgeIp, String username, String lightId, boolean isOn){

        try{

            RequestBody body = RequestBody.create(JSON, objectMapper.writeValueAsBytes(new PhilipsLightStateDescriptor(isOn)));

            Request request = new Request.Builder()
                    .url(getBridgeLightStateUrl(bridgeIp, username, lightId))
                    .header("Content-Type", "application/json")
                    .put(body)
                    .build();

            Response response = client.newCall(request).execute();

            System.out.println(response.body().string());

            return true;

        }catch (Exception e){
            logger.error("Error changing light state ! Error: {}", e.getLocalizedMessage());
            return false;
        }

    }

    public List<PhilipsHueLightDescriptor> getBridgeLightList(String bridgeIp, String username){

        try{

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(2, TimeUnit.MINUTES)
                    .readTimeout(2, TimeUnit.MINUTES)
                    .build();

            Request request = new Request.Builder()
                    .url(getBridgeLightListUrl(bridgeIp, username))
                    .get()
                    .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {

                ArrayList<PhilipsHueLightDescriptor> lightList = new ArrayList<>();

                String responseString = new String(response.body().bytes());

                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode jsonNode = objectMapper.readTree(responseString);

                for (Iterator<String> it = jsonNode.fieldNames(); it.hasNext(); ) {
                    String lightId = it.next();
                    lightList.add(parseLightData(lightId, jsonNode.get(lightId)));
                }

                return lightList;
            }
            else
                return null;

        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }

    public PhilipsHueLightDescriptor getBridgeLight(String bridgeIp, String username, String lightId){

        try{

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(2, TimeUnit.MINUTES)
                    .readTimeout(2, TimeUnit.MINUTES)
                    .build();

            Request request = new Request.Builder()
                    .url(getBridgeLightUrl(bridgeIp, username, lightId))
                    .get()
                    .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {

                String responseString = new String(response.body().bytes());

                ObjectMapper objectMapper = new ObjectMapper();

                JsonNode jsonNode = objectMapper.readTree(responseString);

                return parseLightData(lightId, jsonNode);

            }
            else
                return null;

        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }

    private PhilipsHueLightDescriptor parseLightData(String lightId, JsonNode jsonNode){

        try{

            PhilipsHueLightDescriptor philipsHueLightDescriptor = new PhilipsHueLightDescriptor();
            philipsHueLightDescriptor.setId(lightId);
            philipsHueLightDescriptor.setOn(jsonNode.get("state").get("on").asBoolean());
            philipsHueLightDescriptor.setBrightness(jsonNode.get("state").get("bri").asInt());
            philipsHueLightDescriptor.setHue(jsonNode.get("state").get("hue").asInt());
            philipsHueLightDescriptor.setSaturation(jsonNode.get("state").get("sat").asInt());
            philipsHueLightDescriptor.setAlert(jsonNode.get("state").get("alert").asText());
            philipsHueLightDescriptor.setEffect(jsonNode.get("state").get("effect").asText());
            philipsHueLightDescriptor.setReachable(jsonNode.get("state").get("reachable").asBoolean());
            philipsHueLightDescriptor.setType(jsonNode.get("type").asText());
            philipsHueLightDescriptor.setName(jsonNode.get("name").asText());
            philipsHueLightDescriptor.setModelId(jsonNode.get("modelid").asText());
            philipsHueLightDescriptor.setManufacturerName(jsonNode.get("manufacturername").asText());
            philipsHueLightDescriptor.setProductName(jsonNode.get("productname").asText());
            philipsHueLightDescriptor.setUniqueId(jsonNode.get("uniqueid").asText());
            philipsHueLightDescriptor.setSoftwareVersion(jsonNode.get("swversion").asText());
            philipsHueLightDescriptor.setSoftwareConfigId(jsonNode.get("swconfigid").asText());
            philipsHueLightDescriptor.setProductId(jsonNode.get("productid").asText());

            return philipsHueLightDescriptor;

        }catch (Exception e){
            logger.error("Error Parsing Light Data !");
            return null;
        }

    }

    private String getBridgeLightListUrl(String bridgeIp, String username) {
        return String.format("http://%s/api/%s/lights/", bridgeIp, username);
    }

    private String getBridgeLightUrl(String bridgeIp, String username, String lightId) {
        return String.format("http://%s/api/%s/lights/%s/", bridgeIp, username, lightId);
    }

    private String getBridgeLightStateUrl(String bridgeIp, String username, String lightId) {
        return String.format("http://%s/api/%s/lights/%s/state", bridgeIp, username, lightId);
    }

    public static void main(String[] args) {

        PhilipsHueLightDescriptor lightDescriptor = PhilipsHueBridgeConnector.getInstance().getBridgeLight("192.168.1.230", "hf0Y561H6gp54UhhahAu0xN6MnYGPRp5ujq6FdGM", "5");

        if(lightDescriptor != null)
            PhilipsHueBridgeConnector.getInstance().changeLightState("192.168.1.230", "hf0Y561H6gp54UhhahAu0xN6MnYGPRp5ujq6FdGM", "5", !lightDescriptor.isOn());
        else
            System.err.println("Light not found !");
    }

}
