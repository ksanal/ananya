package org.motechproject.bbcwt.ivr.jobaid.action;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.motechproject.bbcwt.domain.Chapter;
import org.motechproject.bbcwt.domain.JobAidCourse;
import org.motechproject.bbcwt.ivr.IVRContext;
import org.motechproject.bbcwt.ivr.IVRMessage;
import org.motechproject.bbcwt.ivr.IVRRequest;
import org.motechproject.bbcwt.ivr.builder.IVRDtmfBuilder;
import org.motechproject.bbcwt.ivr.builder.IVRResponseBuilder;
import org.motechproject.bbcwt.ivr.jobaid.CallFlowExecutor;
import org.motechproject.bbcwt.ivr.jobaid.IVRAction;
import org.motechproject.bbcwt.ivr.jobaid.JobAidFlowState;
import org.motechproject.bbcwt.service.JobAidContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LessonSelection implements IVRAction {
    private static final Logger LOGGER = Logger.getLogger(ChapterSelection.class);

    @Autowired
    private JobAidContentService jobAidContentService;
    @Autowired
    private PlayLesson playLesson;
    @Autowired
    private IVRMessage messages;

    public LessonSelection() {

    }

    public LessonSelection(JobAidContentService jobAidContentService, PlayLesson playLesson, IVRMessage ivrMessage) {
        this.jobAidContentService = jobAidContentService;
        this.playLesson = playLesson;
        this.messages = ivrMessage;
    }

    @Override
    public void processRequest(IVRContext context, IVRRequest request, IVRResponseBuilder responseBuilder) {
        //do nothing
    }

    @Override
    public void playPrompt(IVRContext context, IVRRequest request, IVRDtmfBuilder dtmfBuilder) {
        Chapter chapter = currentChapter(context);
        final String chapterMenu = messages.absoluteFileLocation("jobAid/" + chapter.menu());
        LOGGER.info(String.format("Playing chapter menu: %s", chapterMenu));
        dtmfBuilder.addPlayAudio(chapterMenu);
        dtmfBuilder.withMaximumLengthOfResponse(1);

    }

    private Chapter currentChapter(IVRContext context) {
        JobAidCourse course = jobAidContentService.getCourse("JobAidCourse");
        return course.levels().get(currentLevelNumber(context)).chapters().get(currentChapterNumber(context));
    }

    private int currentLevelNumber(IVRContext context) {
        return ((JobAidFlowState)context.flowSpecificState()).level();
    }

    private int currentChapterNumber(IVRContext context) {
        return ((JobAidFlowState)context.flowSpecificState()).chapter();
    }


    @Override
    public CallFlowExecutor.ProcessStatus validateInput(IVRContext context, IVRRequest request) {
        if(StringUtils.isEmpty(request.getData())) {
            LOGGER.info("No data received.");
            return CallFlowExecutor.ProcessStatus.NO_IP;
        }

        if(!StringUtils.isNumeric(request.getData())) {
            LOGGER.info("Non numeric data received.");
            return CallFlowExecutor.ProcessStatus.INVALID_IP;
        }

        int lessonRequested = Integer.parseInt(request.getData());
        int noOfLessons = currentChapter(context).getLessons().size();

        if(lessonRequested < 0 || lessonRequested > noOfLessons) {
            LOGGER.info(String.format("Lesson Requested: %d should be less than Number of Available Lessons: %s", lessonRequested, noOfLessons));
            return CallFlowExecutor.ProcessStatus.INVALID_IP;
        }

        return CallFlowExecutor.ProcessStatus.OK;
    }

    @Override
    public IVRAction processAndForwardToNextState(IVRContext context, IVRRequest request) {
        int lessonRequested = Integer.parseInt(request.getData());
        ((JobAidFlowState)context.flowSpecificState()).setLesson(lessonRequested);
        return playLesson;
    }
}