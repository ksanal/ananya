package org.motechproject.ananya.service;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ananya.domain.CertificationCourseLog;
import org.motechproject.ananya.domain.CertificationCourseLogItem;
import org.motechproject.ananya.domain.CourseItemState;
import org.motechproject.ananya.domain.CourseItemType;
import org.motechproject.ananya.domain.dimension.CourseItemDimension;
import org.motechproject.ananya.domain.dimension.FrontLineWorkerDimension;
import org.motechproject.ananya.domain.dimension.TimeDimension;
import org.motechproject.ananya.domain.measure.CourseItemMeasure;
import org.motechproject.ananya.repository.ReportDB;
import org.motechproject.ananya.repository.dimension.AllCourseItemDimensions;
import org.motechproject.ananya.repository.dimension.AllFrontLineWorkerDimensions;
import org.motechproject.ananya.repository.dimension.AllTimeDimensions;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class CourseItemMeasureServiceTest {

    @Mock
    private ReportDB reportDB;
    @Mock
    private AllFrontLineWorkerDimensions allFrontLineWorkerDimensions;
    @Mock
    private AllTimeDimensions allTimeDimensions;
    @Mock
    private AllCourseItemDimensions allCourseItemDimensions;
    @Mock
    private CertificateCourseLogService certificateCourseLogService;

    CourseItemMeasureService courseItemMeasureService;
    String callId;
    String callerId;
    String calledNumber;
    DateTime now;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        callId = "callId";
        callerId = "123456789";
        calledNumber = "1234";
        now = DateTime.now();
        courseItemMeasureService = new CourseItemMeasureService(reportDB, allFrontLineWorkerDimensions, allTimeDimensions, allCourseItemDimensions, certificateCourseLogService);
    }

    @Test
    public void shouldSaveCourseItemMeasure() {
        String contentName = "Chapter 1";
        String contentId = "contentId";
        CourseItemType contentType = CourseItemType.CHAPTER;
        CourseItemState event = CourseItemState.START;
        CertificationCourseLog certificationCourseLog = new CertificationCourseLog(callerId, calledNumber, null, null, "", callId, "");
        certificationCourseLog.addCourseLogItem(new CertificationCourseLogItem(contentId, contentType, contentName, null, event, now));
        TimeDimension timeDimension = new TimeDimension();
        FrontLineWorkerDimension frontLineWorkerDimension = new FrontLineWorkerDimension();
        CourseItemDimension courseItemDimension = new CourseItemDimension();

        when(certificateCourseLogService.getCertificateCourseLogFor(callId)).thenReturn(certificationCourseLog);
        when(allTimeDimensions.getFor(now)).thenReturn(timeDimension);
        when(allFrontLineWorkerDimensions.fetchFor(Long.valueOf(callerId))).thenReturn(frontLineWorkerDimension);
        when(allCourseItemDimensions.getFor(contentName, contentType)).thenReturn(courseItemDimension);

        courseItemMeasureService.createCourseItemMeasure(callId);

        ArgumentCaptor<CourseItemMeasure> captor = ArgumentCaptor.forClass(CourseItemMeasure.class);
        verify(reportDB).add(captor.capture());

        CourseItemMeasure courseItemMeasure = captor.getValue();
        assertEquals(timeDimension, courseItemMeasure.getTimeDimension());
        assertEquals(frontLineWorkerDimension, courseItemMeasure.getFrontLineWorkerDimension());
        assertEquals(courseItemDimension, courseItemMeasure.getCourseItemDimension());
        assertEquals(event, courseItemMeasure.getEvent());
        assertEquals(null, courseItemMeasure.getScore());
    }

    @Test
    public void shouldSaveCourseItemWithScore() {
        String contentName = "Chapter 1";
        String contentId = "contentId";
        CourseItemType contentType = CourseItemType.QUIZ;
        CourseItemState event = CourseItemState.START;
        CertificationCourseLog certificationCourseLog = new CertificationCourseLog(callerId, calledNumber, null, null, "", callId, "");
        certificationCourseLog.addCourseLogItem(new CertificationCourseLogItem(contentId, contentType, contentName, "3", event, now));
        TimeDimension timeDimension = new TimeDimension();
        FrontLineWorkerDimension frontLineWorkerDimension = new FrontLineWorkerDimension();
        CourseItemDimension courseItemDimension = new CourseItemDimension();

        when(certificateCourseLogService.getCertificateCourseLogFor(callId)).thenReturn(certificationCourseLog);
        when(allTimeDimensions.getFor(now)).thenReturn(timeDimension);
        when(allFrontLineWorkerDimensions.fetchFor(Long.valueOf(callerId))).thenReturn(frontLineWorkerDimension);
        when(allCourseItemDimensions.getFor(contentName, contentType)).thenReturn(courseItemDimension);

        courseItemMeasureService.createCourseItemMeasure(callId);

        ArgumentCaptor<CourseItemMeasure> captor = ArgumentCaptor.forClass(CourseItemMeasure.class);
        verify(reportDB).add(captor.capture());

        CourseItemMeasure courseItemMeasure = captor.getValue();
        assertEquals(Integer.valueOf(3), courseItemMeasure.getScore());
    }

    @Test
    public void shouldSaveMultipleCourseItem() {
        String contentName1 = "Chapter 1", contentId1 = "contentId1";
        CourseItemType contentType1 = CourseItemType.QUIZ;
        String contentName2 = "Chapter 2", contentId2 = "contentI21";
        CourseItemType contentType2 = CourseItemType.COURSE;
        CourseItemState event = CourseItemState.START;

        TimeDimension timeDimension1 = new TimeDimension();
        TimeDimension timeDimension2 = new TimeDimension();
        FrontLineWorkerDimension frontLineWorkerDimension = new FrontLineWorkerDimension();
        CourseItemDimension courseItemDimension1 = new CourseItemDimension();
        CourseItemDimension courseItemDimension2 = new CourseItemDimension();

        CertificationCourseLog certificationCourseLog = new CertificationCourseLog(callerId, calledNumber, null, null, "", callId, "");
        certificationCourseLog.addCourseLogItem(new CertificationCourseLogItem(contentId1, contentType1, contentName1, "3", event, now));
        certificationCourseLog.addCourseLogItem(new CertificationCourseLogItem(contentId2, contentType2, contentName2, "", event, now.plusDays(5)));


        when(certificateCourseLogService.getCertificateCourseLogFor(callId)).thenReturn(certificationCourseLog);
        when(allTimeDimensions.getFor(now)).thenReturn(timeDimension1);
        when(allTimeDimensions.getFor(now.plusDays(5))).thenReturn(timeDimension2);
        when(allFrontLineWorkerDimensions.fetchFor(Long.valueOf(callerId))).thenReturn(frontLineWorkerDimension);
        when(allCourseItemDimensions.getFor(contentName1, contentType1)).thenReturn(courseItemDimension1);
        when(allCourseItemDimensions.getFor(contentName2, contentType2)).thenReturn(courseItemDimension2);

        courseItemMeasureService.createCourseItemMeasure(callId);

        ArgumentCaptor<CourseItemMeasure> captor = ArgumentCaptor.forClass(CourseItemMeasure.class);
        verify(reportDB, times(2)).add(captor.capture());

        List<CourseItemMeasure> allValues = captor.getAllValues();
        CourseItemMeasure courseItemMeasure1 = allValues.get(0);
        assertEquals(timeDimension1, courseItemMeasure1.getTimeDimension());
        assertEquals(frontLineWorkerDimension, courseItemMeasure1.getFrontLineWorkerDimension());
        assertEquals(courseItemDimension1, courseItemMeasure1.getCourseItemDimension());
        assertEquals(event, courseItemMeasure1.getEvent());
        assertEquals(Integer.valueOf(3), courseItemMeasure1.getScore());

        CourseItemMeasure courseItemMeasure2 = allValues.get(1);
        assertEquals(timeDimension2, courseItemMeasure2.getTimeDimension());
        assertEquals(frontLineWorkerDimension, courseItemMeasure2.getFrontLineWorkerDimension());
        assertEquals(courseItemDimension2, courseItemMeasure2.getCourseItemDimension());
        assertEquals(event, courseItemMeasure2.getEvent());
        assertEquals(null, courseItemMeasure2.getScore());
    }

    @Test
    public void shouldDeleteCertificateCourseLogAfterSavingTheCourseItemMeasure() {
        String contentName = "Chapter 1";
        String contentId = "contentId";
        CourseItemType contentType = CourseItemType.CHAPTER;
        CourseItemState event = CourseItemState.START;
        CertificationCourseLog certificationCourseLog = new CertificationCourseLog(callerId, calledNumber, null, null, "", callId, "");
        certificationCourseLog.addCourseLogItem(new CertificationCourseLogItem(contentId, contentType, contentName, null, event, now));
        TimeDimension timeDimension = new TimeDimension();
        FrontLineWorkerDimension frontLineWorkerDimension = new FrontLineWorkerDimension();
        CourseItemDimension courseItemDimension = new CourseItemDimension();

        when(certificateCourseLogService.getCertificateCourseLogFor(callId)).thenReturn(certificationCourseLog);
        when(allTimeDimensions.getFor(now)).thenReturn(timeDimension);
        when(allFrontLineWorkerDimensions.fetchFor(Long.valueOf(callerId))).thenReturn(frontLineWorkerDimension);
        when(allCourseItemDimensions.getFor(contentName, contentType)).thenReturn(courseItemDimension);

        courseItemMeasureService.createCourseItemMeasure(callId);

        verify(certificateCourseLogService).deleteCertificateCourseLogsFor(callId);
    }

    @Test
    public void shouldDoNothingWhenNoCertificateCourseLogIsPresentForACallId() {
        when(certificateCourseLogService.getCertificateCourseLogFor(callId)).thenReturn(null);
        courseItemMeasureService.createCourseItemMeasure("callId");
        verify(reportDB, never()).add(any(CourseItemMeasure.class));

    }
}
