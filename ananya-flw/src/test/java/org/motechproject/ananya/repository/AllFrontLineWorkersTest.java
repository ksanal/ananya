package org.motechproject.ananya.repository;

import org.junit.Test;
import org.motechproject.ananya.domain.FrontLineWorker;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static junit.framework.Assert.assertEquals;

public class AllFrontLineWorkersTest extends FLWSpringIntegrationTest {
    @Autowired
    private AllFrontLineWorkers allFrontLineWorkers;

    @Test
    public void shouldAddAndRetrieveRecord() {
        String msisdn = "9901";
        FrontLineWorker frontLineWorker = new FrontLineWorker(msisdn);
        allFrontLineWorkers.add(frontLineWorker);

        markForDeletion(frontLineWorker);
        List<FrontLineWorker> frontLineWorkers = allFrontLineWorkers.getAll();
        assertEquals(msisdn, frontLineWorkers.get(0).msisdn());
    }

    @Test
    public void shouldRetrieveFrontLineWorkerByMSISDN() {
        String msisdn = "9901";
        FrontLineWorker frontLineWorker = new FrontLineWorker(msisdn);
        allFrontLineWorkers.add(frontLineWorker);

        markForDeletion(frontLineWorker);

        FrontLineWorker dbFrontLineWorker = allFrontLineWorkers.findByMsisdn(msisdn);
        assertEquals(msisdn, dbFrontLineWorker.msisdn());
    }
}
