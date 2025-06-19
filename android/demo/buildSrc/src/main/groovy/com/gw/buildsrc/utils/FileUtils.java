package com.gw.buildsrc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @message FileUtils
 * @user caizhiyong
 * @date 9/20/2022
 */
public class FileUtils {
    /**
     * 删除文件（目录）.
     *
     * @param file 待删除的文件（目录）路径
     */
    public static void deleteFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile != null || childFile.length > 0) {
                for (File f : childFile) {
                    deleteFile(f);
                }
            }
            file.delete();
        }
    }


    /**
     * 将一个文件（目录）的所有文件拷贝到另外一个文件（目录）下.
     *
     * @param oldPath 源文件路径
     * @param newPath 目的文件路径
     * @return the boolean 是否拷贝成功
     */
    public static boolean copyFolder(String oldPath, String newPath) {
        try {
            File newFile = new File(newPath);
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    return false;
                }
            }
            File oldFile = new File(oldPath);
            String[] oldFiles = oldFile.list();
            File temp;
            for (String file : oldFiles) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file);
                } else {
                    temp = new File(oldPath + File.separator + file);
                }
                if (temp.isDirectory()) {
                    copyFolder(oldPath + File.separator + file, newPath + File.separator + file);
                } else if (!temp.exists()) {
                    return false;
                } else if (!temp.isFile()) {
                    return false;
                } else if (!temp.canRead()) {
                    return false;
                } else {
                    FileInputStream fileInputStream = new FileInputStream(temp);
                    FileOutputStream fileOutputStream = new FileOutputStream(newPath + File.separator + temp.getName());
                    byte[] buffer = new byte[1024];
                    int byteRead;
                    while ((byteRead = fileInputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, byteRead);
                    }
                    fileInputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
