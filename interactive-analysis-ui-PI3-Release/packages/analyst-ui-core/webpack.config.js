const path = require('path');

const NODE_ENV = process.env.NODE_ENV || 'development';

// Default development values
var graphql_proxy_uri       = process.env.GRAPHQL_PROXY_URI || 'http://localhost:3000';
var waveforms_proxy_uri     = process.env.WAVEFORMS_PROXY_URI || 'http://localhost:3000';
var subscriptions_proxy_uri = process.env.SUBSCRIPTIONS_PROXY_URI || 'http://localhost:4000';

module.exports = {
    entry: {
        app: "./src/ts/index.tsx",
    },
    module: {
        rules: [
            {
                loader: "ts-loader",
                test: /\.tsx?$/,
                // options: {
                //     transpileOnly: true,
                // },
            },
            {
                loader: "style-loader!css-loader!resolve-url-loader",
                test: /\.css$/,
            },
            {
                loader: "style-loader!css-loader!resolve-url-loader!sass-loader?sourceMap",
                test: /\.scss$/,
            },
            {
                loader: "url-loader?limit=100000",
                test: /\.(png|woff|woff2|eot|ttf|svg)$/,
            },
            {
                loader: 'script-loader',
                test: /Cesium\.js$/
            }
        ],
    },
    output: {
        filename: "./build/analyst-ui-core.js",
    },
    resolve: {
        extensions: [".webpack.js", ".web.js", ".ts", ".tsx", ".js", ".scss", ".css"],
        alias: {
            "analyst-ui/css": path.resolve("src/css"),
            "analyst-ui": path.resolve("src/ts/analyst-workspace")
        }
    },
    externals: {
        electron: "electron"
    },
    devServer: {
        host: "0.0.0.0",
        disableHostCheck: true,
        overlay: {
            warnings: false,
            errors: true,
        },
        proxy: {
            "/graphql": {
                target: graphql_proxy_uri,
                secure: false,
                changeOrigin: true,
                logLevel: "debug",
            },
            "/waveforms": {
                target: waveforms_proxy_uri,
                secure: false,
                changeOrigin: true,
                logLevel: "debug",
            },
            "/subscriptions": {
                target: subscriptions_proxy_uri,
                ws: true
            }
        },

    },
    target: "web",
    devtool: "cheap-module-source-map",
};
