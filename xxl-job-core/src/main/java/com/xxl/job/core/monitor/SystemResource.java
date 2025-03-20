package com.xxl.job.core.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SystemResource {
    private static final Logger logger = LoggerFactory.getLogger(SystemResource.class);

    public String idleResource() {
        SystemInfo si = new SystemInfo();
        CentralProcessor processor = si.getHardware().getProcessor();
        GlobalMemory memory = si.getHardware().getMemory();

        // 获取CPU核心
        int cpuCores = processor.getLogicalProcessorCount();
        logger.info("CPU Cores: {}", cpuCores);

        long availableMemory = 0;
        String memCurrentPath = "/sys/fs/cgroup/memory.current";
        String memMaxPath = "/sys/fs/cgroup/memory.max";
        if (Files.exists(Paths.get(memCurrentPath)) && Files.exists(Paths.get(memMaxPath))) {
            try {
                // 读取文件内容
                long current = Long.parseLong(new String(Files.readAllBytes(Paths.get(memCurrentPath))).trim());
                long max = Long.parseLong(new String(Files.readAllBytes(Paths.get(memMaxPath))).trim());
                availableMemory = (max - current)  / (1024 * 1024);
            } catch (IOException e) {
                logger.error("Error reading the file: {}", e.getMessage());
            } catch (NumberFormatException e) {
                logger.error("Error parsing long value from file content: {}",  e.getMessage());
            }
        } else {
            // 获取可用物理内存
            availableMemory = memory.getAvailable() / 1024 / 1024;
        }
        logger.info("Available Memory: {} MB", availableMemory);

        return "cpu=" + cpuCores +",memory="+ availableMemory + "MB";
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
