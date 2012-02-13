package org.motechproject.ananya.service;

import org.motechproject.ananya.domain.FrontLineWorker;
import org.motechproject.ananya.domain.Location;
import org.motechproject.ananya.domain.LogData;
import org.motechproject.ananya.domain.RegistrationLog;
import org.motechproject.ananya.domain.dimension.FrontLineWorkerDimension;
import org.motechproject.ananya.domain.dimension.LocationDimension;
import org.motechproject.ananya.domain.dimension.TimeDimension;
import org.motechproject.ananya.domain.measure.RegistrationMeasure;
import org.motechproject.ananya.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportDataMeasure {

    private AllRegistrationLogs allRegistrationLogs;
    private AllFrontLineWorkers allFrontLineWorkers;
    private AllLocations allLocations;

    private AllLocationDimensions allLocationDimensions;
    private AllFrontLineWorkerDimensions allFrontLineWorkerDimensions;
    private AllTimeDimensions allTimeDimensions;

    private ReportDB reportDB;

    @Autowired
    public ReportDataMeasure(
            AllRegistrationLogs allRegistrationLogs, AllFrontLineWorkers allFrontLineWorkers, AllLocations allLocations,
            AllLocationDimensions allLocationDimensions, AllFrontLineWorkerDimensions allFrontLineWorkerDimensions,
            AllTimeDimensions allTimeDimensions, ReportDB reportDB) {

        this.allRegistrationLogs = allRegistrationLogs;
        this.allFrontLineWorkers = allFrontLineWorkers;
        this.allLocations = allLocations;

        this.allFrontLineWorkerDimensions = allFrontLineWorkerDimensions;
        this.allTimeDimensions = allTimeDimensions;
        this.allLocationDimensions = allLocationDimensions;
        this.reportDB = reportDB;
    }

    public void createRegistrationMeasure(LogData logData) {

        RegistrationLog registrationLog = allRegistrationLogs.get(logData.getDataId());
        FrontLineWorker frontLineWorker = allFrontLineWorkers.findByMsisdn(registrationLog.getCallerId());
        Location location = allLocations.get(frontLineWorker.getLocationId());

        FrontLineWorkerDimension frontLineWorkerDimension = allFrontLineWorkerDimensions.getOrMakeFor(
                Long.valueOf(frontLineWorker.getMsisdn()), registrationLog.getOperator(),
                frontLineWorker.getName(), frontLineWorker.getStatus().toString());

        LocationDimension locationDimension = allLocationDimensions.getOrMakeFor(
                location.getExternalId(),location.district(), location.blockName(), location.panchayat());

        TimeDimension timeDimension = allTimeDimensions.getOrMakeFor(registrationLog.getStartTime());

        RegistrationMeasure registrationMeasure = new RegistrationMeasure(frontLineWorkerDimension, locationDimension, timeDimension);
        reportDB.add(registrationMeasure);
    }
}