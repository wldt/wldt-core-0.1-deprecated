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

    private Map<String, ModelFunction> modelFunctionMap = null;

    public ModelEngine(IDigitalTwinState digitalTwinState){
        this.digitalTwinState = digitalTwinState;
        this.modelFunctionMap = new HashMap<>();
    }

    /**
     *
     * @param modelFunction
     * @throws ModelException
     */
    public void startModelFunction(ModelFunction modelFunction, boolean observeState, List<String> observePropertyList) throws ModelException, EventBusException, ModelFunctionException {
        if(modelFunction == null || modelFunction.getId() == null)
            throw new ModelException("Error ! ModelFunction = Null or ModelFunction-Id = Null !");

        modelFunction.init(this.digitalTwinState);

        this.modelFunctionMap.put(modelFunction.getId(), modelFunction);

        this.modelFunctionMap.get(modelFunction.getId()).onStart();

        if(observeState)
            this.modelFunctionMap.get(modelFunction.getId()).observeDigitalTwinState();

        if(observePropertyList != null && observePropertyList.size() > 0)
            this.modelFunctionMap.get(modelFunction.getId()).observeDigitalTwinProperties(observePropertyList);
    }

    /**
     *
     * @param modelFunctionId
     * @throws ModelException
     */
    public void stopModelFunction(String modelFunctionId) throws ModelException, ModelFunctionException {
        if(modelFunctionId == null || !modelFunctionMap.containsKey(modelFunctionId))
            throw new ModelException(String.format("Error ! Provided modelFunctionId(%s) invalid or not found !", modelFunctionId));

        this.modelFunctionMap.get(modelFunctionId).onStop();
        this.modelFunctionMap.remove(modelFunctionId);
    }

}
