package gms.core.waveformqc.waveformqccontrol.control;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.waveformqc.waveformqccontrol.mock.MockWaveformQcPluginComponent;
import gms.core.waveformqc.waveformqccontrol.objects.PluginVersion;
import gms.core.waveformqc.waveformqccontrol.objects.RegistrationInfo;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcConfiguration;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcParameters;
import gms.core.waveformqc.waveformqccontrol.osdgateway.client.InvokeInputDataMap;
import gms.core.waveformqc.waveformqccontrol.util.TestUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

//TODO: Replace Spring MVC test with some manner of component/integration test
///**
// * Unit tests for {@link WaveformQcHttpController}. Uses Spring MVC to generate a mock version of
// * our application, used to test expectations of REST-ful operations performed on the service.
// */
//@RunWith(SpringRunner.class)
//@AutoConfigureWebClient
//@WebMvcTest(WaveformQcHttpController.class)
//public class WaveformQcHttpControllerTests {
//
//  @Autowired
//  private MockMvc mockMvc;
//
//  @Autowired
//  private ObjectMapper mapper;
//
//  @MockBean
//  private WaveformQcControlOsdGatewayAccessLibrary waveformQcControlOsdGatewayAccessLibrary;
//
//  @Mock
//  private WaveformQcConfiguration waveformQcConfiguration;
//
//  /**
//   * Tests that our service can be sent an empty {@link ControlInvokeDto} object without error, and
//   * will return back an empty array of {@link QcMask}
//   *
//   * @throws Exception any exception thrown by the underlying mocked service.
//   */
//  @Test
//  public void testInitializeAndInvoke() throws Exception {
//    //load configuration
//    given(waveformQcControlOsdGatewayAccessLibrary.loadConfiguration())
//        .willReturn(waveformQcConfiguration);
//
//    WaveformQcParameters waveformQcParameters = WaveformQcParameters.create(new UUID(0L, 0L),
//        Collections.singletonList(RegistrationInfo.from("mock", PluginVersion.from(1, 0, 0))));
//    given(waveformQcConfiguration.createParameters(new UUID(0L, 0L)))
//        .willReturn(Optional.of(waveformQcParameters));
//
//    given(waveformQcControlOsdGatewayAccessLibrary.loadPluginConfiguration(any()))
//        .willReturn(TestUtility.TEST_PLUGIN_CONFIGURATION);
//    mockMvc.perform(
//        post("/waveform-qc-control/initialize")
//            .contentType(MediaType.APPLICATION_JSON_UTF8))
//        .andExpect(status().isOk());
//    verify(waveformQcControlOsdGatewayAccessLibrary, times(1)).loadConfiguration();
//    verify(waveformQcControlOsdGatewayAccessLibrary, times(2)).loadPluginConfiguration(any());
//
//    Set<UUID> processingChannelIds = Collections.singleton(new UUID(0L, 0L));
//
//    when(waveformQcControlOsdGatewayAccessLibrary.loadInvokeInputData(
//        processingChannelIds, Instant.MIN, Instant.MAX))
//        .thenReturn(InvokeInputDataMap
//            .create(Collections.emptySet(), Collections.emptySet(), Collections.emptySet()));
//
//    ProcessingContext processingContext = ProcessingContext.createAutomatic(
//        UUID.randomUUID(), UUID.randomUUID(), LongIdentity.from(1),
//        StorageVisibility.PRIVATE);
//
//    ControlInvokeDto body = new ControlInvokeDto(processingChannelIds, Instant.MIN,
//        Instant.MAX, processingContext);
//
//    QcMask[] expectedResult = new QcMask[]{MockWaveformQcPluginComponent.mockQcMask()};
//
//    this.mockMvc
//        .perform(
//            post("/waveform-qc-control/invoke")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(mapper.writeValueAsString(body)))
//        .andExpect(status().isOk())
//        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//        .andExpect(content()
//            .json(mapper.writeValueAsString(expectedResult)));
//  }
//}
