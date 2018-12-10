package gms.dataacquisition.cssreader.referencereaders;

import com.github.ffpojo.FFPojoHelper;
import gms.dataacquisition.cssreader.data.AffiliationRecord;
import gms.dataacquisition.cssreader.data.AffiliationRecordCss30;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlatFileAffiliationReader {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FlatFileAffiliationReader.class);

    private List<String> networks;
    private List<String> stations;
    private Instant time;
    private Instant endtime;
    private static final int AFFILIATION_CSS_3_LINE_LENGTH = 33;

    public FlatFileAffiliationReader() {}

    /**
     * The arguments provided will be used to filter the affiliation file entries, and may be null so
     * they are not used.
     * @param networks
     * @param stations
     * @param time
     * @param endtime
     */
    public FlatFileAffiliationReader(List<String> networks,
                                List<String> stations,
                                Instant time,
                                Instant endtime) {
        logger.info("Creating AffiliationRecord reader with parameters: networks=" + networks +
                " stations=" + stations + " time=" + time + " endtime=" + endtime);
        this.networks = networks;
        this.stations = stations;
        this.time = time;
        this.endtime = endtime;
    }

    /**
     * Reads the file at the specified path into Affiliations.
     * @param affiliationPath the path to the file to read
     * @return the read affiliation records
     * @throws Exception
     */
    public List<AffiliationRecord> read(String affiliationPath) throws Exception {
        // Validate file path argument.
        Validate.notEmpty(affiliationPath);
        Path path = Paths.get(affiliationPath);
        if (path == null || Files.exists(path) == false) {
            throw new IllegalArgumentException("Path " + affiliationPath + " doesn't exist in file system.");
        }

        // read the lines out of the file
        List<String> fileLines = Files.readAllLines(path);
        logger.debug("AffiliationRecord has " + fileLines.size() + " entries in file: " + path);
        if (fileLines.size() <= 0) {
            logger.error("Provided AffiliationRecord file " + affiliationPath + " is empty");
            return new ArrayList<>();
        }

        // convert the file into Affiliations
        final FFPojoHelper ffpojo = FFPojoHelper.getInstance();
        List<AffiliationRecord> affiliationRecords = new ArrayList<>();
        for (int i = 0; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            try {
                AffiliationRecord affiliationRecord = readAsAffiliation(ffpojo, line);
                affiliationRecords.add(affiliationRecord);
            }
            catch(Exception ex) {
                logger.error("Encountered error (" + ex.getMessage() + ") at line " + (i + 1) + " of file: " + line);
                ex.printStackTrace();
            }
        }

        // Apply filters on station, channel, start time, endtime.
        Stream<AffiliationRecord> affiliationStream = affiliationRecords.stream()
                .filter(affiliationRecord -> networks == null || networks.contains(affiliationRecord.getNet()))
                .filter(affiliationRecord -> stations == null || stations.contains(affiliationRecord.getSta()))
                .filter(affiliationRecord -> time == null || (time.compareTo(affiliationRecord.getTime()) <= 0))
                .filter(affiliationRecord -> endtime == null || (endtime.compareTo(affiliationRecord.getEndtime()) >= 0));

        // Generate the list of Affiliations.
        // Sort the Affiliations stream by directory, filename, and file-offset.
        Comparator<AffiliationRecord> affiliationComparator = Comparator.comparing(AffiliationRecord::getNet)
                .thenComparing(AffiliationRecord::getSta);
        affiliationRecords = affiliationStream.sorted(affiliationComparator)
                .collect(Collectors.toList());

        logger.debug(affiliationRecords.size() + " affiliationRecords records retrieved.");
        return affiliationRecords;
    }

    /**
     * Helper method to read a String into an AffiliationRecord using a FFPojoHelper.
     * @param ffpojo used to parse the input string into an AffiliationRecord
     * @param l the line to parse
     * @return the parsed AffiliationRecord, or null if an error occurs.
     */
    private static AffiliationRecord readAsAffiliation(FFPojoHelper ffpojo, String l) throws Exception {
        AffiliationRecord affiliationRecord;
            affiliationRecord = ffpojo.createFromText(AffiliationRecordCss30.class, l);

        logger.debug("Read AffiliationRecord line: " + affiliationRecord.toString());
        affiliationRecord.validate();

        return affiliationRecord;
    }
}
