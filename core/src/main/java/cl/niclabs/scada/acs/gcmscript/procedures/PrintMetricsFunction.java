package cl.niclabs.scada.acs.gcmscript.procedures;

import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.events.RecordEvent;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.fscript.ScriptExecutionError;
import org.objectweb.fractal.fscript.diagnostics.Diagnostic;
import org.objectweb.fractal.fscript.diagnostics.Severity;
import org.objectweb.fractal.fscript.interpreter.Context;
import org.objectweb.fractal.fscript.types.Signature;
import org.objectweb.proactive.extra.component.fscript.model.AbstractGCMProcedure;
import org.objectweb.proactive.extra.component.fscript.model.GCMComponentNode;

import java.util.List;

import static org.objectweb.fractal.fscript.types.PrimitiveType.STRING;

/**
 * Created by mibanez
 */
public class PrintMetricsFunction extends AbstractGCMProcedure {

    @Override
    public String getName() {
        return "print-metrics";
    }

    @Override
    public boolean isPureFunction() {
        return true;
    }

    @Override
    public Signature getSignature() {
        return new Signature(STRING, model.getNodeKind("component"));
    }

    @Override
    public Object apply(List<Object> objects, Context context) throws ScriptExecutionError {

        GCMComponentNode node = (GCMComponentNode) objects.get(0);
        try {
            MonitoringController monitoringCtrl = ACSManager.getMonitoringController(node.getComponent());
            List<String> metrics = monitoringCtrl.getRegisteredIds();

            String info = "\t(NAME)\t(STATE)\n";
            for (String name : metrics) {
                Wrapper<Boolean> state = monitoringCtrl.isEnabled(name);
                info += "\t" + name + "\t" + (state.unwrap() ? "ENABLE" : "DISABLE");

                String subscriptions = "[";
                for (RecordEvent recordEvent : monitoringCtrl.getSubscriptions(name).unwrap()) {
                    subscriptions += recordEvent + ", ";
                }

                info += "\t" + subscriptions + "]\n";

            }
            return info + "\t-------(END)------\n";

        } catch (NoSuchInterfaceException e) {
            throw new ScriptExecutionError(new Diagnostic(Severity.ERROR, e.getMessage()), e);
        }
    }

}
