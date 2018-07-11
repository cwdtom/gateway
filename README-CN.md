# Gateway

![Version](https://img.shields.io/badge/version-3.1.0-green.svg)
[![Build Status](https://travis-ci.org/cwdtom/gateway.svg?branch=master)](https://travis-ci.org/cwdtom/gateway)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](http://opensource.org/licenses/MIT)

## Overview
- 基于netty和okHttp的反向代理网关。
1. 支持http协议。
1. 支持https协议。
1. 支持cors协议。
1. 支持基于本地令牌桶的限流。
1. 相对独立的线程池。
1. 负载均衡采用
    1. RandomLoadBalance 随机负载均衡算法
    1. ConsistentHash 一致性hash算法
1. 支持熔断不可用服务，并在单独线程中进行重试，成功以后重新设置为可用。
1. 在linux下采用epoll其他系统采用nio。
1. 静态文件映射。
1. 支持consul自动服务发现，启用后原mapping配置失效，每个节点拥有相同的权重。
1. 支持zookeeper自动服务发现，启用后原mapping配置失效，每个节点拥有相同的权重，zk优先级高于consul。
1. 开发者模式输出详细日志，生产环境时只输出warn及以上。

## Download

下载最[新版本JAR包](https://github.com/cwdtom/gateway/releases/download/3.1.0/gateway-3.1.0.jar)

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
    1. mode: 运行模式，不为dev或缺省时，日志只输出warn及以上
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
        1. mode: 负载均衡算法，默认为RandomLoadBalance
        1. list: 映射表
            1. key: host
            1. value: 反向代理地址列表
                1. url: 反向代理地址
                1. weight: 权重
    1. static: 静态文件映射
        1. key: host地址
        1. value: 本地映射文件夹
    1. cors: 跨域相关配置
        1. enable: 是否开启跨域
        1. whiteList: 跨域白名单，列表为空且开启跨域情况下为允许全部origin跨域请求
    1. flowLimits: 限流配置
        1. enable: 是否开启限流
        1. rate: 令牌生产速率，单位ms
        1. maxSize: 令牌桶大小
    1. filter: 拦截器，拦截顺序按配置文件中的顺序执行
        1. before: 前置拦截器
        1. after: 后置连接器
    1. consul: consul注册中心配置
        1. enable: 是否启用，启用后原mapping配置失效
        1. host: consul地址
        1. mapping: 服务映射
            1. key: service名字 spring.application.name
            1. value: host列表
    1. zk: zookeeper注册中心配置
        1. enable: 是否启用，启用后原mapping配置失效
        1. host: zookeeper地址
        1. mapping: 服务映射
            1. key: service名字 spring.application.name
            1. value: host列表

## CUSTOMIZE

- 负载均衡算法：可以自定义算法，算法类需要继承UrlMapping类。父类UrlMapping含有映射表成员变量mapping。

    ```java
    public class RandomLoadBalance extends UrlMapping {
        @Override
        public Mapper getLoadBalance(String host, String ip) {
            // do something
        }
    }
    ```
    
- 拦截器：可以自定义拦截器，拦截类需要实现BeforeFilter或AfterFilter接口。

    - 前置拦截器，返回是否继续执行。
    
    ```java
    public class TestFilter implements BeforeFilter {
        @Override
        public boolean filter(FullHttpRequest request, byte[] content) {
            // do something
            return false;
        }
    }
    ```
    
    - 后置拦截器。
    
    ```java
    public class TestFilter implements AfterFilter {  
        @Override
        public void filter(FullHttpResponse response) {
            // do something
        }
    }
    ```