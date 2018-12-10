package gms.dataacquisition.cssreader.referencereaders;

import com.github.ffpojo.FFPojoHelper;
import gms.dataacquisition.cssreader.data.InstrumentRecord;
import org.apache.commons.lang3.Validate;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlatFileInstrumentReader {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FlatFileInstrumentReader.class);

    private List<Long> inids;
    private List<String> insnames;
    private List<String> instypes;

    public FlatFileInstrumentReader() {}

    /**
     * The arguments provided will be used to filter the instrument file entries, and may be null so
     * they are not used.
     * @param inids
     * @param insnames
     * @param instypes
     */
    public FlatFileInstrumentReader(List<Long> inids,
                                     List<String> insnames,
                                    List<String> instypes) {
        logger.info("Creating InstrumentRecord reader with parameters: inids=" + inids +
                " insnames=" + insnames + " instypes=" + instypes);
        this.inids = inids;
        this.insnames = insnames;
        this.instypes = instypes;
    }

    /**
     * Reads the file at the specified path into Instruments.
     * @param instrumentPath the path to the file to read
     * @return the read instrument records
     * @throws Exception
     */
    public List<InstrumentRecord> read(String instrumentPath) throws Exception {
        // Validate file path argument.
        Validate.notEmpty(instrumentPath);
        Path path = Paths.get(instrumentPath);
        if (path == null || Files.exists(path) == false) {
            throw new IllegalArgumentException("Path " + instrumentPath + " doesn't exist in file system.");
        }

        // read the lines out of the file
        List<String> fileLines = Files.readAllLines(path);
        logger.debug("InstrumentRecord has " + fileLines.size() + " entries in file: " + path);
        if (fileLines.size() <= 0) {
            logger.error("Provided InstrumentRecord file " + instrumentPath + " is empty");
            return new ArrayList<>();
        }

        // convert the file into Instruments
        final FFPojoHelper ffpojo = FFPojoHelper.getInstance();
        List<InstrumentRecord> instrumentRecords = new ArrayList<>();
        for (int i = 0; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            try {
                InstrumentRecord instrumentRecord = readAsInstrument(ffpojo, line);
                instrumentRecords.add(instrumentRecord);
            }
            catch(Exception ex) {
                logger.error("Encountered error (" + ex.getMessage() + ") at line " + (i + 1) + " of file: " + line);
                ex.printStackTrace();
            }
        }

        // Apply filters on station, channel, start time, endtime.
        Stream<InstrumentRecord> instrumentStream = instrumentRecords.stream()
                .filter(instrumentRecord -> inids == null || inids.contains(instrumentRecord.getInid()))
                .filter(instrumentRecord -> insnames == null || insnames.contains(instrumentRecord.getInsname()))
                .filter(instrumentRecord -> instypes == null || (instypes.contains(instrumentRecord.getInstype())));

        // Generate the list of Instruments.
        // Sort the Instruments stream by directory, filename, and file-offset.
        Comparator<InstrumentRecord> instrumentComparator = Comparator.comparing(InstrumentRecord::getInid)
                .thenComparing(InstrumentRecord::getInsname)
                .thenComparing(InstrumentRecord::getInstype);
        instrumentRecords = instrumentStream.sorted(instrumentComparator)
                .collect(Collectors.toList());

        logger.debug(instrumentRecords.size() + " instrumentRecords records retrieved.");
        return instrumentRecords;
    }

    /**
     * Helper method to read a String into an InstrumentRecord using a FFPojoHelper.
     * @param ffpojo used to parse the input string into an InstrumentRecord
     * @param l the line to parse
     * @return the parsed InstrumentRecord, or null if an error occurs.
     */
    private static InstrumentRecord readAsInstrument(FFPojoHelper ffpojo, String l) throws Exception {
        InstrumentRecord instrumentRecord = ffpojo.createFromText(InstrumentRecord.class, l);
        
        logger.debug("Read InstrumentRecord line: " + instrumentRecord.toString());
        instrumentRecord.validate();

        return instrumentRecord;
    }
}
