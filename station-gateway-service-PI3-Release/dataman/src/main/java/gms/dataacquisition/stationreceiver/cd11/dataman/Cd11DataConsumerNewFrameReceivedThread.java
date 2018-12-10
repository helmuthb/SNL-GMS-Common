package gms.dataacquisition.stationreceiver.cd11.dataman;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11Socket;
import gms.dataacquisition.stationreceiver.cd11.common.GracefulThread;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Cd11DataConsumerNewFrameReceivedThread extends GracefulThread {

  private static Logger logger = LoggerFactory
      .getLogger(Cd11DataConsumerNewFrameReceivedThread.class);

  private final Cd11Socket cd11Socket;
  private final BlockingQueue<Message> eventQueue;

  public Cd11DataConsumerNewFrameReceivedThread(
      String threadName, BlockingQueue<Message> eventQueue, Cd11Socket cd11Socket) {
    super(threadName, true, false);

    this.eventQueue = eventQueue;
    this.cd11Socket = cd11Socket;
  }

  @Override
  protected void onStart() throws Exception {
    try {
      while (this.keepThreadRunning()) {
        // Check if we've received anything from the Data Consumer.
        Cd11Frame cd11Frame = this.cd11Socket.read(this::shutThreadDown);

        // Check if we need to shutdown.
        if (this.shutThreadDown()) {
          break;
        }

        // Generate an event.
        eventQueue.put(new Message(MessageType.NewFrameReceived, cd11Frame));
      }
    } catch (InterruptedException e) {
      logger.debug(String.format(
          "InterruptedException thrown in thread %1$s, closing thread.", this.getThreadName()), e);
    }
  }
}
