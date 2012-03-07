package org.motechproject.ananya.service;

import org.apache.commons.lang.StringUtils;
import org.motechproject.ananya.domain.*;
import org.motechproject.ananya.repository.AllCertificateCourseLogs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CertificateCourseService {
    private AllCertificateCourseLogs allCertificateCourseLogs;
    private FrontLineWorkerService frontLineWorkerService;
    private SendSMSService sendSMSService;

    @Autowired
    public CertificateCourseService(
            AllCertificateCourseLogs allCertificateCourseLogs, FrontLineWorkerService frontLineWorkerService, SendSMSService sendSMSService) {
        this.allCertificateCourseLogs = allCertificateCourseLogs;
        this.frontLineWorkerService = frontLineWorkerService;
        this.sendSMSService = sendSMSService;
    }

    public void saveBookmark(CertificationCourseStateRequest courseStateRequest) {
        final BookMark bookMark = new BookMark(courseStateRequest.getInteractionKey(),
                courseStateRequest.getChapterIndex(), courseStateRequest.getLessonOrQuestionIndex());

        frontLineWorkerService.addBookMark(courseStateRequest.getCallerId(), bookMark);
    }

    private void saveScores(final CertificationCourseStateRequest courseStateRequest) {
        final Integer chapterIndex = courseStateRequest.getChapterIndex();
        final Integer lessonOrQuestionIndex = courseStateRequest.getLessonOrQuestionIndex();
        final Boolean result = courseStateRequest.isResult();
        final String callerId = courseStateRequest.getCallerId();

        String interactionKey = courseStateRequest.getInteractionKey();
        if("startQuiz".equals(interactionKey)) {
            frontLineWorkerService.resetScoresForChapterIndex(callerId, chapterIndex);

        } else if ("playAnswerExplanation".equals(interactionKey)) {
            final ReportCard.Score score = new ReportCard.Score(chapterIndex.toString(), lessonOrQuestionIndex.toString(), result, courseStateRequest.getCallId());
            frontLineWorkerService.addScore(callerId, score);

        } else if ("playCourseResult".equals(interactionKey)) {
            FrontLineWorker frontLineWorker = frontLineWorkerService.getFrontLineWorker(callerId);
            int totalScore = frontLineWorkerService.totalScore(callerId);
            int currentCertificateCourseAttempts = frontLineWorkerService.incrementCertificateCourseAttempts(frontLineWorker);

            if(totalScore >= FrontLineWorkerService.CERTIFICATE_COURSE_PASSING_SCORE) {
                sendSMSService.buildAndSendSMS(callerId, frontLineWorker.getLocationId(), currentCertificateCourseAttempts);
            }
        }
    }

    public void saveState(List<CertificationCourseStateRequest> certificationCourseStateRequestCollection) {

        if (certificationCourseStateRequestCollection == null || certificationCourseStateRequestCollection.size() == 0)
            return;

        CertificationCourseLog courseLogDocument;
        CertificationCourseStateRequest courseStateRequest = certificationCourseStateRequestCollection.get(0);
        final String callId = courseStateRequest.getCallId();

        courseLogDocument = allCertificateCourseLogs.findByCallId(callId);
        if (courseLogDocument == null) {
            courseLogDocument = new CertificationCourseLog(courseStateRequest.getCallerId(),
                    courseStateRequest.getCalledNumber(), null, null, "", callId, courseStateRequest.getCertificateCourseId()
            );
            allCertificateCourseLogs.add(courseLogDocument);
        }

        for (CertificationCourseStateRequest certificationCourseStateRequest : certificationCourseStateRequestCollection) {
            saveScores(certificationCourseStateRequest);

            // Only a bookmark, not a log item. continue
            if (!StringUtils.isBlank(certificationCourseStateRequest.getContentId())) {
                CertificationCourseLogItem courseLogItem = new CertificationCourseLogItem(
                        certificationCourseStateRequest.getContentId(),
                        CourseItemType.valueOf(certificationCourseStateRequest.getContentType().toUpperCase()),
                        certificationCourseStateRequest.getContentName(),
                        certificationCourseStateRequest.getContentData(),
                        CourseItemState.valueOf(certificationCourseStateRequest.getCourseItemState().toUpperCase()),
                        certificationCourseStateRequest.getTimeAsDateTime()
                );

                courseLogDocument.addCourseLogItem(courseLogItem);
            }
        }
        allCertificateCourseLogs.update(courseLogDocument);

        CertificationCourseStateRequest recentCourseRequest =
                certificationCourseStateRequestCollection.get(certificationCourseStateRequestCollection.size() - 1);
        saveBookmark(recentCourseRequest);
    }

    public CertificationCourseLog getCertificateCourseLogFor(String callId) {
        return allCertificateCourseLogs.findByCallId(callId);
    }

    public void deleteCertificateCourseLogsFor(String callId) {
        allCertificateCourseLogs.deleteFor(callId);
    }
}