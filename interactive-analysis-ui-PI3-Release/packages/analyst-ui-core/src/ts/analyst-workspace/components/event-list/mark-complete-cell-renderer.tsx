import { Button, Intent } from '@blueprintjs/core';
import * as React from 'react';

/**
 * Renders the 'Mark Complete' button for the event list
 */
export class MarkCompleteCellRenderer extends React.Component<any, {}> {

    public constructor(props) {
        super(props);
    }

    /**
     * react component lifecycle
     */
    public render() {
        return (
            <Button
                text="Mark Complete"
                disabled={this.props.data.disabled}
                intent={Intent.PRIMARY}
                onClick={e => this.props.context
                        .markEventComplete([this.props.data.eventId], this.props.data.stageId)
                }
                style={{
                    lineHeight: '1rem',
                    height: '20px',
                    minHeight: '0px',
                    minWidth: '0px',
                }}
            />
        );
    }
}
