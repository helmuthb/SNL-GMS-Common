const path = require('path');
const webpack = require('webpack');

const NODE_ENV = process.env.NODE_ENV || 'development';
const SERVER_URL = process.env.SERVER_URL || 'http://localhost:8080';

module.exports = {
    entry: {
        app: "./src/ts/index.ts",
    },
    module: {
        rules: [
            {
                loader: "ts-loader",
                test: /\.tsx?$/,
            }
        ],
    },
    plugins: [new webpack.DefinePlugin({
        'DEFAULT_SERVER_URL': JSON.stringify(SERVER_URL),
    })],
    output: {
        filename: "./build/analyst-ui-electron.js",
    },
    resolve: {
        extensions: [".webpack.js", ".web.js", ".ts", ".tsx", ".js", ".scss", ".css"],
    },
    target: "electron",
};