package org.motechproject.ananya.service;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.motechproject.ananya.domain.SMSReference;
import org.motechproject.ananya.domain.dimension.FrontLineWorkerDimension;
import org.motechproject.ananya.domain.dimension.LocationDimension;
import org.motechproject.ananya.domain.dimension.TimeDimension;
import org.motechproject.ananya.domain.measure.RegistrationMeasure;
import org.motechproject.ananya.domain.measure.SMSSentMeasure;
import org.motechproject.ananya.repository.dimension.AllFrontLineWorkerDimensions;
import org.motechproject.ananya.repository.dimension.AllTimeDimensions;
import org.motechproject.ananya.repository.measure.AllRegistrationMeasures;
import org.motechproject.ananya.repository.measure.AllSMSSentMeasures;
import org.motechproject.ananya.service.dimension.LocationDimensionService;
import org.motechproject.ananya.service.measure.SMSSentMeasureService;
import org.motechproject.ananya.service.measure.TransferableMeasureService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class SMSSentMeasureServiceTest {

    private SMSSentMeasureService smsSentMeasureService;

    @Mock
    FrontLineWorkerService frontLineWorkerService;
    @Mock
    AllFrontLineWorkerDimensions frontLineWorkerDimensions;
    @Mock
    AllTimeDimensions timeDimensions;
    @Mock
    AllRegistrationMeasures allRegistrationMeasures;
    @Mock
    AllSMSSentMeasures allSMSSentMeasures;
    @Mock
    private SMSReferenceService smsReferenceService;
    @Mock
    private LocationDimensionService locationDimensionService;
    @Captor
    private ArgumentCaptor<List<SMSSentMeasure>> captor;

    @Before
    public void setUp() {
        initMocks(this);
        smsSentMeasureService = new SMSSentMeasureService(allSMSSentMeasures, frontLineWorkerService, smsReferenceService, frontLineWorkerDimensions, timeDimensions, allRegistrationMeasures, locationDimensionService);
    }

    @Test
    public void shouldCreateSMSSentMeasureWhenSMSIsSent() {
        String callerId = "9876543210";
        Integer courseAttemptNum = 1;
        String smsRefNum = "41413";
        String flwId = "77abcd";
        FrontLineWorkerDimension frontLineWorkerDimension = new FrontLineWorkerDimension(Long.valueOf(callerId), null, "", "", "", "", "", UUID.randomUUID(), null);
        frontLineWorkerDimension.setId(1);
        LocationDimension locationDimension = new LocationDimension("", "", "", "", "", "VALID");
        TimeDimension timeDimension = new TimeDimension(DateTime.now());

        when(frontLineWorkerService.getCurrentCourseAttempt(callerId)).thenReturn(courseAttemptNum);
        SMSReference smsReference = new SMSReference(callerId, flwId);
        smsReference.add(smsRefNum, courseAttemptNum);
        when(smsReferenceService.getSMSReferenceNumber(callerId)).thenReturn(smsReference);
        when(frontLineWorkerDimensions.fetchFor(Long.valueOf(callerId))).thenReturn(frontLineWorkerDimension);
        when(timeDimensions.getFor(any(DateTime.class))).thenReturn(timeDimension);
        RegistrationMeasure registrationMeasure = new RegistrationMeasure(frontLineWorkerDimension, locationDimension, timeDimension, "");
        when(allRegistrationMeasures.fetchFor(1)).thenReturn(registrationMeasure);
        mockAddFlwHistory();

        smsSentMeasureService.createSMSSentMeasure(callerId);

        InOrder inOrder = inOrder(allSMSSentMeasures, smsSentMeasureService);
        ArgumentCaptor<SMSSentMeasure> captor = ArgumentCaptor.forClass(SMSSentMeasure.class);

        inOrder.verify(smsSentMeasureService).addFlwHistory(captor.capture());
        inOrder.verify(allSMSSentMeasures).save(captor.capture());

        SMSSentMeasure smsSentMeasure = captor.getValue();
        assertEquals(smsSentMeasure.getSmsReferenceNumber(), smsRefNum);
        assertEquals(smsSentMeasure.getFrontLineWorkerDimension().getMsisdn(), Long.valueOf(callerId));
        assertTrue(smsSentMeasure.getSmsSent());
        assertEquals(smsSentMeasure.getCourseAttempt(), courseAttemptNum);
    }

    private void mockAddFlwHistory() {
        smsSentMeasureService = spy(smsSentMeasureService);
        doNothing().when((TransferableMeasureService) smsSentMeasureService).addFlwHistory(any(SMSSentMeasure.class));
    }

    @Test
    public void shouldCreateSMSSentMeasureWithUpdatedCourseAttemptWhenSMSIsNotSent() {
        String callerId = "9876543210";
        Integer courseAttemptNum = 1;
        String flwId = "77abcd";
        int flwd_id = 1;

        FrontLineWorkerDimension frontLineWorkerDimension = new FrontLineWorkerDimension(Long.valueOf(callerId), null, "", "", "", "", "", UUID.randomUUID(), null);
        frontLineWorkerDimension.setId(flwd_id);
        LocationDimension locationDimension = new LocationDimension("", "", "", "", "", "VALID");
        TimeDimension timeDimension = new TimeDimension(DateTime.now());
        SMSReference smsReference = new SMSReference(callerId, flwId);


        when(frontLineWorkerService.getCurrentCourseAttempt(callerId)).thenReturn(courseAttemptNum);
        when(smsReferenceService.getSMSReferenceNumber(callerId)).thenReturn(smsReference);
        when(frontLineWorkerDimensions.fetchFor(Long.valueOf(callerId))).thenReturn(frontLineWorkerDimension);
        when(timeDimensions.getFor(any(DateTime.class))).thenReturn(timeDimension);

        RegistrationMeasure registrationMeasure = new RegistrationMeasure(frontLineWorkerDimension, locationDimension, timeDimension, "");
        when(allRegistrationMeasures.fetchFor(flwd_id)).thenReturn(registrationMeasure);
        mockAddFlwHistory();

        smsSentMeasureService.createSMSSentMeasure(callerId);

        ArgumentCaptor<SMSSentMeasure> captor = ArgumentCaptor.forClass(SMSSentMeasure.class);
        verify(allSMSSentMeasures).save(captor.capture());

        SMSSentMeasure smsSentMeasure = captor.getValue();
        assertEquals(null, smsSentMeasure.getSmsReferenceNumber());
        assertEquals(Long.valueOf(callerId), smsSentMeasure.getFrontLineWorkerDimension().getMsisdn());
        assertFalse(smsSentMeasure.getSmsSent());
        assertEquals(smsSentMeasure.getCourseAttempt(), courseAttemptNum);
    }

    @Test
    public void shouldUpdateLocationOfAllSMSSentMeasuresWithACallerId() {
        long callerId = 1234L;
        String location_id = "location_id";
        final SMSSentMeasure smsSentMeasure = new SMSSentMeasure();
        ArrayList<SMSSentMeasure> smsSentMeasures = new ArrayList<SMSSentMeasure>() {{
            add(smsSentMeasure);
        }};
        LocationDimension expectedLocationDimension = new LocationDimension();
        when(locationDimensionService.getFor(location_id)).thenReturn(expectedLocationDimension);
        when(allSMSSentMeasures.findByCallerId(callerId)).thenReturn(smsSentMeasures);

        smsSentMeasureService.updateLocation(callerId, location_id);

        verify(allSMSSentMeasures).updateAll(captor.capture());
        List<SMSSentMeasure> actualSMSSentMeasures = captor.getValue();
        Assert.assertEquals(expectedLocationDimension, actualSMSSentMeasures.get(0).getLocationDimension());
    }

    @Test
    public void shouldUpdateLocationForAllSMSSentMeasures() {
        String newLocationId = "newLocationId";
        String oldLocationId = "oldLocationId";
        LocationDimension newLocation = new LocationDimension(newLocationId, "S1", "D2", "B2", "P2", "VALID");
        ArrayList<SMSSentMeasure> smsSentMeasures = new ArrayList<>();
        SMSSentMeasure smsSentMeasure = new SMSSentMeasure();
        smsSentMeasure.setLocationDimension(new LocationDimension(oldLocationId, "S1", "D1", "B1", "P1", "VALID"));
        smsSentMeasures.add(smsSentMeasure);
        when(allSMSSentMeasures.findByLocationId(oldLocationId)).thenReturn(smsSentMeasures);
        when(locationDimensionService.getFor(newLocationId)).thenReturn(newLocation);

        smsSentMeasureService.updateLocation(oldLocationId, newLocationId);

        verify(allSMSSentMeasures).updateAll(captor.capture());
        List<SMSSentMeasure> actualSMSSentMeasures = captor.getValue();
        assertEquals(1, actualSMSSentMeasures.size());
        assertEquals(newLocation, actualSMSSentMeasures.get(0).getLocationDimension());
    }

    @Test
    public void shouldTransferRecords() {
        FrontLineWorkerDimension fromFlw = new FrontLineWorkerDimension();
        fromFlw.setId(1);
        FrontLineWorkerDimension toFlw = new FrontLineWorkerDimension();
        toFlw.setId(2);
        smsSentMeasureService.transfer(fromFlw, toFlw);
        verify(allSMSSentMeasures).transfer(SMSSentMeasure.class, 1, 2);
    }
}
