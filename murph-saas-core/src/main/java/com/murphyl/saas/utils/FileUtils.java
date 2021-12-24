package com.murphyl.saas.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * 文件 - 工具类
 *
 * @date: 2021/12/24 16:42
 * @author: murph
 */
public final class FileUtils {

    public static Collection<File> list(File location, boolean recursive) {
        File[] files = location.listFiles();
        if (!recursive) {
            return Arrays.asList(files);
        }
        Set<File> result = new TreeSet<>();
        for (File item : files) {
            if (item.isDirectory()) {
                result.addAll(list(item, true));
            } else if (item.isFile()) {
                result.add(item);
            }
        }
        return result;
    }

    public static String read(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("读取文件失败", e);
        }
    }

}
