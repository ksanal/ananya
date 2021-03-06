package org.motechproject.ananya.action;

import org.apache.commons.lang.StringUtils;
import org.motechproject.ananya.domain.FrontLineWorker;
import org.motechproject.ananya.domain.Interaction;
import org.motechproject.ananya.domain.Score;
import org.motechproject.ananya.contract.CertificateCourseStateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ScoreActionInteraction {

    StartCourse(Interaction.StartCertificationCourse) {
        @Override
        public void process(FrontLineWorker frontLineWorker, CertificateCourseStateRequest stateRequest) {
            frontLineWorker.reportCard().clearAllScores();
            log.info("course started, cleared all scores for " + frontLineWorker);
        }
    },
    StartQuiz(Interaction.StartQuiz) {
        @Override
        public void process(FrontLineWorker frontLineWorker, CertificateCourseStateRequest stateRequest) {
            String chapterIndex = stateRequest.getChapterIndex().toString();
            frontLineWorker.reportCard().clearScoresForChapterIndex(chapterIndex);
            log.info("quiz started, cleared scores for chapter " + chapterIndex + " for " + frontLineWorker);
        }
    },
    PlayAnswerExplanation(Interaction.PlayAnswerExplanation) {
        @Override
        public void process(FrontLineWorker frontLineWorker, CertificateCourseStateRequest stateRequest) {
            Score score = new Score(
                    stateRequest.getChapterIndex().toString(),
                    stateRequest.getLessonOrQuestionIndex().toString(),
                    stateRequest.getResult(),
                    stateRequest.getCallId());
            frontLineWorker.reportCard().addScore(score);
            log.info("played answer explanation, added scores for chapter for " + frontLineWorker);
        }
    },
    Default("") {
        @Override
        public void process(FrontLineWorker frontLineWorker, CertificateCourseStateRequest stateRequest) {
            //do nothing
        }
    };

    private static Logger log = LoggerFactory.getLogger(ScoreActionInteraction.class);
    private String interactionKey;

    ScoreActionInteraction(String interactionKey) {
        this.interactionKey = interactionKey;
    }

    public static ScoreActionInteraction findFor(String interactionKey) {
        for (ScoreActionInteraction scoreActionInteraction : ScoreActionInteraction.values())
            if (StringUtils.endsWithIgnoreCase(scoreActionInteraction.interactionKey, interactionKey))
                return scoreActionInteraction;
        return ScoreActionInteraction.Default;
    }

    public abstract void process(FrontLineWorker frontLineWorker, CertificateCourseStateRequest stateRequest);

}
