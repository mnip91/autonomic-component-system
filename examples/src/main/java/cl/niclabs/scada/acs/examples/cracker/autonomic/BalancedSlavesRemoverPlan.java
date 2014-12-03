package cl.niclabs.scada.acs.examples.cracker.autonomic;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.execution.ExecutionController;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.planning.Plan;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;

import java.util.PriorityQueue;
import java.util.Queue;

import static cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm.ERROR;
import static cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm.VIOLATION;

public class BalancedSlavesRemoverPlan extends Plan {

    public static final String NAME = "balancedSlavesRemover";

    private boolean sleeping = false;
    private long sleepStartTime = 0;

    public BalancedSlavesRemoverPlan() {
        subscribeTo(MinRespTimeRule.NAME);
    }

    @Override
    public void doPlanFor(String ruleName, ACSAlarm alarm, MonitoringController monitorCtrl, ExecutionController executionCtrl) {

        if (alarm == ERROR) {
            System.out.println("[WARNING] SlavesAdder: ERROR alarm received from " + ruleName);
        } else if (alarm == VIOLATION) {

            if (stillSleeping()) {
                return;
            }

            // Sort, first the one with more assigned work
            Wrapper<DistributionPoint> point = monitorCtrl.getValue(DistributionPointMetric.NAME);
            if (point.isValid()) {

                double x = point.unwrap().getX(), y = point.unwrap().getY();

                Queue<Pair> queue = new PriorityQueue<>();
                queue.add(new Pair(0, x));
                queue.add(new Pair(1, y - x));
                queue.add(new Pair(2, 1 - y));

                for (Pair pair : queue) {
                    Wrapper<Double> result = executionCtrl.execute("slaves-number($this/child::Solver" + pair.index + ");");
                    if (result.unwrap().intValue() > 1) {
                        executionCtrl.execute("remove-slave($this/child::Solver" + pair.index + ");");
                        goToSleep();
                        System.out.println("[ACTION] Slave removed from Solver" + pair.index + " (now there is " + (result.unwrap() - 1) + ")");
                        return;
                    }
                }
            }
        }
    }

    private void goToSleep() {
        sleeping = true;
        sleepStartTime = System.currentTimeMillis();
    }

    private boolean stillSleeping() {
        if (sleeping && (System.currentTimeMillis() - sleepStartTime) > CrackerConfig.SLEEP_TIME) {
            sleeping = false;
        }
        return sleeping;
    }

    private boolean areValidValues(Wrapper<Long> s1, Wrapper<Long> s2, Wrapper<Long> s3) {
        if ( !( s1.isValid() && s2.isValid() && s3.isValid() ) ) {
            String report = "\n" + s1.getMessage() + "\n" + s2.getMessage() + "\n" + s3.getMessage();
            System.err.println("[WARNING] DistributionPoint fail: " + report);
            return false;
        } else if (s1.unwrap() <= 0 || s2.unwrap() <= 0 || s3.unwrap() <= 0) {
            // avoid broken values
            return false;
        }
        return true;
    }

    class Pair implements Comparable<Pair> {
        int index;
        double time;
        Pair (int i, double t) {
            index = i;
            time = t;
        }
        @Override
        public int compareTo(Pair o) {
            double d = (o.time - time);
            return d > 0 ? 1 : d < 0 ? -1 : 0; // NOTE THIS
        }
    }
}
