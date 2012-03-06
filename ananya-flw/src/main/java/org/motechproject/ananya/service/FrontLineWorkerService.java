package org.motechproject.ananya.service;

import org.motechproject.ananya.domain.*;
import org.motechproject.ananya.exceptions.WorkerDoesNotExistException;
import org.motechproject.ananya.repository.AllFrontLineWorkers;
import org.motechproject.ananya.repository.AllLocations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class FrontLineWorkerService {
    public static final int CERTIFICATE_COURSE_PASSING_SCORE = 18;
    private static Logger log = LoggerFactory.getLogger(FrontLineWorkerService.class);

    private AllFrontLineWorkers allFrontLineWorkers;
    private AllLocations allLocations;

    @Autowired
    public FrontLineWorkerService(AllFrontLineWorkers allFrontLineWorkers, AllLocations allLocations) {
        this.allFrontLineWorkers = allFrontLineWorkers;
        this.allLocations = allLocations;
    }

    public RegistrationStatus getStatus(String msisdn) {
        FrontLineWorker frontLineWorker = getFrontLineWorker(msisdn);
        return frontLineWorker != null ? frontLineWorker.status() : RegistrationStatus.UNREGISTERED;
    }

    public boolean isCallerRegistered(String msisdn) {
        return getStatus(msisdn).isRegistered();
    }

    public String createNew(String msisdn, Designation designation, String panchayatCode, String operator) {
        Location location = allLocations.findByExternalId(panchayatCode);
        FrontLineWorker frontLineWorker = new FrontLineWorker(msisdn, designation, location.getId(), operator)
                                            .status(RegistrationStatus.PENDING_REGISTRATION);
        allFrontLineWorkers.add(frontLineWorker);
        return msisdn;
    }

    public FrontLineWorker getFrontLineWorker(String msisdn) {
        return allFrontLineWorkers.findByMsisdn(msisdn);
    }

    public void addBookMark(String callerId, BookMark bookMark){
        FrontLineWorker frontLineWorker = getFrontLineWorker(callerId);
        frontLineWorker.addBookMark(bookMark);
        save(frontLineWorker);
    }

    public void save(FrontLineWorker frontLineWorker) {
        allFrontLineWorkers.update(frontLineWorker);
    }

    public void addScore(String callerId, ReportCard.Score score) {
        FrontLineWorker frontLineWorker = getFrontLineWorker(callerId);
        frontLineWorker.reportCard().addScore(score);
        save(frontLineWorker);
    }

    public BookMark getBookmark(String msisdn) {
        FrontLineWorker frontLineWorker = getFrontLineWorker(msisdn);
        return frontLineWorker == null ? new EmptyBookmark() : frontLineWorker.bookMark();
    }

    public Map<String, Integer> scoresByChapter(String msisdn){
        final FrontLineWorker frontLineWorker = getFrontLineWorker(msisdn);
        
        return frontLineWorker == null? new HashMap() : frontLineWorker.reportCard().scoresByChapterIndex();
    }

    public void resetScoresForChapterIndex(String callerId, Integer chapterIndex) {
        final FrontLineWorker frontLineWorker = getFrontLineWorker(callerId);
        frontLineWorker.reportCard().clearScoresForChapterIndex(chapterIndex.toString());
        save(frontLineWorker);
    }

    public FrontLineWorker saveName(String msisdn, String name) throws WorkerDoesNotExistException {
        FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(msisdn);

        if (frontLineWorker == null)
            throw new WorkerDoesNotExistException();
        
        frontLineWorker.name(name);
        frontLineWorker.status(RegistrationStatus.REGISTERED);
        allFrontLineWorkers.update(frontLineWorker);

        return frontLineWorker;
    }
    
    public void resetScoresWhenStartingCertificateCourse(String msisdn) {
        final FrontLineWorker frontLineWorker = getFrontLineWorker(msisdn);
        if(frontLineWorker != null && !frontLineWorker.hasStartedCertificationCourse()) {
            resetAllScores(frontLineWorker);
        }
    }

    private void resetAllScores(FrontLineWorker frontLineWorker) {
        frontLineWorker.reportCard().clearAllScores();
        save(frontLineWorker);
    }

    public int totalScore(String msisdn) {
        int totalScore = 0;
        
        Collection<Integer> scores = this.scoresByChapter(msisdn).values();
        Iterator<Integer> scoresIterator = scores.iterator();
        while(scoresIterator.hasNext()) {
            totalScore += scoresIterator.next();
        }

        return totalScore;
    }

    public int incrementCertificateCourseAttempts(FrontLineWorker frontLineWorker) {
        int certificateCourseAttempts = frontLineWorker.incrementCertificateCourseAttempts();
        save(frontLineWorker);

        return certificateCourseAttempts;
    }

    public void addSMSReferenceNumber(String msisdn, String smsReferenceNumber) {
        FrontLineWorker frontLineWorker = getFrontLineWorker(msisdn);
        frontLineWorker.addSMSReferenceNumber(smsReferenceNumber);
        save(frontLineWorker);
    }

    public int getCurrentCourseAttempt(String msisdn) {
        FrontLineWorker frontLineWorker = getFrontLineWorker(msisdn);
        return frontLineWorker.currentCourseAttempt();
    }

    public String getSMSReferenceNumber(String msisdn, int courseAttempt) {
        FrontLineWorker frontLineWorker = getFrontLineWorker(msisdn);
        return frontLineWorker.smsReferenceNumber(courseAttempt);
    }
}
