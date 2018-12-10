import gql from 'graphql-tag';

/**
 * query type for map display
 */
export interface MapData {
    defaultStations: MapDataDefaultStation[];
}

/**
 * A default station loaded in the map display
 */
export interface MapDataDefaultStation {
    id: string;
    name: string;
    networks: {
        id: string;
        name: string;
        monitoringOrganization: string;
    }[];
    location: {
        latDegrees: number;
        lonDegrees: number;
        elevationKm: number;
    };
    sites: [{
        id: string;
        location: {
            latDegrees: number;
            lonDegrees: number;
            elevationKm: number;
        };
    }];
}

export const mapQuery = gql`
query mapDisplayDefaultStations {
    defaultStations {
        id
        name
        networks {
            id
            name
            monitoringOrganization
        }
        location {
            latDegrees
            lonDegrees
            elevationKm
        }
        sites{
            id,
            location{
                latDegrees,
                lonDegrees,
                elevationKm
            }
        }
    }
}
`;
