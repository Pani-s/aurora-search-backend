# Aurora 聚合搜索平台后端

## 项目介绍

前端仓库地址：[Aurora 聚合搜索平台前端 - github](https://github.com/Pani-s/aurora-search-front)

### 介绍:

企业级一站式聚合搜索平台，基于 **Vue3前端+Spring Boot后端+Elastic Stack** 的全栈项目。
对用户来说，使用该平台，可以在同一个页面集中搜索出不同来源、不同类型的内容，提升用户的检索效率和搜索
体验。
对企业来说，当企业内部有多个项目的数据都存在搜索需求时，无需针对每个项目单独开发搜索功能，可以直接将
各项目的数据源接入搜索中台，从而提升开发效率、降低系统维护成本。

### ***预览图：***

在线访问：在线访问：[在线访问](http://search.soogyu.xyz)

<img src="http://pics.soogyu.xyz/picgo/search_display1.png" style="zoom: 50%;" />

<img src="http://pics.soogyu.xyz/picgo/search_display2.png" style="zoom: 50%;" />

<img src="http://pics.soogyu.xyz/picgo/search_display3.png" style="zoom: 50%;" />

<img src="http://pics.soogyu.xyz/picgo/search_display4.png" style="zoom: 50%;" />

## 项目架构

<img src="http://pics.soogyu.xyz/picgo/search_structure.PNG" style="zoom: 50%;" />

## 技术栈

### **前端**
- Vue 3
- Ant Design Vue组件库
- 页面状态同步机制

### **后端**
- Spring Boot2.7框架
- springboot-init项目模板
- MySQL数据库
- Elastic Stack
  - Elasticsearch搜索引擎
  - Logstash数据管道
  - Kibana数据可视化
- 数据抓取
  - 离线和实时抓取
  - Jsoup和HttpClient库
- 设计模式
  - 门面模式
  - 适配器模式
  - 注册器模式
- 数据同步(4种方式)
  - 定时
  - 双写
  - Logstash
  - Canal
- JMeter 压力测试



## 项目核心亮点

- 此项目采用了基于 Spring Boot 、Elastic Stack 和 Vue3 的一站式信息聚合搜索平台，将前后端技术、数据处理技术和搜索技术完美结合。其中， Spring Boot 和 MyBatis X 提供了强大的后端支持，用于处理基本的增删改查操作；Elastic Stack提供了高效、灵活的搜索功能；而 Vue3 则提供了前端用户界面，使用户可以在同一页面集中搜索不同来源和类型的内容。
- 数据源获取与创新：通过 HttpClient 和 jsoup 分别实现了离线获取外部网站文章和实时请求视频搜索接口的功能。使用 JSONUtil 解析和预处理文章，并使用 CSS Selector 语法解析和预处理图片信息，这些创新性的方法提高了数据获取的效率和准确性。
- 高效的数据聚合与处理：通过门面模式在后端对各类数据源的搜索结果进行聚合，统一返回给前端，这大大减少了前端请求次数，并降低了开发复杂度。通过 CompletableFuture 并发搜索各数据源，进一步提升了搜索接口的性能。
- 创新的搜索解决方案：为了解决文章搜不出的问题，自主搭建了 Elasticsearch 来代替 MySQ L的模糊查询，并通过为索引绑定 ik 分词器实现了更灵活的分词搜索。使用 Kibana DevTools + DSL 调试 ES 的搜索效果，并使用 Spring Data Elasticsearch 的 QueryBuilder 组合查询条件，以实现对 ES 内文章的灵活查询。
- 实时数据同步机制：通过 Spring Scheduler 定时同步近5分钟内发生更新的 MySQL 文章数据到 ES ，这确保了数据的实时性和准确性。



## 项目启动

### 后端

- 下载/拉取本项目到本地
- 通过 IDEA 代码编辑器进行打开项目，等待依赖的下载
- 修改配置文件 `application.yaml` 的信息，比如数据库、Elasticsearch 等
- 修改信息完成后，一键行运行项目
