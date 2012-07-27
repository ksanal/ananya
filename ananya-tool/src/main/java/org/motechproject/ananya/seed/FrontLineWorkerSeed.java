package org.motechproject.ananya.seed;

import liquibase.util.csv.CSVReader;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.motechproject.ananya.domain.FrontLineWorker;
import org.motechproject.ananya.domain.LocationList;
import org.motechproject.ananya.domain.dimension.FrontLineWorkerDimension;
import org.motechproject.ananya.repository.AllFrontLineWorkers;
import org.motechproject.ananya.response.RegistrationResponse;
import org.motechproject.ananya.seed.service.FrontLineWorkerExecutable;
import org.motechproject.ananya.seed.service.FrontLineWorkerSeedService;
import org.motechproject.ananya.service.LocationService;
import org.motechproject.ananya.service.RegistrationService;
import org.motechproject.deliverytools.seed.Seed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Date;
import java.util.List;

@Component
public class FrontLineWorkerSeed {

    private static final Logger log = LoggerFactory.getLogger(FrontLineWorkerSeed.class);
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private FrontLineWorkerSeedService seedService;
    @Autowired
    private AllFrontLineWorkers allFrontLineWorkers;

    @Value("#{ananyaProperties['seed.flw.file']}")
    private String inputFileName;
    @Value("#{ananyaProperties['seed.flw.file.out']}")
    private String outputFileName;
    @Value("#{ananyaProperties['environment']}")
    private String environment;

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext-tool.xml");
        FrontLineWorkerSeed frontLineWorkerSeed =
                (FrontLineWorkerSeed) context.getBean("frontLineWorkerSeed");
        frontLineWorkerSeed.activateNewRegistrationStatusesForAllFLWs();
    }

    @Seed(priority = 0, version = "1.0", comment = "FLWs pre-registration via CSV, 20988 nos [P+C]")
    public void createFrontlineWorkersFromCSVFile() throws IOException {

        String inputCSVFile = environment.equals("prod") ? inputFileName : getClass().getResource(inputFileName).getPath();
        String outputFilePath = new File(inputCSVFile).getParent();
        String outputCSVFile = outputFilePath + File.separator + outputFileName + new Date().getTime();

        File file = new File(outputCSVFile);
        file.createNewFile();

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputCSVFile));
        CSVReader csvReader = new CSVReader(new FileReader(inputCSVFile));
        LocationList locationList = new LocationList(locationService.getAll());

        String msisdn, name, designation, currentDistrict, currentBlock, currentPanchayat;
        String[] currentRow;
        csvReader.readNext();
        currentRow = csvReader.readNext();

        while (currentRow != null) {
            msisdn = currentRow[0];
            name = currentRow[1];
            designation = currentRow[2];
            currentDistrict = currentRow[3];
            currentBlock = currentRow[4];
            currentPanchayat = currentRow[5];

            RegistrationResponse registrationResponse = registrationService.registerFlw(
                    msisdn, name, designation, currentDistrict, currentBlock, currentPanchayat, locationList);

            writer.write(msisdn + " : " + registrationResponse.getMessage());
            writer.newLine();
            currentRow = csvReader.readNext();
        }
        writer.close();
    }

    @Seed(priority = 7, version = "1.1", comment = "FLWs registered via calls should be now 'unregistered' status. [P+C]")
    public void updateRegistrationStatusOfFrontLineWorkersRegisteredViaCalls() {
        seedService.correctRegistrationStatusInCouchAndPostgres();
    }

    @Seed(priority = 6, version = "1.1", comment = "1) merging duplicates")
    public void updateCorrectCallerIdsCircleOperatorAndDesignation() {
        List<FrontLineWorker> allFrontLineWorkers = seedService.getAllFromCouchDb();
        seedService.correctDuplicatesInCouchAndPostgres(allFrontLineWorkers);
    }

    @Seed(priority = 5, version = "1.1", comment = "1) Appending 91 to callerIds [P+C], 2) Update missing designation, operator [P], 3) Add default circle [C] ")
    public void updateOperatorDesignationCircleAndCorrectMsisdnInPostgresAndCouchDb() {
        String defaultCircle = "bihar";
        List<FrontLineWorker> allFrontLineWorkers = seedService.getAllFromCouchDb();
        seedService.updateOperatorDesignationCircleAndCorrectMsisdnInPostgresAndCouchDb(allFrontLineWorkers, defaultCircle);
    }

    @Seed(priority = 4, version = "1.3", comment = "Correction of invalid status for AWW [P+C]. If getting rid of designation from couchdb, updated only the postgres entries.")
    public void correctInvalidDesignationsForAnganwadi() throws IOException {

        String inputCSVFile = environment.equals("prod") ? inputFileName : getClass().getResource(inputFileName).getPath();
        String outputFilePath = new File(inputCSVFile).getParent();
        String outputCSVFile = outputFilePath + File.separator + outputFileName + new Date().getTime();

        File file = new File(outputCSVFile);
        file.createNewFile();
        CSVReader csvReader = new CSVReader(new FileReader(inputCSVFile));

        String msisdn, designation;
        String[] currentRow;
        csvReader.readNext();
        currentRow = csvReader.readNext();

        while (currentRow != null) {
            msisdn = currentRow[0];
            designation = currentRow[2];
            if ("AWW".equalsIgnoreCase(designation))
                seedService.correctInvalidDesignationsForAnganwadi(msisdn);
            currentRow = csvReader.readNext();
        }
    }

    @Seed(priority = 0, version = "1.3", comment = "Sanitization of registration status of FLWs")
    public void correctInvalidRegistrationStatusForAllFLWs() {
        log.info("Correcting Registration status of FLWs");
        int startId = 1;
        int counter = 0;

        log.info("Fetching FLWs from start id : " + startId);
        List<FrontLineWorkerDimension> frontLineWorkerDimensions = seedService.getFrontLineWorkers();

        for (FrontLineWorkerDimension frontLineWorkerDimension : frontLineWorkerDimensions) {
            counter++;
            seedService.correctRegistrationStatus(frontLineWorkerDimension);
            if (counter % 100 == 0)
                log.info("Completed " + counter + " of " + frontLineWorkerDimensions.size() + " FLWs");
        }
        log.info("Correction of registration statuses done.");
    }

    private void doWithBatch(FrontLineWorkerExecutable executable, int batchSize) {
        // if (batchSize > 1000)
        //      You're treading on thin ice buddy. Couch can die on you

        String startKey = "";
        List<FrontLineWorker> frontLineWorkers;

        while (true) {
            frontLineWorkers = allFrontLineWorkers.getMsisdnsFrom(startKey, batchSize);

            if (frontLineWorkers.size() < batchSize) break;

            for (FrontLineWorker frontLineWorker : frontLineWorkers.subList(0, frontLineWorkers.size() - 1)) {
                log.info("Modifying (1) for FLW with msisdn : " + frontLineWorker.getMsisdn());
                try {
                    executable.execute(frontLineWorker);
                } catch (Exception e) {
                    log.error("Error modifying FLW " + e.getMessage() + ExceptionUtils.getFullStackTrace(e));
                }
            }

            startKey = frontLineWorkers.get(frontLineWorkers.size() - 1).getMsisdn();
        }

        for (FrontLineWorker frontLineWorker : frontLineWorkers) {
            log.info("Modifying (2) FLW with msisdn : " + frontLineWorker.getMsisdn());
            try {
                executable.execute(frontLineWorker);
            } catch (Exception e) {
                log.error("Error modifying FLW " + e.getMessage() + ExceptionUtils.getFullStackTrace(e));
            }
        }
    }

    public void countInvalidFLWs() {
        List<FrontLineWorker> frontLineWorkers = allFrontLineWorkers.getAll();
        int counter = 0;
        for(FrontLineWorker frontLineWorker : frontLineWorkers) {
            String existingDesignation = frontLineWorker.designationName();
            if (existingDesignation == null || !existingDesignation.equalsIgnoreCase("INVALID")) continue;

            counter++;
            log.info("FLW Invalid : " + frontLineWorker.getMsisdn() + "  -  " + existingDesignation);
        }

        log.info("FLW - total invalid count - " + counter);
    }

    @Seed(priority = 1, version = "1.6", comment = "Remove invalid designations")
    public void removeInvalidDesignations() {
        doWithBatch(new FrontLineWorkerExecutable() {
            @Override
            public void execute(FrontLineWorker frontLineWorker) {
                seedService.removeInvalidDesignation(frontLineWorker);
            }
        }, 100);
    }

    @Seed(priority = 0, version = "1.6", comment = "Changing registration status of FLWs based on new definition")
    public void activateNewRegistrationStatusesForAllFLWs() {
        doWithBatch(new FrontLineWorkerExecutable() {
            @Override
            public void execute(FrontLineWorker frontLineWorker) {
                seedService.activateNewRegistrationStatusForFLW(frontLineWorker);
            }
        }, 100);
    }

}
