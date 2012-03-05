package org.motechproject.ananya.domain.dimension;

import org.motechproject.ananya.domain.CourseItemType;

import javax.persistence.*;

@Entity
@Table(name = "course_item_dimension")
@NamedQuery(name = CourseItemDimension.FIND_BY_NAME, query = "select cid from CourseItemDimension cid where cid.name=:name")
public class CourseItemDimension {

    public static final String FIND_BY_NAME = "find.by.name";

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="name")
    private String name;

    @Column(name="content_id")
    private String contentId;

    @Column(name="type")
    private String type;

    public CourseItemDimension() {
    }

    public CourseItemDimension(String name, String contentId, CourseItemType type) {
        this.name = name;
        this.contentId = contentId;
        this.type = String.valueOf(type);
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CourseItemType getType() {
        return CourseItemType.valueOf(type);
    }
}
