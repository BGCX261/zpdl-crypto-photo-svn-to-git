package com.zpdl.api.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import com.zpdl.api.service.ARunnable;

public class AFileRunnable extends ARunnable {
    public static final int RESULT_OK       = 0x00000001;
    public static final int RESULT_CANCEL   = 0x00000002;
    public static final int RESULT_ERR      = 0x00000003;

    public static final int COPY            = 0x00010000;
    public static final int MOVE            = 0x00020000;
    public static final int DELETE          = 0x00030000;

    private AFileScanner mAFileScanner;

    private boolean running;

    private int      mAction;
    private String[] mIn;

    private String   mOutDir;

    private AFileListener mListener;

    private int mProgressCnt;
    private int mProgressPer;

    private long mProgressTotalSize;
    private long mProgressSize;

    public AFileRunnable(int action, String[] in, String outDir) {
        super();

        mAFileScanner = null;

        running = true;
        mListener = null;

        mAction = action;
        mIn = in;

        mOutDir = outDir;

        mProgressCnt = 0;
        mProgressPer = 0;
        mProgressTotalSize = 0;
        mProgressSize = 0;
    }

    public synchronized void setFileListener(AFileListener l) {
        mListener = l;
    }

    public void cancel() {
        running = false;
    }

    public int getProgressCount() {
        return mProgressCnt;
    }

    public int getProgressPercent() {
        return mProgressPer;
    }

    public String getProgressFileString() {
        return mIn[mProgressCnt];
    }

    @Override
    protected void aRun() {
        int result = RESULT_OK;

        String out = null;
        try {
            mAFileScanner = new AFileScanner(mContext);

            switch(mAction) {
            case COPY :
            case MOVE : {
                if(!AFileUtil.getInstance().isSdCard(mOutDir)) {
                    throw new AFileExceptionError("Copy is error because taget directory is External SD cards.");
                }
                if(AFileUtil.getInstance().createDirectory(new File(mOutDir).getParent()) < 0) {
                    throw new AFileExceptionError("is not create out directory , "+new File(out).getParent());
                }
            } break;
            }

            for(int i = 0; i < mIn.length; i++) {
                _progressCount(i);
                String in = mIn[i];

                switch(mAction) {
                case COPY : {
                    out = String.format("%s%s%s", mOutDir, File.separator, new File(in).getName());
                    out = AFileUtil.getInstance().distinctFileString(out);

                    mProgressSize = 0;
                    mProgressTotalSize = _getFileSizeAndCount(out).size;
                    _copy(new File(in), new File(out), false);
                    mAFileScanner.insertFile(out);
                } break;
                case MOVE : {
                    out = String.format("%s%s%s", mOutDir, File.separator, new File(in).getName());
                    out = AFileUtil.getInstance().distinctFileString(out);

                    File inFile = new File(in);
                    File outFile = new File(out);
                    if(inFile.renameTo(outFile)) {
                        _progressPercent(100);
                    } else {
                        mProgressSize = 0;
                        mProgressTotalSize = _getFileSizeAndCount(out).size;
                        _copy(new File(in), new File(out), true);
                    }
                    mAFileScanner.moveFile(in, out);
                } break;
                case DELETE : {
                    mProgressSize = 0;
                    mProgressTotalSize = _getFileSizeAndCount(in).size;
                    _delete(new File(in));
                    mAFileScanner.deleteFile(in);
                } break;
                }
            }
        } catch (AFileExceptionError e) {
            e.printStackTrace();
            result = RESULT_ERR;
        } catch (AFileExceptionCancel e) {
            e.printStackTrace();
            result = RESULT_ERR;
        } catch (IOException e) {
            e.printStackTrace();
            result = RESULT_ERR;
        } finally {
            if(mAFileScanner != null) {
                mAFileScanner.release();
                mAFileScanner = null;
            }
            _complete(result);
        }
    }

    private void _copy(File in, File out, boolean deleteIn) throws IOException, AFileExceptionCancel {
        if(in.exists() && in.isDirectory()) {
            out.mkdir();
            for(File cFile : in.listFiles()) {
                _copy(cFile, new File(out, cFile.getName()), deleteIn);
                _stopCheck();
            }
            if(deleteIn) in.delete();
        } else if(in.exists() && in.isFile()) {
            FileInputStream inputStream = null;
            FileOutputStream outputStream = null;
            BufferedInputStream bin = null;
            BufferedOutputStream bout = null;

            try {
                inputStream = new FileInputStream(in);
                outputStream = new FileOutputStream(out);
                bin = new BufferedInputStream(inputStream);
                bout = new BufferedOutputStream(outputStream);

                int bytesRead = 0;
                byte[] buffer = new byte[8096];
                while ((bytesRead = bin.read(buffer, 0, 1024)) != -1) {
                    bout.write(buffer, 0, bytesRead);
                    mProgressSize += bytesRead;
                    int newprogress = (int)((double) mProgressSize * 100 / (double) mProgressTotalSize);
                    if(newprogress > mProgressPer && mProgressPer <= 100) {
                        _progressPercent(newprogress);
                    }
                    _stopCheck();
                }
            } finally {
                bout.close();
                bin.close();
                outputStream.close();
                inputStream.close();

                if(deleteIn) in.delete();
            }
        }
    }

    private void _delete(File f) throws IOException, AFileExceptionCancel {
        if(f.exists() && f.isDirectory()) {
            for(File dFile : f.listFiles()) {
                _delete(dFile);
            }

            if(!f.delete()) {
                throw new IOException("Failed delete folder : " + f.getPath());
            }
        } else if(f.exists() && f.isFile()) {
            mProgressSize += f.length();
            if(f.delete()) {
                int newprogress = (int)((double) mProgressSize * 100 / (double) mProgressTotalSize);
                if(newprogress > mProgressPer && mProgressPer <= 100) {
                    _progressPercent(newprogress);
                }
            } else {
                throw new IOException("Failed delete file : " + f.getPath());
            }
            _stopCheck();
        }
    }

    private CountAndSize _getFileSizeAndCount(String path) {
        CountAndSize cs = new CountAndSize();
        LinkedList<String> ll = new LinkedList<String>();
        ll.offer(path);

        while(!ll.isEmpty()) {
            File file = new File(ll.poll());

            if(file.exists() && file.isDirectory()) {
                for(File addFile : file.listFiles()) {
                    ll.offer(addFile.getPath());
                }
            } else if(file.exists() && file.isFile()) {
                cs.plusSize(file.length());
                cs.plusCount(1);
            }
        }
        return cs;
    }

    private void _stopCheck() throws AFileExceptionCancel {
        if(!running) {
            throw new AFileExceptionCancel("EncryptoRunnable : Stop");
        }
    }

    private synchronized void _progressCount(int cnt) {
        mProgressCnt = cnt;
        mProgressPer = 0;
        if(mListener != null) mListener.onFileProgress(mIn[mProgressCnt], mProgressCnt, mProgressPer);
    }

    private synchronized void _progressPercent(int per) {
        mProgressPer = per;
        if(mListener != null) mListener.onFileProgress(null, mProgressCnt, mProgressPer);
    }

    private synchronized void _complete(int result) {
        if(mListener != null) mListener.onFileComplete(mAction | result, mOutDir);
    }

    private class CountAndSize {
        int count;
        long size;

        public CountAndSize() {
            count = 0;
            size = 0;
        }

        public void plusCount(int c) {
            count += c;
        }

        public void plusSize(long s) {
            size += s;
        }

        @SuppressWarnings("unused")
        public void plus(CountAndSize cs) {
            this.count += cs.count;
            this.size += cs.size;
        }
    }
}