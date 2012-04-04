package org.motechproject.ananya.mapper;

import org.motechproject.ananya.domain.CertificationCourseLogItem;
import org.motechproject.ananya.domain.CourseItemState;
import org.motechproject.ananya.domain.CourseItemType;
import org.motechproject.ananya.request.CertificationCourseStateRequest;

public class CertificationCourseLogItemMapper {

    public CertificationCourseLogItem mapFrom(CertificationCourseStateRequest stateRequest) {
        return new CertificationCourseLogItem(
                stateRequest.getContentId(),
                CourseItemType.valueOf(stateRequest.getContentType().toUpperCase()),
                stateRequest.getContentName(),
                stateRequest.getContentData(),
                CourseItemState.valueOf(stateRequest.getCourseItemState().toUpperCase()),
                stateRequest.getTimeAsDateTime()
        );

    }
}