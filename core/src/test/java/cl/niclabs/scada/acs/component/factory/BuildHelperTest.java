package cl.niclabs.scada.acs.component.factory;

import cl.niclabs.scada.acs.AbstractComponentTest;
import cl.niclabs.scada.acs.component.ACSManager;
import cl.niclabs.scada.acs.component.controllers.monitoring.MonitoringController;
import cl.niclabs.scada.acs.component.controllers.monitoring.metrics.RemoteMonitoringManager;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;

import java.util.HashSet;
import java.util.List;

/**
 * BuildHelper unit test
 * Created by mibanez
 */
public class BuildHelperTest extends AbstractComponentTest {

    @Test
    public void getAcsNfInterfaces() {

        InterfaceType[] interfaceTypes = null;
        try {
            interfaceTypes = new InterfaceType[]{
                    factory.createInterfaceType("server-itf", FooInterface.class, false, false),
                    factory.createInterfaceType("client-itf", FooInterface.class, true, true)
            };
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        List<InterfaceType> acsNfInterfaces = null;
        try {
            acsNfInterfaces = (new BuildHelper(factory)).getExtraNfInterfaces(interfaceTypes);
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        HashSet<String> nameSet = new HashSet<>();
        for (InterfaceType itfType : acsNfInterfaces) {
            nameSet.add(itfType.getFcItfName());
        }

        Assert.assertTrue(nameSet.contains(ACSManager.MONITORING_CONTROLLER));
        Assert.assertTrue(nameSet.contains(ACSManager.ANALYSIS_CONTROLLER));
        Assert.assertTrue(nameSet.contains(ACSManager.PLANNING_CONTROLLER));

        Assert.assertTrue(nameSet.contains("client-itf" + RemoteMonitoringManager.REMOTE_MONITORING_SUFFIX));
        Assert.assertTrue(nameSet.contains("server-itf" + RemoteMonitoringManager.REMOTE_MONITORING_SUFFIX));

    }

    @Test
    public void addACSControllers() {

        Component foo = null;
        try {
            ComponentType componentType = factory.createComponentType(new InterfaceType[]{
                    factory.createInterfaceType("server-itf", FooInterface.class, false, false),
                    factory.createInterfaceType("client-itf", FooInterface.class, true, true)
            });
            foo = factory.getGenericFactory().newFcInstance(componentType,
                    new ControllerDescription("FooComponent", "primitive"),
                    new ContentDescription(FooComponent.class.getName()), null);
        } catch (ACSFactoryException | InstantiationException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        BuildHelper buildHelper = null;
        try {
            buildHelper = new BuildHelper(factory);
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        try {
            buildHelper.addObjectControllers(foo);
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail("addObjectControllers: " + e.getMessage());
        }

        try {
            buildHelper.addACSControllers(foo);
        } catch (ACSFactoryException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        Object monitorObj = null;
        try {
            monitorObj = foo.getFcInterface("monitor-controller");
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(monitorObj instanceof MonitoringController);
    }

}
