package org.motechproject.ananya.repository.dimension;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.motechproject.ananya.domain.dimension.LocationDimension;
import org.motechproject.ananya.repository.DataAccessTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class AllLocationDimensions {

    @Autowired
    private DataAccessTemplate template;

    public AllLocationDimensions() {
    }

    public LocationDimension getFor(String locationCode) {
        return (LocationDimension) template.getUniqueResult(LocationDimension.FIND_BY_LOCATION_ID, new String[]{"location_id"}, new Object[]{locationCode});
    }

    public LocationDimension saveOrUpdate(LocationDimension locationDimension) {
        template.saveOrUpdate(locationDimension);
        return locationDimension;
    }

    public List<LocationDimension> getFilteredLocationFor(String district, String block, String panchayat) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(LocationDimension.class);
        if (district != null)
            detachedCriteria.add(Restrictions.eq("district", district).ignoreCase());
        if (block != null)
            detachedCriteria.add(Restrictions.eq("block", block).ignoreCase());
        if (panchayat != null)
            detachedCriteria.add(Restrictions.eq("panchayat", panchayat).ignoreCase());

        return template.findByCriteria(detachedCriteria);
    }

    public void delete(String locationCode) {
        LocationDimension locationDimension = getFor(locationCode);
        template.delete(locationDimension);
    }
}
