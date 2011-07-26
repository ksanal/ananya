package org.motechproject.bbcwt.domain;

import org.ektorp.support.CouchDbDocument;
import org.ektorp.support.TypeDiscriminator;

@TypeDiscriminator("doc.documentType == 'HealthWorker'")
public class HealthWorker extends CouchDbDocument {
    private String callerId;

    public HealthWorker() {

    }

    public HealthWorker(String callerId) {
        this.callerId = callerId;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }
}
