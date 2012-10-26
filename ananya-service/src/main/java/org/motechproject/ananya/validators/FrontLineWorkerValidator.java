package org.motechproject.ananya.validators;

import org.apache.commons.lang.StringUtils;
import org.motechproject.ananya.domain.FrontLineWorker;
import org.motechproject.ananya.domain.Location;
import org.motechproject.ananya.mapper.FrontLineWorkerMapper;
import org.motechproject.ananya.request.FrontLineWorkerRequest;
import org.motechproject.ananya.response.FLWValidationResponse;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class FrontLineWorkerValidator {

    public FLWValidationResponse validate(FrontLineWorker frontLineWorker, Location locationOfFrontLineWorker) {
        FLWValidationResponse flwValidationResponse = new FLWValidationResponse();
        if (StringUtils.length(frontLineWorker.getMsisdn()) < 10 || !StringUtils.isNumeric(frontLineWorker.getMsisdn()))
            flwValidationResponse.forInvalidMsisdn();
        if (StringUtils.isNotBlank(frontLineWorker.name()) && !Pattern.matches("[a-zA-Z0-9\\s\\.]*", frontLineWorker.name()))
            flwValidationResponse.forInvalidName();
        if (locationOfFrontLineWorker == null)
            flwValidationResponse.forInvalidLocation();
        if (!validFlwGuid(frontLineWorker.getFlwGuid()))
            flwValidationResponse.forInvalidFlwGuid();

        return flwValidationResponse;
    }

    private boolean validFlwGuid(UUID flwGuid) {
        return flwGuid != null && Pattern.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$", flwGuid.toString());
    }

    public FLWValidationResponse validateWithBulkValidation(FrontLineWorkerRequest frontLineWorkerRequest,
                                                            Location location,
                                                            Map<String, Integer> msisdnOccurrenceMap) {
        FLWValidationResponse validationResponse = validate(FrontLineWorkerMapper.mapFrom(frontLineWorkerRequest), location);
        if (msisdnOccurrenceMap.get(frontLineWorkerRequest.getMsisdn()) != 1)
            validationResponse.forDuplicates();
        return validationResponse;
    }
}
