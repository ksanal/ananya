package org.motechproject.ananya.repository.measure;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.motechproject.ananya.SpringIntegrationTest;
import org.motechproject.ananya.domain.RegistrationStatus;
import org.motechproject.ananya.domain.dimension.FrontLineWorkerDimension;
import org.motechproject.ananya.domain.dimension.JobAidContentDimension;
import org.motechproject.ananya.domain.dimension.LanguageDimension;
import org.motechproject.ananya.domain.dimension.LocationDimension;
import org.motechproject.ananya.domain.dimension.TimeDimension;
import org.motechproject.ananya.domain.measure.JobAidContentMeasure;
import org.motechproject.ananya.repository.dimension.AllFrontLineWorkerDimensions;
import org.motechproject.ananya.repository.dimension.AllJobAidContentDimensions;
import org.motechproject.ananya.repository.dimension.AllLanguageDimension;
import org.motechproject.ananya.repository.dimension.AllLocationDimensions;
import org.motechproject.ananya.repository.dimension.AllTimeDimensions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;

public class AllJobAidContentMeasuresIT extends SpringIntegrationTest {

    @Autowired
    AllCourseItemMeasures allCourseItemMeasures;

    @Autowired
    AllTimeDimensions allTimeDimensions;

    @Autowired
    AllFrontLineWorkerDimensions allFrontLineWorkerDimensions;

    @Autowired
    AllJobAidContentDimensions allJobAidContentDimensions;

    @Autowired
    AllJobAidContentMeasures allJobAidContentMeasures;

    @Autowired
    AllLocationDimensions allLocationDimensions;

    @Autowired
    AllLanguageDimension allLanguageDimension;
    
    @Before
    @After
    public void tearDown() {
        template.deleteAll(template.loadAll(JobAidContentDimension.class));
        template.deleteAll(template.loadAll(JobAidContentMeasure.class));
        template.deleteAll(template.loadAll(TimeDimension.class));
        template.deleteAll(template.loadAll(FrontLineWorkerDimension.class));
        template.deleteAll(template.loadAll(LocationDimension.class));
        template.deleteAll(template.loadAll(LanguageDimension.class));
    }

    @Test
    public void shouldGetFilteredMsisdns() {
        TimeDimension timeDimension = new TimeDimension(DateTime.now());
        TimeDimension timeDimension1 = new TimeDimension(DateTime.now().minusDays(1));
        TimeDimension timeDimension2 = new TimeDimension(DateTime.now().minusDays(2));
        FrontLineWorkerDimension frontLineWorkerDimension = new FrontLineWorkerDimension(911234567890L, null, "airtel", "bihar", "name", "ANM", RegistrationStatus.REGISTERED.name(), UUID.randomUUID(), null);
        FrontLineWorkerDimension frontLineWorkerDimension1 = new FrontLineWorkerDimension(911234567891L, null, "airtel", "bihar", "name", "ANM", RegistrationStatus.REGISTERED.name(), UUID.randomUUID(), null);
        LocationDimension locationDimension = new LocationDimension("S02123431243", "S1", "D1", "B1", "P1", "VALID");
        LanguageDimension languageDimension = new LanguageDimension("bhojpuri", "bho", "badhai ho..");
        JobAidContentDimension jobAidContentDimension = new JobAidContentDimension("1234567", null, "name", "type");
        JobAidContentMeasure jobAidContentMeasure = new JobAidContentMeasure("callId", frontLineWorkerDimension, locationDimension, jobAidContentDimension, timeDimension, languageDimension, DateTime.now(), 123, 12);
        JobAidContentMeasure jobAidContentMeasure1 = new JobAidContentMeasure("callId", frontLineWorkerDimension1, locationDimension, jobAidContentDimension, timeDimension1, languageDimension, DateTime.now(), 123, 12);
        JobAidContentMeasure jobAidContentMeasure2 = new JobAidContentMeasure("callId", frontLineWorkerDimension1, locationDimension, jobAidContentDimension, timeDimension2, languageDimension, DateTime.now(), 123, 12);
        template.save(timeDimension);
        template.save(timeDimension1);
        template.save(timeDimension2);
        template.save(languageDimension);
        template.save(frontLineWorkerDimension);
        template.save(frontLineWorkerDimension1);
        template.save(locationDimension);
        template.save(jobAidContentDimension);
        template.save(jobAidContentMeasure);
        template.save(jobAidContentMeasure1);
        template.save(jobAidContentMeasure2);

        List<Long> filteredFrontLineWorkerMsisdns = allJobAidContentMeasures.getFilteredFrontLineWorkerMsisdns(DateTime.now().toDate(), DateTime.now().toDate());

        assertEquals(1, filteredFrontLineWorkerMsisdns.size());
        assertEquals((Long) 911234567890L, filteredFrontLineWorkerMsisdns.get(0));
    }

    @Test
    public void shouldFetchAllJobAidContentMeasuresForACallerId() {
        Long callerId = 1234L;
        FrontLineWorkerDimension frontLineWorkerDimension = allFrontLineWorkerDimensions.createOrUpdate(callerId, null, "operator", "circle", "name", "ASHA", "REGISTERED", UUID.randomUUID(), null);
        LocationDimension locationDimension = new LocationDimension("locationId", "", "", "", "", "VALID");
        LanguageDimension languageDimension = new LanguageDimension("bhojpuri", "bho", "badhai ho..");
        TimeDimension timeDimension = allTimeDimensions.makeFor(DateTime.now().minusDays(1));
        allLocationDimensions.saveOrUpdate(locationDimension);
        JobAidContentDimension jobAidContentDimension = new JobAidContentDimension("1234567", null, "name", "type");
        allJobAidContentDimensions.add(jobAidContentDimension);
        allLanguageDimension.add(languageDimension);
        allJobAidContentMeasures.add(new JobAidContentMeasure("callId", frontLineWorkerDimension, locationDimension, jobAidContentDimension, timeDimension, languageDimension, DateTime.now(), 23, 23));

        List<JobAidContentMeasure> jobAidContentMeasureList = allJobAidContentMeasures.findByCallerId(callerId);

        assertEquals(1, jobAidContentMeasureList.size());
        assertEquals(callerId, jobAidContentMeasureList.get(0).getFrontLineWorkerDimension().getMsisdn());
    }

    @Test
    public void shouldFetchAllJobAidContentMeasuresForALocationId() {
        Long callerId = 1234L;
        FrontLineWorkerDimension frontLineWorkerDimension = allFrontLineWorkerDimensions.createOrUpdate(callerId, null, "operator", "circle", "name", "ASHA", "REGISTERED", UUID.randomUUID(), null);
        String locationId = "locationId";
        LocationDimension locationDimension = new LocationDimension(locationId, "", "", "", "", "VALID");
        LanguageDimension languageDimension = new LanguageDimension("bhojpuri", "bho", "badhai ho..");
        TimeDimension timeDimension = allTimeDimensions.makeFor(DateTime.now().minusDays(1));
        allLocationDimensions.saveOrUpdate(locationDimension);
        JobAidContentDimension jobAidContentDimension = new JobAidContentDimension("1234567", null, "name", "type");
        allJobAidContentDimensions.add(jobAidContentDimension);
        allLanguageDimension.add(languageDimension);
        allJobAidContentMeasures.add(new JobAidContentMeasure("callId", frontLineWorkerDimension, locationDimension, jobAidContentDimension, timeDimension, languageDimension, DateTime.now(), 23, 23));

        List<JobAidContentMeasure> jobAidContentMeasureList = allJobAidContentMeasures.findByLocationId(locationId);

        assertEquals(1, jobAidContentMeasureList.size());
        assertEquals(callerId, jobAidContentMeasureList.get(0).getFrontLineWorkerDimension().getMsisdn());
    }
}
