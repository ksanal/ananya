describe("Report chapter score interaction", function() {

    var metadata, course, reportChapterScoreInteraction;
    var audioFileBase = "./audio/";
    var certificateCourseLocation = "certificatecourse/";
    var courseState;

    beforeEach(function() {
        metadata = {"audio.url": audioFileBase, "certificate.audio.url" : certificateCourseLocation};
        course = certificationCourseWithTwoLessonsInEveryChapter();
        courseState = new CourseState();
        CertificateCourse.interactions = new Array();
        reportChapterScoreInteraction = new ReportChapterScoreInteraction(metadata, course, courseState);
    });

    it("should play the message corresponding to the chapter and score", function () {
        var currentChapterIndex = 1;
        courseState.setChapterIndex(currentChapterIndex);
        courseState.scoresByChapter[currentChapterIndex] = 3;
        expect(reportChapterScoreInteraction.playAudio()).toEqual("./audio/certificatecourse/chapter2_3_out_of_4.wav");

        currentChapterIndex = 0;
        courseState.setChapterIndex(currentChapterIndex);
        courseState.scoresByChapter[currentChapterIndex] = 0;
        expect(reportChapterScoreInteraction.playAudio()).toEqual("./audio/certificatecourse/chapter1_0_out_of_4.wav");
    });

    it("should return end of chapter menu as next interaction and should not change the course state", function () {
        courseState.setChapterIndex(0);
        courseState.setLessonOrQuestionIndex(2);
        CertificateCourse.interactions["endOfChapterMenu"] = {};

        expect(reportChapterScoreInteraction.nextInteraction()).toEqual(CertificateCourse.interactions["endOfChapterMenu"]);
        expect(courseState.chapterIndex).toEqual(0);
        expect(courseState.lessonOrQuestionIndex).toEqual(2);
    });

    it("should not take any input", function() {
        expect(reportChapterScoreInteraction.doesTakeInput()).toEqual(false);
    });
});