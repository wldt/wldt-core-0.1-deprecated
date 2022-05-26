package it.unimore.dipi.iot.wldt.model;

import it.unimore.dipi.iot.wldt.adapter.PhysicalAssetDescription;
import it.unimore.dipi.iot.wldt.engine.LifeCycleListener;
import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.exception.ModelException;
import it.unimore.dipi.iot.wldt.exception.ModelFunctionException;
import it.unimore.dipi.iot.wldt.exception.WldtRuntimeException;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
import it.unimore.dipi.iot.wldt.worker.WldtWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class ModelEngine extends WldtWorker implements LifeCycleListener {

    private static final Logger logger = LoggerFactory.getLogger(ModelEngine.class);

    private IDigitalTwinState digitalTwinState = null;

    private Map<String, StateModelFunction> modelFunctionMap = null;

    private ShadowingModelFunction shadowingModelFunction = null;

    public ModelEngine(IDigitalTwinState digitalTwinState, ShadowingModelFunction shadowingModelFunction) throws ModelException, EventBusException {

        this.digitalTwinState = digitalTwinState;
        this.modelFunctionMap = new HashMap<>();

        if(shadowingModelFunction == null)
            throw new ModelException("Error ! Provided ShadowingModelFunction == Null !");

        this.shadowingModelFunction = shadowingModelFunction;
    }

    /**
     *
     * @param stateModelFunction
     * @throws ModelException
     */
    public void addStateModelFunction(StateModelFunction stateModelFunction, boolean observeState, List<String> observePropertyList) throws ModelException, EventBusException, ModelFunctionException {
        if(stateModelFunction == null || stateModelFunction.getId() == null)
            throw new ModelException("Error ! ModelFunction = Null or ModelFunction-Id = Null !");

        stateModelFunction.init(this.digitalTwinState);

        this.modelFunctionMap.put(stateModelFunction.getId(), stateModelFunction);

        this.modelFunctionMap.get(stateModelFunction.getId()).onAdded();

        if(observeState)
            this.modelFunctionMap.get(stateModelFunction.getId()).observeDigitalTwinState();

        if(observePropertyList != null && observePropertyList.size() > 0)
            this.modelFunctionMap.get(stateModelFunction.getId()).observeDigitalTwinProperties(observePropertyList);
    }

    /**
     *
     * @param modelFunctionId
     * @throws ModelException
     */
    public void removeStateModelFunction(String modelFunctionId) throws ModelException {
        if(modelFunctionId == null || !modelFunctionMap.containsKey(modelFunctionId))
            throw new ModelException(String.format("Error ! Provided modelFunctionId(%s) invalid or not found !", modelFunctionId));

        this.modelFunctionMap.get(modelFunctionId).onRemoved();
        this.modelFunctionMap.remove(modelFunctionId);
    }

    @Override
    public void onWorkerCreated() {
        if(this.shadowingModelFunction != null)
            this.shadowingModelFunction.onCreate();
    }

    @Override
    public void onWorkerStop() {

        logger.info("Stopping Model Engine ....");

        //Stop Shadowing Function
        if(this.shadowingModelFunction != null)
            this.shadowingModelFunction.onStop();

        //Remove all the stored State Model Function
        for (Map.Entry<String, StateModelFunction> entry : this.modelFunctionMap.entrySet())
            try{
                removeStateModelFunction(entry.getKey());
            }catch (Exception e){
                logger.error("Error Removing State Model Function: {}", e.getLocalizedMessage());
            }

        logger.info("Model Engine Correctly Stopped !");
    }

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try {
            this.shadowingModelFunction.onStart();
        } catch (Exception e) {
            String errorMessage = String.format("Shadowing Function Error Observing Physical Event: %s", e.getLocalizedMessage());
            logger.error(errorMessage);
            throw new WldtRuntimeException(errorMessage);
        }
    }

    @Override
    public void onCreate() {
        logger.debug("DT-LifeCycle: onCreate()");
    }

    @Override
    public void onStart() {
        logger.debug("DT-LifeCycle: onCreate()");
    }

    @Override
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
    }

    @Override
    public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        logger.debug("DT-LifeCycle: onPhysicalAdapterBindingUpdate()");
        this.shadowingModelFunction.onPhysicalAdapterBidingUpdate(adapterId, physicalAssetDescription);
    }

    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {
        //logger.debug("DT-LifeCycle: onCreate()");
    }

    @Override
    public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        logger.debug("DT-LifeCycle: onDigitalTwinBound()");
        this.shadowingModelFunction.onDigitalTwinBound(adaptersPhysicalAssetDescriptionMap);
    }

    @Override
    public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
        logger.debug("DT-LifeCycle: onDigitalTwinUnBound()");
        this.shadowingModelFunction.onDigitalTwinUnBound(adaptersPhysicalAssetDescriptionMap, errorMessage);
    }

    @Override
    public void onSync() {
        //logger.debug("DT-LifeCycle: onCreate()");
    }

    @Override
    public void onUnSync() {
        //logger.debug("DT-LifeCycle: onCreate()");
    }

    @Override
    public void onStop() {
        logger.debug("DT-LifeCycle: onStop()");
    }

    @Override
    public void onDestroy() {
        logger.debug("DT-LifeCycle: onDestroy()");
    }
}
