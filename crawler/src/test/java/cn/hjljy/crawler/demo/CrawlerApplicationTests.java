package cn.hjljy.crawler.demo;

import cn.hjljy.crawler.demo.crawler.AreaCrawler;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class CrawlerApplicationTests {

    @Autowired
    AreaCrawler areaCrawler;

    public static String 东莞市 = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/44/4419.html";
    public static String 中山市 = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/44/4420.html";
    public static String 儋州市 = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2020/46/4604.html";

    @Test
    void contextLoads() throws IOException {

//        List<String> isNotCountyHtml =new ArrayList<>();
//        isNotCountyHtml.add(东莞市);
        areaCrawler.getStreetInfo("46","4604","00","460400000000",儋州市,true);
    }

}
