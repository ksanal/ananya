package org.motechproject.ananya.service;

import org.motechproject.ananya.domain.FrontLineWorker;
import org.motechproject.ananya.domain.JobAidCallDetails;
import org.motechproject.ananya.domain.Location;
import org.motechproject.ananya.domain.SMSReference;
import org.motechproject.ananya.exception.ValidationException;
import org.motechproject.ananya.mapper.FLWUsageResponseMapper;
import org.motechproject.ananya.request.FLWNighttimeCallsRequest;
import org.motechproject.ananya.response.FLWNighttimeCallsResponse;
import org.motechproject.ananya.response.FLWUsageResponse;
import org.motechproject.ananya.service.measure.CallDurationMeasureService;
import org.motechproject.ananya.service.measure.response.CallDetailsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FLWDetailsService {
    private FrontLineWorkerService frontLineWorkerService;
    private CallDurationMeasureService callDurationMeasureService;
    private LocationService locationService;
    private SMSReferenceService smsReferenceService;
    private FLWUsageResponseMapper frontLineWorkerUsageResponseMapper;

    @Autowired
    public FLWDetailsService(FrontLineWorkerService frontLineWorkerService,
                             CallDurationMeasureService callDurationMeasureService,
                             LocationService locationService,
                             SMSReferenceService smsReferenceService,
                             FLWUsageResponseMapper frontLineWorkerUsageResponseMapper) {
        this.frontLineWorkerService = frontLineWorkerService;
        this.callDurationMeasureService = callDurationMeasureService;
        this.locationService = locationService;
        this.smsReferenceService = smsReferenceService;
        this.frontLineWorkerUsageResponseMapper = frontLineWorkerUsageResponseMapper;
    }

    public FLWUsageResponse getUsage(String flwId) {
        FrontLineWorker frontLineWorker = getFrontLineWorker(UUID.fromString(flwId));
        String msisdn = frontLineWorker.getMsisdn();
        Location location = locationService.findByExternalId(frontLineWorker.getLocationId());
        CallDetailsResponse callDetails = callDurationMeasureService.getCallDetails(msisdn);
        SMSReference smsReferenceNumber = smsReferenceService.getSMSReferenceNumber(msisdn);

        return frontLineWorkerUsageResponseMapper.mapUsageResponse(frontLineWorker, location, callDetails, smsReferenceNumber);
    }

    private FrontLineWorker getFrontLineWorker(UUID flwId) {
        FrontLineWorker frontLineWorker = frontLineWorkerService.findByFlwId(flwId);
        if (frontLineWorker == null) {
            throw new ValidationException(String.format("unknown flw id: %s", flwId));
        }
        return frontLineWorker;
    }

    public FLWNighttimeCallsResponse getNighttimeCalls(FLWNighttimeCallsRequest nighttimeCallsRequest) {
        FrontLineWorker frontLineWorker = getFrontLineWorker(nighttimeCallsRequest.getFlwId());

        List<JobAidCallDetails> nighttimeCalls = callDurationMeasureService.getJobAidCallDurations(frontLineWorker.getMsisdn(), nighttimeCallsRequest.getStartDate(), nighttimeCallsRequest.getEndDate());

        return frontLineWorkerUsageResponseMapper.mapNighttimeCallsResponse(nighttimeCalls);
    }
}
