package gms.dataacquisition.cssreader.waveformreaders;

import gms.dataacquisition.cssreader.data.WfdiscRecord;
import org.apache.commons.lang3.Validate;
import gms.utilities.waveformreader.WaveformReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.LoggerFactory;

/**
 * Class for reading waveform files (.w).
 */
public class FlatFileWaveformReader {

  private static final org.slf4j.Logger logger = LoggerFactory
      .getLogger(FlatFileWaveformReader.class);

  // improving performance by saving last read file; assumes wfdiscs are sorted prior to reading
  private String lastReadFile = "";
  private ByteArrayInputStream lastReadWaveform;

  /**
   * Performance is improved dramatically if multiple waveforms are sorted by path to .w files (dir
   * + dfile) prior to reading.  However, this method still works even if not sorted.
   *
   * @param wfd the wfdisc record to read
   * @return int[] of the waveform samples
   * @throws Exception if the input waveform is null or problems occur during reading.
   */
  public int[] readWaveform(WfdiscRecord wfd, String wfdiscFilePath) throws Exception {
    Validate.notNull(wfd);

    // prepend path with directory of wfdisc file if path is not absolute.
    String prefix = wfd.getDir().startsWith(File.separator) ?
        "" : new File(wfdiscFilePath).getParent() + File.separator;
    
    return readSamples(prefix + wfd.getDir(), wfd.getDfile(), wfd.getFoff(), wfd.getNsamp(),
        wfd.getDatatype());
  }

  private int[] readSamples(String dir, String fileName, int skip,
      int samplesToRead, String format) throws Exception {
    Validate.notNull(dir);
    Validate.notNull(fileName);

    if (dir.endsWith(File.separator) == false) {
      dir = dir + File.separator;
    }

    String wf_file_path = dir + fileName;
    if (new File(wf_file_path).exists() == false) {
      String error = "File at path " + wf_file_path + " doesn't exist";
      logger.error(error);
      throw new Exception(error);
    }

    logger.debug("Reading waveform: " + wf_file_path);
    if (lastReadFile.equals(wf_file_path)) {
      logger.debug("Re-using waveform from memory");
      lastReadWaveform.reset();
    } else {
      byte[] data = Files.readAllBytes(Paths.get(wf_file_path));
      if (data.length == 0) {
        String error = "File at path " + wf_file_path + " has no data";
        logger.error(error);
        throw new Exception(error);
      }

      lastReadWaveform = new ByteArrayInputStream(data);
      lastReadFile = wf_file_path;
    }

    return WaveformReader.readSamples(
        lastReadWaveform, format, samplesToRead, skip);
  }
}
