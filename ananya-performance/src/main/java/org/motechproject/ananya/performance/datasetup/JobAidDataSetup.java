package org.motechproject.ananya.performance.datasetup;

import org.motechproject.ananya.domain.Operator;
import org.motechproject.ananya.performance.PerformanceData;
import org.motechproject.ananya.performance.PerformanceDataPublishService;
import org.motechproject.ananya.service.FrontLineWorkerService;
import org.motechproject.ananya.service.JobAidService;
import org.motechproject.ananya.service.OperatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobAidDataSetup {

    private static Logger log = LoggerFactory.getLogger(JobAidDataSetup.class);
    private final int usersPerOperator = 2000;

    private FrontLineWorkerService frontLineWorkerService;
    private OperatorService operatorService;
    private JobAidService jobAidService;
    private PerformanceDataPublishService publishService;

    @Autowired
    public JobAidDataSetup(FrontLineWorkerService frontLineWorkerService,
                           OperatorService operatorService,
                           JobAidService jobAidService,
                           @Qualifier("performanceDataPublishService")PerformanceDataPublishService publishService) {
        this.frontLineWorkerService = frontLineWorkerService;
        this.operatorService = operatorService;
        this.jobAidService = jobAidService;
        this.publishService = publishService;
    }

    @PerformanceData(testName = "jobaid", description = "create worker groups of different operators, msisdn incremented by jmeter thread count")
    public void loadData() {
        jobAidService.setPublishService(publishService);
        List<Operator> allOperators = operatorService.getAllOperators();
        for (int i = 0; i < allOperators.size(); i++)
            for (int j = 0; j < usersPerOperator; j++) {
                String msisdn = i + "" + j;
                jobAidService.createCallerData(msisdn, allOperators.get(i).getName());
                jobAidService.updateCurrentUsageForUser(msisdn, j % (allOperators.get(i).getAllowedUsagePerMonth() + 1));
            }
        log.info("Loaded jobaid performance data");
    }
}
