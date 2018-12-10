import { Colors } from '@blueprintjs/core';
import * as React from 'react';

import { AzimuthSlowness } from '../azimuth-slowness';
import { SdFkData } from '../graphql/query';

/**
 * Standalone Azimuth Slowness Props
 */
// tslint:disable-next-line:no-empty-interface
export interface StandaloneAzimuthSlownessProps {

}

/**
 * Standalone Azimuth Slowness State
 */
// tslint:disable-next-line:no-empty-interface
export interface StandaloneAzimuthSlownessState {

}

/**
 * Standalone Azimuth Slowness Component - meant to isolate the azimuth slowness display into a test environment
 * separate from redux & apollo
 */
export class StandaloneAzimuthSlowness extends
    React.Component<StandaloneAzimuthSlownessProps, StandaloneAzimuthSlownessState> {

    public constructor(props: StandaloneAzimuthSlownessProps) {
        super(props);
    }

    /**
     * React component lifecycle
     */
    public render() {
        return (
            <div
                style={{
                    border: `1px solid ${Colors.GRAY3}`,
                    resize: 'both',
                    overflow: 'auto',
                    height: '700px',
                    width: '1000px',
                }}
            >
                <AzimuthSlowness
                    currentTimeInterval={undefined}
                    selectedSdIds={undefined}
                    openEventId={undefined}
                    selectedSortType={undefined}

                    data={
                        {
                            eventHypothesisById: {
                                signalDetectionAssociations: this.generateDummyFkData()
                            },
                            error: undefined,
                            loading: false,
                            networkStatus: undefined,
                            fetchMore: undefined,
                            refetch: undefined,
                            startPolling: undefined,
                            stopPolling: undefined,
                            subscribeToMore: () => () => {/**/},
                            updateQuery: undefined,
                            variables: undefined
                        }

                    }
                    setSelectedSdIds={undefined}
                    createFk={undefined}
                    updateAzSlowFromFkMutation={undefined}
                />
            </div>
        );
    }

    /**
     * generate some dummy data
     */
    private generateDummyFkData(): SdFkData[] {
        const data: SdFkData[] = [];
        const numberOfFk = 30;
        for (let i = 1; i <= numberOfFk; i++) {
            const fkData: SdFkData = {
                signalDetectionHypothesis: {
                    id: String(i),
                    phase: 'p',
                    arrivalTimeMeasurement: {
                      id: String(i),
                      featureType: 'ArrivalTime',
                      timeSec: 1274394559.45
                    },
                    azSlownessMeasurement: {
                      azimuthDefiningRules: {
                            isDefining: true,
                            operationType: 'Location'
                      },
                      slownessDefiningRules: {
                            isDefining: true,
                            operationType: 'Location'
                      },
                      fkData: {
                        id: String(i),
                        attenuation: 0,
                        accepted: true,
                        fkGrid: undefined,
                        slownessScale: {
                          maxValue: 0,
                          scaleValues: undefined,
                          scaleValueCount: 0
                        },
                        peak: {
                          xSlowness: 0,
                          ySlowness: 0,
                          azimuthDeg: 0,
                          radialSlowness: 0,
                          azimuthUncertainty: 0,
                          slownessUncertainty: 0,
                          fstat: 0
                        },
                        theoretical: {
                          xSlowness: 0,
                          ySlowness: 0,
                          azimuthDeg: 0,
                          radialSlowness: 0,
                          azimuthUncertainty: 0,
                          slownessUncertainty: 0,
                          fstat: 0
                        },
                        frequencyBand: {
                          minFrequencyHz: 0.5,
                          maxFrequencyHz: 1
                        },
                        windowParams: {
                          windowType: 'hanning',
                          leadSeconds: 2,
                          lengthSeconds: 5
                        },
                        contribChannels: [
                            {
                                id: String(i),
                                name: String(i) + '/SHZ',
                                site: {
                                    name: 'Foo'
                                }
                            }
                        ],
                        fstatData: {
                          azimuthWf: {
                            id: String(i),
                            startTime: 1274394558.45,
                            endTime: 1274394560.45,
                            sampleRate: 2,
                            sampleCount: 4,
                            // tslint:disable-next-line:no-magic-numbers
                            waveformSamples: [0.1, 0.2, 0.3, 0.4]
                          },
                          slownessWf: {
                            id: String(i),
                            startTime: 1274394558.45,
                            endTime: 1274394560.45,
                            sampleRate: 2,
                            sampleCount: 4,
                            // tslint:disable-next-line:no-magic-numbers
                            waveformSamples: [0.1, 0.2, 0.3, 0.4]
                          },
                          beamWf: {
                            id: String(i),
                            startTime: 1274394558.45,
                            endTime: 1274394560.45,
                            sampleRate: 2,
                            sampleCount: 4,
                            // tslint:disable-next-line:no-magic-numbers
                            waveformSamples: [0.1, 0.2, 0.3, 0.4]
                          },
                          fstatWf: {
                            id: String(i),
                            startTime: 1274394558.45,
                            endTime: 1274394560.45,
                            sampleRate: 2,
                            sampleCount: 4,
                            // tslint:disable-next-line:no-magic-numbers
                            waveformSamples: [0.1, 0.2, 0.3, 0.4]
                          }
                        }
                      }
                    },
                    signalDetection: {
                      id: '1',
                      station: {
                        id: String(i),
                        name: String(i),
                        defaultChannel: {
                            id: '1',
                            name: '1'
                        },
                        distanceToSource: {
                            distanceKm: 1000
                        }
                      }
                    }
                }
            };
            data.push(fkData);
        }
        return data;
    }
}
