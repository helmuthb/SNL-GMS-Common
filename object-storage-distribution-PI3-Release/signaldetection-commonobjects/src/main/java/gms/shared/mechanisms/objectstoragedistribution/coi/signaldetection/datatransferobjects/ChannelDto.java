package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Calibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Response;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelDataType;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ChannelType;
import java.util.UUID;

/**
 * Create class to allow transformation to and from JSON.
 */
public interface ChannelDto {

    @JsonCreator
    static Channel from(
        @JsonProperty("id") UUID id,
        @JsonProperty("name") String name,
        @JsonProperty("channelType") ChannelType channelType,
        @JsonProperty("dataType") ChannelDataType dataType,
        @JsonProperty("latitude") double latitude,
        @JsonProperty("longitude") double longitude,
        @JsonProperty("elevation") double elevation,
        @JsonProperty("depth") double depth,
        @JsonProperty("verticalAngle") double verticalAngle,
        @JsonProperty("horizontalAngle") double horizontalAngle,
        @JsonProperty("sampleRate") double sampleRate,
        @JsonProperty("response") Response response,
        @JsonProperty("calibration") Calibration calibration) {

        return Channel.from(id, name, channelType, dataType, latitude,
            longitude, elevation, depth, verticalAngle, horizontalAngle,
            sampleRate, response, calibration);
    }
}
