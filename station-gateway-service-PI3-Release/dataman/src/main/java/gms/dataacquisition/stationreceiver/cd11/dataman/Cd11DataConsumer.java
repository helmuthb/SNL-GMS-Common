package gms.dataacquisition.stationreceiver.cd11.dataman;

import com.google.common.net.InetAddresses;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11GapList;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11Socket;
import gms.dataacquisition.stationreceiver.cd11.common.GracefulThread;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11SocketConfig;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11StationConfig;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11AcknackFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11CommandResponseFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11DataFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.dataman.configuration.Cd11DataConsumerConfig;
import gms.dataacquisition.stationreceiver.osdgateway.StationReceiverOsdGatewayInterface;
import gms.dataacquisition.stationreceiver.osdgateway.accesslibrary.StationReceiverOsdGatewayAccessLibrary;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquisitionProtocol;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame.AuthenticationStatus;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11DataConsumer extends GracefulThread {

  private static Logger sharedLogger = LoggerFactory.getLogger(Cd11DataConsumer.class);

  private final Logger stationLogger;

  private final Cd11DataConsumerConfig config;

  private final StationReceiverOsdGatewayInterface osdGateway;
  private final Cd11Socket cd11Socket;
  private final Message SHUTDOWN_EVENT = new Message(MessageType.Shutdown);

  // Event generators.
  private final Cd11DataConsumerConnectionExpiredThread connectionExpiredEvent;
  private final Cd11DataConsumerNewFrameReceivedThread newFrameReceivedEvent;
  private final Cd11DataConsumerSendAcknackThread sendAcknackEvent;
  private final Cd11DataConsumerPersistGapStateThread persistGapStateEvent;
  private final Cd11DataConsumerRemoveExpiredGaps removeExpiredGapsEvent;

  // Event queue.
  private final BlockingQueue<Message> eventQueue = new LinkedBlockingQueue<>();

  // Gap list.
  private Cd11GapList cd11GapList;

  // Listening socket, for receiving connections from a Data Provider.
  private ServerSocket serverSocket = null;

  // Statistics and state information.
  private final AtomicLong totalDataFramesReceived = new AtomicLong(0);

  //-------------------- Constructors --------------------

  /**
   * Creates a Cd11DataConsumer to listen for Data Provider connection, and receive its data.
   */
  public Cd11DataConsumer(Cd11DataConsumerConfig config) {
    this(config, new StationReceiverOsdGatewayAccessLibrary(
        config.osdGatewayServiceIpAddress, config.osdGatewayServicePort,
        config.fsOutputDirectory));
  }

  /**
   * Alternate constructor, meant for testing only!
   *
   * @throws NullPointerException if osdGateway is null
   */
  public Cd11DataConsumer(
      Cd11DataConsumerConfig config, StationReceiverOsdGatewayInterface osdGateway) {
    super(config.threadName, true, true);

    // Initialize properties.
    this.config = Objects.requireNonNull(config);

    // Create an OSD Gateway Access Library.
    this.osdGateway = osdGateway;

    // Create a CD 1.1 client.
    this.cd11Socket = new Cd11Socket(Cd11SocketConfig.builder()
        .setStationOrResponderName(config.responderName)
        .setStationOrResponderType(config.responderType)
        .setServiceType(config.serviceType)
        .setFrameCreator(config.frameCreator)
        .setFrameDestination(config.frameDestination)
        .setAuthenticationKeyIdentifier(config.authenticationKeyIdentifier)
        .setProtocolMajorVersion(config.protocolMajorVersion)
        .setProtocolMinorVersion(config.protocolMinorVersion)
        .build());

    // Load the CD 1.1 gap list.
    this.cd11GapList = Cd11StationConfig.loadGapState(config.dataProviderStationName);

    // Initialize the event generators.
    this.newFrameReceivedEvent = new Cd11DataConsumerNewFrameReceivedThread(
        "NewFrameReceivedThread", eventQueue, cd11Socket);
    this.sendAcknackEvent = new Cd11DataConsumerSendAcknackThread(
        "SendAcknackThread", eventQueue, cd11Socket);
    this.connectionExpiredEvent = new Cd11DataConsumerConnectionExpiredThread(
        "ConnectionExpiredThread", eventQueue, cd11Socket,
        this.config.connectionExpiredTimeLimitSec);
    this.persistGapStateEvent = new Cd11DataConsumerPersistGapStateThread(
        "GapStateThread", eventQueue, this.config.storeGapStateIntervalMinutes);
    this.removeExpiredGapsEvent = new Cd11DataConsumerRemoveExpiredGaps(
        "RemoveExpiredGapsThread", eventQueue);

    // TODO: Refactor this after determining what we are doing with CSV statistics.
    configCustomLogger();
    this.stationLogger = LoggerFactory.getLogger(this.config.dataProviderStationName);
  }

  //-------------------- Graceful Thread Methods --------------------

  @Override
  protected boolean onBeforeStart() {
    totalDataFramesReceived.set(0);
    return true;
  }

  /**
   * Start the Data Consumer thread.
   */
  @Override
  protected void onStart() throws Exception {
    // Listen for a connection.
    log(Level.INFO,
        String.format(
            "Listening on port %d for a Data Provider to connect.",
            this.config.dataConsumerPort),
        null);

    this.serverSocket = new ServerSocket(
        this.config.dataConsumerPort, 0,
        InetAddresses.forString(this.config.dataConsumerIpAddress));
    Socket socket;

    while (true) {
      // Check whether the thread is shutting down.
      if (this.shutThreadDown()) {
        this.shutdownGracefully();
        return; // Exit the thread.
      }

      // Indicate that this GracefulThread is now initialized.
      this.setThreadAsInitialized();

      // Wait for the desired connection to arrive.
      try {
        socket = serverSocket.accept();
      } catch (SocketException e) {
        // Check whether the thread is shutting down.
        if (this.shutThreadDown()) {
          // This is expected; SocketException was throw to break the blocking call and allow the thread to shut down.
          this.shutdownGracefully();
          return; // Exit the thread.
        } else {
          throw e; // This is unexpected, and should be logged as an error.
        }
      }

      // Determine the IP Address of the connecting Data Provider.
      String dpIpAddress =
          ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress();
      log(Level.INFO,
          String.format("Received Data Provider from remote address %s", dpIpAddress),
          null);

      // Hard close socket after 3 seconds so we don't get "Bind failed, address eventQueue use" errors.
      socket.setSoLinger(true, 3);

      // Check whether the remote IP Address matches the expected address.
      if (!dpIpAddress.equals(config.expectedDataProviderIpAddress)) {
        sharedLogger.error(String.format(
            "Data Provider IP address (%s) does not match the expected value (%s).",
            dpIpAddress, config.expectedDataProviderIpAddress));

        // TODO: In the future, reject these connections and continue looping!!!
        //continue;
        break;
      } else {
        break;
      }
    }

    // Close the ServerSocket, since we have the one connection we were listening for.
    serverSocket.close();
    serverSocket = null;

    // Establish connection.
    cd11Socket.connect(socket);

    // Start up a thread to listen for incoming data frames.
    newFrameReceivedEvent.start();

    // Start up a thread that triggers an event when it is time to send an Acknack frame.
    sendAcknackEvent.start();

    // Start up a thread that triggers an event when the connection has expired due to lack of contact.
    connectionExpiredEvent.start();

    // Start up a thread that triggers an event when it is time to persist the gap state.
    persistGapStateEvent.start();

    // Start up a thread that triggers an even when it is time to remove expired gaps.
    removeExpiredGapsEvent.start();

    // Enter the event loop.
    while (this.keepThreadRunning()) {
      // Check that all event threads are running.
      if (!eventQueue.contains(SHUTDOWN_EVENT) && (
          !connectionExpiredEvent.isRunning() ||
              !newFrameReceivedEvent.isRunning() ||
              !sendAcknackEvent.isRunning() ||
              !persistGapStateEvent.isRunning() ||
              !removeExpiredGapsEvent.isRunning())) {
        sharedLogger.error(
            "One or more event threads shut down unexpectedly, shutting down the Data Consumer.");
        this.shutdownGracefully();
        return; // Exit the thread.
      }

      // Read the next message from the event queue.
      Message mt;
      try {
        mt = eventQueue.take(); // NOTE: This is a blocking call!
      } catch (InterruptedException e) {
        sharedLogger.debug(String.format(
            "InterruptedException thrown in thread %1$s, closing thread.", this.getThreadName()),
            e);
        this.shutdownGracefully();
        return; // Exit the thread.
      }

      // Process the queue message.
      switch (mt.messageType) {

        case NewFrameReceived:
          this.processNewFrame(mt.cd11Frame);
          break;

        case PersistGapState:
          // TODO: In the future, the gap state should be persisted to the OSD.
          Cd11StationConfig.persistGapState(this.config.dataProviderStationName, this.cd11GapList);
          break;

        case RemoveExpiredGaps:
          if (config.gapExpirationInDays > 0) {
            cd11GapList.removeExpiredGaps(config.gapExpirationInDays);
          }
          break;

        case SendAcknack:
          this.sendAcknack(cd11Socket);
          break;

        case Shutdown:
          this.shutdownGracefully();
          return; // Exit the thread.

        default:
          String errMsg = "Invalid MessageType received (this should never occur).";
          sharedLogger.error(errMsg);
          throw new IllegalStateException(errMsg);
      }
    }
  }

  /**
   * Indicate that the Data Consumer thread needs to stop.
   */
  @Override
  protected void onStop() {
    // Unblock the server socket listening for Data Provider connections (if currently in use).
    if (this.serverSocket != null) {
      try {
        this.serverSocket.close();
      } catch (Exception e) {
        // Ignore.
      }
    }
  }

  /**
   * Runs when GracefulThread catches an unhandled exception. (NOTE: This should never occur in
   * practice, since we are taking care to catch all exceptions.)
   *
   * @param thread Thread that threw the uncaught exception.
   * @param throwable Exception object.
   */
  @Override
  protected void onUncaughtException(Thread thread, Throwable throwable) {
    // Shut down gracefully.
    try {
      this.shutdownGracefully();
    } catch (Exception ex) {
    }
  }

  //-------------------- Private Methods --------------------

  /**
   * Processes a newly arrived CD 1.1 frame.
   */
  private void processNewFrame(Cd11Frame cd11Frame) {
    try {
      switch (cd11Frame.frameType) {

        case ACKNACK:
          // Parse the Acknack frame, and update the gap list.
          Cd11AcknackFrame acknackFrame = cd11Frame.asFrameType(Cd11AcknackFrame.class);
          cd11GapList.update(acknackFrame);
          break;

        case ALERT:
          try {
            eventQueue.put(new Message(MessageType.Shutdown));
          } catch (Exception e) {
            this.stop();
          }
          break;

        case CD_ONE_ENCAPSULATION:
          // TODO: Handle CD 1 Encapsulation frames.
          log(Level.ERROR, "Received CD_ONE_ENCAPSULATION frame, which is not yet supported!");
          break;

        case COMMAND_REQUEST:
          log(Level.ERROR,
              "Received COMMAND_REQUEST frame, which should never have been sent by the Data Provider! Ignoring this frame.");

          break;

        case COMMAND_RESPONSE:
          log(Level.ERROR,
              "Received COMMAND_RESPONSE frame, recording the sequence number but ignoring the frame!");

          // Update the gaps list.
          Cd11CommandResponseFrame commandResponseFrame = cd11Frame
              .asFrameType(Cd11CommandResponseFrame.class);
          cd11GapList.addSequenceNumber(commandResponseFrame);

          break;

        case CONNECTION_REQUEST:
          log(Level.ERROR,
              "Received CONNECTION_REQUEST frame, which should never have been sent by the Data Provider! Ignoring this frame.");
          break;

        case CONNECTION_RESPONSE:
          log(Level.ERROR,
              "Received CONNECTION_RESPONSE frame, which should never have been sent by the Data Provider! Ignoring this frame.");
          break;

        case DATA:
          handleDataFrame(cd11Frame);
          break;

        case OPTION_REQUEST:
          // Respond to the option request.
          handleOptionRequestFrame();
          log(Level.INFO, "Received OPTION_REQUEST frame, ignoring frame.");
          break;

        case OPTION_RESPONSE:
          log(Level.INFO, "Received OPTION_RESPONSE frame, ignoring frame.");
          break;

        case CUSTOM_RESET_FRAME:
          log(Level.INFO, "Received CUSTOM_RESET_FRAME frame, clearing gap list and shutting down.");

          // Remove the "persist gap state" event from the queue, if one currently exist.
          eventQueue.remove(new Message(MessageType.PersistGapState));

          // Clear the gap state.
          Cd11StationConfig.clearGapState(this.config.dataProviderStationName);

          // Shut down.
          try {
            eventQueue.put(new Message(MessageType.Shutdown));
          } catch (Exception e) {
            this.stop();
          }
          break;

        default:
          String msg = "Invalid CD 1.1 frame messageType received (this should never occur).";
          sharedLogger.error(msg);
          throw new IllegalStateException(msg);
      }
    } catch (Exception e) {
      log(Level.ERROR, "Error processing frame", e);
      this.shutdownGracefully();
    }
  }

  /**
   * Sends an acknack frame.
   *
   * @param cd11Socket CD 1.1 Socket cd11Frame.
   */
  private void sendAcknack(Cd11Socket cd11Socket) throws Exception {
    // Send the Acknack frame.
    cd11Socket.sendCd11AcknackFrame(
        cd11Socket.getFramesetAcked(),
        cd11GapList.getLowestSequenceNumber(),
        cd11GapList.getHighestSequenceNumber(),
        cd11GapList.getGaps());

    log(Level.INFO, (String.format(
        "Sending acknack for sequences: %d - %d",
        cd11GapList.getLowestSequenceNumber(), cd11GapList.getHighestSequenceNumber())));
  }

  private void handleDataFrame(Cd11Frame cd11Frame) {
    Cd11DataFrame dataFrame = cd11Frame.asFrameType(Cd11DataFrame.class);
    // Increment the total number of data frames received.
    totalDataFramesReceived.incrementAndGet();

    try {
      // Send data to the OSD.
      RawStationDataFrame rawStationDataFrame = toRawStationDataFrame(dataFrame);
      this.osdGateway.storeRawStationDataFrame(rawStationDataFrame);

      // Update gaps upon successful storage.
      cd11GapList.addSequenceNumber(dataFrame);

      // Log successful storage.
      log(Level.INFO, String.format("DataFrame %d stored successfully.",
          dataFrame.getFrameHeader().sequenceNumber));
    } catch (Exception e) {
      log(Level.ERROR, "Could not convert/store CD 1.1 Data Frame", e);
    }
  }

  private void handleOptionRequestFrame() {
    //TODO: Do we need to read the content of the option request, and act on it? Currently, we just send a dull response, and ignore the content.
    try {
      // Right now the only option messageType is 1 with the station name as the option response.
      cd11Socket.sendCd11OptionResponseFrame(1, cd11Socket.getStationOrResponderName());

      log(Level.INFO, "Option Response Frame Sent");
    } catch (Exception e) {
      log(Level.ERROR, ExceptionUtils.getStackTrace(e));
    }
  }

  private void configCustomLogger() {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration ctxConfiguration = ctx.getConfiguration();
    Layout<? extends Serializable> layout = PatternLayout.newBuilder()
        .withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n")
        .withConfiguration(ctxConfiguration)
        .build();
    Appender appender = FileAppender.newBuilder()
        .setConfiguration(ctxConfiguration)
        .withFileName("shared-volume/logs/stations/" + this.config.dataProviderStationName
            + ".log")
        .withLayout(layout)
        .withName(this.config.dataProviderStationName + "_RollingFile")
        .build();
    appender.start();
    ctxConfiguration.addAppender(appender);
    AppenderRef ref = AppenderRef.createAppenderRef(appender.getName(), null, null);
    AppenderRef[] refs = new AppenderRef[]{ref};
    LoggerConfig loggerConfig = LoggerConfig.createLogger(true, Level.INFO,
        this.config.dataProviderStationName + "_RollingFile", "true", refs, null, ctxConfiguration,
        null);
    loggerConfig.addAppender(appender, null, null);
    ctxConfiguration.addLogger(this.config.dataProviderStationName, loggerConfig);
    ctx.updateLoggers();
  }

  private void log(Level level, String message) {
    log(level, message, null);
  }

  // Logs to both the shared log and station log
  private void log(Level level, String message, Exception e) throws IllegalArgumentException {
    if (level == Level.INFO) {
      sharedLogger.info(message);
      stationLogger.info(message);
    } else if (level == Level.ERROR) {
      if (e == null) {
        sharedLogger.error(message);
        stationLogger.error(message);
      } else {
        sharedLogger.error(message, e);
        stationLogger.error(message, e);
      }

    } else if (level == Level.DEBUG) {
      sharedLogger.debug(message);
      stationLogger.debug(message);
    } else if (level == Level.TRACE) {
      sharedLogger.trace(message);
      stationLogger.trace(message);
    } else if (level == Level.WARN) {
      sharedLogger.warn(message);
      stationLogger.warn(message);
    } else {
      throw new IllegalArgumentException("Unhandled log level.");
    }

  }

  private RawStationDataFrame toRawStationDataFrame(Cd11DataFrame df) throws Exception {
    Instant receptionTime = Instant.now();
    Cd11ChannelSubframe sf1 = df.channelSubframes[0];
    Instant startTime = sf1.timeStamp;
    Instant endTime = startTime.plusMillis(sf1.subframeTimeLength);
    return RawStationDataFrame.create(
        this.config.osdStationId, AcquisitionProtocol.CD11,
        this.config.dataProviderStationName,
        startTime, endTime, receptionTime, df.getRawNetworkBytes(),
        AuthenticationStatus.NOT_YET_AUTHENITCATED,
        creationInfo());

  }

  private static CreationInfo creationInfo() {
    String creatorName = Cd11DataConsumer.class.getName();

    // TODO: Creation info version number must not be hardcoded.
    return new CreationInfo(creatorName, new SoftwareComponentInfo(creatorName, "0.0.1"));
  }

  /**
   * Shuts down all event threads, and closes the CD 1.1 Socket connection.
   */
  private void shutdownGracefully() {
    // Signal that this thread needs to stop running.
    this.stop();

    // Attempt to send a CD 1.1 Alert frame to the Data Consumer.
    try {
      if (cd11Socket.isConnected()) {
        cd11Socket.sendCd11AlertFrame("Shutting down.");
      }
    } catch (Exception e) {
      // Do nothing.
    }

    // Stop all running threads.
    connectionExpiredEvent.stop();
    newFrameReceivedEvent.stop();
    sendAcknackEvent.stop();
    persistGapStateEvent.stop();
    removeExpiredGapsEvent.stop();
    connectionExpiredEvent.waitUntilThreadStops();
    newFrameReceivedEvent.waitUntilThreadStops();
    sendAcknackEvent.waitUntilThreadStops();
    persistGapStateEvent.waitUntilThreadStops();
    removeExpiredGapsEvent.waitUntilThreadInitializes();

    // Collect all "last error messages" written by this thread, and all of its event threads.
    this.setErrorMessage(GracefulThread.aggregateErrorMessages(
        this,
        connectionExpiredEvent,
        newFrameReceivedEvent,
        sendAcknackEvent,
        persistGapStateEvent,
        removeExpiredGapsEvent));

    // Disconnect the CD 1.1 Socket.
    cd11Socket.disconnect();
  }

  //-------------------- Statistics and State Info Methods --------------------

  /**
   * The port number that the CD 1.1 Data Consumer is using to receive data from the Data Provider.
   *
   * @return Port number.
   */
  public int getCd11ListeningPort() {
    return this.config.dataConsumerPort;
  }

  /**
   * Returns the total number of data frames received, since the Data Consumer thread was started.
   *
   * @return Total data frames received.
   */
  public long getTotalDataFramesReceived() {
    return totalDataFramesReceived.get();
  }
}
