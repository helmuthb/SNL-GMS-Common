package gms.core.signalenhancement.waveformfiltering.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import gms.core.signalenhancement.waveformfiltering.TestFixtures;
import gms.core.signalenhancement.waveformfiltering.control.ExecuteClaimCheckCommand;
import gms.core.signalenhancement.waveformfiltering.control.ExecuteStreamingCommand;
import gms.core.signalenhancement.waveformfiltering.control.FilterControl;
import gms.core.signalenhancement.waveformfiltering.objects.FilterConfiguration;
import gms.core.signalenhancement.waveformfiltering.osdgateway.client.OsdGatewayClient;
import gms.core.signalenhancement.waveformfiltering.plugin.FilterControlPluginRegistry;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link FilterControlRouteHandler}
 */
@RunWith(MockitoJUnitRunner.class)
public class FilterControlRouteHandlerTests {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private FilterControlRouteHandler routeHandler;
  private FilterControl filterControl;
  private FilterControl mockFilterControl;

  @Mock
  private OsdGatewayClient mockOsdGatewayClient;

  @Mock
  private FilterConfiguration mockFilterConfiguration;

  private FilterControlRouteHandler routeHandlerMockControl;
  private List<ChannelSegment> outputChannelSegs;
  private List<UUID> outputChannelSegIds;

  @Before
  public void setUp() {
    FilterControlPluginRegistry pluginRegistry = new FilterControlPluginRegistry();
    filterControl = FilterControl.create(pluginRegistry, mockOsdGatewayClient);
    filterControl.initialize();
    mockFilterControl = mock(FilterControl.class);

    routeHandler = FilterControlRouteHandler.create(filterControl);

    routeHandlerMockControl = FilterControlRouteHandler.create(mockFilterControl);
    outputChannelSegs = List
        .of(TestFixtures.randomChannelSegment(), TestFixtures.randomChannelSegment());
    given(mockFilterControl.execute((ExecuteStreamingCommand) notNull()))
        .willReturn(outputChannelSegs);

    outputChannelSegIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    given(mockFilterControl.execute((ExecuteClaimCheckCommand) notNull()))
        .willReturn(outputChannelSegIds);
  }

  /**
   * @return {@link StreamingDto} serialized in JSON
   */
  private byte[] getJsonStreamingDto() {
    return ObjectSerialization.writeJson(TestFixtures.getStreamingDto());
  }

  /**
   * @return {@link StreamingDto} serialized in MessagePack
   */
  private byte[] getMessagePackStreamingDto() {
    return ObjectSerialization.writeMessagePack(TestFixtures.getStreamingDto());
  }

  /**
   * Obtain the {@link ContentType}s passing the provided filter
   *
   * @param filter {@link ContentType} predicate
   * @return stream of ContentType matching the filter
   */
  private Stream<ContentType> getContentTypeStream(Predicate<ContentType> filter) {
    return Arrays.stream(ContentType.values()).filter(filter);
  }

  /**
   * Determines whether the {@link ContentType} is acceptable to {@link
   * FilterControlRouteHandler#streaming(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return wheter the contentType is acceptable to the streaming interface
   */
  private static boolean streamingTypeFilter(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON
        || contentType == ContentType.APPLICATION_MSGPACK;
  }

  /**
   * Determines whether the {@link ContentType} is unacceptable to {@link
   * FilterControlRouteHandler#streaming(ContentType, byte[], ContentType)}
   *
   * @param contentType a {@link ContentType}
   * @return whether the contentType is unacceptable to the streaming interface
   */
  private static boolean negativeStreamingTypeFilter(ContentType contentType) {
    return !streamingTypeFilter(contentType);
  }

  @Test
  public void testCreate() {
    FilterControlRouteHandler handler = FilterControlRouteHandler.create(mock(FilterControl.class));
    assertNotNull(handler);
  }

  @Test
  public void testCreateNullExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception
        .expectMessage("FilterControlRouteHandler cannot be constructed with null FilterControl");
    FilterControlRouteHandler.create(null);
  }

  @Test
  public void testStreamingRequestContentTypeJson() {
    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_JSON, getJsonStreamingDto(),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testStreamingRequestContentTypeMessagePack() {
    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_MSGPACK, getMessagePackStreamingDto(),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testStreamingRequestContentTypeUnsupportedExpect415() {
    getContentTypeStream(FilterControlRouteHandlerTests::negativeStreamingTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, routeHandler
                .streaming(c, new byte[0], ContentType.APPLICATION_JSON).getHttpStatus())
        );
  }

  @Test
  public void testStreamingAcceptTypeJson() {
    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_JSON, getJsonStreamingDto(),
            ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testStreamingAcceptTypeMessagePack() {
    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_JSON, getJsonStreamingDto(),
            ContentType.APPLICATION_MSGPACK);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testStreamingAcceptTypeUnsupportedExpect406() {
    getContentTypeStream(FilterControlRouteHandlerTests::negativeStreamingTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.NOT_ACCEPTABLE_406, routeHandler
                .streaming(ContentType.APPLICATION_JSON, new byte[0], c).getHttpStatus())
        );
  }

  @Test
  public void testStreamingProvidesJsonResponse() {
    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_MSGPACK, getMessagePackStreamingDto(),
            ContentType.APPLICATION_JSON);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertTrue(Arrays.equals(ObjectSerialization.writeJson(outputChannelSegs),
        (byte[]) response.getResponseBody()));
  }

  @Test
  public void testStreamingProvidesMsgPackResponse() {
    final StandardResponse response = routeHandlerMockControl
        .streaming(ContentType.APPLICATION_JSON, getJsonStreamingDto(),
            ContentType.APPLICATION_MSGPACK);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertTrue(Arrays.equals(ObjectSerialization.writeMessagePack(outputChannelSegs),
        (byte[]) response.getResponseBody()));
  }

  @Test
  public void testStreamingNullBodyTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterControlRouteHandler requires non-null requestBodyType");
    routeHandler.streaming(null, new byte[0], ContentType.APPLICATION_JSON);
  }

  @Test
  public void testStreamingNullBodyExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterControlRouteHandler requires non-null body");
    routeHandler
        .streaming(ContentType.APPLICATION_JSON, null, ContentType.APPLICATION_MSGPACK);
  }

  @Test
  public void testStreamingNullResponseTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterControlRouteHandler requires non-null responseBodyType");
    routeHandler.streaming(ContentType.APPLICATION_JSON, new byte[0], null);
  }

  @Test
  public void testStreamingInvokesExecute() {
    final byte[] requestBody = getMessagePackStreamingDto();
    routeHandlerMockControl
        .streaming(ContentType.APPLICATION_MSGPACK, requestBody, ContentType.APPLICATION_JSON);

    verify(mockFilterControl, times(1)).execute(getExecuteStreamingCommandFromDto(requestBody));
  }

  private ExecuteStreamingCommand getExecuteStreamingCommandFromDto(byte[] requestBody) {
    StreamingDto dto = ObjectSerialization.readMessagePack(requestBody, StreamingDto.class);
    return ExecuteStreamingCommand
        .create(dto.getChannelSegmentToOutputChannelIdMap(), dto.getFilterDefinition(),
            dto.getProcessingContext());
  }

  private byte[] getClaimCheckDto() {
    return ObjectSerialization.writeJson(TestFixtures.getClaimCheckDto());
  }

  private static boolean claimCheckTypeFilter(ContentType contentType) {
    return contentType == ContentType.APPLICATION_JSON;
  }

  private static boolean negativeClaimCheckTypeFilter(ContentType contentType) {
    return !claimCheckTypeFilter(contentType);
  }

  @Test
  public void testClaimCheckRequestContentTypeUnsupportedExpect415() {
    getContentTypeStream(FilterControlRouteHandlerTests::negativeClaimCheckTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, routeHandler
                .claimCheck(c, new byte[0], ContentType.APPLICATION_JSON).getHttpStatus())
        );
  }

  @Test
  public void testClaimCheckAcceptTypeUnsupportedExpect406() {
    getContentTypeStream(FilterControlRouteHandlerTests::negativeClaimCheckTypeFilter)
        .forEach(c ->
            assertEquals(HttpStatus.NOT_ACCEPTABLE_406, routeHandler
                .claimCheck(ContentType.APPLICATION_JSON, new byte[0], c).getHttpStatus())
        );
  }

  @Test
  public void testClaimCheckProvidesJsonResponse() {
    final StandardResponse response = routeHandlerMockControl
        .claimCheck(ContentType.APPLICATION_JSON, getClaimCheckDto(), ContentType.APPLICATION_JSON);

    assertNotNull(response);
    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
    assertTrue(Arrays.equals(ObjectSerialization.writeJson(outputChannelSegIds),
        (byte[]) response.getResponseBody()));
  }

  @Test
  public void testClaimCheckNullBodyTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterControlRouteHandler requires non-null requestBodyType");
    routeHandler.claimCheck(null, new byte[0], ContentType.APPLICATION_JSON);
  }

  @Test
  public void testClaimCheckNullBodyExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterControlRouteHandler requires non-null body");
    routeHandler
        .claimCheck(ContentType.APPLICATION_JSON, null, ContentType.APPLICATION_MSGPACK);
  }

  @Test
  public void testClaimCheckNullResponseTypeExpectNullPointerException() {
    exception.expect(NullPointerException.class);
    exception.expectMessage("FilterControlRouteHandler requires non-null responseBodyType");
    routeHandler.claimCheck(ContentType.APPLICATION_JSON, new byte[0], null);
  }

  @Test
  public void testClaimCheckAcceptTypeJson() {
    given(mockFilterControl.execute(any(ExecuteClaimCheckCommand.class))).willReturn(List.of());

    final StandardResponse response = routeHandlerMockControl
        .claimCheck(ContentType.APPLICATION_JSON, getClaimCheckDto(), ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testClaimCheckRequestContentTypeJson() {
    given(mockFilterControl.execute(any(ExecuteClaimCheckCommand.class))).willReturn(List.of());

    final StandardResponse response = routeHandlerMockControl
        .claimCheck(ContentType.APPLICATION_JSON, getClaimCheckDto(), ContentType.APPLICATION_JSON);

    assertEquals(HttpStatus.OK_200, response.getHttpStatus());
  }

  @Test
  public void testClaimCheckInvokesExecute() {
    final byte[] requestBody = getClaimCheckDto();
    routeHandlerMockControl
        .claimCheck(ContentType.APPLICATION_JSON, requestBody, ContentType.APPLICATION_JSON);

    verify(mockFilterControl, times(1)).execute(getExecuteClaimCheckCommandFromDto(requestBody));
  }

  private ExecuteClaimCheckCommand getExecuteClaimCheckCommandFromDto(byte[] requestBody) {
    ClaimCheckDto dto = ObjectSerialization.readJson(requestBody, ClaimCheckDto.class);
    return ExecuteClaimCheckCommand
        .create(dto.getInputToOutputChannelIds(), dto.getChannelProcessingStepId(),
            dto.getStartTime(), dto.getEndTime(), dto.getProcessingContext());
  }

  @Test
  public void demotest() throws Exception {

    Instant now = Instant.now();

    ClaimCheckDto dto = new ClaimCheckDto();
    dto.setChannelProcessingStepId(UUID.randomUUID());
    dto.setStartTime(now.minus(Duration.ofHours(1)));
    dto.setEndTime(now);
    dto.setInputToOutputChannelIds(Map.of(UUID.randomUUID(), UUID.randomUUID()));
    dto.setProcessingContext(ProcessingContext.createAutomatic(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), StorageVisibility.PUBLIC));

    System.out.println(ObjectSerialization.getJsonClientObjectMapper().writeValue(dto));
  }
}
