package cl.niclabs.scada.acs.component.controllers.monitoring;

import cl.niclabs.scada.acs.component.controllers.MonitorController;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.core.component.componentcontroller.AbstractPAComponentController;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MonitorControllerImpl extends AbstractPAComponentController
        implements MonitorController, MonitorNotifier, LifeCycleController {

    private final Map<String, Metric> metrics = new HashMap<>();
    private final RecordStore recordStore = new RecordStore();
    private ACSEventListener eventListener;

    @Override
    public Wrapper<Boolean> addMetric(String name, Metric metric) {
        if (metrics.containsKey(name)) {
            return new Wrapper<>(false, String.format("the metric name %s already exists", name));
        }
        metrics.put(name, metric);
        return new Wrapper<>(true, String.format("metric %s added correctly", name));
    }

    @Override
    public Wrapper<Boolean> removeMetric(String name) {
        if (metrics.containsKey(name)) {
            metrics.remove(name);
            return new Wrapper<>(true);
        }
        return new Wrapper<>(false, String.format("no metric with name %s found", name));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <VALUE extends Serializable> Wrapper<VALUE> getValue(String name) {
        if (metrics.containsKey(name)) {
            return metrics.get(name).getWrappedValue();
        }
        return new Wrapper(null, String.format("no metric found with name %s", name));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <VALUE extends Serializable> Wrapper<VALUE> measure(String name) {
        if (metrics.containsKey(name)) {
            Metric metric = metrics.get(name);
            metric.measure();
            return metric.getWrappedValue();
        }
        return new Wrapper<>(null, String.format("no metric with name %s found", name));
    }

    @Override
    public Wrapper<String[]> getMetricNames() {
        return new Wrapper<>(metrics.keySet().toArray(new String[metrics.size()]));
    }

    @Override
    public String getFcState() {
        return null; // never used on
    }

    @Override
    public void startFc() throws IllegalLifeCycleException {
        if (eventListener == null) {
            String runtimeURL = ProActiveRuntimeImpl.getProActiveRuntime().getURL();
            eventListener = new ACSEventListener(this, recordStore, hostComponent.getID(), runtimeURL);
        }
    }

    @Override
    public void stopFc() throws IllegalLifeCycleException {
        // nothing
    }

    @Override
    public void notifyACSEvent(ACSEventType eventType) {
        //logger.debug("record notification {}", eventType);
    }
}