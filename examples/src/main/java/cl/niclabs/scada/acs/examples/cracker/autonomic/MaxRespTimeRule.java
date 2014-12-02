package cl.niclabs.scada.acs.examples.cracker.autonomic;

import cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm;
import cl.niclabs.scada.acs.component.controllers.analysis.Rule;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.utils.Wrapper;
import cl.niclabs.scada.acs.examples.cracker.CrackerConfig;

import static cl.niclabs.scada.acs.component.controllers.analysis.ACSAlarm.*;


public class MaxRespTimeRule extends Rule {

    public static final String NAME = "maxRespTime";

    private ACSAlarm alarm;
    private long threshold;
    private String avgRespTimeMetricName;

    public MaxRespTimeRule() {
        this.alarm = OK;
        this.threshold = CrackerConfig.MAX_RESPONSE_TIME;
        this.avgRespTimeMetricName = AvgRespTimeMetric.NAME;

        subscribeTo(AvgRespTimeMetric.NAME);
    }

    @Override
    public ACSAlarm getAlarm(MonitoringController monitoringController) {
        return alarm;
    }

    @Override
    public ACSAlarm verify(MonitoringController monitoringCtrl) {

        Wrapper<Long> respTimeWrapper = monitoringCtrl.getValue(avgRespTimeMetricName);

        if ( !respTimeWrapper.isValid() ) {
            alarm = ERROR;
        } else if (respTimeWrapper.unwrap() > threshold) {
            alarm = VIOLATION;
        } else {
            alarm = OK;
        }

        return alarm;
    }
}
