import * as React from 'react';

/**
 * Props for TheoreticalPhaseWindow
 */
export interface TheoreticalPhaseWindowProps {
    left: number;
    right: number;
    label: string;
    color: string;
}

/**
 * Displays a window of time on a channel where a phase may theoretically exist
 */
export class TheoreticalPhaseWindow extends React.Component<TheoreticalPhaseWindowProps, {}> {

    /**
     * React component lifecycle
     */
    public render() {
        const fracToPct = 100;
        const color = this.props.color;
        const theoreticalWindowStyle: React.CSSProperties = {
            backgroundColor: color,
            bottom: '20%',
            left: `${this.props.left * fracToPct}%`,
            opacity: 0.35,
            position: 'absolute',
            right: `${(1 - this.props.right) * fracToPct}%`,
            top: '55%',
        };
        const labelStyle: React.CSSProperties = {
            bottom: '5%',
            color: this.props.color,
            fontSize: '65%',
            left: `${this.props.left * fracToPct}%`,
            position: 'absolute',
            right: `${(1 - this.props.right) * fracToPct}%`,
            textAlign: 'center',
        };

        return (
            <div>
                <div style={theoreticalWindowStyle}/>
                <div style={labelStyle}>
                    {this.props.label}
                </div>
            </div>
        );
    }
}
