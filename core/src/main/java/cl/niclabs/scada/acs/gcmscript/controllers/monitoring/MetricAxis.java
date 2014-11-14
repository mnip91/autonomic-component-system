package cl.niclabs.scada.acs.gcmscript.controllers.monitoring;

import cl.niclabs.scada.acs.component.ACSUtils;
import cl.niclabs.scada.acs.component.controllers.CommunicationException;
import cl.niclabs.scada.acs.component.controllers.MetricProxy;
import cl.niclabs.scada.acs.component.controllers.MonitoringController;
import cl.niclabs.scada.acs.gcmscript.ACSModel;
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
        Component comp;
        if (source instanceof GCMComponentNode) {
            comp = ((GCMComponentNode) source).getComponent();
        } else if (source instanceof GCMInterfaceNode) {
            comp = ((GCMInterfaceNode) source).getInterface().getFcItfOwner();
        } else {
            throw new IllegalArgumentException("Invalid source node kind " + source.getKind());
        }

        HashSet<Node> result = new HashSet<>();
        try {
            MonitoringController monitoringController = ACSUtils.getMonitoringController(comp);
            for (String metricId : monitoringController.getRegisteredIds()) {
                Node node = ((ACSModel) model).createMetricNode(new MetricProxy(metricId, comp));
                result.add(node);
            }
        } catch (NoSuchInterfaceException | CommunicationException e) {
            e.printStackTrace();
        }

        return result;
    }

}