package com.zpdl.api.file;

public interface AFileListener {
    public void onFileProgress(String message, int count, int percent);
    public void onFileComplete(int result, String out);
}