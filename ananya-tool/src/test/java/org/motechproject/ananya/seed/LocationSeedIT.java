package org.motechproject.ananya.seed;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.ananya.TestDataAccessTemplate;
import org.motechproject.ananya.domain.Location;
import org.motechproject.ananya.domain.LocationStatus;
import org.motechproject.ananya.domain.dimension.LocationDimension;
import org.motechproject.ananya.repository.AllLocations;
import org.motechproject.ananya.service.dimension.LocationDimensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-tool.xml")
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class LocationSeedIT {

    @Autowired
    private LocationSeed locationSeed;

    @Autowired
    private AllLocations allLocations;

    @Autowired
    private LocationDimensionService locationDimensionService;

    @Autowired
    private TestDataAccessTemplate template;

    @Before
    public void setUp() {
        allLocations.removeAll();
        template.deleteAll(template.loadAll(LocationDimension.class));
    }

    @After
    public void tearDown() {
        allLocations.removeAll();
        template.deleteAll(template.loadAll(LocationDimension.class));
    }

    @Test
    @Ignore
    public void shouldLoadAllTheLocationsFromTheCSVFile() throws IOException {
        locationSeed.loadLocationsFromCSVFile();

        List<Location> locations = allLocations.getAll();
        Location location = locations.get(1);
        String externalId = location.getExternalId();
        LocationDimension locationDimension = locationDimensionService.getFor(externalId);

        assertEquals(19, locations.size());
        Assert.assertEquals(location.getDistrict(), locationDimension.getDistrict());
        Assert.assertEquals(location.getBlock(), locationDimension.getBlock());
        Assert.assertEquals(location.getPanchayat(), locationDimension.getPanchayat());
    }

    @Test
    public void shouldUpdateAllLocationRegistrationService() {
        Location expectedLocation = new Location("s1", "d1", "b1", "p1");
        allLocations.add(expectedLocation);

        locationSeed.locationStatusUpdateToValid();

        List<Location> locationList = allLocations.getAll();
        assertEquals(1, locationList.size());
        assertEquals(expectedLocation, locationList.get(0));
        assertEquals(LocationStatus.VALID, locationList.get(0).getLocationStatusAsEnum());
    }
    
    @Test
    public void shouldUpdateDefaultLocationStateNameAndExternalId() {
        Location previousDefaultLocation = new Location();
        previousDefaultLocation.setExternalId("S01D000B000V000");
        allLocations.add(previousDefaultLocation);

        locationSeed.updateStateAndExternalIdForDefaultLocation();

        List<Location> locationList = allLocations.getAll();
        assertEquals(1, locationList.size());
        assertEquals(locationList.get(0).getState(), "C00");
        assertEquals(locationList.get(0).getExternalId(), "S00D000B000V000");
    }
}
