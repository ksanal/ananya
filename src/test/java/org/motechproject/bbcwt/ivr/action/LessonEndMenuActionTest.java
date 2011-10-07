package org.motechproject.bbcwt.ivr.action;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.bbcwt.domain.Chapter;
import org.motechproject.bbcwt.domain.HealthWorker;
import org.motechproject.bbcwt.domain.Lesson;
import org.motechproject.bbcwt.domain.Milestone;
import org.motechproject.bbcwt.ivr.IVR;
import org.motechproject.bbcwt.ivr.IVRRequest;
import org.motechproject.bbcwt.repository.ChaptersRespository;
import org.motechproject.bbcwt.repository.HealthWorkersRepository;
import org.motechproject.bbcwt.repository.MilestonesRepository;

import java.util.Date;

import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.when;

public class LessonEndMenuActionTest extends BaseActionTest {
    private LessonEndMenuAction lessonEndMenuAction;

    @Mock
    private ChaptersRespository chaptersRespository;
    @Mock
    private MilestonesRepository milestonesRepository;
    @Mock
    private HealthWorkersRepository healthWorkersRepository;

    private Chapter chapter;
    private Lesson currentLesson;
    private Lesson lastLesson;
    private HealthWorker healthWorker;
    private String callerId;
    private Milestone currentMilestone;

    @Before
    public void setUp()
    {
        lessonEndMenuAction = new LessonEndMenuAction(healthWorkersRepository, chaptersRespository, milestonesRepository, messages);

        callerId = "9999988888";
        healthWorker = new HealthWorker(callerId);
        healthWorker.setId("UniqueHealthWorkerId");

        chapter = new Chapter(1);
        chapter.setId("ChapterId1");
        Lesson lesson1 = new Lesson(1, "Lesson 1", "lessonEndMenu1");
        currentLesson = new Lesson(2, "Lesson 2", "lessonEndMenu2");
        lastLesson = new Lesson(3, "Lesson 3", "lessonEndMenu3");
        chapter.addLesson(lesson1);
        chapter.addLesson(currentLesson);
        chapter.addLesson(lastLesson);

        currentMilestone = new Milestone(healthWorker.getId(), chapter.getId(), currentLesson.getId(), null, new Date());

        when(session.getAttribute(IVR.Attributes.CALLER_ID)).thenReturn(callerId);
        when(healthWorkersRepository.findByCallerId(callerId)).thenReturn(healthWorker);
        when(chaptersRespository.get(currentMilestone.getChapterId())).thenReturn(chapter);
        when(milestonesRepository.findByHealthWorker(healthWorker)).thenReturn(currentMilestone);

    }

    @Test
    public void shouldBuildEndOfLessonMenu(){
        IVRRequest ivrRequest = new IVRRequest("unique-call-id", null, null, null);

        String endAction = lessonEndMenuAction.handle(ivrRequest,request,response);

        verify(ivrDtmfBuilder, times(1)).addPlayAudio(CONTENT_LOCATION + currentLesson.getEndMenuFileName());
        verify(ivrResponseBuilder, times(1)).withCollectDtmf(collectDtmf);
        verify(session, times(1)).setAttribute(IVR.Attributes.NEXT_INTERACTION, "/lessonEndAnswer");
    }

    @Test
    public void shouldBuildEndOfChapterMenu(){
        IVRRequest ivrRequest = new IVRRequest("unique-call-id", null, null, null);

        currentMilestone.setLessonId(lastLesson.getId());

        String endAction = lessonEndMenuAction.handle(ivrRequest,request,response);

        verify(ivrDtmfBuilder, times(1)).addPlayAudio(CONTENT_LOCATION + lastLesson.getEndMenuFileName());
        verify(ivrResponseBuilder, times(1)).withCollectDtmf(collectDtmf);
        verify(session, times(1)).setAttribute(IVR.Attributes.NEXT_INTERACTION, "/chapterEndAnswer");

    }

    @Test
    public void shouldMarkLastMilestoneAsCompleted() {
        IVRRequest ivrRequest = new IVRRequest(null, null, null, "1");

        lessonEndMenuAction.handle(ivrRequest, request, response);

        verify(milestonesRepository).markLastMilestoneFinish(callerId);
    }
}