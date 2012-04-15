package org.motechproject.ananya;

import org.hibernate.SessionFactory;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "prototype")
@Ignore
public class TestDataAccessTemplate extends HibernateTemplate {

    @Autowired
    public TestDataAccessTemplate(@Qualifier(value = "sessionFactory") SessionFactory sessionFactory) {
        super(sessionFactory);
        setAllowCreate(true);
    }

}