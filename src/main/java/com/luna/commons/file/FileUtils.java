package com.luna.commons.file;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.luna.commons.dto.constant.ResultCode;
import com.luna.commons.exception.FileException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * @author Tony
 */
public class FileUtils {
    /**
     * 读取文本文件所有内容
     * 
     * @param file
     * @return
     */
    public static List<String> readAllLines(String file) {
        try {
            return Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileException(ResultCode.PARAMETER_INVALID, "读取文件错误,请检查文件路径:" + file);
        }
    }

    /**
     * 删除文件或空目录
     * 
     * @param file
     */
    public static void deleteIfExists(String file) {
        try {
            Files.deleteIfExists(Paths.get(file));
        } catch (IOException e) {
            throw new FileException(ResultCode.PARAMETER_INVALID, "删除文件错误:" + file);
        }
    }

    /**
     * 判断http文件是否存在
     * 
     * @param httpPath 网路路径
     * @return
     */
    public static Boolean existHttpPath(String httpPath) {
        try {
            URL httpurl = new URL(new URI(httpPath).toASCIIString());
            URLConnection urlConnection = httpurl.openConnection();
            String headerField = urlConnection.getHeaderField(0);
            if (headerField.startsWith("HTTP/1.1 404")) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 获取文件数目
     *
     * @param path
     * @return
     */
    public static Integer getFileLength(String path) {
        int fileCount = 0;
        int folderCount = 0;
        File d = new File(path);
        File list[] = d.listFiles();
        for (int i = 0; i < list.length; i++) {
            if (list[i].isFile()) {
                fileCount++;
            } else {
                folderCount++;
            }
        }
        return fileCount;
    }

    /**
     * 判断一个文件是否存在
     *
     * @param fileName
     * @return
     */
    public static boolean isFileExists(String fileName) {
        return Files.exists(Paths.get(fileName));
    }

    /** 行数统计时一次性读byte大小 */
    private static final int BYTE_SIZE = 1024 * 8;

    /**
     * 计算文件中行数
     *
     * @param fileName
     * @return
     * @throws Exception
     */
    public static long countFileLines(String fileName) {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(fileName));

            byte[] c = new byte[BYTE_SIZE];
            long count = 0L;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            is.close();
            return (count == 0 && !empty) ? 1 : count;
        } catch (Exception e) {
            throw new FileException(ResultCode.PARAMETER_INVALID, "读取文件错误:" + fileName);
        }
    }

    /**
     * 下载文件
     * <p>
     * 若文件已存在，覆盖
     * </p>
     * <p>
     * 有异常时抛出异常
     * </p>
     * 
     * @param url 网络路径
     * @param file 本地路径
     */
    public static void downloadFile(String url, String file) {
        try {
            org.apache.commons.io.FileUtils.copyURLToFile(new URL(url), new File(file), 5000, 5000);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 下载文件，失败在优先次数内重试
     * 
     * @param url
     * @param file
     * @param maxRetry
     */
    public static void downloadFileWithRetry(String url, String file, int maxRetry) {
        for (int i = 0; i < maxRetry - 1; i++) {
            try {
                downloadFile(url, file);
                return;
            } catch (Exception e) {
                // do nothing
            }
        }

        downloadFile(url, file);
    }

    /**
     * 上传到文件服务器
     *
     * @param file 文件字节
     * @param filePath 网络路径
     * @return
     * @throws Exception
     */
    public static void uploadFile(byte[] file, String filePath) {
        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        Client client = new Client();
        WebResource resource = client.resource(filePath);
        resource.put(String.class, file);
    }

    /**
     * 删除httpd服务器文件
     *
     * @param filePath 网络路径
     * @throws Exception
     */
    public static void delete(String filePath) {
        Client client = new Client();
        WebResource resource = client.resource(filePath);
        resource.delete();
    }
}
