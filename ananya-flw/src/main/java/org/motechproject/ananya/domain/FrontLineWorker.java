package org.motechproject.ananya.domain;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.TypeDiscriminator;
import org.motechproject.model.MotechBaseDataObject;

@TypeDiscriminator("doc.documentType === 'FrontLineWorker'")
public class FrontLineWorker extends MotechBaseDataObject {
    @JsonProperty
    private String documentType = "FrontLineWorker";
    @JsonProperty
    private String name;

    public FrontLineWorker() {
    }

    public FrontLineWorker(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
