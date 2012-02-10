package org.motechproject.ananya.repository;

import org.joda.time.DateTime;
import org.motechproject.ananya.domain.dimension.TimeDimension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AllTimeDimensions {

    @Autowired
    private DataAccessTemplate template;

    public TimeDimension getOrMakeFor(DateTime dateTime) {
        TimeDimension timeDimension = (TimeDimension) template.getUniqueResult(
                TimeDimension.FIND_BY_DAY_MONTH_YEAR, new String[]{"year", "month", "day"}, new Object[]{dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfYear()});
        if (timeDimension == null) {
            timeDimension = new TimeDimension(dateTime);
            template.save(timeDimension);
        }
        return timeDimension;
    }

    public TimeDimension fetchFor(DateTime dateTime) {
        return (TimeDimension) template.getUniqueResult(
                TimeDimension.FIND_BY_DAY_MONTH_YEAR, new String[]{"year", "month", "day"}, new Object[]{dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfYear()});
    }
}
