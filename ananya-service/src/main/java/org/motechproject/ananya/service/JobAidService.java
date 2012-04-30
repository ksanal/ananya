package org.motechproject.ananya.service;

import org.motechproject.ananya.domain.AudioTrackerLog;
import org.motechproject.ananya.domain.FrontLineWorker;
import org.motechproject.ananya.domain.ServiceType;
import org.motechproject.ananya.mapper.AudioTrackerLogItemMapper;
import org.motechproject.ananya.request.AudioTrackerRequest;
import org.motechproject.ananya.request.AudioTrackerRequestList;
import org.motechproject.ananya.request.JobAidPromptRequest;
import org.motechproject.ananya.response.JobAidCallerDataResponse;
import org.motechproject.ananya.service.publish.DataPublishService;
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
    private AudioTrackerLogService audioTrackerLogService;

    @Autowired
    public JobAidService(FrontLineWorkerService frontLineWorkerService,
                         OperatorService operatorService,
                         DataPublishService dataPublishService,
                         AudioTrackerLogService audioTrackerLogService) {
        this.frontLineWorkerService = frontLineWorkerService;
        this.operatorService = operatorService;
        this.dataPublishService = dataPublishService;
        this.audioTrackerLogService = audioTrackerLogService;
    }

    public void updateJobAidPrompts(JobAidPromptRequest jobAidPromptRequest) {
        frontLineWorkerService.updatePromptsFor(
                jobAidPromptRequest.getCallerId(),
                jobAidPromptRequest.getPromptList());
    }

    public JobAidCallerDataResponse createCallerData(String callerId, String operator) {
        log.info("Creating caller data for msisdn: " + callerId + " for operator " + operator);

        boolean isNewFLW = frontLineWorkerService.isNewFLW(callerId);
        FrontLineWorker frontLineWorker = frontLineWorkerService.getFLWForJobAidCallerData(callerId, operator);

        if(isNewFLW) {
            dataPublishService.publishNewRegistration(callerId);
        }

        Integer currentJobAidUsage = frontLineWorker.getCurrentJobAidUsage();
        Integer allowedUsagePerMonthForOperator = operatorService.findMaximumUsageFor(operator);

        return new JobAidCallerDataResponse(
                frontLineWorker.status().isRegistered(),
                currentJobAidUsage,
                allowedUsagePerMonthForOperator,
                frontLineWorker.getPromptsHeard());
    }

    public void updateCurrentUsageAndSetLastAccessTimeForUser(String msisdn, Integer callDuration) {
        frontLineWorkerService.updateJobAidCurrentUsageFor(msisdn, callDuration);
        frontLineWorkerService.updateJobAidLastAccessTime(msisdn);
    }

    public void saveAudioTrackerState(AudioTrackerRequestList audioTrackerRequestList) {
        log.info("Audio Tracker Request List " + audioTrackerRequestList);
        if (audioTrackerRequestList.isEmpty()) return;

        AudioTrackerLog audioTrackerLog = createAudioTrackerLog(audioTrackerRequestList);

        audioTrackerLogService.createNew(audioTrackerLog);
    }

    private AudioTrackerLog createAudioTrackerLog(AudioTrackerRequestList audioTrackerRequestList) {
        AudioTrackerLog audioTrackerLog = new AudioTrackerLog(audioTrackerRequestList.getCallId(),
                audioTrackerRequestList.getCallerId(), ServiceType.JOB_AID);

        for (AudioTrackerRequest audioTrackerRequest : audioTrackerRequestList.getAll()) {
            audioTrackerLog.addItem(AudioTrackerLogItemMapper.mapFrom(audioTrackerRequest));
        }

        return audioTrackerLog;
    }

}
