package it.unimore.dipi.iot.wldt.worker;

import java.util.Map;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 08/03/2021 - 11:39
 */
public interface MirroringListener {

    public void onPhysicalDeviceMirrored(String deviceId, Map<String, Object> metadata);

    public void onPhysicalDeviceMirroringError(String deviceId, String errorMsg);

    public void onPhysicalResourceMirrored(String resourceId, Map<String, Object> metadata);

    public void onPhysicalResourceMirroringError(String deviceId, String errorMsg);

}
