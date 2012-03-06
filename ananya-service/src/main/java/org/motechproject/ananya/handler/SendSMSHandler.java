package org.motechproject.ananya.handler;

import org.apache.log4j.Logger;
import org.motechproject.ananya.webservice.SendSMSClient;
import org.motechproject.model.MotechEvent;
import org.motechproject.server.event.annotations.MotechListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class SendSMSHandler {
    private static final Logger LOG = Logger.getLogger(SendSMSHandler.class);

    public static final String SUBJECT_SEND_SINGLE_SMS = "sendSingleSMS";
    public static final String PARAMETER_SMS_MESSAGE = "smsMessage";
    public static final String PARAMETER_MOBILE_NUMBER = "mobileNumber";
    public static final String PARAMETER_SMS_REFERENCE_NUMBER = "smsReferenceNumber";

//    private static int attempt = 0;
    private SendSMSClient smsClient;

    @Autowired
    public SendSMSHandler(SendSMSClient smsClient) {
        this.smsClient = smsClient;
    }

    @MotechListener(subjects = {SUBJECT_SEND_SINGLE_SMS})
    public void sendSingleSMS(MotechEvent motechEvent) throws IOException {
        Map<String,Object> eventParams = motechEvent.getParameters();
        Map parameters = (Map) eventParams.get("0");
        String smsMessage = (String) parameters.get(PARAMETER_SMS_MESSAGE);
        String mobileNumber = (String) parameters.get(PARAMETER_MOBILE_NUMBER);
        String smsReferenceNumber = (String) parameters.get(PARAMETER_SMS_REFERENCE_NUMBER);

//        FileWriter file= new FileWriter("mock-handler-log.txt",true);
//        BufferedWriter bw = new BufferedWriter(file);
//        bw.append(smsMessage + " | attempt: " + (++attempt) + "\r\n");
//        bw.close();
        smsClient.sendSingleSMS(mobileNumber, smsMessage, smsReferenceNumber);
    }
}
