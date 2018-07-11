# Gateway

![Version](https://img.shields.io/badge/version-3.1.0-green.svg)
[![Build Status](https://travis-ci.org/cwdtom/gateway.svg?branch=master)](https://travis-ci.org/cwdtom/gateway)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

## Overview
- API proxy gateway base on netty and okHttp.
1. Support http protocol.
1. Support https protocol.
1. Support cors protocol.
1. Support local flow limits base on token bucket.
1. Every proxy has independent thread pool.
1. Load balance
    1. RandomLoadBalance
    1. ConsistentHash
1. Support fuse service which is offline,then retest service in single thread,when it is reachable,take it online.
1. Use epoll model in Linux,other uses nio model.
1. Static file mapping.
1. Support consul auto service discovery.if it is enable,origin mapping config will be failure,every node has same weight.
1. Support zookeeper auto service discovery.If it is enable,origin mapping config will be failure,every node has same weight,zk priority is higher than consul.
1. Print detail log in developer mode,and print log which level is higher than 'warn' in production environment.

## Download

Download [the latest release](https://github.com/cwdtom/gateway/releases/download/3.1.0/gateway-3.1.0.jar)

## Usage

- start jar package
    ```shell
    java -jar gateway.jar -c /root/config.json
    ```
    1. -v version
    1. -c json config file path
    1. -h show help info
    
- config.json
    ```json
    {
      "mode": "dev",
      "http": {
        "port": 8080,
        "redirectHttps": false
      },
      "https": {
        "enable": false,
        "port": 8081,
        "keyPwd": "123456",
        "keyPath": "/Users/xxx/workspace/gateway/ssl/cwd.keystore"
      },
      "threadPool": {
        "core": 150,
        "max": 200,
        "timeout": 5000
      },
      "mapping": {
        "mode": "com.github.cwdtom.gateway.environment.lb.ConsistentHash",
        "list": {
          "127.0.0.1:8080": [
            {
              "url": "123.125.115.110:80",
              "weight": 200
            },
            {
              "url": "220.181.57.216:80",
              "weight": 100
            }
          ]
        }
      },
      "static": {
        "localhost:8080": "/Users/xxx/workspace/gateway"
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
      },
      "filter": {
        "before": [
          "com.github.cwdtom.gateway.filter.BeforeTestFilter"
        ],
        "after": [
          "com.github.cwdtom.gateway.filter.AfterTestFilter"
        ]
      },
      "consul": {
        "enable": true,
        "host": "192.168.0.236:8500",
        "mapping": {
          "test": [
            "localhost:8080"
          ]
        }
      },
      "zk": {
        "enable": true,
        "host": "127.0.0.1:2181",
        "mapping": {
          "test": [
            "localhost:8080"
          ]
        }
     }
    }
    ```
    1. mode: If it is not 'dev',only log level higher than 'warn' print.
    1. http: http config.
        1. port: http service port.
        1. redirectHttps: Whether need redirect https or not.
    1. https: https config.
        1. enable: enable https service.
        1. port: https service port.
        1. keyPwd: SSL certificate password.
        1. keyPath: SSL certificate file path.
    1. threadPool: single proxy thread pool config.
        1. core: core thread count.
        1. max: max thread count.
        1. timeout: thread pool timeout.
    1. mapping: mapping config,every host mapping multiple proxy address.
        1. mode: load balance algorithm,default RandomLoadBalance.
        1. list: mapping list.
            1. key: host.
            1. value: proxy address list.
                1. url: proxy address.
                1. weight: weight.
    1. static: static file mapping.
        1. key: host.
        1. value: local folder.
    1. cors: cors config.
        1. enable: enable cors.
        1. whiteList: cors white list,when white list is empty and cors enable,allow all requested.
    1. flowLimits: flow limits config.
        1. enable: enable flow limits.
        1. rate: token production rate,unit millisecond.
        1. maxSize: token bucket size.
    1. filter: request filter,the execution order is same as this config.
        1. before: pre filter.
        1. after: post filter.
    1. consul: consul config.
        1. enable: enable consul,if enable origin mapping will be failure.
        1. host: consul address.
        1. mapping: service mapping.
            1. key: service name(spring.application.name).
            1. value: host list
    1. zk: zookeeper config
        1. enable: enable zookeeper,if enable origin mapping will be failure.
        1. host: zookeeper address
        1. mapping: service mapping.
            1. key: service name(spring.application.name).
            1. value: host list

## CUSTOMIZE

- Load balance algorithm:Support customize algorithm.Algorithm class need extends UrlMapping class.Father class has proxy mapping map variable.

    ```java
    public class RandomLoadBalance extends UrlMapping {
        @Override
        public Mapper getLoadBalance(String host, String ip) {
            // do something
        }
    }
    ```
    
- filter:Support customize filter.Filter class need implements BeforeFilter interface or AfterFilter interface.

    - pre filter,return whether continue or not.
    
    ```java
    public class TestFilter implements BeforeFilter {
        @Override
        public boolean filter(FullHttpRequest request, byte[] content) {
            // do something
            return false;
        }
    }
    ```
    
    - post filter.
    
    ```java
    public class TestFilter implements AfterFilter {  
        @Override
        public void filter(FullHttpResponse response) {
            // do something
        }
    }
    ```