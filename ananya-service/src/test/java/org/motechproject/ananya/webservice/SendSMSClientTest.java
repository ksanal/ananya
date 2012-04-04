package org.motechproject.ananya.webservice;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ananya.domain.FrontLineWorker;
import org.motechproject.ananya.domain.SMSReference;
import org.motechproject.ananya.service.FrontLineWorkerService;
import org.motechproject.ananya.service.SMSPublisherService;

import static junit.framework.Assert.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class SendSMSClientTest {

    private SendSMSClient sendSMSClient;

    @Mock
    private OnMobileSendSMSService onMobileSendSMSService;

    @Mock
    private FrontLineWorkerService frontLineWorkerService;

    @Mock
    private SMSPublisherService smsPublisherService;

    @Before
    public void setUp() {
        initMocks(this);
        sendSMSClient = new SendSMSClient(onMobileSendSMSService, frontLineWorkerService, smsPublisherService);
    }

    @Test
    public void shouldSendSingleSMSAndAddTheSMSReferenceNumber() {
        String mobileNumber = "9876543210";
        String smsMessage = "Hello";
        String smsRefNum = "141241";
        SMSReference smsReference = new SMSReference(mobileNumber, smsRefNum);
        FrontLineWorker frontLineWorker = new FrontLineWorker(mobileNumber, "airtel");
        when(onMobileSendSMSService.singlePush(argThat(is(mobileNumber)), argThat(is(SendSMSClient.SENDER_ID)), argThat(is(smsRefNum)))).thenReturn("success");
        when(frontLineWorkerService.findByCallerId(mobileNumber)).thenReturn(frontLineWorker);
        when(frontLineWorkerService.getSMSReferenceNumber(mobileNumber)).thenReturn(null);

        sendSMSClient.sendSingleSMS(mobileNumber, smsMessage, smsRefNum);

        ArgumentCaptor<SMSReference> captor = ArgumentCaptor.forClass(SMSReference.class);
        verify(frontLineWorkerService).addSMSReferenceNumber(captor.capture());
        SMSReference value = captor.getValue();
        assertEquals(mobileNumber, value.getMsisdn());
        verify(smsPublisherService).publishSMSSent(mobileNumber);
    }

    @Test
    public void shouldSendSingleSMSAndUpdateTheSMSReferenceNumber() {
        String mobileNumber = "9876543210";
        String smsMessage = "Hello";
        String smsRefNum = "141241";
        SMSReference smsReference = new SMSReference(mobileNumber, smsRefNum);
        FrontLineWorker frontLineWorker = new FrontLineWorker(mobileNumber, "airtel");
        when(onMobileSendSMSService.singlePush(argThat(is(mobileNumber)), argThat(is(SendSMSClient.SENDER_ID)), argThat(is(smsRefNum)))).thenReturn("success");
        when(frontLineWorkerService.findByCallerId(mobileNumber)).thenReturn(frontLineWorker);
        when(frontLineWorkerService.getSMSReferenceNumber(mobileNumber)).thenReturn(smsReference);

        sendSMSClient.sendSingleSMS(mobileNumber, smsMessage, smsRefNum);

        ArgumentCaptor<SMSReference> captor = ArgumentCaptor.forClass(SMSReference.class);
        verify(frontLineWorkerService).updateSMSReferenceNumber(captor.capture());
        SMSReference value = captor.getValue();
        assertEquals(mobileNumber, value.getMsisdn());
        assertEquals(smsReference, value);
        verify(smsPublisherService).publishSMSSent(mobileNumber);
    }

    @Test
    public void shouldThrowRuntimeExceptionWhenSendSingleSMSFails() {
        String mobileNumber = "9876543210";
        String smsMessage = "Hello";
        String smsRefNum = "141241";

        when(onMobileSendSMSService.singlePush(argThat(is(mobileNumber)), argThat(is(SendSMSClient.SENDER_ID)), argThat(is(smsMessage)))).thenReturn("failure");
        try {
            sendSMSClient.sendSingleSMS(mobileNumber, smsMessage, smsRefNum);
        } catch (RuntimeException e) {
            assertTrue(true);
            return;
        }

        assertFalse(true);
    }

}