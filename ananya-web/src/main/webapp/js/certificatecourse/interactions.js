/*
    WelcomeInteraction
*/

var WelcomeInteraction = function(metadata, course) {
    this.init = function(metadata, course) {
        AbstractCourseInteraction.call(this, metadata);
        this.course = course;
    }

    this.init(metadata, course);
};

WelcomeInteraction.prototype = new AbstractCourseInteraction();
WelcomeInteraction.prototype.constructor = WelcomeInteraction;

WelcomeInteraction.prototype.playAudio = function() {
    return this.findAudio(this.course, "introduction");
};

WelcomeInteraction.prototype.doesTakeInput = function() {
    return false;
}

WelcomeInteraction.prototype.nextInteraction = function() {
    return CertificateCourse.interactions["startCourseOption"];
}

WelcomeInteraction.prototype.bookMark = function() {

}

/*
    StartCourseOption
*/
var StartCourseOption = function(metadata, course) {
    this.init = function(metadata, course) {
        AbstractCourseInteraction.call(this, metadata);
        this.course = course;
    }

    this.init(metadata, course);
};

StartCourseOption.prototype = new AbstractCourseInteraction();
StartCourseOption.prototype.constructor = StartCourseOption;

StartCourseOption.prototype.doesTakeInput = function() {
    return true;
}

StartCourseOption.prototype.playAudio = function() {
    return this.findAudio(this.course, "menu");
};

StartCourseOption.prototype.validateInput = function(input) {
    return input == '1' || input == '2';
};

StartCourseOption.prototype.continueWithoutInput = function(){
    return CertificateCourse.interactions["startNextChapter"];
}

StartCourseOption.prototype.processInputAndReturnNextInteraction = function(input){
    if(input == 1) {
        return CertificateCourse.interactions["welcome"];
    }
    return CertificateCourse.interactions["startNextChapter"];
}

/*
    StartNextChapter
*/
//TODO: This should extend from something called silent interaction.
var StartNextChapter = function(metadata, course, courseState) {
    this.init = function(metadata, course, courseState) {
        this.course = course;
        this.courseState = courseState;
    }

    this.processSilentlyAndReturnNextState = function() {
        var nextState;
        if(this.courseState.chapterIndex == null) {
            this.courseState.setChapterIndex(0);
            this.courseState.setLessonOrQuestionIndex(0);
            nextState = CertificateCourse.interactions["lesson"];
        }
        else {
            var currentChapterIndex = this.courseState.chapterIndex;
            var currentLessonOrQuestionIndex = this.courseState.lessonOrQuestionIndex;
            var maxChapterIndex = course.children.length-1;
            if(currentChapterIndex >= maxChapterIndex) {
               nextState = CertificateCourse.interactions["endOfCourse"];
            }
            else {
                this.courseState.chapterIndex++;
                this.courseState.setLessonOrQuestionIndex(0);
                nextState = CertificateCourse.interactions["lesson"];
            }
        }
        return nextState;
    }

    this.init(metadata, course, courseState);
};

/*
    LessonInteraction
*/
var LessonInteraction = function(metadata, course, courseState) {
    this.init = function(metadata, course, courseState) {
        AbstractCourseInteraction.call(this, metadata);
        this.course = course;
        this.courseState = courseState;
    }

    this.init(metadata, course, courseState);
};

LessonInteraction.prototype = new AbstractCourseInteraction();
LessonInteraction.prototype.constructor = LessonInteraction;

LessonInteraction.prototype.playAudio = function() {
    var currentChapterIndex = this.courseState.chapterIndex;
    var currentLessonIndex = this.courseState.lessonOrQuestionIndex;

    var currentLesson = this.course.children[currentChapterIndex].children[currentLessonIndex];

    return this.findAudio(currentLesson, "lesson");
};

LessonInteraction.prototype.doesTakeInput = function() {
    return false;
}

LessonInteraction.prototype.nextInteraction = function() {
    return CertificateCourse.interactions["lessonEndMenu"];
}

/*
    LessonEndMenuInteraction
*/

var LessonEndMenuInteraction = function(metadata, course, courseState) {
    this.init = function(metadata, course, courseState) {
        AbstractCourseInteraction.call(this, metadata);
        this.course = course;
        this.courseState = courseState;
    }

    this.init(metadata, course, courseState);
};

LessonEndMenuInteraction.prototype = new AbstractCourseInteraction();
LessonEndMenuInteraction.prototype.constructor = StartCourseOption;

LessonEndMenuInteraction.prototype.doesTakeInput = function() {
    return true;
}

LessonEndMenuInteraction.prototype.playAudio = function() {
    var chapterIndex = this.courseState.chapterIndex;
    var lessonIndex = this.courseState.lessonOrQuestionIndex;
    var currentLesson = this.course.children[chapterIndex].children[lessonIndex];
    return this.findAudio(currentLesson, "menu");
};


LessonEndMenuInteraction.prototype.validateInput = function(input) {
    return input == '1' || input == '2';
};

LessonEndMenuInteraction.prototype.continueWithoutInput = function(){
    var chapterIndex = this.courseState.chapterIndex;
    var lessonIndex = this.courseState.lessonOrQuestionIndex;
    var currentChapter = this.course.children[chapterIndex];
    var nextInLine = currentChapter.children[lessonIndex+1];
    if(nextInLine.data.type=="quiz"){
        return CertificateCourse.interactions["startNextChapter"];
    }
    this.courseState.setLessonOrQuestionIndex(lessonIndex+1);
    return CertificateCourse.interactions["lesson"];
};

LessonEndMenuInteraction.prototype.processInputAndReturnNextInteraction = function(input){
    if(input == 2) {
        var chapterIndex = this.courseState.chapterIndex;
        var lessonIndex = this.courseState.lessonOrQuestionIndex;
        var currentChapter = this.course.children[chapterIndex];
        var nextInLine = currentChapter.children[lessonIndex+1];
        if(nextInLine.data.type=="quiz"){
            return CertificateCourse.interactions["startQuiz"];
        }
        this.courseState.setLessonOrQuestionIndex(lessonIndex+1);
    }
    return CertificateCourse.interactions["lesson"];
}


/*
    InvalidInputInteraction
*/
var InvalidInputInteraction = function(interactionToReturnTo, metadata) {
    this.init = function(interactionToReturnTo, metadata) {
        AbstractCourseInteraction.call(null, metadata);
        this.metadata = metadata;
        this.interactionToReturnTo = interactionToReturnTo;
    }

    this.init(interactionToReturnTo, metadata);
};

InvalidInputInteraction.prototype = new AbstractCourseInteraction();
InvalidInputInteraction.prototype.constructor = InvalidInputInteraction;

InvalidInputInteraction.prototype.playAudio = function() {
    return this.metadata['audio.url'] + this.metadata['invalid.input.retry.audio'];
};

InvalidInputInteraction.prototype.doesTakeInput = function() {
    return false;
};

InvalidInputInteraction.prototype.nextInteraction = function() {
    return this.interactionToReturnTo;
};