package gms.core.waveformqc.waveformqccontrol.control;

import static gms.core.waveformqc.waveformqccontrol.Application.control;

import gms.core.waveformqc.waveformqccontrol.util.ObjectSerialization;
import gms.core.waveformqc.waveformqccontrol.util.StandardResponse;
import gms.core.waveformqc.waveformqccontrol.util.StatusResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handler to provide Spark {@link Route}s used to make calls to {@link WaveformQcControl}
 */
public class ControlHandler {

  private static Logger logger = LoggerFactory.getLogger(ControlHandler.class);


  public static Object invokeControlNoContext(Request request, Response response) {
    ControlInvokeDto controlInvokeDto = ObjectSerialization
        .readValue(request.body(), ControlInvokeDto.class);

    logger.info("Invoke request received: {}", controlInvokeDto);
    control.execute(convertHttpInputNoContext(controlInvokeDto));
    logger.info("Invoke request completed.");

    return ObjectSerialization.writeValue(new StandardResponse(StatusResponse.SUCCESS,
        "Invoke request executed successfully."));
  }

  private static ExecuteCommand convertHttpInputNoContext(ControlInvokeDto controlInvokeDto) {
    return ExecuteCommand.create(controlInvokeDto.getProcessingChannelIds(),
        controlInvokeDto.getStartTime(), controlInvokeDto.getEndTime(),
        mockContext());
  }

  private static ProcessingContext mockContext() {
    return ProcessingContext.createAutomatic(UUID.randomUUID(), UUID.randomUUID(),
        UUID.randomUUID(), StorageVisibility.PUBLIC);
  }

  /**
   * Invocation request, primary means of invoking processing via the {@link
   * WaveformQcControl}. Accepts a JSON body representing the expected input to the invocation.
   *
   * @return {@link QcMask} objects created from {@link WaveformQcControl} execution.
   */
  public static Object invokeControl(Request request, Response response) {
    ControlInvokeDto controlInvokeDto = ObjectSerialization
        .readValue(request.body(), ControlInvokeDto.class);

    logger.info("Invoke request received: {}", controlInvokeDto);
    control.execute(convertHttpInput(controlInvokeDto));
    return ObjectSerialization.writeValue(new StandardResponse(StatusResponse.SUCCESS,
        "Invoke request executed successfully."));
  }

  /**
   * Service aliveness check that obtains a plaintext message with the current time.  Used by
   * clients to determine if the service is responsive.
   *
   * @param request invocation request, currently unused, not null
   * @param response response, not null
   * @return plaintext string containing a brief message and the current time, not null
   */
  public static Object alive(Request request, Response response) {
    logger.info("Alive request received");
    return ObjectSerialization.writeValue(new StandardResponse(StatusResponse.SUCCESS,
        "Waveform QC Control HTTP service alive at " + Instant.now()));
  }

  /**
   * Convenience method for converting {@link ControlInvokeDto} to {@link ExecuteCommand} used by
   * {@link WaveformQcControl}
   *
   * @param controlInvokeDto Input DTO to our service for invoking QC.
   * @return The Command object used to invoke QC.
   */
  private static ExecuteCommand convertHttpInput(ControlInvokeDto controlInvokeDto) {
    return ExecuteCommand.create(controlInvokeDto.getProcessingChannelIds(),
        controlInvokeDto.getStartTime(), controlInvokeDto.getEndTime(),
        controlInvokeDto.getProcessingContext());
  }

}
