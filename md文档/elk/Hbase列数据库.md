Hbase列数据库

## hbase+es

https://blog.csdn.net/weixin_44809337/article/details/121456509

hbase也能和es结合，实现海量数据存储+es的快速检索能力

将[Elasticsearch](https://so.csdn.net/so/search?q=Elasticsearch&spm=1001.2101.3001.7020)的DOC ID和Hbase的rowkey相关联：

```mermaid
graph LR
	数据源 --> 数据预处理
	数据预处理  --> es/索引数据
	数据预处理  --> hbase/原始数据
	es/索引数据 --> 通过DOC_ID和rowkey相关联
	hbase/原始数据  --> 通过DOC_ID和rowkey相关联
```

将源数据根据业务特点划分为索引数据和原始数据：

索引数据：指需要被检索的字段，存储在Elasticsearch集群中；

原始数据：指不需要被ES检索的字段，包括某些超长的文本数据等，存储在Hbase集群中。

将HBase的rowkey设定为ES的文档ID，搜索时根据业务条件先从ES里面全文检索出相对应的文档，从而获取出文档ID，即拿到了rowkey，再从HBase里面抽取数据。

**优点**
发挥了Elasticsearch的全文检索的优势，能够快速根据关键字检索出相关度最高的结果；
同时减少了Elasticsearch的存储压力，这种场景下不需要存储检索无关的内容，甚至可以禁用_source，节约一半的存储空间，同时提升最少30%的写入速度；
避免了Elasticsearch大数据量下查询返回慢的问题，大数据量下Hbase的抽取速度明显优于Elasticsearch；
各取所长，发挥两个组件各自的优势。

**缺点**

1、两个组件之间存在时效不一致的问题

相对而言，Elasticsearch的入库速度肯定是要快于Hbase的，这是需要业务容忍一定的时效性，对业务的要求会比较高。

2、同时管理两个组件增加了管理成本