const path = require('path');

const NODE_ENV = process.env.NODE_ENV || 'development'

module.exports = {
    entry: {
        app: "./src/ts/server.ts",
    },
    module: {
        rules: [
            {
                loader: "ts-loader",
                test: /\.tsx?$/,
            },
        ],
    },
    output: {
        filename: "./build/api-gateway.bundle.js",
    },
    resolve: {
        extensions: [".webpack.js", ".web.js", ".ts", ".tsx", ".js", ".scss", ".css"],
    },
    // the config dependency fails to list these as dependencies and only uses them conditionally.
    // we are using js-yaml, which we explicitly declare as a dependency
    externals: {
        hjson: "hjson",
        cson: "cson",
        toml: "toml",
        properties: "properties",
        x2js: "x2js",
        yaml: "yaml",
        bufferutil: "bufferutil",
        "iced-coffee-script": "iced-coffee-script",
        "ts-node": "ts-node",
        "coffee-script": "coffee-script",
        "utf-8-validate": "utf-8-validate",
    },
    target: "node",
};