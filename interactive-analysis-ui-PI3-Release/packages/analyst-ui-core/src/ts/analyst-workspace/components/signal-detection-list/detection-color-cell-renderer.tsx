import * as React from 'react';

/**
 * Renders the Detection color cell for the signal detection list
 */
export class DetectionColorCellRenderer extends React.Component<any, {}> {

    public constructor(props) {
        super(props);
    }

    /**
     * react component lifecycle
     */
    public render() {
        return (
            <div
                style={{
                    height: '20px',
                    width: '20px',
                    backgroundColor: this.props.data.color,
                }}
            />
        );
    }
}
