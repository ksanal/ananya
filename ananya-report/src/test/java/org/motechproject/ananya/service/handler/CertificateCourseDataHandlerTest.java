package org.motechproject.ananya.service.handler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.ananya.domain.RegistrationLog;
import org.motechproject.ananya.domain.SMSLog;
import org.motechproject.ananya.requests.CallMessage;
import org.motechproject.ananya.requests.CallMessageType;
import org.motechproject.ananya.service.*;
import org.motechproject.model.MotechEvent;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CertificateCourseDataHandlerTest {

    private CertificateCourseDataHandler handler;
    @Mock
    private CourseItemMeasureService courseItemMeasureService;
    @Mock
    private RegistrationMeasureService registrationMeasureService;
    @Mock
    private CallDurationMeasureService callDurationMeasureService;
    @Mock
    private RegistrationLogService registrationLogService;
    @Mock
    private SMSLogService smsLogService;
    @Mock
    private SendSMSService sendSMSService;

    @Before
    public void setUp() {
        initMocks(this);
        handler = new CertificateCourseDataHandler(courseItemMeasureService,
                callDurationMeasureService,
                registrationMeasureService, registrationLogService, smsLogService, sendSMSService);
    }

    @Test
    public void shouldHandleCertificateCourseData() {
        String callId = "callId";
        String callerId = "callerId";
        CallMessage logData = new CallMessage(CallMessageType.CERTIFICATE_COURSE_DATA, callId, callerId);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", logData);
        MotechEvent event = new MotechEvent("", map);

        when(registrationLogService.getRegistrationLogFor(callerId)).thenReturn(new RegistrationLog(callerId, "", ""));
        when(smsLogService.getSMSLogFor(callId)).thenReturn(new SMSLog(callId, callerId, "location", 1));

        handler.handleCertificateCourseData(event);

        verify(registrationMeasureService).createOrUpdateFor(callerId);
        verify(courseItemMeasureService).createCourseItemMeasure(callId);
        verify(callDurationMeasureService).createCallDurationMeasure(callId);
        verify(sendSMSService).buildAndSendSMS(callerId, "location", 1);
    }
}