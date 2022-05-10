package it.unimore.dipi.iot.wldt.model;

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
public class ModelEngine extends WldtWorker {

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
    public void onWorkerStart() {
        if(this.shadowingModelFunction != null)
            this.shadowingModelFunction.onStart();
    }

    @Override
    public void onWorkerStop() {

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
    }

    @Override
    public void handleWorkerJob() throws WldtRuntimeException {
        try {
            this.shadowingModelFunction.observePhysicalEvents();
        } catch (EventBusException | ModelException e) {
            String errorMessage = String.format("Shadowing Function Error Observing Physical Event: %s", e.getLocalizedMessage());
            logger.error(errorMessage);
            throw new WldtRuntimeException(errorMessage);
        }
    }
}
