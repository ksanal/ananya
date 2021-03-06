package org.motechproject.ananya.repository;

import org.ektorp.BulkDeleteDocument;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.support.View;
import org.motechproject.ananya.domain.CertificationCourseLog;
import org.motechproject.dao.MotechBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AllCertificateCourseLogs extends MotechBaseRepository<CertificationCourseLog> {
    @Autowired
    public AllCertificateCourseLogs(@Qualifier("ananyaDbConnector") CouchDbConnector dbCouchDbConnector) {
        super(CertificationCourseLog.class, dbCouchDbConnector);
    }

    @View(name = "by_callId", map = "function(doc) { if (doc.type=='CertificationCourseLog') { emit([doc.callId], doc); } }")
    public CertificationCourseLog findByCallId(String callId) {
        ViewQuery query = createQuery("by_callId").key(ComplexKey.of(callId));
        List<CertificationCourseLog> result = db.queryView(query, CertificationCourseLog.class);
        if(result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    @View(name = "by_invalid_msisdn", map="function(doc) { if(doc.type === 'CertificationCourseLog' && doc.callerId && doc.callerId.indexOf('E') !== -1 ) {emit(doc._id)} }")
    public void deleteCCLogsForInvalidMsisdns() {
        List<CertificationCourseLog> certificationCourseLogs = queryView("by_invalid_msisdn");
        List<BulkDeleteDocument> bulkDeleteDocuments = new ArrayList<>();
        for (CertificationCourseLog certificationCourseLog : certificationCourseLogs) {
            bulkDeleteDocuments.add(BulkDeleteDocument.of(certificationCourseLog));
        }
        db.executeBulk(bulkDeleteDocuments);
    }

    public void deleteFor(String callId) {
        CertificationCourseLog courseLog = findByCallId(callId);
        if(courseLog != null){
            remove(courseLog);
        }
    }
}
