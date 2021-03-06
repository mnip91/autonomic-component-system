package cl.niclabs.scada.acs.gcmscript.model;

import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.apache.log4j.Logger;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.model.AbstractAxis;
import org.objectweb.fractal.fscript.model.Node;
import org.objectweb.fractal.fscript.model.fractal.FractalModel;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mibanez
 */
public class SubscriptionAxis extends AbstractAxis {

    private static final Logger logger = ProActiveLogger.getLogger("ACS");

    public SubscriptionAxis(FractalModel model) {
        super(model, "subscription", "generic-element", "generic-element");
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isModifiable() {
        return true;
    }

    @Override
    public Set<Node> selectFrom(Node source) {

        if (source instanceof RuleNode) {
            return selectFromRule((RuleNode) source);
        } else if (source instanceof PlanNode) {
            return selectFromPlan((PlanNode) source);
        } else {
            return new HashSet<>();
        }
    }

    private HashSet<Node> selectFromRule(RuleNode ruleNode) {

        HashSet<Node> metricNodes = new HashSet<>();
        Wrapper<HashSet<String>> wrapper = ruleNode.getAnalysisController().getSubscriptions(ruleNode.getRuleName());

        if (wrapper.isValid()) {
            for (String metricId : wrapper.unwrap()) {
                try {
                    metricNodes.add(((ACSModel) model).createMetricNode(ruleNode.getHost(), metricId));
                } catch (NoSuchInterfaceException ignore) {}
            }
        } else {
            //logger.debug("Invalid wrapper: {}", wrapper.getMessage());
        }

        return metricNodes;
    }

    private Set<Node> selectFromPlan(PlanNode planNode) {

        HashSet<Node> ruleNodes = new HashSet<>();
        Wrapper<HashSet<String>> wrapper = planNode.getPlanningController().getSubscriptions(planNode.getPlanName());

        if (wrapper.isValid()) {
            for (String subscription : wrapper.unwrap()) {
                try {
                    ruleNodes.add(((ACSModel) model).createRuleNode(planNode.getHost(), subscription));
                } catch (NoSuchInterfaceException ignore) {}
            }
        } else {
            //logger.debug("Invalid wrapper: {}", wrapper.getMessage());
        }

        return ruleNodes;
    }

    @Override
    public void connect(Node source, Node dest) {
        if ((source instanceof RuleNode) && (dest instanceof MetricNode)) {
            RuleNode ruleNode = (RuleNode) source;
            MetricNode metricNode = (MetricNode) dest;
            ruleNode.getAnalysisController().subscribeTo(ruleNode.getRuleName(), metricNode.getMetricName());
        } else if ((source instanceof PlanNode) && (dest instanceof RuleNode)) {
            PlanNode planNode = (PlanNode) source;
            RuleNode ruleNode = (RuleNode) dest;
            planNode.getPlanningController().subscribeTo(planNode.getPlanName(), ruleNode.getRuleName());
        } else {
            throw new IllegalArgumentException("Operation not supported for for given source and destination nodes");
        }
    }

    /**
     * Removes the arc in the underlying model connecting the given source and destination
     * nodes with this axis.
     *
     * @param source
     *            the source node of the arc to remove.
     * @param dest
     *            the destination node of the arc to remove.
     * @throws UnsupportedOperationException
     *             if this axis does not support direct manipulation of its arcs.
     * @throws IllegalArgumentException
     *             if it is not possible to remove the requested arc or if it does not
     *             exist.
     */
    @Override
    public void disconnect(Node source, Node dest) {
        if ((source instanceof RuleNode) && (dest instanceof MetricNode)) {
            RuleNode ruleNode = (RuleNode) source;
            MetricNode metricNode = (MetricNode) dest;
            ruleNode.getAnalysisController().unsubscribeFrom(ruleNode.getRuleName(), metricNode.getMetricName());
        } else if ((source instanceof PlanNode) && (dest instanceof RuleNode)) {
            PlanNode planNode = (PlanNode) source;
            RuleNode ruleNode = (RuleNode) dest;
            planNode.getPlanningController().unsubscribeFrom(planNode.getPlanName(), ruleNode.getRuleName());
        } else {
            throw new IllegalArgumentException("Operation not supported for for given source and destination nodes");
        }
    }
}
