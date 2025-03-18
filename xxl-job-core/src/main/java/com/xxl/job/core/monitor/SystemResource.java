package com.xxl.job.core.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

public class SystemResource {
    private static Logger logger = LoggerFactory.getLogger(SystemResource.class);

    public String idleResource() {
        SystemInfo si = new SystemInfo();
        CentralProcessor processor = si.getHardware().getProcessor();
        GlobalMemory memory = si.getHardware().getMemory();

        // 获取CPU负载
        int cpuCores = processor.getLogicalProcessorCount();
        logger.info("CPU Cores: {}", cpuCores);

        // 获取物理内存总量
        long totalMemory = memory.getTotal();
        logger.info("Total Memory: {}", totalMemory / 1024 / 1024 + " MB");

        // 获取可用物理内存
        long availableMemory = memory.getAvailable();
        logger.info("Available Memory: {}", availableMemory / 1024 / 1024 + " MB");

        return "cpu=" + cpuCores +",memory="+ (availableMemory / 1024 / 1024 + "MB");
    }

    /**
     * 解析输入字符串并提取内存值。
     * @param input 输入字符串，格式如 "cpu=8,memory=5908MB"
     * @return 内存值（以 MB 为单位）
     */
    public static Integer parseMemory(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        // 使用正则表达式忽略大小写地匹配 'memory=' 后面跟随的数字和'MB'
        String patternString = "(?i)memory=(\\d+)(mb)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternString);
        java.util.regex.Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // 如果找到了匹配项，则转换并返回内存值
            return Integer.parseInt(matcher.group(1));
        } else {
            return null; // 如果没有找到匹配项，返回null
        }
    }
}
