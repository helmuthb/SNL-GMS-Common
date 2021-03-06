package gms.core.signaldetection.signaldetectorcontrol.osdgateway.gateway;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gms.core.signaldetection.signaldetectorcontrol.objects.dto.InvokeInputDataRequestDto;
import gms.core.signaldetection.signaldetectorcontrol.objects.dto.StoreSignalDetectionsDto;
import gms.core.signaldetection.signaldetectorcontrol.osdgateway.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurement;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.FeatureMeasurementType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GatewayHandlerTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private OsdGateway mockOsdGateway;
  private ChannelSegment channelSegment1; // = randomChannelSegment();
  private ChannelSegment channelSegment2; // = randomChannelSegment();
  private Set<ChannelSegment> testChannelSegments; // = Set.of(channelSegment1, channelSegment2);

  @Before
  public void setUp() {
    mockOsdGateway = mock(OsdGateway.class);
    channelSegment1 = randomChannelSegment();
    channelSegment2 = randomChannelSegment();
    testChannelSegments = Set.of(channelSegment1, channelSegment2);
    testChannelSegments = Set.of(channelSegment1, channelSegment2);

    given(mockOsdGateway.loadInvokeInputData(
        notNull(), notNull(), notNull()))
        .willReturn(testChannelSegments);
  }

  @Test
  public void testCreate() {
    OsdGateway gateway = mock(OsdGateway.class);
    GatewayHandler handler = GatewayHandler.create(gateway);

    assertNotNull(handler);
  }

  @Test
  public void testFetchInvokeInputDataJsonInJsonOut() {
    GatewayHandler handler = GatewayHandler.create(mockOsdGateway);

    StandardResponse response = handler.fetchInvokeInputData(ContentType.APPLICATION_JSON,
        getInvokeInputDataRequestDto(UUID.randomUUID(), ContentType.APPLICATION_JSON),
        ContentType.APPLICATION_JSON);

    ChannelSegment[] channelSegmentArray = getDeserializer(ContentType.APPLICATION_JSON,
        ChannelSegment[].class).apply((byte[]) response.getResponseBody());

    Set<ChannelSegment> channelSegments = Set.of(channelSegmentArray);

    verifyInvokeInputDataResponse(channelSegments, response);
  }

  @Test
  public void testFetchInvokeInputDataJsonInMessagePackOut() {
    GatewayHandler handler = GatewayHandler.create(mockOsdGateway);

    StandardResponse response = handler.fetchInvokeInputData(ContentType.APPLICATION_JSON,
        getInvokeInputDataRequestDto(UUID.randomUUID(), ContentType.APPLICATION_JSON),
        ContentType.APPLICATION_MSGPACK);

    ChannelSegment[] channelSegmentArray = getDeserializer(ContentType.APPLICATION_MSGPACK,
        ChannelSegment[].class).apply((byte[]) response.getResponseBody());

    Set<ChannelSegment> channelSegments = Set.of(channelSegmentArray);

    verifyInvokeInputDataResponse(channelSegments, response);
  }

  @Test
  public void testFetchInvokeInputDataMessagePackInJsonOut() {
    GatewayHandler handler = GatewayHandler.create(mockOsdGateway);

    StandardResponse response = handler.fetchInvokeInputData(ContentType.APPLICATION_MSGPACK,
        getInvokeInputDataRequestDto(UUID.randomUUID(), ContentType.APPLICATION_MSGPACK),
        ContentType.APPLICATION_JSON);

    ChannelSegment[] channelSegmentArray = getDeserializer(ContentType.APPLICATION_JSON,
        ChannelSegment[].class).apply((byte[]) response.getResponseBody());

    Set<ChannelSegment> channelSegments = Set.of(channelSegmentArray);

    verifyInvokeInputDataResponse(channelSegments, response);
  }

  @Test
  public void testFetchInvokeInputDataMessagePackInMessagePackOut() {
    GatewayHandler handler = GatewayHandler.create(mockOsdGateway);

    StandardResponse response = handler.fetchInvokeInputData(ContentType.APPLICATION_MSGPACK,
        getInvokeInputDataRequestDto(UUID.randomUUID(), ContentType.APPLICATION_MSGPACK),
        ContentType.APPLICATION_MSGPACK);

    ChannelSegment[] channelSegmentArray = getDeserializer(ContentType.APPLICATION_MSGPACK,
        ChannelSegment[].class).apply((byte[]) response.getResponseBody());

    Set<ChannelSegment> channelSegments = Set.of(channelSegmentArray);

    verifyInvokeInputDataResponse(channelSegments, response);
  }

  @Test
  public void testStoreSignalDetectionsJsonInJsonOut() {
    GatewayHandler handler = GatewayHandler.create(mockOsdGateway);

    StandardResponse response = handler.storeSignalDetections(ContentType.APPLICATION_JSON,
        getSerializedChannelSegmentList(ContentType.APPLICATION_JSON),
        ContentType.APPLICATION_JSON);

    verifyResponseCodeStoreCalled(response);
  }

  @Test
  public void testStoreSignalDetectionsJsonInMessagePackOut() {
    GatewayHandler handler = GatewayHandler.create(mockOsdGateway);

    StandardResponse response = handler.storeSignalDetections(ContentType.APPLICATION_JSON,
        getSerializedChannelSegmentList(ContentType.APPLICATION_JSON),
        ContentType.APPLICATION_MSGPACK);

    verifyResponseCodeStoreCalled(response);
  }

  @Test
  public void testStoreSignalDetectionsJsonInAnyOut() {
    GatewayHandler handler = GatewayHandler.create(mockOsdGateway);

    StandardResponse response = handler.storeSignalDetections(ContentType.APPLICATION_JSON,
        getSerializedChannelSegmentList(ContentType.APPLICATION_JSON),
        ContentType.APPLICATION_ANY);

    verifyResponseCodeStoreCalled(response);
  }

  @Test
  public void testStoreSignalDetectionsMessagePackInJsonOut() {
    GatewayHandler handler = GatewayHandler.create(mockOsdGateway);

    StandardResponse response = handler.storeSignalDetections(ContentType.APPLICATION_MSGPACK,
        getSerializedChannelSegmentList(ContentType.APPLICATION_MSGPACK),
        ContentType.APPLICATION_JSON);

    verifyResponseCodeStoreCalled(response);
  }

  @Test
  public void testStoreSignalDetectionsMessagePackInMessagePackOut() {
    GatewayHandler handler = GatewayHandler.create(mockOsdGateway);

    StandardResponse response = handler.storeSignalDetections(ContentType.APPLICATION_MSGPACK,
        getSerializedChannelSegmentList(ContentType.APPLICATION_MSGPACK),
        ContentType.APPLICATION_MSGPACK);

    verifyResponseCodeStoreCalled(response);
  }

  @Test
  public void testStoreSignalDetectionsMessagePackInAnyOut() {
    GatewayHandler handler = GatewayHandler.create(mockOsdGateway);

    StandardResponse response = handler.storeSignalDetections(ContentType.APPLICATION_MSGPACK,
        getSerializedChannelSegmentList(ContentType.APPLICATION_MSGPACK),
        ContentType.APPLICATION_ANY);

    verifyResponseCodeStoreCalled(response);
  }

  @Test
  public void testCreateNullGatewayExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("GatewayHandler requires a non-null OsdGateway");
    GatewayHandler.create(null);
  }

  @Test
  public void testRequestContentTypeUnsupportedExpect415() {
    GatewayHandler handler = GatewayHandler.create(mockOsdGateway);

    final Predicate<ContentType> invalidRequestTypes = c ->
        ContentType.APPLICATION_ANY == c || ContentType.TEXT_PLAIN == c;

    Arrays.stream(ContentType.values()).filter(invalidRequestTypes)
        .forEach(c ->
            {
              assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, handler.fetchInvokeInputData(
                  c, new byte[0], ContentType.APPLICATION_JSON).getHttpStatus());

              assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, handler.storeSignalDetections(
                  c, new byte[0], ContentType.APPLICATION_JSON).getHttpStatus());
            }
        );
  }

  @Test
  public void testStreamingAcceptTypeUnsupportedExpect406() {
    GatewayHandler handler = GatewayHandler.create(mockOsdGateway);

    final Predicate<ContentType> invalidResponseTypes = c -> ContentType.TEXT_PLAIN == c;

    Arrays.stream(ContentType.values()).filter(invalidResponseTypes)
        .forEach(c -> {
              assertEquals(HttpStatus.NOT_ACCEPTABLE_406, handler
                  .fetchInvokeInputData(ContentType.APPLICATION_JSON, new byte[0], c).getHttpStatus());

              assertEquals(HttpStatus.NOT_ACCEPTABLE_406, handler
                  .storeSignalDetections(ContentType.APPLICATION_JSON, new byte[0], c).getHttpStatus());
            }
        );
  }

  private Stream<ContentType> getContentTypeStream(Predicate<ContentType> filter) {
    return Arrays.stream(ContentType.values()).filter(filter);
  }

  private static byte[] getSerializedChannelSegmentList(ContentType type) {
    //return getSerializationOp(type).apply(List.of(randomChannelSegment()));
    return getSerializationOp(type).apply(
        new StoreSignalDetectionsDto(Set.of(randomSignalDetection()), StorageVisibility.PUBLIC));
  }

  private static byte[] getInvokeInputDataRequestDto(UUID channelId, ContentType type) {
    InvokeInputDataRequestDto dto = new InvokeInputDataRequestDto(List.of(channelId),
        Instant.now().minusSeconds(1000), Instant.now());

    return getSerializationOp(type).apply(dto);
  }

  private static Function<Object, byte[]> getSerializationOp(ContentType type) {
    if (ContentType.APPLICATION_JSON == type || ContentType.APPLICATION_ANY == type) {
      return ObjectSerialization::writeJson;
    } else if (ContentType.APPLICATION_MSGPACK == type) {
      return ObjectSerialization::writeMessagePack;
    }

    throw new IllegalStateException(
        "GatewayHandlerHandler cannot instantiate a serializer for an unsupported ContentType.");
  }

  private static <T> BiFunction<byte[], Class<T>, T> getDeserializationOp(ContentType type) {
    if (ContentType.APPLICATION_JSON == type) {
      return ObjectSerialization::readJson;
    } else if (ContentType.APPLICATION_MSGPACK == type) {
      return ObjectSerialization::readMessagePack;
    }

    throw new IllegalStateException(
        "GatewayHandler cannot instantiate a deserializer for an unsupported ContentType.");
  }

  private static <T> Function<byte[], T> getDeserializer(ContentType type, Class<T> classType) {
    // There is a type error when all of this is on one line.  Doubtless it involves type erasure.
    BiFunction<byte[], Class<T>, T> deserialization = getDeserializationOp(type);
    return b -> deserialization.apply(b, classType);
  }

  private static SignalDetection randomSignalDetection() {
    return SignalDetection.create("monitoringOrganization",
        UUID.randomUUID(), "phase",
        List.of(FeatureMeasurement.create(FeatureMeasurementType.ARRIVAL_TIME,
            1.0, UUID.randomUUID())),
        UUID.randomUUID());
  }

  private static ChannelSegment randomChannelSegment() {
    final Waveform wf1 = Waveform.withInferredEndTime(Instant.EPOCH, 2, 20, randoms(20));

    return ChannelSegment.from(UUID.randomUUID(), UUID.randomUUID(), "ChannelName",
        ChannelSegment.ChannelSegmentType.RAW, wf1.getStartTime(), wf1.getEndTime(),
        new TreeSet<>(List.of(wf1)), CreationInfo.DEFAULT);
  }

  private void verifyInvokeInputDataResponse(Collection<ChannelSegment> channelSegments,
      StandardResponse response) {
    assertEquals(2, channelSegments.size());
    assertEquals(200, response.getHttpStatus());
    assertTrue(testChannelSegments.containsAll(channelSegments) && channelSegments
        .containsAll(testChannelSegments));
  }

  private void verifyResponseCodeStoreCalled(StandardResponse response) {
    verify(mockOsdGateway, times(1)).store(notNull(), notNull());

    assertEquals(200, response.getHttpStatus());
  }

  static double[] randoms(int length) {
    double[] random = new double[length];
    IntStream.range(0, length).forEach(i -> random[i] = Math.random());
    return random;
  }

}
