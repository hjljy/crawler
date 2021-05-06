
### SQL完整数据下载地址
sql文件地址：
### 完整代码地址：https://gitee.com/hjljy/crawler
### JAVA爬取代码
爬取代码非常简单，只需要使用jsoup即可。
```xml
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.11.3</version>
        </dependency>
```
```java
package cn.hjljy.crawler.demo.crawler;

import cn.hjljy.crawler.demo.pojo.jsoupCrawler.po.SysArea;
import cn.hjljy.crawler.demo.service.jsoupCrawler.ISysAreaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hjljy
 * @date 2021/04/30
 * 全国区域信息  省市区街道社区五级  爬取
 */
@Component
@Slf4j
public class AreaCrawler  implements ApplicationRunner{
    @Resource
    ISysAreaService service;

    public static void main(String[] args) throws IOException {
//        start();
    }


    /**
     * 数据来源网址： 国家统计局2020年统计用区划代码和城乡划分代码
     */
    public static String SOURCE_HTML = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/index.html";
    public static String CITY_TEST_HTML = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/13.html";
    public static String COUNTY_TEST_HTML = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/51/5101.html";
    public static String STREET_TEST_HTML = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/51/01/510108.html";
    public static String COMMITTEE_TEST_HTML = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/51/01/08/510108002.html";



    public  void start() throws IOException {
        //
        getProvinceInfo(SOURCE_HTML,true);
//        getCityInfo("13","130000000000", CITY_TEST_HTML, true);
//        getCountyInfo("51","5101","510100000000", COUNTY_TEST_HTML, true);
//        getStreetInfo("51","5101","510108","510108000000", STREET_TEST_HTML, true);
//        getCommitteeInfo("51","5101","510108","510108002", "510108002000",COMMITTEE_TEST_HTML, true);
    }

    /**
     * 获取省份数据
     * @param sourceHtml  区划代码网址
     * @param next 是否获取下一级
     * @throws IOException
     */
    private  void getProvinceInfo(String sourceHtml, boolean next) throws IOException {
        Document document = this.getDocument(null,sourceHtml,5);
        int sort =1;
        List<SysArea> list = new ArrayList<>();
        //省份信息
        Elements provincetr = document.getElementsByClass("provincetr");
        for (Element element : provincetr) {
            Elements aElements = element.getElementsByTag("a");
            for (Element aElement : aElements) {
                //省份名称
                String provinceName = aElement.text();
                String href = aElement.attr("href");
                //省份行政区划代码 2位
                String provinceCode = href.split("\\.")[0];
                long code = Long.parseLong(provinceCode);
                //省份行政区划代码 12位
                String provinceRCode = provinceCode+"0000000000";
                if (next) {
                    String absUrl = aElement.absUrl("href");
                    try {
                        getCityInfo(provinceCode,provinceRCode, absUrl, next);
                    } catch (Exception e) {
                        log.error(provinceName+"获取城市数据失败：" + e.getMessage());
                    }
                }
                SysArea area =new SysArea();
                area.setId(Long.parseLong(provinceRCode));
                area.setPid(0L);
                area.setProvinceCode(Long.parseLong(provinceCode));
                area.setName(provinceName);
                area.setSort(sort);
                area.setLevel(1);
                service.save(area);
                sort++;
            }
        }
    }

    /**
     * 获取省份下面的城市数据
     * @param provinceCode 省份行政区划代码 2位
     * @param provinceRCode 省份行政区划代码 12位
     * @param sourceHtml 爬取网页
     * @param next 是否获取下级
     * @throws IOException
     */
    private  void getCityInfo(String provinceCode,String provinceRCode, String sourceHtml, boolean next) throws IOException {
        Document document = this.getDocument(null,sourceHtml,5);
        int sort =1;
        List<SysArea> list = new ArrayList<>();
        //城市信息
        Elements citytr = document.getElementsByClass("citytr");
        for (Element element : citytr) {
            Elements tds = element.getElementsByTag("td");
            Element aElement = tds.get(0);
            Element aElement2 = tds.get(1);
            //城市12位区划代码
            String cityRCode = aElement.text();
            //城市名称
            String cityName = aElement2.text();
            //城市4位区划代码
            String cityCode = "";
            Element a = aElement.getElementsByTag("a").first();
            if (a != null) {
                String href = a.attr("href");
                //城市4位区划代码
                cityCode = href.split("\\.")[0].split("/")[1];
                if (next) {
                    String absUrl = a.absUrl("href");
                    try{
                        getCountyInfo(provinceCode,cityCode,cityRCode,absUrl,next);
                    }catch (Exception e){
                        log.error(cityName+"获取区/县数据失败"+e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            SysArea area =new SysArea();
            area.setId(Long.parseLong(cityRCode));
            area.setPid(Long.parseLong(provinceRCode));
            area.setCityCode(Long.parseLong(cityCode));
            area.setProvinceCode(Long.parseLong(provinceCode));
            area.setName(cityName);
            area.setSort(sort);
            area.setLevel(2);
            service.save(area);
            sort++;
        }
    }

    private Document getDocument(Document document, String sourceHtml, int time) throws IOException {
        if(null ==document && time>0){
            try{
                document = Jsoup.connect(sourceHtml).get();
            }catch (SocketTimeoutException timeoutException){
                document = getDocument(null,sourceHtml,time-1);
            }
        }
        return document;
    }

    /**
     *
     * 获取城市下面的区、县数据
     * @param provinceCode 省份行政区划代码 2位
     * @param cityCode 城市行政区划代码 4位
     * @param cityRCode 城市行政区划代码 12位
     * @param sourceHtml 爬取网页
     * @param next 是否获取下级
     * @throws IOException
     */
    private  void getCountyInfo(String provinceCode, String cityCode, String cityRCode, String sourceHtml, boolean next) throws IOException {
        Document document = this.getDocument(null,sourceHtml,5);
        //城市信息
        Elements countytr = document.getElementsByClass("countytr");
        int sort =1;
        List<SysArea> list = new ArrayList<>();
        for (Element element : countytr) {
            Elements tds = element.getElementsByTag("td");
            Element aElement = tds.get(0);
            Element aElement2 = tds.get(1);
            //区/县12位区划代码
            String countyRCode = aElement.text();
            //区/县名称
            String countyName = aElement2.text();
            //区/县6位区划代码
            String countyCode = "";
            Element a = aElement.getElementsByTag("a").first();
            if(a !=null){
                String href = a.attr("href");
                //区/县6位区划代码
                countyCode = href.split("\\.")[0].split("/")[1];
                if (next) {
                    String absUrl = a.absUrl("href");
                    try{
                        getStreetInfo(provinceCode,cityCode,countyCode,countyRCode,absUrl,next);
                    }catch (Exception e){
                        log.error(countyName+"获取数据失败："+e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            SysArea area =new SysArea();
            area.setId(Long.parseLong(countyRCode));
            area.setPid(Long.parseLong(cityRCode));
            area.setAreaCode(Long.parseLong(StringUtils.isNotEmpty(countyCode)?countyCode:countyRCode));
            area.setCityCode(Long.parseLong(cityCode));
            area.setProvinceCode(Long.parseLong(provinceCode));
            area.setName(countyName);
            area.setSort(sort);
            area.setLevel(3);
            System.out.println(area.toString());
            service.save(area);
            sort++;
        }
    }

    private  void getStreetInfo(String provinceCode, String cityCode, String countyCode, String countyRCode, String sourceHtml, boolean next) throws IOException {
        Document document = this.getDocument(null,sourceHtml,5);
        int sort =1;
        //城市信息
        Elements towntr = document.getElementsByClass("towntr");
        for (Element element : towntr) {
            Elements tds = element.getElementsByTag("td");
            Element aElement = tds.get(0);
            Element aElement2 = tds.get(1);
            //街道/镇12位区划代码
            String streetRCode = aElement.text();
            //街道/镇名称
            String streetName = aElement2.text();
            //街道/镇8位区划代码
            String streetCode = "";
            Element a = aElement.getElementsByTag("a").first();
            if(a !=null){
                String href = a.attr("href");
                //街道/镇8位区划代码
                streetCode = href.split("\\.")[0].split("/")[1];
                if (next) {
                    String absUrl = a.absUrl("href");
                    try{
                        getCommitteeInfo(provinceCode,cityCode,countyCode,streetCode,streetRCode,absUrl,next);
                    }catch (Exception e){
                        log.error(streetName+"获取数据失败"+e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            SysArea area =new SysArea();
            area.setId(Long.parseLong(streetRCode));
            area.setPid(Long.parseLong(countyRCode));
            area.setStreetCode(Long.parseLong(StringUtils.isNotEmpty(streetCode)?countyCode:streetRCode));
            area.setAreaCode(Long.parseLong(countyCode));
            area.setCityCode(Long.parseLong(cityCode));
            area.setProvinceCode(Long.parseLong(provinceCode));
            area.setName(streetName);
            area.setSort(sort);
            area.setLevel(4);
            service.save(area);
            sort++;
        }
    }

    private  void getCommitteeInfo(String provinceCode, String cityCode, String countyCode, String streetCode, String streetRCode, String sourceHtml, boolean next) throws IOException {
        Document document = this.getDocument(null,sourceHtml,5);
        //城市信息
        Elements villagetr = document.getElementsByClass("villagetr");
        int sort =1;
        for (Element element : villagetr) {
            Elements tds = element.getElementsByTag("td");
            Element aElement = tds.get(0);
            Element aElement2 = tds.get(1);
            Element aElement3 = tds.get(2);
            //社区/乡12位区划代码
            String committeeRCode = aElement.text();
            //城乡分类代码
            String type = aElement2.text();
            //社区/乡名称
            String committeeName = aElement3.text();
            SysArea area =new SysArea();
            area.setId(Long.parseLong(committeeRCode));
            area.setCommitteeCode(Long.parseLong(committeeRCode));
            area.setPid(Long.parseLong(streetRCode));
            area.setStreetCode(Long.parseLong(streetCode));
            area.setAreaCode(Long.parseLong(countyCode));
            area.setCityCode(Long.parseLong(cityCode));
            area.setProvinceCode(Long.parseLong(provinceCode));
            area.setName(committeeName);
            area.setCommitteeType(Long.parseLong(type));
            area.setSort(sort);
            area.setLevel(5);
            service.save(area);
            sort++;
        }
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        start();
    }
}

```

