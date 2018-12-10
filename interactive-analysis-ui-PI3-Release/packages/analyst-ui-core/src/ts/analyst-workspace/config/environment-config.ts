export interface EnvironmentConfig {
    map: {
        online: boolean;
        offlineImagery: {
            url: string;
            maxResolutionLevel: number;
        };
    };
    additionalTimeToLoad: number;
}

export const environmentConfig: EnvironmentConfig = {
    map: {
        online: true,
        offlineImagery: {
            url: 'Assets/Textures/NaturalEarthII',
            maxResolutionLevel: 2
        },
    },
    additionalTimeToLoad: 900
};
