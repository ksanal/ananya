package org.motechproject.ananya.service;

import org.motechproject.ananya.domain.*;
import org.motechproject.ananya.repository.AllFrontLineWorkers;
import org.motechproject.ananya.repository.AllOperators;
import org.motechproject.ananya.request.CertificateCourseStateFlwRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FrontLineWorkerService {

    private static Logger log = LoggerFactory.getLogger(FrontLineWorkerService.class);

    private AllFrontLineWorkers allFrontLineWorkers;
    private SendSMSService sendSMSService;
    private SMSPublisherService smsPublisherService;

    @Autowired
    public FrontLineWorkerService(AllFrontLineWorkers allFrontLineWorkers, SendSMSService sendSMSService, SMSPublisherService smsPublisherService) {
        this.allFrontLineWorkers = allFrontLineWorkers;
        this.sendSMSService = sendSMSService;
        this.smsPublisherService = smsPublisherService;
    }

    public FrontLineWorker createNew(String msisdn, String name, Location location) {
       throw new RuntimeException("Not implemented");
    }

    public FrontLineWorker createNew(String msisdn, String operator) {
        FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(msisdn);
        if (frontLineWorker != null)
            return frontLineWorker;
        frontLineWorker = new FrontLineWorker(msisdn, operator).status(RegistrationStatus.PARTIALLY_REGISTERED);
        allFrontLineWorkers.add(frontLineWorker);
        return frontLineWorker;
    }

    public void addBookMark(String callerId, BookMark bookMark) {
        FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(callerId);
        frontLineWorker.addBookMark(bookMark);
        allFrontLineWorkers.update(frontLineWorker);
    }

    public FrontLineWorker findByCallerId(String callerId) {
        return allFrontLineWorkers.findByMsisdn(callerId);
    }

    public void addSMSReferenceNumber(String msisdn, String smsReferenceNumber) {
        FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(msisdn);
        frontLineWorker.addSMSReferenceNumber(smsReferenceNumber);
        allFrontLineWorkers.update(frontLineWorker);
        smsPublisherService.publishSMSSent(msisdn);
    }

    public int getCurrentCourseAttempt(String msisdn) {
        FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(msisdn);
        return frontLineWorker.currentCourseAttempt();
    }

    public String getSMSReferenceNumber(String msisdn, int courseAttempt) {
        FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(msisdn);
        return frontLineWorker.smsReferenceNumber(courseAttempt);
    }

    public void saveScore(CertificateCourseStateFlwRequest request) {
        String callId = request.getCallId();
        String callerId = request.getCallerId();
        Integer chapterIndex = request.getChapterIndex();
        Integer lessonOrQuestionIndex = request.getLessonOrQuestionIndex();
        Boolean result = request.getResult();

        if (request.isStartCertificationCourseInteraction()) {
            resetAllScores(callerId);
        } else if (request.isStartQuizInteraction()) {
            resetScoresForChapterIndex(callerId, chapterIndex);
        } else if (request.isPlayAnswerExplanationInteraction()) {
            final ReportCard.Score score = new ReportCard.Score(chapterIndex.toString(), lessonOrQuestionIndex.toString(), result, callId);
            addScore(callerId, score);
        } else if (request.isPlayCourseResultInteraction()) {
            FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(callerId);

            int totalScore = frontLineWorker.reportCard().totalScore();
            int currentCertificateCourseAttempts = incrementCertificateCourseAttempts(frontLineWorker);

            if (totalScore >= FrontLineWorker.CERTIFICATE_COURSE_PASSING_SCORE) {
                sendSMSService.buildAndSendSMS(callerId, frontLineWorker.getLocationId(), currentCertificateCourseAttempts);
            }
        }
    }

    public void updatePromptsForFLW(String msisdn, List<String> promptList) {
        FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(msisdn);
        for (String prompt : promptList)
            frontLineWorker.markPromptHeard(prompt);
        allFrontLineWorkers.update(frontLineWorker);
    }

    public void updateCurrentUsageForUser(String msisdn, Integer currentUsage) {
        FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(msisdn);
        frontLineWorker.setCurrentJobAidUsage(currentUsage);
        allFrontLineWorkers.update(frontLineWorker);
    }

    private void addScore(String callerId, ReportCard.Score score) {
        FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(callerId);
        frontLineWorker.reportCard().addScore(score);
        allFrontLineWorkers.update(frontLineWorker);
    }

    private void resetScoresForChapterIndex(String callerId, Integer chapterIndex) {
        final FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(callerId);
        frontLineWorker.reportCard().clearScoresForChapterIndex(chapterIndex.toString());
        allFrontLineWorkers.update(frontLineWorker);
    }

    private int incrementCertificateCourseAttempts(FrontLineWorker frontLineWorker) {
        int certificateCourseAttempts = frontLineWorker.incrementCertificateCourseAttempts();
        allFrontLineWorkers.update(frontLineWorker);
        return certificateCourseAttempts;
    }

    private void resetAllScores(String msisdn) {
        final FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(msisdn);
        if (frontLineWorker != null) {
            frontLineWorker.reportCard().clearAllScores();
            allFrontLineWorkers.update(frontLineWorker);
        }
    }

    public void updateLocation(String msisdn, Location location) {
        final FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(msisdn);
        frontLineWorker.updateLocation(location);
        allFrontLineWorkers.update(frontLineWorker);
    }
}
