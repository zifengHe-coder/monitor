'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  Bucket: '"xxx-xxx-xxx"', // Bucket
  Region: '"ap-beijing"', // Region
  Domain:'"xxx.xxx.com"',  // Domain
})
