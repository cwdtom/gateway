# Gateway

![Version](https://img.shields.io/badge/version-1.6.0-green.svg)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

## Overview
- 基于netty的nio服务网关
1. 支持http协议
1. 支持https协议
1. 支持cors协议
1. 支持基于本地令牌桶的限流
1. 相对独立的线程池
1. 负载均衡采用 RandomLoadBalance 随机负载均衡算法
1. 支持熔断不可用服务，并在单独线程中进行重试，成功以后重新设置为可用

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
        "core": 150,
        "max": 200,
        "timeout": 5000
      },
      "mapping": {
        "localhost:8080": [
          {
            "url": "123.125.115.110:80",
            "weight": 200
          },
          {
            "url": "220.181.57.216:80",
            "weight": 100
          }
        ]
      },
      "cors": {
        "enable": true,
        "whiteList": []
      },
      "flowLimits": {
        "enable": true,
        "timeout": 500,
        "rate": 5,
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
    1. threadPool: 单个反射配置的线程池配置
        1. core: 核心线程数量
        1. max: 最大线程数量
        1. timeout: 超时时间
    1. mapping: 映射配置，每个host对应多个反向代理地址
        1. url: 反向代理地址
        1. weight: 权重
    1. cors: 跨域相关配置
        1. enable: 是否开启跨域
        1. whiteList: 跨域白名单，列表为空且开启跨域情况下为允许全部origin跨域请求
    1. flowLimits: 限流配置
        1. enable: 是否开启限流
        1. timeout: 请求超时时间，单位ms
        1. rate: 令牌生产速率，单位ms
        1. maxSize: 令牌桶大小
