package org.motechproject.ananya.repository.measure;

import org.motechproject.ananya.domain.measure.CourseItemMeasure;
import org.motechproject.ananya.repository.DataAccessTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public class AllCourseItemMeasures {

    @Autowired
    private DataAccessTemplate template;

    public AllCourseItemMeasures() {
    }

    public void save(CourseItemMeasure courseItemMeasure) {
        template.save(courseItemMeasure);
    }
}
