import * as React from 'react';

export interface SignalDetectionProps {
    id: string;
    channelId: string;

    position: number;
    label: string;
    color: string;
    timeSecs: number;

    predicted?: boolean;
    selectedSignalDetections: string[] | undefined;

    getTimeSecsForClientX(clientX: number): number | undefined;
    toggleDragIndicator(show: boolean, color: string): void;
    positionDragIndicator(clientX: number): void;
    onClick?(e: React.MouseEvent<HTMLDivElement>, sdId: string): void;
    onContextMenu?(e: React.MouseEvent<HTMLDivElement>, channelId: string, sdId?: string): void;
    onSignalDetectionDragEnd?(sdId: string, timeSecs: number | undefined): void;
}

export class SignalDetection extends React.Component<SignalDetectionProps, {}> {

    private detectionWrapperRef: HTMLDivElement | null;
    private detectionLineRef: HTMLDivElement | null;

    public render() {
        const color = this.props.color;
        const isSelected = this.props.selectedSignalDetections &&
            this.props.selectedSignalDetections.indexOf(this.props.id) > -1;
        const pickStyle: React.CSSProperties = {
            borderLeft: '1px solid ' + color,
            bottom: this.props.predicted ? '10%' : '55%',
            // tslint:disable-next-line:no-magic-numbers
            left: `${this.props.position * 100}%`,
            position: 'absolute',
            top: this.props.predicted ? '55%' : '10%',
            boxShadow: isSelected ? `0px 0px 10px 3px ${color}` : 'initial'
        };
        const pickLabelStyle: React.CSSProperties = {
            bottom: this.props.predicted ? '10%' : 'initial',
            left: `calc(4px + ${this.props.position} * 100%)`,
            lineHeight: '80%',
            position: 'absolute',
            top: this.props.predicted ? 'initial' : '10%',
            fontSize: '0.95rem',
            color,
            fontWeight: 200
        };
        return (
            <div
                ref={ref => this.detectionWrapperRef = ref}
                onMouseDown={this.onMouseDown}
            >
                <div
                    ref={ref => this.detectionLineRef = ref}
                    style={pickStyle}
                />
                <div
                    className="weavess-sd-label"
                    onClick={this.onClick}
                    onContextMenu={this.onContextMenu}
                    style={pickLabelStyle}
                >
                    {this.props.label}
                </div>
            </div>
        );
    }

    public componentDidUpdate(prevProps: SignalDetectionProps) {
        if (!this.detectionLineRef) return;

        // if the color changes, flash animation
        if (prevProps.color !== this.props.color && this.props.color !== 'red') {
            this.detectionLineRef.style.borderColor = 'white';
            setTimeout(() => {
                if (!this.detectionLineRef) return;

                this.detectionLineRef.style.borderColor = this.props.color;
                this.detectionLineRef.style.transition = 'border-color 0.5s ease-in';
                setTimeout(() => {
                    if (!this.detectionLineRef) return;
                    this.detectionLineRef.style.transition = null;
                    // tslint:disable-next-line:no-magic-numbers align
                }, 500);
                // tslint:disable-next-line:no-magic-numbers align
            }, 500);
        }
    }

    private readonly onClick = (e: React.MouseEvent<HTMLDivElement>): void => {
        // prevent propagation of these events so that the underlying channel click doesn't register
        e.stopPropagation();
        if (this.props.onClick) {
            this.props.onClick(e, this.props.id);
        }
    }

    private readonly onContextMenu = (e: React.MouseEvent<HTMLDivElement>): void => {
        e.stopPropagation();
        if (this.props.onContextMenu) {
            this.props.onContextMenu(e, this.props.channelId, this.props.id);
        }
    }

    private readonly onMouseDown = (e: React.MouseEvent<HTMLDivElement>): void => {
        // if context-menu, don't trigger
        if (e.button === 2) return;
        // prevent propagation of these events so that the underlying channel click doesn't register
        e.stopPropagation();
        const start = e.clientX;
        let currentPos = e.clientX;
        let isDragging = false;
        let diff = 0;

        const onMouseMove = (event: MouseEvent) => {
            if (!this.detectionWrapperRef) return;

            currentPos = event.clientX;
            diff = Math.abs(currentPos - start);
            // begin drag if moving more than 1 pixel
            if (diff > 1 && !isDragging) {
                isDragging = true;
                this.detectionWrapperRef.style.filter = 'brightness(0.5)';
                this.props.toggleDragIndicator(true, this.props.color);
            }
            if (isDragging) {
                this.props.positionDragIndicator(currentPos);
            }
        };

        const onMouseUp = (event: MouseEvent) => {
            if (!this.detectionWrapperRef) return;

            if (isDragging) {
                this.props.toggleDragIndicator(false, this.props.color);
                this.detectionWrapperRef.style.filter = 'initial';
                const time = this.props.getTimeSecsForClientX(currentPos);
                if (this.props.onSignalDetectionDragEnd) {
                    this.props.onSignalDetectionDragEnd(this.props.id, time);
                }
            }
            document.body.removeEventListener('mousemove', onMouseMove);
            document.body.removeEventListener('mouseup', onMouseUp);
        };

        document.body.addEventListener('mousemove', onMouseMove);
        document.body.addEventListener('mouseup', onMouseUp);
    }
}
