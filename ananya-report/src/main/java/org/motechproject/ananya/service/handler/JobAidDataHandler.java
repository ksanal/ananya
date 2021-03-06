package org.motechproject.ananya.service.handler;

import org.motechproject.ananya.requests.CallMessage;
import org.motechproject.ananya.requests.ReportPublishEventKeys;
import org.motechproject.ananya.service.measure.CallDurationMeasureService;
import org.motechproject.ananya.service.measure.JobAidContentMeasureService;
import org.motechproject.ananya.service.measure.RegistrationMeasureService;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.annotations.MotechListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobAidDataHandler {

    private static final Logger log = LoggerFactory.getLogger(JobAidDataHandler.class);
    private JobAidContentMeasureService jobAidContentMeasureService;
    private CallDurationMeasureService callDurationMeasureService;
    private RegistrationMeasureService registrationMeasureService;

    @Autowired
    public JobAidDataHandler(JobAidContentMeasureService jobAidContentMeasureService,
                             CallDurationMeasureService callDurationMeasureService,
                             RegistrationMeasureService registrationMeasureService) {
        
        this.jobAidContentMeasureService = jobAidContentMeasureService;
        this.callDurationMeasureService = callDurationMeasureService;
        this.registrationMeasureService = registrationMeasureService;
    }

    @MotechListener(subjects = {ReportPublishEventKeys.JOBAID_CALL_MESSAGE})
    public void handleJobAidData(MotechEvent event) {
        for (Object object : event.getParameters().values()) {
            if (!(object instanceof CallMessage)) {
                log.info("received unknown object: " + object.toString());
                continue;
            }

            CallMessage callMessage = (CallMessage) object;
            String callId = callMessage.getCallId();
            log.info("received jobaid message: " + callId);

            registrationMeasureService.createFor(callId);
            callDurationMeasureService.createFor(callId);
            jobAidContentMeasureService.createFor(callId);
        }
    }
}
