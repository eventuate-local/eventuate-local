'use strict';

const path = require('path');
const webpack = require('webpack');

const HtmlWebpackPlugin = require('html-webpack-plugin');
const merge = require('webpack-merge');
const validate = require('webpack-validator');

const CopyWebpackPlugin = require('copy-webpack-plugin');

const pkg = require('./package.json');

const parts = require('./webpack/configParts');


const ENTRIES = Object.keys(pkg.dependencies);
const CSS_ENTIRES = [
  //'bootstrap-horizon'
];

const JS_ENTRIES = ENTRIES.filter(p => CSS_ENTIRES.indexOf(p) < 0);

const PATHS = {
  app: path.join(__dirname, 'src'),
  appEntry: path.join(__dirname, 'src', 'index.js'),
  devEntry: path.join(__dirname, 'src', 'devtools.js'),
  style: [
    path.join(__dirname, 'src', 'main.css')
  ],
  styleLess: [
    path.join(__dirname, 'src', 'main.less')
  ],
  build: path.join(__dirname, 'build')
};

const common = {
  entry: {
    app: PATHS.appEntry,
    // ,
    style: PATHS.styleLess
  },
  resolve: {
    extensions: ['', '.js', '.jsx'],
    alias: {
    }
  },
  node: {
    net: 'empty',
    tls: 'empty',
    dns: 'empty'
  },
  output: {
    path: PATHS.build,
    publicPath: '/',
    filename: '[name].js',
    // Modify the name of the generated sourcemap file.
    // You can use [file], [id], and [hash] replacements here.
    // The default option is enough for most use cases.
    sourceMapFilename: '[file].map', // Default

    // This is the sourcemap filename template. It's default format
    // depends on the devtool option used. You don't need to modify this
    // often.
    devtoolModuleFilenameTemplate: 'webpack:///[resource-path]?[loaders]'
  },
  plugins: [
    new CopyWebpackPlugin([
      { from: 'public/**', to: PATHS.build, flatten: true }
    ], {
      ignore: [
        '*.ejs'
      ]
    }),
    new HtmlWebpackPlugin({
      // Required
      inject: false,
      template: './public/index.ejs',

      // Optional
      title: 'Eventuate Local Console',
      description: 'eventuate-local-console App',
      appMountId: 'root',
      // baseHref: 'http://example.com/awesome',
      // devServer: 3001,
      googleAnalytics: {
        trackingId: 'UA-XXXX-XX',
        pageViewOnLoad: true
      },
      mobile: true
      // window: {
      //   env: {
      //     apiHost: 'http://myapi.com/api/v1'
      //   }
      // }
    })
  ]
};


const config = (() => {
  switch(process.env.npm_lifecycle_event) {
    case 'build':
    case 'watch':
      console.log('Production build');
      return merge(
        common,
        {
          devtool: 'source-map',
          output: {
            path: PATHS.build,
            filename: '[name].[chunkhash].js',
            // This is used for require.ensure. The setup
            // will work without but this is useful to set.
            chunkFilename: '[chunkhash].js'
          }
        },
        parts.productionBuild(),
        parts.clean(path.join(PATHS.build, '*')),
        // parts.setupCSS(PATHS.style),
        parts.useJSON(),
        // parts.useJQuery(),
        parts.extractBundle({
          name: 'vendor',
          entries: JS_ENTRIES
        }),
        parts.minify(),
        // parts.extractCSS(PATHS.style),
        parts.extractLESS(PATHS.styleLess),
        parts.purifyCSS([PATHS.app])
      );
    default:
      return merge(
        common,
        {
          entry: {
            dev: PATHS.devEntry,
            // ,
            style: PATHS.styleLess
          }
        },
        {
          devtool: 'source-map'
        },
        parts.useJSON(),
        // parts.useJQuery(),
        parts.extractBundle({
          name: 'vendor',
          entries: JS_ENTRIES
        }),
        // parts.setupCSS(PATHS.style),
        parts.setupLess(PATHS.styleLess),
        parts.devServer({
          // Customize host/port here if needed
          host: process.env.HOST,
          port: process.env.PORT,
          outputPath: PATHS.build
        })

      );
  }

})();

module.exports = validate(config);
