package org.motechproject.ananya.service;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ananya.domain.*;
import org.motechproject.ananya.repository.AllFrontLineWorkers;
import org.motechproject.ananya.repository.AllSMSReferences;
import org.motechproject.util.DateUtil;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.spy;

@PrepareForTest(DateUtil.class)
@RunWith(PowerMockRunner.class)
public class FrontLineWorkerServiceTest {

    private FrontLineWorkerService frontLineWorkerService;
    @Mock
    private AllFrontLineWorkers allFrontLineWorkers;
    @Mock
    private AllSMSReferences allSMSReferences;
    @Mock
    private FrontLineWorker mockedFrontLineWorker;

    @Before
    public void setUp() {
        initMocks(this);
        DateTime now = DateTime.now();
        spy(DateUtil.class);
        given(DateUtil.now()).willReturn(now);
        frontLineWorkerService = new FrontLineWorkerService(allFrontLineWorkers, allSMSReferences);
    }

    @Test
    public void shouldCreateNewFLWIfNotPresentInDB() {
        FrontLineWorker frontLineWorker = new FrontLineWorker("123", "name", Designation.ANM, new Location(), RegistrationStatus.REGISTERED);
        String msisdn = frontLineWorker.getMsisdn();

        when(allFrontLineWorkers.findByMsisdn(msisdn)).thenReturn(null);

        frontLineWorkerService.createOrUpdateUnregistered(msisdn, frontLineWorker.getOperator(), "circle");

        ArgumentCaptor<FrontLineWorker> captor = ArgumentCaptor.forClass(FrontLineWorker.class);
        verify(allFrontLineWorkers).add(captor.capture());
        FrontLineWorker savedFrontLineWorker = captor.getValue();
        assertEquals(frontLineWorker.getMsisdn(), savedFrontLineWorker.getMsisdn());
        assertEquals(frontLineWorker.getOperator(), savedFrontLineWorker.getOperator());
        assertEquals(RegistrationStatus.UNREGISTERED, savedFrontLineWorker.getStatus());
    }

    @Test
    public void shouldNotCreateFLWIfExistsInDBAndNotUpdateFLWIfOperatorIsNotModified() {
        String msisdn = "123";
        String circle = "circle";
        String operator = "airtel";
        FrontLineWorker frontLineWorker = new FrontLineWorker(msisdn, "name", Designation.ANM, new Location(), RegistrationStatus.REGISTERED);
        frontLineWorker.setOperator(operator);
        frontLineWorker.setCircle(circle);
        when(allFrontLineWorkers.findByMsisdn(msisdn)).thenReturn(frontLineWorker);

        FrontLineWorker frontLineWorkerFromDb = frontLineWorkerService.createOrUpdateUnregistered(msisdn, operator, circle);

        verify(allFrontLineWorkers, never()).add(frontLineWorker);
        verify(allFrontLineWorkers, never()).update(frontLineWorker);
        assertEquals(frontLineWorker, frontLineWorkerFromDb);
    }

    @Test
    public void shouldNotCreateNewFLWIfAlreadyPresentInDBButUpdateWhenOperatorIsDifferent() {
        String msisdn = "123";
        String circle = "circle";
        String oldOperator = "vodafone";
        String newOperator = "airtel";
        FrontLineWorker frontLineWorker = new FrontLineWorker(msisdn, "name", Designation.ANM, new Location(), RegistrationStatus.REGISTERED);
        frontLineWorker.setOperator(oldOperator);
        frontLineWorker.setCircle(circle);

        when(allFrontLineWorkers.findByMsisdn(msisdn)).thenReturn(frontLineWorker);

        FrontLineWorker frontLineWorkerFromDb = frontLineWorkerService.createOrUpdateUnregistered(msisdn, newOperator, circle);

        verify(allFrontLineWorkers, never()).add(frontLineWorker);
        verify(allFrontLineWorkers).update(frontLineWorker);
        assertEquals(frontLineWorker, frontLineWorkerFromDb);
    }

    @Test
    public void shouldNotCreateNewFLWIfAlreadyPresentInDBButUpdateWhenCircleIsDifferent() {
        String msisdn = "123";
        String operator = "vodafone";
        FrontLineWorker frontLineWorker = new FrontLineWorker(msisdn, "name", Designation.ANM, new Location(), RegistrationStatus.REGISTERED);
        frontLineWorker.setOperator(operator);

        when(allFrontLineWorkers.findByMsisdn(msisdn)).thenReturn(frontLineWorker);

        FrontLineWorker frontLineWorkerFromDb = frontLineWorkerService.createOrUpdateUnregistered(msisdn, operator, "circle");

        verify(allFrontLineWorkers, never()).add(frontLineWorker);
        verify(allFrontLineWorkers).update(frontLineWorker);
        assertEquals(frontLineWorker, frontLineWorkerFromDb);
    }

    @Test
    public void shouldNotCreateFLWIfAlreadyPresentInDBButShouldUpdateWhenCircleAndOperatorIsDifferent() {
        String msisdn = "123";
        String oldOperator = "vodafone";
        String newOperator = "airtel";
        FrontLineWorker frontLineWorker = new FrontLineWorker(msisdn, "name", Designation.ANM, new Location(), RegistrationStatus.REGISTERED);
        frontLineWorker.setOperator(oldOperator);
        frontLineWorker.setCircle("oldCircle");

        when(allFrontLineWorkers.findByMsisdn(msisdn)).thenReturn(frontLineWorker);

        FrontLineWorker frontLineWorkerFromDb = frontLineWorkerService.createOrUpdateUnregistered(msisdn, newOperator, "newCircle");

        verify(allFrontLineWorkers, never()).add(frontLineWorker);
        verify(allFrontLineWorkers).update(frontLineWorker);
        assertEquals(frontLineWorker, frontLineWorkerFromDb);
    }

    @Test
    public void shouldUpdatePromptsForFLW() {
        String callerId = "callerId";
        List<String> promptIds = Arrays.asList("prompt1", "prompt2");

        when(allFrontLineWorkers.findByMsisdn(callerId)).thenReturn(mockedFrontLineWorker);

        try {
            frontLineWorkerService.updatePromptsFor(callerId, promptIds);
        } catch (Exception e) {
        }

        verify(mockedFrontLineWorker).markPromptHeard(promptIds.get(0));
        verify(mockedFrontLineWorker).markPromptHeard(promptIds.get(1));
        verify(allFrontLineWorkers).update(mockedFrontLineWorker);
    }

    @Test
    public void shouldUpdateTheFrontLineWorkerWithUsage() {
        String callerId = "callerId";
        String operator = "airtel";
        Integer currentUsage = 20;
        FrontLineWorker frontLineWorker = new FrontLineWorker(callerId, operator);
        when(allFrontLineWorkers.findByMsisdn(callerId)).thenReturn(frontLineWorker);

        frontLineWorkerService.updateJobAidUsageAndAccessTime(callerId, currentUsage);

        assertEquals(currentUsage, frontLineWorker.getCurrentJobAidUsage());
        verify(allFrontLineWorkers).update(frontLineWorker);
    }

    @Test
    public void shouldFindByCallerId() {
        FrontLineWorker expectedFrontLineWorker = new FrontLineWorker();
        when(allFrontLineWorkers.findByMsisdn("123")).thenReturn(expectedFrontLineWorker);

        FrontLineWorker frontLineWorker = frontLineWorkerService.findByCallerId("123");

        assertEquals(expectedFrontLineWorker, frontLineWorker);
    }

    @Test
    public void shouldCreateFLWIfNotExists() {
        String msisdn = "919986574410";
        String name = "name";
        Designation designation = Designation.AWW;
        Location location = new Location("district", "block", "panchayat", 123, 124, 125);
        when(allFrontLineWorkers.findByMsisdn(msisdn)).thenReturn(null);

        FrontLineWorker frontLineWorker = frontLineWorkerService.createOrUpdate(new FrontLineWorker(msisdn, name, designation, location, RegistrationStatus.REGISTERED, null), location);

        verify(allFrontLineWorkers).add(frontLineWorker);
        assertEquals(msisdn, frontLineWorker.getMsisdn());
        assertEquals(name, frontLineWorker.getName());
        assertEquals(designation, frontLineWorker.getDesignation());
        assertEquals(location.getExternalId(), frontLineWorker.getLocationId());
    }

    @Test
    public void shouldUpdateFLWIfExists() {
        String msisdn = "123";
        String name = "name";
        Designation designation = Designation.AWW;
        Location location = new Location("district", "block", "panchayat", 123, 124, 125);
        FrontLineWorker frontLineWorker = new FrontLineWorker(msisdn, null, null, new Location(), RegistrationStatus.REGISTERED);

        when(allFrontLineWorkers.findByMsisdn(msisdn)).thenReturn(frontLineWorker);

        frontLineWorker = frontLineWorkerService.createOrUpdate(new FrontLineWorker(msisdn, name, designation, location, RegistrationStatus.REGISTERED), location);

        verify(allFrontLineWorkers).update(frontLineWorker);
        assertEquals(frontLineWorker.getName(), name);
        assertEquals(frontLineWorker.getDesignation(), designation);
        assertEquals(frontLineWorker.getLocationId(), location.getExternalId());
    }

    @Test
    public void shouldUpdateFLWIfExistsWithOperatorAndCircle() {
        String msisdn = "123";
        String name = "name";
        Designation designation = Designation.AWW;
        Location location = new Location("district", "block", "panchayat", 123, 124, 125);
        FrontLineWorker existingFrontLineWorker = new FrontLineWorker(msisdn, null, null, new Location(), RegistrationStatus.REGISTERED, null);
        when(allFrontLineWorkers.findByMsisdn(msisdn)).thenReturn(existingFrontLineWorker);

        frontLineWorkerService.createOrUpdate(new FrontLineWorker(msisdn, name, designation, location, RegistrationStatus.REGISTERED, null), location);

        ArgumentCaptor<FrontLineWorker> captor = ArgumentCaptor.forClass(FrontLineWorker.class);
        verify(allFrontLineWorkers).update(captor.capture());
        FrontLineWorker frontLineWorker = captor.getValue();
        assertEquals(name, frontLineWorker.getName());
        assertEquals(designation, frontLineWorker.getDesignation());
        assertEquals(designation, frontLineWorker.getDesignation());
        assertEquals(location.getExternalId(), frontLineWorker.getLocationId());
    }

    @Test
    public void shouldUpdateFLWUsageByAddingCurrentCallDuration() {
        String callerId = "callerId";
        String operator = "airtel";
        Integer currentUsage = 20;
        int callDuration = 15;
        int newUsage = currentUsage + callDuration;
        FrontLineWorker frontLineWorker = new FrontLineWorker(callerId, operator);
        frontLineWorker.setCurrentJobAidUsage(currentUsage);
        when(allFrontLineWorkers.findByMsisdn(callerId)).thenReturn(frontLineWorker);

        frontLineWorkerService.updateJobAidUsageAndAccessTime(callerId, callDuration);

        ArgumentCaptor<FrontLineWorker> captor = ArgumentCaptor.forClass(FrontLineWorker.class);
        verify(allFrontLineWorkers).update(captor.capture());

        FrontLineWorker captorValue = captor.getValue();
        assertEquals((Object) newUsage, captorValue.getCurrentJobAidUsage());
    }

    @Test
    public void shouldUpdateTheLastAccessTimeForFlw() {
        String callerId = "callerId";
        String operator = "airtel";
        int callDuration = 15;

        FrontLineWorker frontLineWorker = new FrontLineWorker(callerId, operator);
        when(allFrontLineWorkers.findByMsisdn(callerId)).thenReturn(frontLineWorker);

        frontLineWorkerService.updateJobAidUsageAndAccessTime(callerId, callDuration);

        ArgumentCaptor<FrontLineWorker> captor = ArgumentCaptor.forClass(FrontLineWorker.class);
        verify(allFrontLineWorkers).update(captor.capture());

        FrontLineWorker captorValue = captor.getValue();
        assertEquals(captorValue.getLastJobAidAccessTime().getMonthOfYear(), DateTime.now().getMonthOfYear());
        assertEquals(captorValue.getLastJobAidAccessTime().getYear(), DateTime.now().getYear());
    }

    @Test
    public void shouldResetLastAccessTimeForJobAidAndMaxUsagePromptHeardAtBeginningOfTheMonth() {
        String callerId = "callerId";
        String randomPromptKey = "random";
        String operator = "airtel";
        String promptKey = "Max_Usage";
        String circle = "circle";
        DateTimeUtils.setCurrentMillisFixed(DateTime.now().getMillis());

        FrontLineWorker frontLineWorker = new FrontLineWorker(callerId, operator);
        frontLineWorker.setLastJobAidAccessTime(DateTime.now().minusMonths(2));
        frontLineWorker.markPromptHeard(promptKey);
        frontLineWorker.markPromptHeard(randomPromptKey);
        when(allFrontLineWorkers.findByMsisdn(callerId)).thenReturn(frontLineWorker);

        FrontLineWorker flwForJobAidCallerData = frontLineWorkerService.findForJobAidCallerData("callerId", "operator", circle);


        assertEquals((Object) 0, flwForJobAidCallerData.getCurrentJobAidUsage());
        assertFalse(flwForJobAidCallerData.getPromptsHeard().containsKey(promptKey));
    }

    @Test
    public void shouldResetCurrentUsageAtBeginningOfMonthEvenThoughMaxUsageIsNotReached() {
        String callerId = "callerId";
        String operator = "airtel";
        String circle = "circle";
        DateTimeUtils.setCurrentMillisFixed(DateTime.now().getMillis());

        FrontLineWorker frontLineWorker = new FrontLineWorker(callerId, operator);
        frontLineWorker.setLastJobAidAccessTime(DateTime.now().minusMonths(2));
        when(allFrontLineWorkers.findByMsisdn(callerId)).thenReturn(frontLineWorker);

        FrontLineWorker flwForJobAidCallerData = frontLineWorkerService.findForJobAidCallerData("callerId", "operator", circle);

        assertEquals((Object) 0, flwForJobAidCallerData.getCurrentJobAidUsage());
        assertFalse(flwForJobAidCallerData.getPromptsHeard().containsKey("Max_Usage"));
    }

    @Test
    public void shouldAddSMSReferenceNumber() {
        SMSReference smsReference = new SMSReference("1234", "123456");

        frontLineWorkerService.addSMSReferenceNumber(smsReference);

        verify(allSMSReferences).add(smsReference);
    }

    @Test
    public void shouldUpdateSMSReferenceNumber() {
        SMSReference smsReference = new SMSReference("1234", "123456");

        frontLineWorkerService.updateSMSReferenceNumber(smsReference);

        verify(allSMSReferences).update(smsReference);
    }

    @Test
    public void shouldGetAllFrontLineWorkers() {
        ArrayList<FrontLineWorker> expectedFrontLineWorkerList = new ArrayList<FrontLineWorker>();
        when(allFrontLineWorkers.getAll()).thenReturn(expectedFrontLineWorkerList);

        List<FrontLineWorker> actualFrontLineWorkerList = frontLineWorkerService.getAll();

        assertEquals(expectedFrontLineWorkerList, actualFrontLineWorkerList);
    }

    @Test
    public void shouldCreateANewFrontLineWorkerWithIfDoesNotExist() {
        String callerId = "1234";
        String circle = "bihar";
        String operator = "airtel";

        when(allFrontLineWorkers.findByMsisdn(callerId)).thenReturn(null);

        frontLineWorkerService.findForJobAidCallerData(callerId, operator, circle);

        ArgumentCaptor<FrontLineWorker> captor = ArgumentCaptor.forClass(FrontLineWorker.class);
        verify(allFrontLineWorkers).add(captor.capture());
        FrontLineWorker frontLineWorker = captor.getValue();
        assertEquals(circle, frontLineWorker.getCircle());
        assertEquals(operator, frontLineWorker.getOperator());
        assertEquals(RegistrationStatus.UNREGISTERED, frontLineWorker.getStatus());
    }

    @Test
    public void shouldNotUpdateExistingFLWWhenGivenModificationTimeIsOlderThanExistingOne() {
        String msisdn = "123";
        String name = "name";
        Designation designation = Designation.AWW;
        Location location = new Location("district", "block", "panchayat", 123, 124, 125);
        String circle = "bihar";
        String operator = "airtel";
        FrontLineWorker existingFrontLineWorker = new FrontLineWorker(msisdn, null, null, new Location(), RegistrationStatus.REGISTERED, DateTime.now());
        when(allFrontLineWorkers.findByMsisdn(msisdn)).thenReturn(existingFrontLineWorker);

        frontLineWorkerService.createOrUpdate(new FrontLineWorker(msisdn, name, designation, location, RegistrationStatus.REGISTERED, DateTime.now().minusDays(1)), location);

        verify(allFrontLineWorkers, never()).update(any(FrontLineWorker.class));
    }

    @Test
    public void shouldUpdateExistingFLWWhenLastModifiedTimeIsGiven() {
        String msisdn = "123";
        String name = "name";
        Designation designation = Designation.AWW;
        Location location = new Location("district", "block", "panchayat", 123, 124, 125);
        String circle = "bihar";
        String operator = "airtel";

        FrontLineWorker existingFrontLineWorker = new FrontLineWorker(msisdn, null, null, new Location(), RegistrationStatus.REGISTERED, null);
        when(allFrontLineWorkers.findByMsisdn(msisdn)).thenReturn(existingFrontLineWorker);

        DateTime lastModified = DateTime.now();
        frontLineWorkerService.createOrUpdate(new FrontLineWorker(msisdn, name, designation, location, RegistrationStatus.REGISTERED, lastModified), location);

        ArgumentCaptor<FrontLineWorker> captor = ArgumentCaptor.forClass(FrontLineWorker.class);
        verify(allFrontLineWorkers).update(captor.capture());
        FrontLineWorker frontLineWorker = captor.getValue();
        assertEquals(lastModified, frontLineWorker.getLastModified());
    }

    @Test
    public void shouldUpdateExistingFLWWithExistingLastModificationDateIfNewLastModifiedTimeIsNull() {
        String msisdn = "123";
        String name = "name";
        Designation designation = Designation.AWW;
        Location location = new Location("district", "block", "panchayat", 123, 124, 125);
        String circle = "bihar";
        String operator = "airtel";
        DateTime now = DateUtil.now();

        given(DateUtil.now()).willReturn(null);
        FrontLineWorker existingFrontLineWorker = new FrontLineWorker(msisdn, null, null, new Location(), RegistrationStatus.REGISTERED, now);
        when(allFrontLineWorkers.findByMsisdn(msisdn)).thenReturn(existingFrontLineWorker);

        frontLineWorkerService.createOrUpdate(new FrontLineWorker(msisdn, name, designation, location, RegistrationStatus.REGISTERED, null), location);

        ArgumentCaptor<FrontLineWorker> captor = ArgumentCaptor.forClass(FrontLineWorker.class);
        verify(allFrontLineWorkers).update(captor.capture());
        FrontLineWorker frontLineWorker = captor.getValue();
        assertEquals(now, frontLineWorker.getLastModified());
    }

    @Test
    public void shouldDeduceCorrectFLWStatusBasedOnInformation() {
        Location completeLocation = new Location("district", "block", "panchayat", 1, 1, 1);
        Location incompleteLocation = new Location("district", "block", "", 1, 1, 0);
        Location defaultLocation = Location.getDefaultLocation();

        FrontLineWorker flwWithCompleteDetails = new FrontLineWorker(
                "1234", "name", Designation.ANM, completeLocation , RegistrationStatus.REGISTERED);
        FrontLineWorker flwWithoutName = new FrontLineWorker(
                "1234", "", Designation.ANM, completeLocation , RegistrationStatus.REGISTERED);
        FrontLineWorker flwWithoutDesignation = new FrontLineWorker(
                "1234", "name", null, completeLocation , RegistrationStatus.REGISTERED);
        FrontLineWorker flwWithInvalidDesignation = new FrontLineWorker(
                "1234", "name", Designation.INVALID, completeLocation , RegistrationStatus.REGISTERED);
        FrontLineWorker flwWithDefaultLocation = new FrontLineWorker(
                "1234", "name", Designation.ANM, defaultLocation , RegistrationStatus.REGISTERED);
        FrontLineWorker flwWithIncompleteLocation = new FrontLineWorker(
                "1234", "name", Designation.ANM, incompleteLocation, RegistrationStatus.REGISTERED);
        FrontLineWorker flwWithNoDetails = new FrontLineWorker(
                "1234", "", null, defaultLocation, RegistrationStatus.REGISTERED);

        assertEquals(RegistrationStatus.REGISTERED,
                frontLineWorkerService.deduceRegistrationStatus(flwWithCompleteDetails, completeLocation));
        assertEquals(RegistrationStatus.PARTIALLY_REGISTERED,
                frontLineWorkerService.deduceRegistrationStatus(flwWithoutName, completeLocation));
        assertEquals(RegistrationStatus.PARTIALLY_REGISTERED,
                frontLineWorkerService.deduceRegistrationStatus(flwWithoutDesignation, completeLocation));
        assertEquals(RegistrationStatus.PARTIALLY_REGISTERED,
                frontLineWorkerService.deduceRegistrationStatus(flwWithInvalidDesignation, completeLocation));
        assertEquals(RegistrationStatus.PARTIALLY_REGISTERED,
                frontLineWorkerService.deduceRegistrationStatus(flwWithDefaultLocation, defaultLocation));
        assertEquals(RegistrationStatus.PARTIALLY_REGISTERED,
                frontLineWorkerService.deduceRegistrationStatus(flwWithIncompleteLocation, incompleteLocation));
        assertEquals(RegistrationStatus.UNREGISTERED,
                frontLineWorkerService.deduceRegistrationStatus(flwWithNoDetails, defaultLocation));
    }
}
