package cn.hjljy.crawler.demo.crawler;

import cn.hjljy.crawler.demo.pojo.jsoupCrawler.po.SysArea;
import cn.hjljy.crawler.demo.service.jsoupCrawler.ISysAreaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * @author hjljy
 * @date 2021/04/30
 * 全国区域信息  省市区街道社区五级  爬取
 */
@Component
@Slf4j
public class AreaCrawler implements ApplicationRunner {
    @Resource
    ISysAreaService service;
    /**
     * 数据来源网址： 国家统计局2020年统计用区划代码和城乡划分代码
     */
    public static String SOURCE_HTML = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/index.html";
    public static String CITY_TEST_HTML = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/13.html";
    public static String COUNTY_TEST_HTML = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/51/5101.html";
    public static String STREET_TEST_HTML = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/51/15/511525.html";
    public static String COMMITTEE_TEST_HTML = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/11/01/14/110114003.html";


    public void start(String url) throws IOException {
        //
        getProvinceInfo(SOURCE_HTML, true);
//        getCityInfo("13","130000000000", CITY_TEST_HTML, true);
//        getCountyInfo("51","5101","510100000000", COUNTY_TEST_HTML, true);
//        getStreetInfo("51","5115","511525","511525000", STREET_TEST_HTML, true);
//        getCommitteeInfo("11", "1101", "110114", "110114003", "110114003000", COMMITTEE_TEST_HTML, true);
    }

    /**
     * 获取省份数据
     *
     * @param sourceHtml 区划代码网址
     * @param next       是否获取下一级
     * @throws IOException
     */
    public void getProvinceInfo(String sourceHtml, boolean next) throws IOException {
        Document document = this.getDocument2(null, sourceHtml, 5);
        int sort = 1;
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
                //省份行政区划代码 12位
                String provinceRCode = provinceCode + "0000000000";
                if (next) {
                    String absUrl = aElement.absUrl("href");
                    getCityInfo(provinceCode, provinceRCode, absUrl, next);
                }
                System.out.println(provinceName);
                SysArea area = new SysArea();
                area.setId(Long.parseLong(provinceRCode));
                area.setPid(0L);
                area.setProvinceCode(Long.parseLong(provinceCode));
                area.setName(provinceName);
                area.setSort(sort);
                area.setLevel(1);
                //保存到数据库
                service.save(area);
                sort++;
            }
        }
    }

    /**
     * 获取省份下面的城市数据
     *
     * @param provinceCode  省份行政区划代码 2位
     * @param provinceRCode 省份行政区划代码 12位
     * @param sourceHtml    爬取网页
     * @param next          是否获取下级
     * @throws IOException
     */
    public void getCityInfo(String provinceCode, String provinceRCode, String sourceHtml, boolean next) throws IOException {
        Document document = this.getDocument2(null, sourceHtml, 5);
        int sort = 1;
        //城市信息
        if (null == document) {
            if (null == document) {
                document = this.getDocument3(null, sourceHtml, 5);
            }
        }
        Elements citytr = document.getElementsByClass("citytr");
        if (citytr.size() == 0) {
            document = this.getDocument3(null, sourceHtml, 5);
        }
        citytr = document.getElementsByClass("citytr");
        if (citytr.size() == 0) {
            log.error(sourceHtml + "未获取到城市信息");
            log.error(document.wholeText());
        }
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
                    getCountyInfo(provinceCode, cityCode, cityRCode, absUrl, next);
                }
            }
            System.out.println(cityName);
            SysArea area = new SysArea();
            area.setId(Long.parseLong(cityRCode));
            area.setPid(Long.parseLong(provinceRCode));
            area.setCityCode(Long.parseLong(cityCode));
            area.setProvinceCode(Long.parseLong(provinceCode));
            area.setName(cityName);
            area.setSort(sort);
            area.setLevel(2);
            System.out.println(area.toString());
            service.save(area);
            sort++;
        }
    }


    /**
     * 获取城市下面的区、县数据
     *
     * @param provinceCode 省份行政区划代码 2位
     * @param cityCode     城市行政区划代码 4位
     * @param cityRCode    城市行政区划代码 12位
     * @param sourceHtml   爬取网页
     * @param next         是否获取下级
     * @throws IOException
     */
    public void getCountyInfo(String provinceCode, String cityCode, String cityRCode, String sourceHtml, boolean next) throws IOException {
        Document document = this.getDocument2(null, sourceHtml, 5);
        if (null == document) {
            document = this.getDocument3(null, sourceHtml, 5);
        }
        //城市信息
        Elements countytr = document.getElementsByClass("countytr");
        if (countytr.size() == 0) {
            document = this.getDocument3(null, sourceHtml, 5);
        }
        countytr = document.getElementsByClass("countytr");
        if (countytr.size() == 0) {
            log.error(sourceHtml + "未获取到区、县信息");
        }
        int sort = 1;
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
            if (a != null) {
                String href = a.attr("href");
                //区/县6位区划代码
                countyCode = href.split("\\.")[0].split("/")[1];
                if (next) {
                    String absUrl = a.absUrl("href");
                    getStreetInfo(provinceCode, cityCode, countyCode, countyRCode, absUrl, next);
                }
            }
            System.out.println(countyName);
            SysArea area = new SysArea();
            area.setId(Long.parseLong(countyRCode));
            area.setPid(Long.parseLong(cityRCode));
            area.setAreaCode(Long.parseLong(StringUtils.isNotEmpty(countyCode) ? countyCode : countyRCode.substring(0, 6)));
            area.setCityCode(Long.parseLong(cityCode));
            area.setProvinceCode(Long.parseLong(provinceCode));
            area.setName(countyName);
            area.setSort(sort);
            area.setLevel(3);
            service.save(area);
            sort++;
        }
    }

    /**
     * 获取街道信息
     * @param provinceCode 省份CODE
     * @param cityCode  城市code
     * @param countyCode 区县code
     * @param countyRCode 区县完整code
     * @param sourceHtml 街道信息html地址
     * @param next 是否获取下级区划
     * @throws IOException 异常
     */
    public void getStreetInfo(String provinceCode, String cityCode, String countyCode, String countyRCode, String sourceHtml, boolean next) throws IOException {
        Document document = this.getDocument2(null, sourceHtml, 5);
        if (null == document) {
            document = this.getDocument3(null, sourceHtml, 5);
        }
        int sort = 1;
        //城市信息
        Elements towntr = document.getElementsByClass("towntr");
        if (towntr.size() == 0) {
            document = this.getDocument3(null, sourceHtml, 5);
        }
        towntr = document.getElementsByClass("towntr");
        if (towntr.size() == 0) {
            log.error(sourceHtml + "未获取到街道信息");
        }
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
            if (a != null) {
                String href = a.attr("href");
                //街道/镇8位区划代码
                streetCode = href.split("\\.")[0].split("/")[1];
                if (next) {
                    String absUrl = a.absUrl("href");
                    getCommitteeInfo(provinceCode, cityCode, countyCode, streetCode, streetRCode, absUrl, next);
                }
            }
            System.out.println(streetName);
            SysArea area = new SysArea();
            area.setId(Long.parseLong(streetRCode));
            area.setPid(Long.parseLong(countyRCode));
            area.setStreetCode(Long.parseLong(StringUtils.isNotEmpty(streetCode) ? streetCode : streetRCode.substring(0, 9)));
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

    /**
     * 获取社区、乡村信息
     * @param provinceCode 省份CODE
     * @param cityCode  城市code
     * @param countyCode 区县code
     * @param sourceHtml 街道信息html地址
     * @param streetCode 街道code
     * @param streetRCode 街道完整code
     * @throws IOException
     */
    public void getCommitteeInfo(String provinceCode, String cityCode, String countyCode, String streetCode, String streetRCode, String sourceHtml, boolean next) throws IOException {
        Document document = this.getDocument2(null, sourceHtml, 5);
        if (null == document) {
            document = this.getDocument3(null, sourceHtml, 5);
        }
        //城市信息
        Elements villagetr = document.getElementsByClass("villagetr");
        if (villagetr.size() == 0) {
            document = this.getDocument3(null, sourceHtml, 5);
        }
        villagetr = document.getElementsByClass("villagetr");
        if (villagetr.size() == 0) {
            log.error(sourceHtml + "未获取到社区信息");
        }
        int sort = 1;
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
            System.out.println(committeeName);
            SysArea area = new SysArea();
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

    public Document getDocument2(Document document, String sourceHtml, int time) throws IOException {
        if (null == document && time > 0) {
            try {
                //避免生僻字乱码
                Connection connect = HttpConnection.connect(sourceHtml);
                connect.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36 Edg/90.0.818.51");
                connect.cookie("_trs_uv", "ko2mem1r_6_2wb2");
                connect.cookie("SF_cookie_1", "37059734");
                connect.followRedirects(false);
                BufferedInputStream inputStream = connect.execute().bodyStream();
                //获取到网页数据，采用GBK编码的方式，避免乱码存在
                document = Jsoup.parse(inputStream, "GBK", sourceHtml);
            } catch (Exception timeoutException) {
                log.warn("2链接失败：{}，第几次：{}", sourceHtml, time);
                if (time == 1) {
                    timeoutException.printStackTrace();
//                    throw new IOException();
                }
                try {
                    //减少请求频率，避免被反爬机制监控
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                document = getDocument2(null, sourceHtml, time - 1);
            }
        }
        return document;
    }

    public Document getDocument3(Document document, String sourceHtml, int time) throws IOException {
        if (null == document && time > 0) {
            try {
                Thread.sleep(500);
                document = Jsoup.connect(sourceHtml)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36 Edg/90.0.818.51")
                        .cookie("_trs_uv", "ko2mem1r_6_2wb2")
                        .followRedirects(false)
                        .get();
            } catch (Exception timeoutException) {
                log.warn("3链接失败：{}，第几次：{}", sourceHtml, time);
                if (time == 1) {
                    timeoutException.printStackTrace();
                    throw new IOException();
                }
                document = getDocument3(null, sourceHtml, time - 1);
            }
        }
        return document;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        start(null);
    }
}
