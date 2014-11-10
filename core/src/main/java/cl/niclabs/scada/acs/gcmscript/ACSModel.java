package cl.niclabs.scada.acs.gcmscript;

import cl.niclabs.scada.acs.component.controllers.MetricProxy;
import cl.niclabs.scada.acs.component.controllers.PlanProxy;
import cl.niclabs.scada.acs.component.controllers.RuleProxy;
import cl.niclabs.scada.acs.gcmscript.controllers.analysis.AddRuleAction;
import cl.niclabs.scada.acs.gcmscript.controllers.analysis.PrintRulesFunction;
import cl.niclabs.scada.acs.gcmscript.controllers.analysis.RuleNode;
import cl.niclabs.scada.acs.gcmscript.controllers.monitoring.AddMetricAction;
import cl.niclabs.scada.acs.gcmscript.controllers.monitoring.MetricNode;
import cl.niclabs.scada.acs.gcmscript.controllers.monitoring.PrintMetricsFunction;
import cl.niclabs.scada.acs.gcmscript.controllers.monitoring.RemoveMetricAction;
import cl.niclabs.scada.acs.gcmscript.controllers.planning.PlanNode;
import org.objectweb.fractal.fscript.model.Property;
import org.objectweb.proactive.extra.component.fscript.model.GCMModel;
import org.objectweb.proactive.extra.component.fscript.model.GCMProcedure;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.fractal.fscript.types.PrimitiveType.*;

/**
 * Created by mibanez
 */
public class ACSModel extends GCMModel {

    @Override
    protected void createNodeKinds() {
        super.createNodeKinds();
        addKind("generic-element", new Property("id", STRING, false), new Property("enabled", BOOLEAN, true));
        addKind("metric", getNodeKind("generic-element"), new Property("value", OBJECT, false));
        addKind("rule", getNodeKind("generic-element"), new Property("alarm", OBJECT, false));
        addKind("plan", getNodeKind("generic-element"));
    }

    @Override
    protected void createAdditionalProcedures() {
        super.createAdditionalProcedures();

        List<GCMProcedure> procedures = new ArrayList<>();
        procedures.add(new AddMetricAction());
        procedures.add(new RemoveMetricAction());
        procedures.add(new PrintMetricsFunction());
        procedures.add(new AddRuleAction());
        procedures.add(new RemoveMetricAction());
        procedures.add(new PrintRulesFunction());

        for (GCMProcedure procedure : procedures) {
            try {
                procedure.bindFc(GCMProcedure.MODEL_NAME, this);
            } catch (Exception e) {
                throw new AssertionError("Internal inconsistency with " + procedure.getName() + " procedure");
            }
            addProcedure(procedure);
        }
    }

    public MetricNode createMetricNode(MetricProxy metricProxy) {
        return new MetricNode(this, metricProxy);
    }

    public RuleNode createRuleNode(RuleProxy ruleProxy) {
        return new RuleNode(this, ruleProxy);
    }

    public PlanNode createPlanNode(PlanProxy planProxy) {
        return new PlanNode(this, planProxy);
    }

}
