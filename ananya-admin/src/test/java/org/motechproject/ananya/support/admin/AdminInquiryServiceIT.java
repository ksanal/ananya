package org.motechproject.ananya.support.admin;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.ananya.domain.CourseItemState;
import org.motechproject.ananya.domain.CourseItemType;
import org.motechproject.ananya.domain.dimension.*;
import org.motechproject.ananya.domain.measure.*;
import org.motechproject.ananya.repository.dimension.*;
import org.motechproject.ananya.repository.measure.AllCallDurationMeasures;
import org.motechproject.ananya.repository.measure.AllCourseItemMeasures;
import org.motechproject.ananya.repository.measure.AllJobAidContentMeasures;
import org.motechproject.ananya.support.TestDataAccessTemplate;
import org.motechproject.ananya.support.admin.domain.CallContent;
import org.motechproject.ananya.support.admin.domain.CallerDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-admin.xml")
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class AdminInquiryServiceIT {
    @Autowired
    private AdminInquiryService adminInquiryService;
    @Autowired
    private AllFrontLineWorkerDimensions allFrontLineWorkerDimensions;
    @Autowired
    private AllCourseItemDimensions allCourseItemDimensions;
    @Autowired
    private AllCourseItemDetailsDimensions allCourseItemDetailsDimensions;
    @Autowired
    private AllCourseItemMeasures allCourseItemMeasures;
    @Autowired
    private AllTimeDimensions allTimeDimensions;
    @Autowired
    private AllLocationDimensions allLocationDimensions;
    @Autowired
    private AllLanguageDimension allLanguageDimension;
    @Autowired
    private AllJobAidContentDimensions allJobAidContentDimensions;
    @Autowired
    private AllJobAidContentDetailsDimensions allJobAidContentDetailsDimensions;
    @Autowired
    private AllJobAidContentMeasures allJobAidContentMeasures;
    @Autowired
    private AllCallDurationMeasures allCallDurationMeasures;
    @Autowired
    private TestDataAccessTemplate template;
    private DateTime now;
    private DateTime twoDaysAgo;
    private TimeDimension timeDimensionTwoDaysAgo;
    private TimeDimension timeDimensionNow;
    private LocationDimension locationDimension;
    private LanguageDimension languageDimension;

    @Before
    public void setUp() {
        resetDB();
        now = DateTime.now();
        twoDaysAgo = now.minusDays(2);
        timeDimensionTwoDaysAgo = allTimeDimensions.addOrUpdate(twoDaysAgo);
        timeDimensionNow = allTimeDimensions.addOrUpdate(now);
        locationDimension = allLocationDimensions.saveOrUpdate(new LocationDimension("S00D00V01", "please", "light", "my", "fire", "VALID"));
        languageDimension = allLanguageDimension.getFor("bhojpuri");
        if(languageDimension==null)
        	languageDimension = allLanguageDimension.addOrUpdate(new LanguageDimension("bhojpuri", "bho", "badhai ho..."));
    }

    @After
    public void tearDown() {
        resetDB();
    }

    private void resetDB() {
        template.deleteAll(template.loadAll(FrontLineWorkerDimension.class));
        template.deleteAll(template.loadAll(TimeDimension.class));
        template.deleteAll(template.loadAll(LocationDimension.class));
        template.deleteAll(template.loadAll(CourseItemDimension.class));
        template.deleteAll(template.loadAll(JobAidContentDimension.class));
        template.deleteAll(template.loadAll(JobAidContentMeasure.class));
        template.deleteAll(template.loadAll(CourseItemMeasure.class));
        template.deleteAll(template.loadAll(RegistrationMeasure.class));
        template.deleteAll(template.loadAll(SMSSentMeasure.class));
        template.deleteAll(template.loadAll(LanguageDimension.class));
    }

    @Test
    public void shouldRetrieveListOfAcademyCallsContent() {
        String callerId = "919988776655";
        setUpAcademyCalls(callerId);

        List<CallContent> academyCallsContent = (List<CallContent>) adminInquiryService.getInquiryData(callerId).get(AdminInquiryService.ACADEMY_CALLS);

        assertEquals(2, academyCallsContent.size());
    }

    @Test
    public void shouldRetrieveListOfKunjiCallsContent() {
        String callerId = "919988776644";
        setUpKunjiCalls(callerId);

        List<CallContent> kunjiCallsContent = (List<CallContent>) adminInquiryService.getInquiryData(callerId).get(AdminInquiryService.KUNJI_CALLS);

        assertEquals(2, kunjiCallsContent.size());
    }

    @Test
    public void shouldRetrieveListOfCallDetails() {
        String callerId = "919988776633";
        setUpCallDetails(callerId);

        List<CallContent> kunjiCallsContent = (List<CallContent>) adminInquiryService.getInquiryData(callerId).get(AdminInquiryService.CALL_DETAILS);

        assertEquals(2, kunjiCallsContent.size());
    }

    @Test
    public void shouldGetCallerDetail() {
        String callerId = "919988776622";
        String name = "Aanchal";
        allFrontLineWorkerDimensions.createOrUpdate(Long.valueOf(callerId), null, "airtel", "bihar", name, "ASHA", "REGISTERED", UUID.randomUUID(), null);

        CallerDetail callerDetail = (CallerDetail) adminInquiryService.getInquiryData(callerId).get(AdminInquiryService.CALLER_DETAIL);

        assertEquals(name, callerDetail.getName());
    }

    private void setUpCallDetails(String callerId) {
        FrontLineWorkerDimension frontLineWorkerDimension = allFrontLineWorkerDimensions.createOrUpdate(Long.valueOf(callerId), null, "bsnl", "bihar", "Raji", "ANGANWADI", "REGISTERED", UUID.randomUUID(), null);

        allCallDurationMeasures.add(new CallDurationMeasure(frontLineWorkerDimension, locationDimension, timeDimensionTwoDaysAgo, callerId + "444", 555444L, 30, twoDaysAgo, twoDaysAgo.plusMinutes(30), "JOBAID", 1));
        allCallDurationMeasures.add(new CallDurationMeasure(frontLineWorkerDimension, locationDimension, timeDimensionNow, callerId + "888", 555444L, 10, now, now.plusMinutes(30), "CC", 1));
    }


    private void setUpKunjiCalls(String callerId) {
        FrontLineWorkerDimension frontLineWorkerDimension = allFrontLineWorkerDimensions.createOrUpdate(Long.valueOf(callerId), null, "vodafone", "bihar", "Ambala", "ANGANWADI", "REGISTERED", UUID.randomUUID(), null);

        JobAidContentDimension root = new JobAidContentDimension("root", null, "Welcome", "root");
        allJobAidContentDimensions.add(root);
        JobAidContentDimension ja1 = new JobAidContentDimension("ja1", root, "Chapter 1", "chapter");
        allJobAidContentDimensions.add(ja1);
        JobAidContentDimension ja2 = new JobAidContentDimension("ja2", root, "Chapter 2", "chapter");
        allJobAidContentDimensions.add(ja2);

        if(allJobAidContentDetailsDimensions.getFor("ja1", languageDimension.getId())==null)
        	allJobAidContentDetailsDimensions.addOrUpdate(new JobAidContentDetailsDimension(languageDimension.getId(), "ja1", "Chapter 1", 1234));
        if(allJobAidContentDetailsDimensions.getFor("ja2", languageDimension.getId())==null)
        	allJobAidContentDetailsDimensions.addOrUpdate(new JobAidContentDetailsDimension(languageDimension.getId(), "ja2", "chapter 2", 1234));

        allJobAidContentMeasures.add(new JobAidContentMeasure(callerId + "-654", frontLineWorkerDimension, locationDimension, ja1, timeDimensionTwoDaysAgo, languageDimension, twoDaysAgo, 777, 80));
        allJobAidContentMeasures.add(new JobAidContentMeasure(callerId + "-999", frontLineWorkerDimension, locationDimension, ja2, timeDimensionNow, languageDimension, now, 2500, 50));
    }

    private void setUpAcademyCalls(String callerId) {
        FrontLineWorkerDimension frontLineWorkerDimension = allFrontLineWorkerDimensions.createOrUpdate(Long.valueOf(callerId), null, "airtel", "bihar", "Santoshi", "ASHA", "REGISTERED", UUID.randomUUID(), null);
        CourseItemDimension root = allCourseItemDimensions.add(new CourseItemDimension("course", "c1l1", CourseItemType.COURSE, null));
        CourseItemDimension c1l1 = allCourseItemDimensions.add(new CourseItemDimension("chapter 1 lesson 1", "c1l1", CourseItemType.LESSON, root));
        CourseItemDimension c1l2 = allCourseItemDimensions.add(new CourseItemDimension("chapter 1 lesson 2", "c1l2", CourseItemType.LESSON, root));

        if(allCourseItemDetailsDimensions.getFor("c1l1", languageDimension.getId())==null)
        	allCourseItemDetailsDimensions.addOrUpdate(new CourseItemDetailsDimension(languageDimension.getId(), "c1l1", "chapter 1 lesson 1", 1234));
        if(allCourseItemDetailsDimensions.getFor("c1l2", languageDimension.getId())==null)
        	allCourseItemDetailsDimensions.addOrUpdate(new CourseItemDetailsDimension(languageDimension.getId(), "c1l2", "chapter 1 lesson 2", 1234));
        
        allCourseItemMeasures.save(new CourseItemMeasure(timeDimensionTwoDaysAgo, c1l1, frontLineWorkerDimension, locationDimension, languageDimension, twoDaysAgo, 14, CourseItemState.END, callerId + "-123"));
        allCourseItemMeasures.save(new CourseItemMeasure(timeDimensionNow, c1l2, frontLineWorkerDimension, locationDimension, languageDimension, now, 14, CourseItemState.END, callerId + "-567"));
    }
}
