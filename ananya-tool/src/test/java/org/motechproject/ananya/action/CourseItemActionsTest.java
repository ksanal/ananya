package org.motechproject.ananya.action;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.ananya.domain.CourseItemType;
import org.motechproject.ananya.domain.Node;
import org.motechproject.ananya.repository.AllNodes;
import org.motechproject.cmslite.api.model.StringContent;
import org.motechproject.cmslite.api.repository.AllStringContents;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class CourseItemActionsTest {

    @Mock
    private AllStringContents allStringContents;
    @Mock
    private AllNodes allNodes;

    @Before
    public void setUp() {
        initMocks(this);
        CourseItemActions.init(allNodes, allStringContents);
    }

    @Test
    public void shouldInvokeCorrectActionForEachCourseItem() {
        CourseItemActions chapterAction = CourseItemActions.findFor(CourseItemType.CHAPTER);
        assertEquals(CourseItemActions.UpdateChapterAudioContent, chapterAction);

        CourseItemActions lessonAction = CourseItemActions.findFor(CourseItemType.LESSON);
        assertEquals(CourseItemActions.UpdateLessonAudioContent, lessonAction);

        CourseItemActions questionAction = CourseItemActions.findFor(CourseItemType.QUIZ);
        assertEquals(CourseItemActions.UpdateQuestionAudioContent, questionAction);
    }

    @Test
    public void shouldUpdateAudioContentForChapter() {
        CourseItemActions action = CourseItemActions.findFor(CourseItemType.CHAPTER);

        Node node = action.updateContents(new Node("Chapter 1"));

        verify(allStringContents, times(7)).add(any(StringContent.class));
        verify(allNodes).update(node);
        assertEquals(7, node.contents().size());
        assertEquals(7, node.contentIds().size());
    }

    @Test
    public void shouldUpdateAudioContentForLesson() {
        CourseItemActions action = CourseItemActions.findFor(CourseItemType.LESSON);

        Node node = action.updateContents(new Node("Chapter 1 Lesson 1"));

        verify(allStringContents, times(2)).add(any(StringContent.class));
        verify(allNodes).update(node);
        assertEquals(2, node.contents().size());
        assertEquals(2, node.contentIds().size());
    }

    @Test
    public void shouldUpdateAudioContentForQuestion() {
        CourseItemActions action = CourseItemActions.findFor(CourseItemType.QUIZ);

        Node node = action.updateContents(new Node("Chapter 1 Question 1"));

        verify(allStringContents, times(3)).add(any(StringContent.class));
        verify(allNodes).update(node);
        assertEquals(3, node.contents().size());
        assertEquals(3, node.contentIds().size());
    }
}
