package org.motechproject.ananya.domain.page;

import org.motechproject.ananya.domain.Sidebar;
import org.motechproject.ananya.support.diagnostics.CouchDBDiagnostic;
import org.motechproject.ananya.support.diagnostics.PostgresDataDiagnostic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Map;

@Service
public class MonitorPage {

    private String view = "admin/monitor";

    private CouchDBDiagnostic couchDBDiagnostic;
    private PostgresDataDiagnostic postgresDiagnostics;

    @Autowired
    public MonitorPage(CouchDBDiagnostic couchDBDiagnostic, PostgresDataDiagnostic postgresDiagnostics) {
        this.couchDBDiagnostic = couchDBDiagnostic;
        this.postgresDiagnostics = postgresDiagnostics;
    }

    public ModelAndView display() throws IOException {
        Map<String, String> couchDbData = couchDBDiagnostic.collect();
        Map<String, String> postgresData = postgresDiagnostics.collect();

        return new ModelAndView(view)
                .addObject("menuMap", new Sidebar().getMenu())
                .addObject("couchdbData", couchDbData)
                .addObject("postgresData", postgresData);
    }
}