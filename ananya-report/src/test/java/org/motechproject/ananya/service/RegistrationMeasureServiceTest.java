package org.motechproject.ananya.service;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ananya.domain.FrontLineWorker;
import org.motechproject.ananya.domain.dimension.FrontLineWorkerDimension;
import org.motechproject.ananya.domain.dimension.LocationDimension;
import org.motechproject.ananya.domain.dimension.TimeDimension;
import org.motechproject.ananya.domain.measure.RegistrationMeasure;
import org.motechproject.ananya.repository.dimension.AllFrontLineWorkerDimensions;
import org.motechproject.ananya.repository.dimension.AllLocationDimensions;
import org.motechproject.ananya.repository.dimension.AllTimeDimensions;
import org.motechproject.ananya.repository.measure.AllRegistrationMeasures;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RegistrationMeasureServiceTest {

    private RegistrationMeasureService registrationMeasureService;
    @Mock
    private AllRegistrationMeasures allRegistrationMeasures;
    @Mock
    private FrontLineWorkerService frontLineWorkerService;
    @Mock
    private AllLocationDimensions allLocationDimensions;
    @Mock
    private AllFrontLineWorkerDimensions allFrontLineWorkerDimensions;
    @Mock
    private AllTimeDimensions allTimeDimensions;

    @Before
    public void setUp(){
        initMocks(this);
        registrationMeasureService = new RegistrationMeasureService(frontLineWorkerService,
                allLocationDimensions, allFrontLineWorkerDimensions, allTimeDimensions,allRegistrationMeasures );
    }
    
    @Test
    public void shouldCreateRegistrationMeasure(){
        String callerId = "12345";
        String operator = "operator";
        DateTime registeredDate = DateTime.now();
        FrontLineWorker frontLineWorker = new FrontLineWorker(callerId, operator);
        frontLineWorker.setRegisteredDate(registeredDate);
        LocationDimension locationDimension = new LocationDimension("id", "district", "block", "panchayat");
        FrontLineWorkerDimension frontLineWorkerDimension = new FrontLineWorkerDimension(Long.valueOf(callerId), operator, "", "", "");
        TimeDimension timeDimension = new TimeDimension(registeredDate);

        when(frontLineWorkerService.findByCallerId(callerId)).thenReturn(frontLineWorker);
        when(allLocationDimensions.getFor(anyString())).thenReturn(locationDimension);
        when(allFrontLineWorkerDimensions.getOrMakeFor(Long.valueOf(callerId), operator, null,null,"UNREGISTERED")).thenReturn(frontLineWorkerDimension);
        when(allTimeDimensions.getFor(registeredDate)).thenReturn(timeDimension);

        registrationMeasureService.createRegistrationMeasure(callerId);

        ArgumentCaptor<RegistrationMeasure> captor = ArgumentCaptor.forClass(RegistrationMeasure.class);
        verify(allRegistrationMeasures).add(captor.capture());

        RegistrationMeasure registrationMeasure = captor.getValue();
        assertNotNull(registrationMeasure);
        assertEquals(locationDimension , registrationMeasure.getLocationDimension());
        assertEquals(frontLineWorkerDimension,registrationMeasure.getFrontLineWorkerDimension());
        assertEquals(timeDimension,registrationMeasure.getTimeDimension());
    }
}