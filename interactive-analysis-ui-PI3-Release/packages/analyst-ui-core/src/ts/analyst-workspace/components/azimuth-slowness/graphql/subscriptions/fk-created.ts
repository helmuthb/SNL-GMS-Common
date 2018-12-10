import gql from 'graphql-tag';
import { FkData } from '../query';

/**
 * Data structure for detectionUpdated subscription callback
 */
export interface FkCreatedSubscription {
  fkCreated: FkData;
}

export const fkCreatedSubscription = gql`
subscription fkCreated{
    fkCreated {
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
        azimuthUncertainty
        slownessUncertainty
        fstat
      }
      theoretical {
        xSlowness
        ySlowness
        azimuthDeg
        radialSlowness
        azimuthUncertainty
        slownessUncertainty
        fstat
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
`;
