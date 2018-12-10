module.exports = {
    entry: "./src/ts/weavess.tsx",
    output: {
        library: "Weavess",
        libraryTarget: "umd",
        filename: "./dist/weavess.js"
    },
    resolve: {
        // Add `.ts` and `.tsx` as a resolvable extension.
        extensions: ['.webpack.js', '.web.js', '.ts', '.tsx', '.js', '.scss']
    },
    module: {
        loaders: [
            {
                loader: 'ts-loader',
                test: /\.tsx?$/
            },
            {
                loader: 'style-loader!css-loader!sass-loader',
                test: /\.scss$/
            }
        ]
    },
    devServer: {
        contentBase: './examples',
        host: '0.0.0.0',
        overlay: {
            warnings: false,
            errors: true,
        }
    }
}