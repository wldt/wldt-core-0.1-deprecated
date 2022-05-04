package it.unimore.dipi.iot.wldt.model;

import it.unimore.dipi.iot.wldt.exception.EventBusException;
import it.unimore.dipi.iot.wldt.exception.ModelException;
import it.unimore.dipi.iot.wldt.exception.ModelFunctionException;
import it.unimore.dipi.iot.wldt.state.IDigitalTwinState;
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
public class ModelEngine {

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
        this.shadowingModelFunction.observePhysicalEvents();
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
    public void remoteModelFunction(String modelFunctionId) throws ModelException, ModelFunctionException {
        if(modelFunctionId == null || !modelFunctionMap.containsKey(modelFunctionId))
            throw new ModelException(String.format("Error ! Provided modelFunctionId(%s) invalid or not found !", modelFunctionId));

        this.modelFunctionMap.get(modelFunctionId).onRemoved();
        this.modelFunctionMap.remove(modelFunctionId);
    }

}
