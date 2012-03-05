package org.motechproject.ananya.repository.dimension;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.motechproject.ananya.SpringIntegrationTest;
import org.motechproject.ananya.domain.dimension.LocationDimension;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class AllLocationDimensionsTest extends SpringIntegrationTest {

    @Autowired
    private AllLocationDimensions allLocationDimensions;

    private void deleteAllLocations() {
        template.deleteAll(template.loadAll(LocationDimension.class));
    }

    @Before
    public void setUp() {
        deleteAllLocations();
    }

    @After
    public void clearAllLocations() {
        deleteAllLocations();
    }


    @Test
    public void shouldUpdateLocationIfExistsInDB(){
        LocationDimension initialLocationDimension = new LocationDimension("ZZZ999", "Mandwa", "Algarh", "Gujarat");

        allLocationDimensions.add(initialLocationDimension);
        assertNotNull(allLocationDimensions.getFor("ZZZ999"));

        LocationDimension updatedLocationDimension = new LocationDimension("ZZZ999", "Patna", "Jila", "Panchayat");
        updatedLocationDimension = allLocationDimensions.addOrUpdate(updatedLocationDimension);

        LocationDimension existingDbLocationDimension = allLocationDimensions.getFor("ZZZ999");

        assertEquals(updatedLocationDimension, existingDbLocationDimension);
    }

}