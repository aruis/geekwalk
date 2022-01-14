### Geekwalk——目标是用Java实现精巧且带UI的反向代理服务器

标题有两重含义：
1. 这是一个我用业余时间，慢慢打磨的项目，几乎所有的编程工作都会以B站 [直播](https://live.bilibili.com/10496628) / [录播](https://www.bilibili.com/video/BV1B64y1y7t1) 的形式共享出来，所以你会看到一个程序员亦步亦趋编写代码的全过程，故而命名为`Geekwalk`
2. 反向代理服务器，在实际项目中主要扮演网关——`Gateway`的角色，因此本项目也以`G`、`W`两个字母开头的单词作为呼应

已完成功能：
* [x] 实现反向代理关键代码
* [x] 反向代理支持配置文件
* [x] 增加单元测试
* [x] 支持静态站点部署
* [x] websocket反向代理 
* [x] websocket反向代理追加单元测试 
* [x] 压力测试一波，对比下Nginx
* [x] 反向代理支持负载均衡
* [x] 支持UI配置 [开发完结](https://github.com/aruis/geekwalkui)

编译：

```shell
./gradlew shadowJar
```

常规部署：

```shell
java -jar build/libs/geekwalk-1.0-SNAPSHOT-fat.jar -conf src/test/resources/config.json
```

静默部署：

```shell
java -jar build/libs/geekwalk-1.0-SNAPSHOT-fat.jar start -conf src/test/resources/config.json
```

一个典型的`config.json`配置文件形如：

```shell
{
  "port": 9090,
  "frontend": [
    {
      "prefix": "/web1",
      "dir": "/Users/liurui/develop/workspace-github/geekwalk/src/test/resources/web1",
      "reroute404": "/web1",
      "cachingEnabled": true,
      "maxAgeSeconds": 30
    },
    {
      "prefix": "/web2",
      "dir": "/Users/liurui/develop/workspace-github/geekwalk/src/test/resources/web2",
      "cachingEnabled": false
    }
  ],
  "backend": [
    {
      "prefix": "/a",
      "upstream": [
        {
          "url": "http://127.0.0.1:8080/a",
          "weight": 1
        },
        {
          "url": "http://127.0.0.1:8080/a",
          "weight": 1
        }
      ]
    },
    {
      "prefix": "/b",
      "upstream": [
        {
          "url": "http://127.0.0.1:8081/b",
          "weight": 1
        },
        {
          "url": "http://127.0.0.1:8082/b",
          "weight": 1
        }
      ]
    },
    {
      "prefix": "/",
      "upstream": "http://127.0.0.1:8080/"
    }
  ]
}
```



