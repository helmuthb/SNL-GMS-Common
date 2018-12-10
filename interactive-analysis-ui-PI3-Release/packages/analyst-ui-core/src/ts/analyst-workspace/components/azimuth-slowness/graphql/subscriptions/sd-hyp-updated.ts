import gql from 'graphql-tag';
import { SdFkData } from '../query';

/**
 * Data structure for detectionUpdated subscription callback
 */
export interface SDHypUpdatedSubscription {
  sdHypUpdated: SdFkData;
}

export const sdHypUpdatedSubscription = gql`
subscription sdHypothesisUpdated {
  sdHypothesisUpdated {
    id
    phase
    arrivalTimeMeasurement {
      id
      featureType
      timeSec
    }
    azSlownessMeasurement {
      fkData {
        id
        accepted
        attenuation
        fkGrid
        slownessScale {
          maxValue
          scaleValues
          scaleValueCount
        }
        peak {
          xSlowness
          ySlowness
          azimuthDeg
          radialSlowness
        }
        theoretical {
          xSlowness
          ySlowness
          azimuthDeg
          radialSlowness
        }
        frequencyBand {
          minFrequencyHz
          maxFrequencyHz
        }
        windowParams {
          windowType
          leadSeconds
          lengthSeconds
        }
        contribChannels {
          id
          name
          site{
            name
          }
        }
        fstatData {
          azimuthWf {
            id
            startTime
            endTime
            sampleRate
            sampleCount
            waveformSamples
          }
          slownessWf {
            id
            startTime
            endTime
            sampleRate
            sampleCount
            waveformSamples
          }
          beamWf {
            id
            startTime
            endTime
            sampleRate
            sampleCount
            waveformSamples
          }
          fstatWf {
            id
            startTime
            endTime
            sampleRate
            sampleCount
            waveformSamples
          }
        }
      }
    }
  }
}
`;
