package org.motechproject.ananya.repository.dimension;

import org.motechproject.ananya.domain.CourseItemType;
import org.motechproject.ananya.domain.dimension.CourseItemDimension;
import org.motechproject.ananya.repository.DataAccessTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class AllCourseItemDimensions {

    @Autowired
    private DataAccessTemplate template;

    public AllCourseItemDimensions() {
    }

    public CourseItemDimension getFor(String name, CourseItemType type) {
        return (CourseItemDimension) template.getUniqueResult(CourseItemDimension.FIND_BY_NAME_AND_TYPE, new String[]{"name", "type"}, new Object[]{name.trim(), type.name()});
    }

    public CourseItemDimension getFor(String contentId) {
        return (CourseItemDimension) template.getUniqueResult(CourseItemDimension.FIND_BY_CONTENT_ID, new String[]{"contentId"}, new Object[]{contentId.trim()});
    }

    public CourseItemDimension add(CourseItemDimension courseItemDimension) {
        template.save(courseItemDimension);
        return courseItemDimension;
    }

    public void update(CourseItemDimension courseItemDimension) {
        template.update(courseItemDimension);
    }
    
    public void delete(CourseItemDimension courseItemDimension){
    	template.delete(courseItemDimension);
    }
}
