package org.motechproject.ananya.handler;

import org.apache.log4j.Logger;
import org.motechproject.ananya.requests.LogData;
import org.motechproject.ananya.requests.LogType;
import org.motechproject.ananya.service.ReportPublisherService;
import org.motechproject.ananya.service.SMSPublisherService;
import org.motechproject.model.MotechEvent;
import org.motechproject.server.event.annotations.MotechListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class SMSSentHandler {
    private static final Logger LOG = Logger.getLogger(SendSMSHandler.class);


    private ReportPublisherService reportPublisherService;

    @Autowired
    public SMSSentHandler(ReportPublisherService reportPublisherService) {

        this.reportPublisherService = reportPublisherService;
    }

    @MotechListener(subjects = {SMSPublisherService.SUBJECT_SMS_SENT})
    public void publishSMSSent(MotechEvent motechEvent) {
        Map<String,Object> eventParams = motechEvent.getParameters();
        Map parameters = (Map) eventParams.get("0");
        String msisdn = (String) parameters.get(SMSPublisherService.PARAMETER_MSISDN);

        LogData logData = new LogData(LogType.SMS_SENT, msisdn);
        reportPublisherService.publishSMSSent(logData);
    }
}