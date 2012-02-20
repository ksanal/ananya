package org.motechproject.ananya.web;


import org.motechproject.ananya.domain.FrontLineWorker;
import org.motechproject.ananya.domain.Location;
import org.motechproject.ananya.repository.AllFrontLineWorkers;
import org.motechproject.ananya.repository.AllLocations;
import org.motechproject.ananya.view.FrontLineWorkerPresenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminController {
    private static Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AllFrontLineWorkers allFrontLineWorkers;

    @Autowired
    private AllLocations allLocations;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/admin/")
    public ModelAndView showTestPage() {
        List<FrontLineWorkerPresenter> workerPresenters = new ArrayList<FrontLineWorkerPresenter>();
        List<FrontLineWorker> workers = allFrontLineWorkers.getAll();
        try {
            for (FrontLineWorker worker : workers) {
                Location location = allLocations.get(worker.getLocationId());
                workerPresenters.add(new FrontLineWorkerPresenter(worker.getId(), worker.getMsisdn(), worker.status().toString(),
                        location.blockName(), location.district(), location.panchayat(), worker.registeredDate()));
            }
        } catch (Exception e) {
            log.error("Exception:", e);
        }
        return new ModelAndView("admin").addObject("workerPresenters", workerPresenters);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/admin/flw/delete")
    @ResponseBody
    public String removeFlw(String id) {
        FrontLineWorker flwToDelete = allFrontLineWorkers.get(id);
        if (flwToDelete != null) {
            allFrontLineWorkers.remove(flwToDelete);
            log.info("Deleted : " + flwToDelete);
        }
        return "Deleted : " + flwToDelete;
    }
}