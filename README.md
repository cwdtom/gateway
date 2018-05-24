# Gateway

![Version](https://img.shields.io/badge/version-1.1.0-green.svg)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

## Overview
- api网关
    
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
      }
    }
    ```
    1. http: http相关配置
        1. port: 端口号
        1. redirectHttps: 是否重定向至https
    1. https: https相关配置
        1. port: 端口号
        1. keyPwd: 证书密码
        1. keyPath: 证书路径
    1. threadPool: 线程池配置
        1. core: 核心线程数量
        1. max: 最大线程数量
        1. timeout: 超时时间
    1. mapping: 映射配置，每个host对应多个反向代理地址
