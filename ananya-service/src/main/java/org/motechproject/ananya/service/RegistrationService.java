package org.motechproject.ananya.service;

import org.motechproject.ananya.domain.*;
import org.motechproject.ananya.domain.dimension.FrontLineWorkerDimension;
import org.motechproject.ananya.mapper.FrontLineWorkerMapper;
import org.motechproject.ananya.request.FrontLineWorkerRequest;
import org.motechproject.ananya.response.FrontLineWorkerResponse;
import org.motechproject.ananya.response.RegistrationResponse;
import org.motechproject.ananya.response.ValidationResponse;
import org.motechproject.ananya.validators.FrontLineWorkerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class RegistrationService {
    private static Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private FrontLineWorkerService frontLineWorkerService;
    private FrontLineWorkerDimensionService frontLineWorkerDimensionService;
    private CourseItemMeasureService courseItemMeasureService;
    private RegistrationMeasureService registrationMeasureService;
    private LocationService locationService;

    @Autowired
    public RegistrationService(FrontLineWorkerService frontLineWorkerService,
                               CourseItemMeasureService courseItemMeasureService,
                               FrontLineWorkerDimensionService frontLineWorkerDimensionService,
                               RegistrationMeasureService registrationMeasureService,
                               LocationService locationService) {
        this.frontLineWorkerService = frontLineWorkerService;
        this.courseItemMeasureService = courseItemMeasureService;
        this.frontLineWorkerDimensionService = frontLineWorkerDimensionService;
        this.registrationMeasureService = registrationMeasureService;
        this.locationService = locationService;
    }

    public List<RegistrationResponse> registerAllFLWs(List<FrontLineWorkerRequest> frontLineWorkerRequests) {
        LocationList locationList = new LocationList(locationService.getAll());
        List<RegistrationResponse> registrationResponses = new ArrayList<RegistrationResponse>();
        for (FrontLineWorkerRequest frontLineWorkerRequest : frontLineWorkerRequests) {
            RegistrationResponse registrationResponse = registerFlw(frontLineWorkerRequest.getMsisdn(),
                    frontLineWorkerRequest.getName(),
                    frontLineWorkerRequest.getDesignation(),
                    frontLineWorkerRequest.getOperator(),
                    frontLineWorkerRequest.getLocation().getDistrict(),
                    frontLineWorkerRequest.getLocation().getBlock(),
                    frontLineWorkerRequest.getLocation().getPanchayat(),
                    frontLineWorkerRequest.getCircle(),
                    locationList);
            registrationResponses.add(registrationResponse);
        }
        return registrationResponses;
    }

    public RegistrationResponse createOrUpdateFLW(FrontLineWorkerRequest frontLineWorkerRequest) {
        LocationList locationList = new LocationList(locationService.getAll());
        return registerFlw(frontLineWorkerRequest.getMsisdn(),
                frontLineWorkerRequest.getName(),
                frontLineWorkerRequest.getDesignation(),
                frontLineWorkerRequest.getOperator(),
                frontLineWorkerRequest.getLocation().getDistrict(),
                frontLineWorkerRequest.getLocation().getBlock(),
                frontLineWorkerRequest.getLocation().getPanchayat(),
                frontLineWorkerRequest.getCircle(),
                locationList);
    }

    public List<FrontLineWorkerResponse> getFilteredFLW(Long msisdn, String name, String status, String designation, String operator, String circle, Date activityStartDate, Date activityEndDate) {
        List<FrontLineWorkerResponse> filteredFlws = new ArrayList<FrontLineWorkerResponse>();
        List<Long> allFilteredMsisdns = new ArrayList<Long>();
        if (activityStartDate != null && activityEndDate != null) {
            allFilteredMsisdns = courseItemMeasureService.getAllFrontLineWorkerMsisdnsBetween(activityStartDate, activityEndDate);
            if (allFilteredMsisdns.isEmpty())
                return filteredFlws;
        }
        if (msisdn != null) allFilteredMsisdns.add(msisdn);
        List<FrontLineWorkerDimension> frontLineWorkerDimensions = frontLineWorkerDimensionService.getFilteredFLW(allFilteredMsisdns, name, status, designation, operator, circle);

        for (FrontLineWorkerDimension frontLineWorkerDimension : frontLineWorkerDimensions)
            filteredFlws.add(FrontLineWorkerMapper.mapFrom(frontLineWorkerDimension));

        return filteredFlws;
    }

    private RegistrationResponse registerFlw(String callerId, String name, String designation, String operator, String district, String block, String panchayat, String circle, LocationList locationList) {
        Location location = locationList.findFor(district, block, panchayat);
        RegistrationResponse registrationResponse = new RegistrationResponse(name, callerId, designation, operator, circle, district, block, panchayat);
        FrontLineWorkerValidator frontLineWorkerValidator = new FrontLineWorkerValidator();
        RegistrationStatus registrationStatus = isInvalidDesignation(designation) ? RegistrationStatus.PARTIALLY_REGISTERED : RegistrationStatus.REGISTERED;
        FrontLineWorker frontLineWorker = new FrontLineWorker(callerId, name, Designation.getFor(designation), operator, circle, location, registrationStatus);

        ValidationResponse validationResponse = frontLineWorkerValidator.validate(frontLineWorker, location);
        if(validationResponse.isInValid())
            return registrationResponse.withValidationResponse(validationResponse);

        frontLineWorker = frontLineWorkerService.createOrUpdate(frontLineWorker, location);
        registrationMeasureService.createRegistrationMeasure(frontLineWorker.getMsisdn());

        log.info("Registered new FLW:" + callerId);
        return registrationResponse.withNewRegistrationDone();
    }

    private boolean isInvalidDesignation(String designation) {
        return Designation.isInValid(designation);
    }
}
