package it.at7.gemini.api;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        RestAPIControllerBaseEntityAbstTest.class,
        RestAPIControllerGenericEntityRefAbstTest.class,
        RestApiControllerMultipleFieldLKAbstTest.class,
        ClosedDomainRESTAPIControllerAbstTest.class,
        RestAPIControllerOneRecordEntityAbstTest.class,
        RestAPIControllerUUIDAbstTest.class,
        RestAPIControllerListAbstTest.class
})
public class SuiteTestAPI extends SuiteTestWebApiINIT {
}
