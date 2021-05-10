# 2021全国省市区街道社区五级SQL文件以及爬取代码

最近项目当中要用到全国区域信息，需要包含到社区，在网上找了很久，大部分是没有到社区的，或者有的是2019年的数据。
考虑到数据的准确性，所以自己写了个爬取代码进行爬取。
### 数据来源以及说明


数据来源：[国家统计局](http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm)

爬取时间：2021/05/06。国家统计局最新的数据是2020/06月更新的。

数据说明：这里的区域信息，是统计局划分的统计区域，基本是按照行政区域来进行划分了，所以会存在某些区域信息不存在的情况，（整体区域信息是完整的）
例如： 成都高新区，天府新区等等，大致的说法是高新区，天府新区是属于功能区不是行政区，高新区下面的街道/社区，从行政上来说还是属于武侯区，双流区等。

当然，部分APP上面是把高新区独立出来的，例如京东，在京东上可以发现成都桂溪街道属于高新区，但是在美团APP或者淘宝上，桂溪街道属于武侯区。

### sql文档地址
sql地址 [sys_area.sql](/sql/sys_area.sql)