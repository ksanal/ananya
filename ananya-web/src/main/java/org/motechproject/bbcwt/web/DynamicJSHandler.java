package org.motechproject.bbcwt.web;

import org.motechproject.bbcwt.repository.tree.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value="/dynamic/js")
public class DynamicJSHandler {

    private NodeRepository nodeRepository;

    @Autowired
    public  DynamicJSHandler(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/metadata.js")
    public ModelAndView serveMetaData(HttpServletResponse response) throws Exception {
        response.setContentType("application/javascript");
        return new ModelAndView("metadata");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/course_data.js")
    @ResponseBody
    public String serveCourseData(HttpServletResponse response) throws Exception {
        response.setContentType("application/javascript");
        return "var courseData = " + nodeRepository.nodeAsJson("JobAidCourse") + ";";
    }
}