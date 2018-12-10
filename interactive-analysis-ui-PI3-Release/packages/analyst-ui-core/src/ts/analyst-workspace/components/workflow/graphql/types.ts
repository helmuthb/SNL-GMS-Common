export interface Stage {
    id: string;
    name: string;
    activities: Activity[];
    intervals: Interval[];
}

export interface Activity {
    name: string;
}

export interface Analyst {
    userName: string;
}

export interface Interval {
    id: string;
    startTime: number;
    endTime: number;
    status: string;
    eventCount: number;
    completedBy: Analyst;
    activityIntervals: ActivityInterval[];
}

export interface ActivityInterval {
    id: string;
    activeAnalysts: Analyst[];
    activity: Activity;
    completedBy: Analyst;
    status: string;
    eventCount: number;
    timeStarted: number;
}
