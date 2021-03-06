package cl.niclabs.scada.acs.examples.cracker.autonomic.metric;

import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.MetricStore;
import cl.niclabs.scada.acs.component.controllers.monitoring.records.RecordStore;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.solver.component.Solver;


public class DummyDistributionPointMetric extends DistributionPointMetric {

    public static final String NAME = "dummyDistributionPoint";

    public DummyDistributionPointMetric() {
        super();
        setEnabled(true);
    }

    @Override
    public DistributionPoint calculate(RecordStore recordStore, MetricStore metricStore) {

        if (initTime <= 0) {
            initTime = System.currentTimeMillis();
            testLogCounter = 0;
            System.out.println("[0] time,s1_time,s2_time,s3_time,s1_assignment,s2_assignment,s3_assignment");
        }

        Wrapper<Long> s1 = metricStore.getValue(avgRespTimeMetric, new String[] { Solver.NAME + "-0" } );
        Wrapper<Long> s2 = metricStore.getValue(avgRespTimeMetric, new String[] { Solver.NAME + "-1" } );
        Wrapper<Long> s3 = metricStore.getValue(avgRespTimeMetric, new String[] { Solver.NAME + "-2" } );

        if (areValidValues(s1, s2, s3)) {
            String testLogMsg = "[" + ++testLogCounter + "] " + String.valueOf(System.currentTimeMillis() - initTime);
            testLogMsg += "," + s1.unwrap() + "," + s2.unwrap() + "," + s3.unwrap();
            System.out.println(testLogMsg + "," + lastPoint.asTestLog());
        }

        return lastPoint;
    }

}
