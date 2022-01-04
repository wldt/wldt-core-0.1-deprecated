package it.unimore.dipi.iot.wldt.exception;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 08/03/2021 - 11:58
 */
public class WldtDigitalTwinStateException extends Exception {

    public WldtDigitalTwinStateException(String errorMsg){
        super(errorMsg);
    }
}
