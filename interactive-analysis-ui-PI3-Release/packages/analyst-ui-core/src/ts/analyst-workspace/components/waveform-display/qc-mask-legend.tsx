import { Button, Checkbox, Intent, Popover, Position } from '@blueprintjs/core';
import * as React from 'react';
import { MaskDisplayFilter, QcMaskDisplayFilters } from '../../config/user-preferences';

/**
 * Waveform Display Controls Props
 */
export interface WaveformDisplayControlsProps {
    maskDisplayFilters: QcMaskDisplayFilters;
    setMaskDisplayFilters(key: string, maskDisplayFilter: MaskDisplayFilter);
}
export class QcMaskLegend extends
    React.Component<WaveformDisplayControlsProps> {

    public constructor(props: WaveformDisplayControlsProps) {
        super(props);
    }

    public render() {
        return (
            <Popover
                content={(
                    <div
                        style={{
                            padding: '1rem'
                        }}
                    >

                        <table>
                            <tbody>
                                {
                                    Object.keys(this.props.maskDisplayFilters)
                                        .map(key => (
                                            <tr key={key}>
                                                <td
                                                    style={{
                                                        verticalAlign: 'middle'
                                                    }}
                                                >
                                                    <Checkbox
                                                        checked={this.props.maskDisplayFilters[key].visible}
                                                        style={{
                                                            marginBottom: '0px'
                                                        }}
                                                        onChange={() =>
                                                            this.onChange(key, this.props.maskDisplayFilters[key])}
                                                    />
                                                </td>
                                                <td
                                                    style={{
                                                        verticalAlign: 'middle'
                                                    }}
                                                >
                                                    {this.props.maskDisplayFilters[key].name}
                                                </td>
                                                <td
                                                    style={{
                                                        verticalAlign: 'middle'
                                                    }}
                                                >
                                                    <div
                                                        style={{
                                                            height: '10px',
                                                            width: '20px',
                                                            backgroundColor: this.props.maskDisplayFilters[key].color,
                                                            marginLeft: '1rem'
                                                        }}
                                                    />
                                                </td>
                                            </tr>
                                        ))
                                }
                            </tbody>
                        </table>
                    </div>
                )}
                target={(<Button
                    text="Masks"
                    intent={Intent.NONE}
                    style={{ marginRight: '0.5rem' }}
                />)}
                position={Position.BOTTOM_LEFT}
            />
        );
    }

    private readonly onChange = (key: string, maskDisplayFilter: MaskDisplayFilter) => {
        maskDisplayFilter.visible = !maskDisplayFilter.visible;
        this.props.setMaskDisplayFilters(key, maskDisplayFilter);
    }
}
