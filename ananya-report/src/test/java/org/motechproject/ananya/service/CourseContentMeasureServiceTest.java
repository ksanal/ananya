package org.motechproject.ananya.service;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.motechproject.ananya.domain.CertificationCourseLog;
import org.motechproject.ananya.domain.CertificationCourseLogItem;
import org.motechproject.ananya.domain.CourseItemState;
import org.motechproject.ananya.domain.CourseItemType;
import org.motechproject.ananya.domain.dimension.CourseItemDimension;
import org.motechproject.ananya.domain.dimension.FrontLineWorkerDimension;
import org.motechproject.ananya.domain.dimension.LocationDimension;
import org.motechproject.ananya.domain.dimension.TimeDimension;
import org.motechproject.ananya.domain.measure.CourseItemMeasure;
import org.motechproject.ananya.domain.measure.RegistrationMeasure;
import org.motechproject.ananya.repository.dimension.AllCourseItemDimensions;
import org.motechproject.ananya.repository.dimension.AllFrontLineWorkerDimensions;
import org.motechproject.ananya.repository.dimension.AllLanguageDimension;
import org.motechproject.ananya.repository.dimension.AllTimeDimensions;
import org.motechproject.ananya.repository.measure.AllCourseItemMeasures;
import org.motechproject.ananya.repository.measure.AllRegistrationMeasures;
import org.motechproject.ananya.service.measure.CourseContentMeasureService;
import org.motechproject.ananya.service.measure.TransferableMeasureService;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class CourseContentMeasureServiceTest {

    @Mock
    private AllCourseItemMeasures allCourseItemMeasures;
    @Mock
    private AllTimeDimensions allTimeDimensions;
    @Mock
    private AllCourseItemDimensions allCourseItemDimensions;
    @Mock
    private AllLanguageDimension allLanguageDimension;
    @Mock
    private CertificateCourseLogService certificateCourseLogService;
    @Mock
    private AudioTrackerLogService audioTrackerLogService;
    @Mock
    private AllFrontLineWorkerDimensions allFrontLineWorkerDimensions;
    @Mock
    private AllRegistrationMeasures allRegistrationMeasures;

    private CourseContentMeasureService courseContentMeasureService;

    private String callId = "callId";
    private String callerId = "123456789";
    private String calledNumber = "1234";
    private DateTime now = DateTime.now();
    private int flwId = 1;

    private TimeDimension timeDimension = new TimeDimension();
    private FrontLineWorkerDimension frontLineWorkerDimension = new FrontLineWorkerDimension();
    private LocationDimension locationDimension = new LocationDimension();
    private RegistrationMeasure registrationMeasure;


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        registrationMeasure = new RegistrationMeasure(frontLineWorkerDimension, locationDimension, timeDimension, callId);
        frontLineWorkerDimension.setId(flwId);
        courseContentMeasureService = new CourseContentMeasureService(
                certificateCourseLogService, allTimeDimensions, allLanguageDimension, allCourseItemDimensions, allFrontLineWorkerDimensions,
                allRegistrationMeasures, allCourseItemMeasures);
    }

    @Test
    public void shouldSaveCourseItemMeasure() {
        String contentName = "Chapter 1";
        String contentId = "contentId";
        String language = "language";
        CourseItemType contentType = CourseItemType.CHAPTER;
        CourseItemState event = CourseItemState.START;

        CertificationCourseLog certificationCourseLog = new CertificationCourseLog(callerId, calledNumber, "", callId, "", language);
        certificationCourseLog.addCourseLogItem(new CertificationCourseLogItem(contentId, contentType, contentName, null, event, now, language));

        CourseItemDimension courseItemDimension = new CourseItemDimension();

        when(certificateCourseLogService.getLogFor(callId)).thenReturn(certificationCourseLog);
        when(allTimeDimensions.getFor(now)).thenReturn(timeDimension);
        when(allCourseItemDimensions.getFor(contentName, contentType)).thenReturn(courseItemDimension);
        when(allFrontLineWorkerDimensions.fetchFor(Long.valueOf(callerId))).thenReturn(frontLineWorkerDimension);
        when(allRegistrationMeasures.fetchFor(flwId)).thenReturn(registrationMeasure);
        mockAddFlwHistory();

        courseContentMeasureService.createFor(callId);

        ArgumentCaptor<CourseItemMeasure> captor = ArgumentCaptor.forClass(CourseItemMeasure.class);
        verify(allCourseItemMeasures).save(captor.capture());
        verify(certificateCourseLogService).remove(certificationCourseLog);

        CourseItemMeasure courseItemMeasure = captor.getValue();
        assertEquals(timeDimension, courseItemMeasure.getTimeDimension());
        assertEquals(frontLineWorkerDimension, courseItemMeasure.getFrontLineWorkerDimension());
        assertEquals(courseItemDimension, courseItemMeasure.getCourseItemDimension());
        assertEquals(event, courseItemMeasure.getEvent());
        assertEquals(null, courseItemMeasure.getScore());
    }

    private void mockAddFlwHistory() {
        courseContentMeasureService = spy(courseContentMeasureService);
        doNothing().when((TransferableMeasureService) courseContentMeasureService).addFlwHistory(any(CourseItemMeasure.class));
    }

    @Test
    public void shouldSaveCourseItemWithScore() {
        String contentName = "Chapter 1";
        String contentId = "contentId";
        String language = "language";
        CourseItemType contentType = CourseItemType.QUIZ;
        CourseItemState event = CourseItemState.START;

        CertificationCourseLog certificationCourseLog = new CertificationCourseLog(callerId, calledNumber, "", callId, "", language);
        certificationCourseLog.addCourseLogItem(new CertificationCourseLogItem(contentId, contentType, contentName, "3", event, now, language));
        CourseItemDimension courseItemDimension = new CourseItemDimension();

        when(certificateCourseLogService.getLogFor(callId)).thenReturn(certificationCourseLog);
        when(allTimeDimensions.getFor(now)).thenReturn(timeDimension);
        when(allCourseItemDimensions.getFor(contentName, contentType)).thenReturn(courseItemDimension);
        when(allFrontLineWorkerDimensions.fetchFor(Long.valueOf(callerId))).thenReturn(frontLineWorkerDimension);
        when(allRegistrationMeasures.fetchFor(flwId)).thenReturn(registrationMeasure);
        mockAddFlwHistory();

        courseContentMeasureService.createFor(callId);

        InOrder inOrder = inOrder(courseContentMeasureService, allCourseItemMeasures);
        ArgumentCaptor<CourseItemMeasure> captor = ArgumentCaptor.forClass(CourseItemMeasure.class);
        inOrder.verify(courseContentMeasureService).addFlwHistory(captor.capture());
        inOrder.verify(allCourseItemMeasures).save(captor.capture());
        verify(certificateCourseLogService).remove(certificationCourseLog);

        CourseItemMeasure courseItemMeasure = captor.getValue();
        assertEquals(Integer.valueOf(3), courseItemMeasure.getScore());
    }

    @Test
    public void shouldSaveMultipleCourseItem() {
        String contentName1 = "Chapter 1", contentId1 = "contentId1";
        CourseItemType contentType1 = CourseItemType.QUIZ;
        String contentName2 = "Chapter 2", contentId2 = "contentI21";
        CourseItemType contentType2 = CourseItemType.COURSE;
        String language = "language";
        CourseItemState event = CourseItemState.START;

        TimeDimension timeDimension = new TimeDimension();
        CourseItemDimension courseItemDimension1 = new CourseItemDimension();
        CourseItemDimension courseItemDimension2 = new CourseItemDimension();

        CertificationCourseLog certificationCourseLog = new CertificationCourseLog(callerId, calledNumber, "", callId, "", language);
        certificationCourseLog.addCourseLogItem(new CertificationCourseLogItem(contentId1, contentType1, contentName1, "3", event, now, language));
        certificationCourseLog.addCourseLogItem(new CertificationCourseLogItem(contentId2, contentType2, contentName2, "", event, now.plusDays(5), language));

        when(certificateCourseLogService.getLogFor(callId)).thenReturn(certificationCourseLog);
        when(allTimeDimensions.getFor(now)).thenReturn(timeDimension);
        when(allTimeDimensions.getFor(now.plusDays(5))).thenReturn(timeDimension);
        when(allCourseItemDimensions.getFor(contentName1, contentType1)).thenReturn(courseItemDimension1);
        when(allCourseItemDimensions.getFor(contentName2, contentType2)).thenReturn(courseItemDimension2);
        when(allFrontLineWorkerDimensions.fetchFor(Long.valueOf(callerId))).thenReturn(frontLineWorkerDimension);
        when(allRegistrationMeasures.fetchFor(flwId)).thenReturn(registrationMeasure);
        mockAddFlwHistory();

        courseContentMeasureService.createFor(callId);

        ArgumentCaptor<CourseItemMeasure> captor = ArgumentCaptor.forClass(CourseItemMeasure.class);
        verify(allCourseItemMeasures, times(2)).save(captor.capture());
        verify(certificateCourseLogService).remove(certificationCourseLog);

        List<CourseItemMeasure> allValues = captor.getAllValues();
        CourseItemMeasure courseItemMeasure1 = allValues.get(0);
        assertEquals(timeDimension, courseItemMeasure1.getTimeDimension());
        assertEquals(frontLineWorkerDimension, courseItemMeasure1.getFrontLineWorkerDimension());
        assertEquals(courseItemDimension1, courseItemMeasure1.getCourseItemDimension());
        assertEquals(event, courseItemMeasure1.getEvent());
        assertEquals(Integer.valueOf(3), courseItemMeasure1.getScore());

        CourseItemMeasure courseItemMeasure2 = allValues.get(1);
        assertEquals(timeDimension, courseItemMeasure2.getTimeDimension());
        assertEquals(frontLineWorkerDimension, courseItemMeasure2.getFrontLineWorkerDimension());
        assertEquals(courseItemDimension2, courseItemMeasure2.getCourseItemDimension());
        assertEquals(event, courseItemMeasure2.getEvent());
        assertEquals(null, courseItemMeasure2.getScore());
    }

    @Test
    public void shouldDoNothingWhenNoCertificateCourseLogIsPresentForACallId() {
        courseContentMeasureService.createFor("callId");
        verify(allCourseItemMeasures, never()).save(any(CourseItemMeasure.class));
    }

    @Test
    public void shouldDeleteCourseLogAndAudioTrackerLogAndReturnWhenItemsAreEmpty() {
        String contentName = "Chapter 1";
        String language = "language";
        CourseItemType contentType = CourseItemType.CHAPTER;
        CertificationCourseLog certificationCourseLog = new CertificationCourseLog(callerId, calledNumber, "", callId, "", language);

        when(certificateCourseLogService.getLogFor(callId)).thenReturn(certificationCourseLog);

        courseContentMeasureService.createFor(callId);

        verify(allTimeDimensions, never()).getFor(now);
        verify(allCourseItemDimensions, never()).getFor(contentName, contentType);
        verify(certificateCourseLogService).remove(certificationCourseLog);
    }
}
