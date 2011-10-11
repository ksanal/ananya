package org.motechproject.bbcwt.domain;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.ektorp.support.TypeDiscriminator;

import java.util.ArrayList;
import java.util.List;

@TypeDiscriminator("doc.documentType == 'Chapter'")
public class Chapter extends BaseCouchEntity {
    private int number;

    private List<Lesson> lessons = new ArrayList<Lesson>();
    private List<Question> questions = new ArrayList<Question>();
    private String goodScoreSummary;
    private String belowParScoreSummary;
    private String certificateAndCourseSummaryPrompt;
    private String courseSummaryPrompt;

    private static final int GOOD_SCORE = 3;

    public Chapter() {
    }

    public Chapter(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public Lesson getLessonByNumber(final int number) {
        return (Lesson)CollectionUtils.find(this.getLessons(), new Predicate() {
            @Override
            public boolean evaluate(Object lesson) {
                return number == ((Lesson)lesson).getNumber();
            }
        });
    }

    public Lesson getLessonById(final String lessonId) {
        return (Lesson)CollectionUtils.find(lessons, new Predicate() {
            @Override
            public boolean evaluate(Object lesson) {
                return StringUtils.equals(lessonId, ((Lesson)lesson).getId());
            }
        });
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public Question getQuestionByNumber(final int number) {
        return (Question)CollectionUtils.find(this.getQuestions(), new Predicate() {
            @Override
            public boolean evaluate(Object question) {
                return  number == ((Question)question).getNumber();
            }
        });
    }

    public Question getQuestionById(final String questionId) {
        return (Question)CollectionUtils.find(this.getQuestions(), new Predicate() {
            @Override
            public boolean evaluate(Object question) {
                return StringUtils.equals(questionId, ((Question)question).getId());
            }
        });
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public Lesson nextLesson(Lesson lesson) {
        if(lessons.contains(lesson)) {
            return this.getLessonByNumber(lesson.getNumber()+1);
        }
        return null;
    }

    public Question nextQuestion(Question question) {
        if(questions.contains(question)) {
            return this.getQuestionByNumber(question.getNumber() + 1);
        }
        return null;
    }

    public boolean hasQuestions() {
        return questions.size() > 0;
    }

    public String getGoodScoreSummary() {
        return goodScoreSummary;
    }

    public void setGoodScoreSummary(String summary) {
        this.goodScoreSummary = summary;
    }

    public void setBelowParScoreSummary(String belowParScoreSummary) {
        this.belowParScoreSummary = belowParScoreSummary;
    }

    public void setCourseSummaryPrompt(String courseSummaryPrompt) {
        this.courseSummaryPrompt = courseSummaryPrompt;
    }

    public void setCertificateAndCourseSummaryPrompt(String certificateAndCourseSummaryPrompt) {
        this.certificateAndCourseSummaryPrompt = certificateAndCourseSummaryPrompt;
    }

    public String getBelowParScoreSummary() {
        return belowParScoreSummary;
    }

    public String getCertificateAndCourseSummaryPrompt() {
        return certificateAndCourseSummaryPrompt;
    }

    public String getCourseSummaryPrompt() {
        return courseSummaryPrompt;
    }

    public String getSummaryForScore(int score) {
        return isGoodScore(score)?getGoodScoreSummary():getBelowParScoreSummary();
    }

    public String getCourseSummaryPromptForScore(int score) {
        return isGoodScore(score)?getCertificateAndCourseSummaryPrompt():getCourseSummaryPrompt();
    }

    private boolean isGoodScore(int score) {
        return score >= GOOD_SCORE;
    }

}