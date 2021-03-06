package cl.niclabs.scada.acs.gcmscript.model;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.gcmscript.model.ACSModel;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.model.AbstractAxis;
import org.objectweb.fractal.fscript.model.Node;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;
import org.objectweb.proactive.extra.component.fscript.model.GCMInterfaceNode;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mibanez
 */
public class MetricAxis extends AbstractAxis {

    public MetricAxis(FractalModel model) {
        super(model, "metric", "component", "metric");
    }

    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    /**
     * Locates all the destination nodes the given source node is connected to through
     * this axis.
     *
     * @param source
     *            the source node from which to select adjacent nodes.
     * @return all the destination nodes the given source node is connected to through
     *         this axis.
     */
    @Override
    public Set<Node> selectFrom(Node source) {
        Component host;
        if (source instanceof GCMComponentNode) {
            host = ((GCMComponentNode) source).getComponent();
        } else if (source instanceof GCMInterfaceNode) {
            host = ((GCMInterfaceNode) source).getInterface().getFcItfOwner();
        } else {
            throw new IllegalArgumentException("Invalid source node kind " + source.getKind());
        }

        HashSet<Node> result = new HashSet<>();
        try {
            MonitoringController monitoringController = ACSManager.getMonitoringController(host);
            for (String metricId : monitoringController.getRegisteredIds()) {
                Node node = ((ACSModel) model).createMetricNode(host, metricId);
                result.add(node);
            }
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        }

        return result;
    }

}
