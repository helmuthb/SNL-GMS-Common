package postgres.repository;

import javax.persistence.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.DoubleStream;

/**
 * Created by jwvicke on 11/29/17.
 */
@Entity
@Table(name = "waveform_blob")
public class WaveformBlobDao {

    @Id
    @GeneratedValue
    private long primaryKey;

    @Column(unique = true, nullable=false)
    private UUID id;

    @Column(name = "processing_channel_id", nullable=false)
    private UUID processingChannelId;

    @Column(name = "startTime", nullable=false)
    private Instant startTime;

    @Column(name = "endTime", nullable=false)
    private Instant endTime;

    @Lob
    @Column(name = "samples", nullable=false)
    // Note: could not get this to work with primitive array
    private Double[] samples;

    /**
     * Default constructor for use by JPA
     */
    public WaveformBlobDao() {

    }

    public WaveformBlobDao(UUID id, UUID processingChannelId, Instant startTime,
                           Instant endTime, double[] samples) {
        this.id = id;
        this.processingChannelId = processingChannelId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.samples = DoubleStream.of(samples).boxed().toArray(Double[]::new);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProcessingChannelId() { return processingChannelId; }
    public void setProcessingChannelId(UUID processingChannelId) {
        this.processingChannelId = processingChannelId;
    }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }

    public Double[] getSamples() { return samples; }
    public void setSamples(Double[] s) { this.samples = s; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WaveformBlobDao that = (WaveformBlobDao) o;

        if (primaryKey != that.primaryKey) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (processingChannelId != null ? !processingChannelId.equals(that.processingChannelId) : that.processingChannelId != null)
            return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        if (endTime != null ? !endTime.equals(that.endTime) : that.endTime != null) return false;
        return Arrays.equals(samples, that.samples);
    }

    @Override
    public int hashCode() {
        int result = (int) (primaryKey ^ (primaryKey >>> 32));
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (processingChannelId != null ? processingChannelId.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(samples);
        return result;
    }

    @Override
    public String toString() {
        return "WaveformBlobDao{" +
                "primaryKey=" + primaryKey +
                ", id=" + id +
                ", processingChannelId=" + processingChannelId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", samples=" + Arrays.toString(samples) +
                '}';
    }
}
