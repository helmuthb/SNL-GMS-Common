import gql from 'graphql-tag';

/**
 * Query for FK data for an event hypothesis as part of
 * associated signal detection hypotheses.
 */
export interface EventFkDataQuery {
  eventHypothesisById: {
    signalDetectionAssociations: SdFkData[];
  };
}

/**
 * Fk Data query input
 */
export interface EventFkDataQueryInput {
  eventId: number;
}

export interface FkFstatData {
  azimuthWf: {
    id: string;
    startTime: number;
    endTime: number;
    sampleRate: number;
    sampleCount: number;
    waveformSamples: number[];
  };
  slownessWf: {
    id: string;
    startTime: number;
    endTime: number;
    sampleRate: number;
    sampleCount: number;
    waveformSamples: number[];
  };
  beamWf: {
    id: string;
    startTime: number;
    endTime: number;
    sampleRate: number;
    sampleCount: number;
    waveformSamples: number[];
  };
  fstatWf: {
    id: string;
    startTime: number;
    endTime: number;
    sampleRate: number;
    sampleCount: number;
    waveformSamples: number[];
  };
}

export interface FrequencyBand {
    minFrequencyHz: number;
    maxFrequencyHz: number;
}

export interface WindowParams {
  windowType: string;
  leadSeconds: number;
  lengthSeconds: number;
}

/**
 * Fk Data definition
 */
export interface FkData {
  id: string;
  accepted: boolean;
  attenuation: number;
  fkGrid: number[][];
  slownessScale: {
    maxValue: number;
    scaleValues: number[];
    scaleValueCount: number;
  };
  peak: {
    xSlowness: number;
    ySlowness: number;
    azimuthDeg: number;
    radialSlowness: number;
    azimuthUncertainty: number;
    slownessUncertainty: number;
    fstat: number;
  };
  theoretical: {
    xSlowness: number;
    ySlowness: number;
    azimuthDeg: number;
    radialSlowness: number;
    azimuthUncertainty: number;
    slownessUncertainty: number;
    fstat: number;
  };
  frequencyBand: FrequencyBand;
  windowParams: WindowParams;
  contribChannels: {
    id: string;
    name: string;
    site: {
      name: string;
    };
  }[];
  fstatData: FkFstatData;
}

/**
 * Fk Data Input is simular to the FkData without the 
 * contributing channels and instead a list of contributing channel ids
 */
export interface FkDataInput extends FkData {
  contribChannelIds: string[];
}

/**
 * Fk data associated with a signal detection hypothesis 
 */
export interface SdFkData {
  signalDetectionHypothesis: {
    id: string;
    phase: string;
    arrivalTimeMeasurement: {
      id: string;
      featureType: string;
      timeSec: number;
    };
    azSlownessMeasurement: {
      azimuthDefiningRules: {
        isDefining: boolean;
        operationType: string;
      };
      slownessDefiningRules: {
        isDefining: boolean;
        operationType: string;
      };
      fkData: FkData;
    };
    signalDetection: {
      id: string;
      station: {
        id: string;
        name: string;
        defaultChannel: {
          id: string;
          name: string;
        };
        distanceToSource: {
          distanceKm: number;
        };
      };
    };
  };
}

/**
 * Query for Fk data associated with an event hypothesis.
 */
export const eventFkDataQuery = gql`
query fkDataQuery ($eventId: String!){
  eventHypothesisById(hypothesisId: $eventId){
    signalDetectionAssociations{
      signalDetectionHypothesis{
        id
        phase
        arrivalTimeMeasurement {
          id
          featureType
          timeSec
        }
        azSlownessMeasurement{
            azimuthDefiningRules {
              isDefining
              operationType
            }
            slownessDefiningRules {
              isDefining
              operationType
            }
            fkData{
              id
              accepted
              attenuation
              fkGrid
              slownessScale{
                maxValue
                scaleValues
                scaleValueCount
              }
              peak{
                xSlowness
                ySlowness
                azimuthDeg
                radialSlowness
                azimuthUncertainty
                slownessUncertainty
                fstat
              }
              theoretical{
                xSlowness
                ySlowness
                azimuthDeg
                radialSlowness
                azimuthUncertainty
                slownessUncertainty
                fstat
              }
              frequencyBand{
                minFrequencyHz
                maxFrequencyHz
              }
              windowParams{
                windowType
                leadSeconds
                lengthSeconds
              }
              contribChannels{
                id
                name
                site {
                  name
                }
              }
              fstatData{
                azimuthWf{
                  id
                  startTime
                  endTime
                  sampleRate
                  sampleCount
                  waveformSamples
                }
                slownessWf{
                  id
                  startTime
                  endTime
                  sampleRate
                  sampleCount
                  waveformSamples
                }
                beamWf{
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
        signalDetection{
          id
          station{
            id
            name
            defaultChannel {
              id
              name
            }
            distanceToSource(distanceToSourceInput: {sourceType: Event, sourceId: $eventId}){
              distanceKm
            }
          }
        }
      }
    }
  }
}
`;
