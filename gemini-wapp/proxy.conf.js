const PROXY_CONFIG = [
  {
    context: [
      "/api",
      "/_component"
    ],
    "target": "http://127.0.0.1:8090",
    "secure": false,
    "logLevel": "debug"
    // "changeOrigin": true
  }
];

module.exports = PROXY_CONFIG;
