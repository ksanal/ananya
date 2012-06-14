package org.motechproject.ananya.service;

import org.motechproject.ananya.domain.FrontLineWorker;
import org.motechproject.ananya.domain.RegistrationLog;
import org.motechproject.ananya.domain.ServiceType;
import org.motechproject.ananya.request.AudioTrackerRequestList;
import org.motechproject.ananya.request.JobAidPromptRequest;
import org.motechproject.ananya.response.JobAidCallerDataResponse;
import org.motechproject.ananya.service.publish.DataPublishService;
import org.motechproject.ananya.util.PerformanceCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobAidService {

    private static Logger log = LoggerFactory.getLogger(JobAidService.class);

    private FrontLineWorkerService frontLineWorkerService;
    private OperatorService operatorService;
    private DataPublishService dataPublishService;
    private AudioTrackerService audioTrackerService;
    private RegistrationLogService registrationLogService;

    @Autowired
    public JobAidService(FrontLineWorkerService frontLineWorkerService,
                         OperatorService operatorService,
                         DataPublishService dataPublishService,
                         AudioTrackerService audioTrackerService,
                         RegistrationLogService registrationLogService) {
        this.frontLineWorkerService = frontLineWorkerService;
        this.operatorService = operatorService;
        this.dataPublishService = dataPublishService;
        this.audioTrackerService = audioTrackerService;
        this.registrationLogService = registrationLogService;
    }

    public void updateJobAidPrompts(JobAidPromptRequest jobAidPromptRequest) {
        frontLineWorkerService.updatePromptsFor(
                jobAidPromptRequest.getCallerId(),
                jobAidPromptRequest.getPromptList());
    }

    public JobAidCallerDataResponse createCallerData(String callId, String callerId, String operator, String circle) {
        log.info("Creating caller data for msisdn: " + callerId + " for operator " + operator + " for circle " + circle);

        FrontLineWorker frontLineWorker = frontLineWorkerService.findForJobAidCallerData(callerId, operator, circle);
        if (frontLineWorker.isModified())
            registrationLogService.add(new RegistrationLog(callId, callerId, operator, circle));

        return new JobAidCallerDataResponse(
                frontLineWorker.status().isRegistered(),
                frontLineWorker.getCurrentJobAidUsage(),
                operatorService.findMaximumUsageFor(operator),
                frontLineWorker.getPromptsHeard());
    }

    public void updateCurrentUsageAndSetLastAccessTimeForUser(String callerId, Integer callDuration) {
        frontLineWorkerService.updateJobAidUsageAndAccessTime(callerId, callDuration);
    }

    public void saveAudioTrackerState(AudioTrackerRequestList audioTrackerRequestList) {
        audioTrackerService.saveAudioTrackerState(audioTrackerRequestList, ServiceType.JOB_AID);
    }
}

