package gms.core.waveformqc.waveformqccontrol.objects.dto;

import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus;
import gms.core.waveformqc.waveformqccontrol.objects.WaveformQcChannelSohStatus.Status;
import gms.core.waveformqc.waveformqccontrol.objects.dto.WaveformQcChannelSohStatusDto.StatusChangeDto;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility class with operations to build a {@link WaveformQcChannelSohStatusDto} from an {@link
 * WaveformQcChannelSohStatus} and vice-versa.
 */
public class WaveformQcChannelSohStatusDtoConverter {

  /**
   * Private default constructor to prevent instantiation of this utility class
   */
  private WaveformQcChannelSohStatusDtoConverter() {
  }

  /**
   * Obtains an instance of {@link WaveformQcChannelSohStatusDto} from the {@link
   * WaveformQcChannelSohStatus}
   *
   * @param waveformQcChannelSohStatus a WaveformQcChannelSohStatus, not null
   * @return a WaveformQcChannelSohStatusDto, not null
   * @throws NullPointerException if the waveformQcChannelSohStatus is null
   */
  public static WaveformQcChannelSohStatusDto toDto(
      WaveformQcChannelSohStatus waveformQcChannelSohStatus) {

    Objects.requireNonNull(waveformQcChannelSohStatus,
        "Cannot create WaveformQcChannelSohStatusDto from a null WaveformQcChannelSohStatus");

    WaveformQcChannelSohStatusDto dto = new WaveformQcChannelSohStatusDto();
    dto.setProcessingChannelId(
        waveformQcChannelSohStatus.getProcessingChannelId());
    dto.setQcMaskType(waveformQcChannelSohStatus.getQcMaskType());
    dto.setChannelSohSubtype(waveformQcChannelSohStatus.getChannelSohSubtype());
    dto.setStatusChanges(
        waveformQcChannelSohStatus.getStatusChanges()
            .map(c -> new StatusChangeDto(c.getStartTime(), c.getEndTime(), c.getStatus()))
            .collect(Collectors.toList()));

    return dto;
  }

  /**
   * Obtains an instance of {@link WaveformQcChannelSohStatus} from the {@link
   * WaveformQcChannelSohStatusDto}
   *
   * @param dto a WaveformQcChannelSohStatusDto, not null
   * @return a WaveformQcChannelSohStatus, not null
   * @throws NullPointerException if the WaveformQcChannelSohStatusDto is null
   * @throws IllegalArgumentException if the dto does not contain at least one status entry
   */
  public static WaveformQcChannelSohStatus fromDto(WaveformQcChannelSohStatusDto dto) {

    Objects.requireNonNull(dto,
        "Cannot create WaveformQcChannelSohStatus from a null WaveformQcChannelSohStatusDto");

    final List<StatusChangeDto> statusChangeDtos = dto.getStatusChanges();
    if (null == statusChangeDtos || statusChangeDtos.isEmpty()) {
      throw new IllegalArgumentException(
          "Cannot create WaveformQcChannelSohStatus from WaveformQcChannelSohStatusDto with no StatusChangeDto");
    }

    List<Status> statusChanges = statusChangeDtos.stream()
        .map(d -> Status.create(d.getStartTime(), d.getEndTime(), d.getStatus()))
        .collect(Collectors.toList());

    return WaveformQcChannelSohStatus
        .from(dto.getProcessingChannelId(), dto.getQcMaskType(), dto.getChannelSohSubtype(),
            statusChanges);
  }
}
