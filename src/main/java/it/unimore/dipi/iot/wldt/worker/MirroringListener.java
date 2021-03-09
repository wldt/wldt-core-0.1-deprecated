package it.unimore.dipi.iot.wldt.worker;

import java.util.Map;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 08/03/2021 - 11:39
 */
public interface MirroringListener {

    public void onDeviceMirrored(String deviceId, Map<String, Object> metadata);

    public void onDeviceMirroringError(String deviceId, String errorMsg);

    public void onResourceMirrored(String resourceId, Map<String, Object> metadata);

    public void onResourceMirroringError(String ResourceId, String errorMsg);

}
