describe("Start next chapter interaction", function() {
    var metadata, course, startNextChapter;
    var audioFileBase = "./audio/";
    var certificateCourseLocation = "certificatecourse/";

    beforeEach(function() {
        metadata = {"audio.url": audioFileBase, "certificate.audio.url" : certificateCourseLocation};
        course = certificationCourseWithTwoLessonsInEveryChapter();
        CertificateCourse.interactions = new Array();
    });

    it("should set first chapter first lesson in state and go to lesson interaction, if course state does not contain any chapter number", function () {
        var courseState = new CourseState();
        var startNextChapter = new StartNextChapter(metadata, course, courseState);

        var lessonInteraction = new LessonInteraction(null, null);
        CertificateCourse.interactions["lesson"] = lessonInteraction;

        var nextState = startNextChapter.processSilentlyAndReturnNextState();

        expect(courseState.chapterIndex).toEqual(0);
        expect(courseState.lessonOrQuestionIndex).toEqual(0);
        expect(nextState).toEqual(lessonInteraction);
    });

    it("should set next chapter first lesson in state", function () {
        var courseState = new CourseState();
        courseState.setChapterIndex(0);
        courseState.setLessonOrQuestionIndex(3);
        var startNextChapter = new StartNextChapter(metadata, course, courseState);

        var lessonInteraction = new LessonInteraction(null, null);
        CertificateCourse.interactions["lesson"] = lessonInteraction;

        var nextState = startNextChapter.processSilentlyAndReturnNextState();

        expect(courseState.chapterIndex).toEqual(1);
        expect(courseState.lessonOrQuestionIndex).toEqual(0);
        expect(nextState).toEqual(lessonInteraction);
    });

    it("should set endOfCourse as next state if already at last chapter", function () {
        var lastChapterIndex = 1;
        var courseState = new CourseState();
        courseState.setChapterIndex(lastChapterIndex);
        courseState.setLessonOrQuestionIndex(3);
        var startNextChapter = new StartNextChapter(metadata, course, courseState);

        var endOfCourseInteraction = {};
        CertificateCourse.interactions["endOfCourse"] = endOfCourseInteraction;

        var nextState = startNextChapter.processSilentlyAndReturnNextState();

        expect(nextState).toEqual(endOfCourseInteraction);
    });
});