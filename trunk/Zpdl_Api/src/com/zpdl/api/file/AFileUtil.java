package com.zpdl.api.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.os.Environment;
import android.text.TextUtils;

import com.zpdl.api.util.Alog;

public class AFileUtil {
    private volatile static AFileUtil uniqueInstance;

    public static AFileUtil getInstance() {
        if(uniqueInstance == null) {
            synchronized (AFileUtil.class) {
                if(uniqueInstance == null) {
                    uniqueInstance = new AFileUtil();
                }
            }
        }

        return uniqueInstance;
    }

    public static String getFileName(String path) {
        int filenamePos = path.lastIndexOf(File.separator);
        String filename = 0 <= filenamePos ? path.substring(filenamePos + 1) : path;

        int dotPos = filename.lastIndexOf('.');
        String filenameExceptextension = 0 <= dotPos ? filename.substring(0, dotPos) : filename;

        return filenameExceptextension;
    }

    public static String getFileExtention(String path) {
        int filenamePos = path.lastIndexOf(File.separator);
        String filename = 0 <= filenamePos ? path.substring(filenamePos + 1) : path;

        int dotPos = filename.lastIndexOf('.');
        String extension = 0 <= dotPos ? filename.substring(dotPos + 1) : "";

        return extension;
    }

    private String              sdCard;
    private ArrayList<String>   sdCardAl;

    private AFileUtil() {
        sdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
        sdCardAl = new ArrayList<String>();

        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(File.separatorChar);
        splitter.setString(sdCard);
        for(String dir : splitter) {
            if(dir.length() > 0)
                sdCardAl.add(dir);
        }
    }

    public String getSdCard() {
        return sdCard;
    }

    public ArrayList<String> getSdCardArrayList() {
        return sdCardAl;
    }

    public boolean isSdCard(String path) {
        return path.startsWith(sdCard);
    }

    /*
     * 0 is exist.
     * 1 is create.
     * -1 is fail.
     * */
    public int createDirectory(String path) {
        File directory = new File(path);
        if(directory.exists() && directory.isDirectory()) {
            return 0;
        }

        String absolutePath = directory.getAbsolutePath();

        int index = 0;
        while(index >= 0) {
            index = absolutePath.indexOf(File.separator, index + 1);
            if(index < 0) {
                if(directory.mkdir())
                    break;
                else
                    return -1;
            }

            String subPath = absolutePath.substring(0, index);
            File subFile = new File(subPath);
            if(!subFile.exists()) {
                if(!subFile.mkdir());
                    return -1;
            }
        }
        return 1;
    }

    public String distinctFileString(String path) {
        File infile = new File(path);
        if(!infile.exists()) {
            return path;
        }

        String parentString = infile.getParent();
        String nameString = getFileName(path);
        String extentionString = getFileExtention(path);

        int i = 0;
        String newPath = String.format("%s"+File.separator+"%s_%3d.%s", parentString, nameString, i, extentionString);
        Alog.d("distinctFileString newPath = %s",newPath);
        while(i < 999) {
            File file = new File(newPath);
            if(file.exists()) {
                i++;
                newPath = String.format("%s"+File.separator+"%s_%3d.%s", parentString, nameString, i, extentionString);
                Alog.d("distinctFileString newPath = %s",newPath);
            } else {
                break;
            }
        }

        return newPath;
    }

    public static final int COMPARETYPE_NAME      = 0x0000;
    public static final int COMPARETYPE_DATE      = 0x0001;

    public File[] sortFileList(File[] files, final int compareType, final boolean order) {
        Arrays.sort(files, new Comparator<Object>() {
            @Override
            public int compare(Object object1, Object object2) {

                String s1 = "";
                String s2 = "";

                if (compareType == COMPARETYPE_NAME) {
                    s1 = ((File) object1).getName();
                    s2 = ((File) object2).getName();
                } else if (compareType == COMPARETYPE_DATE) {
                    s1 = ((File) object1).lastModified() + "";
                    s2 = ((File) object2).lastModified() + "";
                }

                if(order)
                    return -(s1.compareTo(s2));
                else
                    return s1.compareTo(s2);
            }
        });

        return files;
    }

    private static final String FILE_SEPARATER = " > ";

    public String getSeparateFileString(String path) {
        ArrayList<String> root = getSdCardArrayList();
        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(File.separatorChar);
        splitter.setString(path);

        StringBuffer sb = new StringBuffer();

        int i = 0;
        for(String dir : splitter) {
            if(dir.length() > 0) {
                if(root.size() > i) {
                    if(!root.get(i).equals(dir)) {
                        sb.append(dir+FILE_SEPARATER);
                    }
                } else {
                    sb.append(dir+FILE_SEPARATER);
                }
                i++;
            }
        }
        return sb.substring(0, sb.length() - FILE_SEPARATER.length());
    }
}
