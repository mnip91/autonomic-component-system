package cl.niclabs.scada.acs.component.controllers.monitor;

/**
 * Created by mibanez
 */
public interface MetricEventListener {

    void notifyUpdate(MetricEvent event);

}