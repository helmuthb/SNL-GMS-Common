package gms.core.waveformqc.waveformqccontrol.osdgateway;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.waveformqc.waveformqccontrol.objects.InvokeInputData;
import gms.core.waveformqc.waveformqccontrol.objects.dto.StoreQcMasksDto;
import gms.core.waveformqc.waveformqccontrol.objects.dto.InvokeInputDataRequestDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.StorageVisibility;
import java.time.Instant;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;

//TODO: Replace Spring MVC with component or integration tests
///**
// * Unit tests for {@link OsdGatewayHttpController}. Uses Spring MVC to generate a mock version of
// * the application to test expectations of REST-ful operations performed on the service.
// */
//@RunWith(SpringRunner.class)
//@WebMvcTest(OsdGatewayHttpController.class)
//public class OsdGatewayHttpControllerTests {
//
//  @Autowired
//  private MockMvc mockMvc;
//
//  @Autowired
//  private ObjectMapper mapper;
//
//  @MockBean
//  private WaveformQcControlOsdGateway waveformQcControlOsdGatewayImplementation;
//
//  /**
//   * Tests the Waveform QC OSD Gateway Service's mockStore endpoint responds correctly when provided
//   * a non-empty string.
//   */
//  @Test
//  public void testStorePrivateContext() throws Exception {
//
//
//    StoreQcMasksDto mockDto = new StoreQcMasksDto(Collections.emptyList(),
//        Collections.emptyList(), //NEW
//        StorageVisibility.PRIVATE);
//
//    this.mockMvc
//        .perform(
//            post("/waveform-qc-control/osd-gateway/store")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(mapper.writeValueAsString(mockDto)))
//        .andExpect(status().isOk());
//
//    verify(waveformQcControlOsdGatewayImplementation, times(1)).store(any(), any(), any());
//  }
//
//  @Test
//  public void testStorePublicContext() throws Exception {
//    StoreQcMasksDto mockDto = new StoreQcMasksDto(Collections.emptyList(),
//        Collections.emptyList(),
//        StorageVisibility.PUBLIC);
//
//    this.mockMvc
//        .perform(
//            post("/waveform-qc-control/osd-gateway/store")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(mapper.writeValueAsString(mockDto)))
//        .andExpect(status().isOk());
//
//    verify(waveformQcControlOsdGatewayImplementation, times(1)).store(any(), any(), any());
//  }
//
//  @Test
//  public void testMockStoreForIncorrectBody() throws Exception {
//    final Instant body = Instant.now();
//
//    this.mockMvc
//        .perform(
//            post("/waveform-qc-control/osd-gateway/store")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(mapper.writeValueAsString(body)))
//        .andExpect(status().isBadRequest());
//  }
//
//  @Test
//  public void testLoadInvokeInputData() throws Exception {
//
//    InvokeInputDataRequestDto mockDto = new InvokeInputDataRequestDto(Collections.emptySet(),
//        Instant.MIN, Instant.MAX);
//
//    InvokeInputData mockResult = InvokeInputData.create(Collections.emptySet(),
//        Collections.emptySet(), Collections.emptySet());
//
//    given(waveformQcControlOsdGatewayImplementation
//        .loadInvokeInputData(mockDto.getProcessingChannelIds(),
//            mockDto.getStartTime(), mockDto.getEndTime())).willReturn(mockResult);
//
//    this.mockMvc
//        .perform(
//            post("/waveform-qc-control/osd-gateway/loadInvokeInputData")
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(mapper.writeValueAsString(mockDto)))
//        .andExpect(status().isOk())
//        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
//        .andExpect(MockMvcResultMatchers.content().string(mapper.writeValueAsString(mockResult)));
//  }
//
//}
