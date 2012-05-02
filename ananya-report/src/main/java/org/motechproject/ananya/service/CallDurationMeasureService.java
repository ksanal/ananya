package org.motechproject.ananya.service;

import org.motechproject.ananya.domain.CallLog;
import org.motechproject.ananya.domain.CallLogItem;
import org.motechproject.ananya.domain.dimension.FrontLineWorkerDimension;
import org.motechproject.ananya.domain.dimension.LocationDimension;
import org.motechproject.ananya.domain.measure.CallDurationMeasure;
import org.motechproject.ananya.domain.measure.RegistrationMeasure;
import org.motechproject.ananya.repository.ReportDB;
import org.motechproject.ananya.repository.dimension.AllFrontLineWorkerDimensions;
import org.motechproject.ananya.repository.measure.AllRegistrationMeasures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CallDurationMeasureService {
    private static Logger log = LoggerFactory.getLogger(CallDurationMeasureService.class);

    private CallLoggerService callLoggerService;
    private ReportDB reportDB;
    private AllFrontLineWorkerDimensions allFrontLineWorkerDimensions;
    private AllRegistrationMeasures allRegistrationMeasures;

    public CallDurationMeasureService() {
    }

    @Autowired
    public CallDurationMeasureService(CallLoggerService callLoggerService,
                                      ReportDB reportDB,
                                      AllFrontLineWorkerDimensions allFrontLineWorkerDimensions,
                                      AllRegistrationMeasures allRegistrationMeasures) {
        this.callLoggerService = callLoggerService;
        this.reportDB = reportDB;
        this.allFrontLineWorkerDimensions = allFrontLineWorkerDimensions;
        this.allRegistrationMeasures = allRegistrationMeasures;
    }



    @Transactional
    public void createCallDurationMeasure(String callId) {
        CallLog callLog = callLoggerService.getCallLogFor(callId);

        for (CallLogItem callLogItem : callLog.getCallLogItems()) {
            if (callLogItem.getStartTime() == null || callLogItem.getEndTime() == null)
                continue;

            Long callerId = callLog.callerIdAsLong();
            Long calledNumber = callLog.calledNumberAsLong();
            FrontLineWorkerDimension flwDimension = allFrontLineWorkerDimensions.fetchFor(callerId);
            RegistrationMeasure registrationMeasure = allRegistrationMeasures.fetchFor(flwDimension.getId());
            LocationDimension locationDimension = registrationMeasure.getLocationDimension();

            CallDurationMeasure callDurationMeasure = new CallDurationMeasure(
                    flwDimension,
                    locationDimension,
                    callId,
                    calledNumber,
                    callLogItem.duration(),
                    callLogItem.getStartTime(),
                    callLogItem.getEndTime(),
                    callLogItem.getCallFlowType().name());
            reportDB.add(callDurationMeasure);
        }
        callLoggerService.delete(callLog);
        log.info("Added CallDurationMeasures for callId=" + callId);
    }
}
