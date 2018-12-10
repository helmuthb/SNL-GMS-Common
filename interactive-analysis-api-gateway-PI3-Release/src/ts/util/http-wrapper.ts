import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';
import { set, toLower } from 'lodash';

import { gatewayLogger as logger } from '../log/gateway-logger';

/**
 * A lightweight wrapper around the Axios HTTP client providing convenience functions for
 * making HTTP GET and POST requests.
 */
export class HttpClientWrapper {

    // The axios HTTP client instance
    private axiosInstance: any;

    /**
     * Initialize the axios wrapper with an instance of the axios http client.
     * @param serviceConfig (optional) configuration settings used to intialize the axios client
     */
    public constructor(config?: any) {
        // TODO consider passing config parameters to the axios.create() method (e.g. base url)
        this.axiosInstance = axios.create();
    }

    public async request(requestConfig: any, data?: any): Promise<any> {

        // Throw an error if the request configuration is undefined or does not include a 'url' field
        if (!requestConfig) {
            throw new Error('Cannot send HTTP service request with undefined request configuration');
        }
        // 'url' is the only required config field
        if (!requestConfig.url) {
            throw new Error('Cannot send HTTP service request with undefined URL');
        }

        // Build the HTTP request object from the provided request config and data
        const request = requestConfig;

        // If request data are provided, provide them as 'params' for GET requests or 'data'
        // for all other methods
        if (data) {
            set(request,
                (!request.method || toLower(request.method) === 'get') ? 'params' : 'data',
                data);
        }

        logger.debug(`Sending service request: ${JSON.stringify(request, undefined, 2)}`);

        let response;
        try {
            response = await this.axiosInstance(request);
        } catch (error) {
            if (error.response) {
                // The request was made and the server responded with a status code
                // outside the range of 2xx
                logger.error(`Error response - status: ${error.response.status}`,
                             `\ndata: ${JSON.stringify(error.response.data, undefined, 2)}`,
                             `\nheaders: ${JSON.stringify(error.response.headers, undefined, 2)}`);
            } else if (error.request) {
                // The request was made but no response was received
                logger.error(`Error - request: ${JSON.stringify(error.request, undefined, 2)}`);
            } else {
                // Something happened in setting up the request that triggered an Error
                logger.error(`Error - message: ${error.message}`);
            }
            logger.error(`Error - config: ${JSON.stringify(error.config, undefined, 2)}`);
        }

        return response.data;
    }

    /**
     * Create & return a new AxiosMockWrapper for this client
     */
    public createHttpMockWrapper(mockConfig?: any): HttpMockWrapper {
        return mockConfig
            ? new HttpMockWrapper(new MockAdapter(this.axiosInstance, mockConfig))
            : new HttpMockWrapper(new MockAdapter(this.axiosInstance));
    }
}

/**
 * A lightweight wrapper around the Axios mock adapter library that provides convenience
 * wrapper functions to register mock handlers around simulated gms backend services.
 */
export class HttpMockWrapper {

    // HTTP response codes
    private static OK_STATUS: number = 200;
    private static BAD_REQUEST_STATUS: number = 400;

    // The axios HTTP server mock adapter
    private mockAdapter: MockAdapter;

    /**
     * Initialize the axios mock wrapper with an instance of the axios mock adapter.
     * @param mockAdapter The axios mock adapter to wrap
     */
    public constructor(mockAdapter: MockAdapter) {
        this.mockAdapter = mockAdapter;
    }

    /**
     * Configure a mock HTTP POST service interface at the provided URL, with the provided handler
     * function. 
     * @param url The URL to mock a POST service interface for
     * @param handler The handler function to be called whenever a POST request is sent to the input URL.
     * This function should accept an input object representing the parsed JSON request body, and should
     * return an object representing the result to encode as the HTTP response body.
     */
    public onPost(url: string, handler: (input: any) => any): void {

        logger.info(`Registering mock POST HTTP interface for url ${url}`);

        // Handle undefined url
        if (!url) {
            throw new Error('Cannot configure mock HTTP service with undefined URL');
        }

        // Configure a mock interface with the input url and handler function
        this.mockAdapter.onPost(url).reply(requestConfig => {

            logger.debug(`Handling mock service request on url ${url}`
                + ` with request config: ${JSON.stringify(requestConfig.data, undefined, 2)}`);

            // Handle undefined request data
            if (!requestConfig.data) {
                return [HttpMockWrapper.BAD_REQUEST_STATUS, 'Invalid request body'];
            }

            // Parse the request data from JSON
            const requestData = JSON.parse(requestConfig.data);

            return[HttpMockWrapper.OK_STATUS, handler(requestData.data)];
        });
    }

    public onGet(url: string, handler: (input: any) => any): void {

        logger.info(`Registering mock GET HTTP interface for url ${url}`);

        // Handle undefined url
        if (!url) {
            throw new Error('Cannot configure mock HTTP service with undefined URL');
        }

        // Configure a mock interface with the input url and handler function
        this.mockAdapter.onGet(url).reply(requestConfig => {

            logger.debug(`Handling mock service request on url ${url}`
                + ` with request config: ${JSON.stringify(requestConfig.params, undefined, 2)}`);

            // Handle undefined request data
            if (!requestConfig.params) {
                return [HttpMockWrapper.BAD_REQUEST_STATUS, 'Invalid query params'];
            }

            return[HttpMockWrapper.OK_STATUS, handler(requestConfig.params)];
        });
    }
}
