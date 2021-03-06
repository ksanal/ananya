package org.motechproject.ananya.web;


import org.apache.commons.io.IOUtils;
import org.motechproject.ananya.domain.PeerServer;
import org.motechproject.ananya.domain.page.InquiryPage;
import org.motechproject.ananya.domain.page.LoginPage;
import org.motechproject.ananya.domain.page.LogsPage;
import org.motechproject.ananya.domain.page.MonitorPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
public class AdminController {

    private static Logger log = LoggerFactory.getLogger(AdminController.class);

    private MonitorPage monitorPage;
    private LoginPage loginPage;
    private InquiryPage inquiryPage;
    private LogsPage logsPage;
    private PeerServer peerServer;

    @Autowired
    public AdminController(MonitorPage monitorPage, LoginPage loginPage, InquiryPage inquiryPage, LogsPage logsPage, PeerServer peerServer) {
        this.monitorPage = monitorPage;
        this.loginPage = loginPage;
        this.inquiryPage = inquiryPage;
        this.logsPage = logsPage;
        this.peerServer = peerServer;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/login")
    public ModelAndView login(HttpServletRequest request) {
        final String error = request.getParameter("login_error");
        return loginPage.display(error);
    }

    @RequestMapping(method = RequestMethod.GET, value = {"/admin/monitor", "/internal/admin/monitor"})
    public ModelAndView showMonitorPage() throws Exception {
        log.info("monitor page displayed for active box");
        return monitorPage.display();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/peer/monitor")
    public void showMonitorPageForPeerBox(HttpServletResponse response) throws Exception {
        log.info("monitor page displayed for passive box");
        peerServer.copyResponse("internal/admin/monitor", response);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/inquiry")
    public ModelAndView showInquiryPage() throws Exception {
        log.info("inquiry page displayed");
        return inquiryPage.display();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/inquiry/data")
    @ResponseBody
    public Map<String, Object> showInquiryPage(@RequestParam String msisdn) throws Exception {
        log.info("inquiry data sent");
        return inquiryPage.display(msisdn);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/internal/admin/logs")
    public ModelAndView showPeerLogs() throws Exception {
        log.info("logs page displayed for active box");
        return logsPage.displayAsPeerLogs();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/logs")
    public ModelAndView showLogs() throws Exception {
        log.info("logs page displayed for active box");
        return logsPage.display();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/peer/logs")
    public void showLogsForPeerBox(HttpServletResponse response) throws Exception {
        log.info("logs page displayed for passive box");
        peerServer.copyResponse("internal/admin/logs", response);
    }

    @RequestMapping(method = RequestMethod.GET, value = {"/admin/logs/{file_name:.*}","/internal/admin/logs/{file_name:.*}"})
    public void getLog(@PathVariable("file_name") String logFilename, HttpServletResponse response) throws Exception {
        log.info(String.format("%s log file requested from active box", logFilename));
        IOUtils.copy(logsPage.getLogFile(logFilename), response.getOutputStream());
        response.setContentType("text/plain");
        response.flushBuffer();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/peer/logs/{file_name:.*}")
    public void getLogFromPeerBox(@PathVariable("file_name") String logFilename, HttpServletResponse response) throws Exception {
        log.info(String.format("%s log file requested from passive box", logFilename));
        peerServer.copyResponse("internal/admin/logs/" + logFilename, response);
    }


}