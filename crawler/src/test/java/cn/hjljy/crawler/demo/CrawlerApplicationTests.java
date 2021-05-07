package cn.hjljy.crawler.demo;

import cn.hjljy.crawler.demo.crawler.AreaCrawler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class CrawlerApplicationTests {

    @Autowired
    AreaCrawler areaCrawler;

    @Test
    void contextLoads() throws IOException {
//        String str = "奤夿屯村委会";
//        String gb2312 = new String(str.getBytes("gb2312"), "UTF-8");
//        System.out.println(str);
//        System.out.println(gb2312);
        areaCrawler.start();
    }

}
