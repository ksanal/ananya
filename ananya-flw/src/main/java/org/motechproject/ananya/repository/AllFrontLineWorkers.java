package org.motechproject.ananya.repository;

import org.ektorp.BulkDeleteDocument;
import org.ektorp.CouchDbConnector;
import org.ektorp.UpdateConflictException;
import org.ektorp.ViewQuery;
import org.ektorp.support.GenerateView;
import org.ektorp.support.View;
import org.motechproject.ananya.domain.FrontLineWorker;
import org.motechproject.dao.MotechBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AllFrontLineWorkers extends MotechBaseRepository<FrontLineWorker> {

    @Autowired
    protected AllFrontLineWorkers(@Qualifier("ananyaDbConnector") CouchDbConnector db) {
        super(FrontLineWorker.class, db);
        initStandardDesignDocument();
    }

    @GenerateView
    public FrontLineWorker findByMsisdn(String msisdn) {
        ViewQuery viewQuery = createQuery("by_msisdn").key(msisdn).includeDocs(true);
        List<FrontLineWorker> workers = db.queryView(viewQuery, FrontLineWorker.class);
        if (workers == null || workers.isEmpty()) return null;
        return workers.get(0);
    }

    @GenerateView
    public List<FrontLineWorker> findByLocationId(String locationId) {
        ViewQuery viewQuery = createQuery("by_locationId").key(locationId).includeDocs(true);
        List<FrontLineWorker> workers = db.queryView(viewQuery, FrontLineWorker.class);
        return workers == null ? new ArrayList<FrontLineWorker>() : workers;
    }

    public List<FrontLineWorker> getAllForMsisdn(String msisdn) {
        ViewQuery viewQuery = createQuery("by_msisdn").key(msisdn).includeDocs(true);
        List<FrontLineWorker> workers = db.queryView(viewQuery, FrontLineWorker.class);
        if (workers == null || workers.isEmpty()) return null;
        return workers;
    }

    public List<FrontLineWorker> getMsisdnsFrom(String startKey, int count) {
        return db.queryView(
                createQuery("by_msisdn").startKey(startKey).limit(count).includeDocs(true),
                FrontLineWorker.class);
    }

    @View(name = "by_invalid_msisdn", map="function(doc) { if(doc.type === 'FrontLineWorker' && doc.msisdn && doc.msisdn.indexOf('E') !== -1) {emit(doc.msisdn, doc._id)} }")
    public void deleteFLWsWithInvalidMsisdn() {
        List<BulkDeleteDocument> bulkDeleteDocuments = new ArrayList<>();

        List<FrontLineWorker> invalidFlws = queryView("by_invalid_msisdn");
        for (FrontLineWorker invalidFlw : invalidFlws) {
            bulkDeleteDocuments.add(BulkDeleteDocument.of(invalidFlw));
        }
        db.executeBulk(bulkDeleteDocuments);
    }

    public FrontLineWorker updateFlw(FrontLineWorker frontLineWorker) {
        try {
            update(frontLineWorker);
        } catch (UpdateConflictException e) {
            FrontLineWorker existingFlw = get(frontLineWorker.getId());
            frontLineWorker = existingFlw.updateWith(frontLineWorker);
            update(frontLineWorker);
            log.error(e.getMessage());
        }
        return frontLineWorker;
    }
}
