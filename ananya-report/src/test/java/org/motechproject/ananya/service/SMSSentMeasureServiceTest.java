package org.motechproject.ananya.service;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ananya.domain.dimension.FrontLineWorkerDimension;
import org.motechproject.ananya.domain.dimension.TimeDimension;
import org.motechproject.ananya.domain.measure.SMSSentMeasure;
import org.motechproject.ananya.repository.ReportDB;
import org.motechproject.ananya.repository.dimension.AllFrontLineWorkerDimensions;
import org.motechproject.ananya.repository.dimension.AllTimeDimensions;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SMSSentMeasureServiceTest {

    private SMSSentMeasureService service;

    @Mock
    FrontLineWorkerService frontLineWorkerService;

    @Mock
    AllFrontLineWorkerDimensions frontLineWorkerDimensions;

    @Mock
    AllTimeDimensions timeDimensions;

    @Mock
    ReportDB db;

    @Before
    public void setUp() {
        initMocks(this);
        service = new SMSSentMeasureService(db, frontLineWorkerDimensions, timeDimensions, frontLineWorkerService);
    }

    @Test
    public void shouldCreateSMSSentMeasure() {
        String callerId = "9876543210";
        int courseAttemptNum = 1;
        String smsRefNum = "41413";

        when(frontLineWorkerService.getCurrentCourseAttempt(callerId)).thenReturn(courseAttemptNum);
        when(frontLineWorkerService.getSMSReferenceNumber(callerId, courseAttemptNum)).thenReturn(smsRefNum);
        when(frontLineWorkerDimensions.getOrMakeFor(Long.valueOf(callerId), "", "", "")).thenReturn(new FrontLineWorkerDimension(Long.valueOf(callerId), "", "", ""));
        when(timeDimensions.getFor(any(DateTime.class))).thenReturn(new TimeDimension(DateTime.now()));

        service.createSMSSentMeasure(callerId);

        ArgumentCaptor<SMSSentMeasure> captor = ArgumentCaptor.forClass(SMSSentMeasure.class);
        verify(db).add(captor.capture());

        SMSSentMeasure smsSentMeasure = captor.getValue();
        assertEquals(smsSentMeasure.getSmsReferenceNumber(), smsRefNum);
        assertEquals(smsSentMeasure.getFrontLineWorkerDimension().getMsisdn(), Long.valueOf(callerId));

    }
}