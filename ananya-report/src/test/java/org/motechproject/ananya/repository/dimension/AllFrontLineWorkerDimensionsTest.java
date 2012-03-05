package org.motechproject.ananya.repository.dimension;

import org.junit.After;
import org.junit.Test;
import org.motechproject.ananya.SpringIntegrationTest;
import org.motechproject.ananya.domain.dimension.FrontLineWorkerDimension;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class AllFrontLineWorkerDimensionsTest extends SpringIntegrationTest {

    @Autowired
    AllFrontLineWorkerDimensions allFrontLineWorkerDimensions;

    @After
    public void After(){
        template.deleteAll(template.loadAll(FrontLineWorkerDimension.class));
    }

    @Test
    public void shouldFetchFrontLineWorkerDimensionIfExists(){
        long msisdn = 9880099L;
        String name = "name";
        String operator = "operator";
        String status = "status";
        template.save(new FrontLineWorkerDimension(msisdn, operator, name, status));
        FrontLineWorkerDimension frontLineWorkerDimension = allFrontLineWorkerDimensions.fetchFor(msisdn);
        
        assertEquals(name,frontLineWorkerDimension.getName());
        assertEquals(operator,frontLineWorkerDimension.getOperator());
        assertEquals(status,frontLineWorkerDimension.getStatus());
    }

    @Test
    public void shouldReturnNullWhenFrontLineWorkerDimensionDoesNotExists(){
        FrontLineWorkerDimension frontLineWorkerDimension = allFrontLineWorkerDimensions.fetchFor(100L);
        assertNull(frontLineWorkerDimension);
    }
}