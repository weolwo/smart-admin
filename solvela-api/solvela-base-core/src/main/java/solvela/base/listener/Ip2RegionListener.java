package solvela.base.listener;

import lombok.extern.slf4j.Slf4j;
import solvela.base.util.SolvelaIpUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

/**
 * 初初始化ip工具类
 *
 * @Author 1024创新实验室: zhuoda
 * @Date 2023-09-03 23:45:26
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href="https://1024lab.net">1024创新实验室</a>
 */
@Order(value = LoggingApplicationListener.DEFAULT_ORDER)
@Slf4j
public class Ip2RegionListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final String IP_FILE_NAME = "ip2region.xdb";

    private static final String LOG_DIRECTORY = "project.log-directory";

    /**
     * IP 归属地库要在<b>环境就绪、容器还没起</b>的这一刻初始化。
     *
     * <p>库文件打在 jar 里，而 ip2region 只能从真实文件路径加载，所以必须先落盘一次。
     * 落到日志目录是因为那是唯一保证可写的目录；用完立刻删 ——
     * {@code SolvelaIpUtil.init} 已经把内容读进内存了，留着只是多一份几 MB 的垃圾。
     */
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent applicationEvent) {
        String logDirectoryPath = requireLogDirectory(applicationEvent.getEnvironment());
        File tempFile = new File(resolveTempFilePath(logDirectoryPath));
        try {
            FileUtils.copyInputStreamToFile(new ClassPathResource(IP_FILE_NAME).getInputStream(), tempFile);
            SolvelaIpUtil.init(tempFile.getPath());
        } catch (IOException e) {
            // 起不来好过起来了但每条登录日志的归属地都是空 —— 那种问题要等到有人查日志才发现
            log.error("无法复制ip数据文件 ip2region.xdb", e);
            throw new ExceptionInInitializerError("无法复制ip数据文件");
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * 取日志目录，顺带把它塞回 System properties。
     *
     * <p>塞回去是给 logback 用的：日志配置在 Spring 环境之前就要解析
     * {@code ${project.log-directory}}，那时它只认 System properties。
     */
    private static String requireLogDirectory(ConfigurableEnvironment environment) {
        String logDirectoryPath = environment.getProperty(LOG_DIRECTORY);
        if (logDirectoryPath == null) {
            throw new ExceptionInInitializerError("环境变量为空：" + LOG_DIRECTORY);
        }
        System.setProperty(LOG_DIRECTORY, logDirectoryPath);
        File logDirectoryFile = new File(logDirectoryPath);
        if (!logDirectoryFile.exists()) {
            logDirectoryFile.mkdirs();
        }
        return logDirectoryPath;
    }

    private static String resolveTempFilePath(String logDirectoryPath) {
        return logDirectoryPath.endsWith("/")
                ? logDirectoryPath + IP_FILE_NAME
                : logDirectoryPath + "/" + IP_FILE_NAME;
    }


}