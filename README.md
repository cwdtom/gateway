# Gateway

![Version](https://img.shields.io/badge/version-1.4.0-green.svg)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

## Overview
- 基于netty的nio服务网关
1. 支持http协议
1. 支持https协议
1. 支持cors协议
1. 支持基于本地令牌桶的限流

## Usage

- 启动jar包
    ```shell
    java -jar gateway.jar -c /root/config.json
    ```
    1. -v 显示版本号
    1. -c 指定json配置文件
    1. -h 显示帮助中心
    
- config.json
    ```json
    {
      "http": {
        "port": 8080,
        "redirectHttps": false
      },
      "https": {
        "enable": false,
        "port": 8081,
        "keyPwd": "123456",
        "keyPath": "/Users/chenweidong/workspace/gateway/ssl/cwd.keystore"
      },
      "threadPool": {
        "core": 200,
        "max": 300,
        "timeout": 5000
      },
      "mapping": {
        "localhost:8080": [
          "123.125.115.110:80",
          "220.181.57.216:80"
        ],
        "localhost:8081": [
          "123.125.115.110:80",
          "220.181.57.216:80"
        ]
      },
      "cors": {
        "enable": true,
        "whiteList": []
      },
      "flowLimits": {
        "enable": true,
        "timeout": 500,
        "rate": 2000000000,
        "maxSize": 200
      }
    }
    ```
    1. http: http相关配置
        1. port: 端口号
        1. redirectHttps: 是否重定向至https
    1. https: https相关配置
        1. enable: 是否开启
        1. port: 端口号
        1. keyPwd: 证书密码
        1. keyPath: 证书路径
    1. threadPool: 线程池配置
        1. core: 核心线程数量
        1. max: 最大线程数量
        1. timeout: 超时时间
    1. mapping: 映射配置，每个host对应多个反向代理地址
    1. cors: 跨域相关配置
        1. enable: 是否开启跨域
        1. whiteList: 跨域白名单，列表为空且开启跨域情况下为允许全部origin跨域请求
    1. flowLimits: 限流配置
        1. enable: 是否开启限流
        1. timeout: 请求超时时间，单位ms
        1. rate: 令牌生产速率，单位ms
        1. maxSize: 令牌桶大小
