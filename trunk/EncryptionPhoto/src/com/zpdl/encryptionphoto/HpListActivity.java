package com.zpdl.encryptionphoto;

import java.io.File;
import java.util.ArrayList;

import com.zpdl.encryptionphoto.R;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zpdl.api.dialog.Adialog;
import com.zpdl.api.dialog.AdialogListener;
import com.zpdl.api.dialog.TdialogFolderBrowser;
import com.zpdl.api.dialog.TdialogProgress;
import com.zpdl.api.dialog.TdialogTask;
import com.zpdl.api.file.AFileListener;
import com.zpdl.api.file.AFileRunnable;
import com.zpdl.api.file.AFileUtil;
import com.zpdl.api.service.ARunnable;
import com.zpdl.api.service.ARunnableServiceManager;
import com.zpdl.api.util.Alog;
import com.zpdl.encryptionphoto.crypto.Crypto;
import com.zpdl.encryptionphoto.crypto.CryptoListener;
import com.zpdl.encryptionphoto.crypto.DecryptoARunnable;
import com.zpdl.encryptionphoto.crypto.EncryptoARunnable;
import com.zpdl.encryptionphoto.griddecrypto.GridDecryptoFragment;
import com.zpdl.encryptionphoto.gridthumbnail.GridTnFragment;

public class HpListActivity extends Activity implements HpDoInterface,
                                                        AdialogListener,
                                                        HpListNavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final int DIALOG_ID_WAITING           = 0x0001;
    private static final int DIALOG_ID_ENCRYPTO_PROGRESS = 0x0002;
    private static final int DIALOG_ID_DECRYPTO_PROGRESS = 0x0003;
    private static final int DIALOG_ID_FILE_COPY_BROWSER = 0x0004;
    private static final int DIALOG_ID_FILE_MOVE_BROWSER = 0x0005;
    private static final int DIALOG_ID_FILE_PROGRESS     = 0x0006;

    private HpListNavigationDrawerFragment mNavigationDrawerFragment;

    private ARunnableServiceManager mServiceManager;
    private ArrayList<Adialog>      mAdialog;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Alog.i("HpListActivity : onCreate");
        mServiceManager = new ARunnableServiceManager(this);
        mAdialog = new ArrayList<Adialog>();

        super.onCreate(savedInstanceState);

        Alog.i("Configuration = "+this.getResources().getConfiguration().toString());

        setContentView(R.layout.hp_list_activity);

        int createDirResult = AFileUtil.getInstance().createDirectory(HpConfigure.WORKING_DIRECTORY);
        if(createDirResult == 1) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(HpConfigure.WORKING_DIRECTORY)));
            sendBroadcast(intent);
        }

//        File workDir = new File(HpConfigure.WORKING_DIRECTORY);
//        if(!workDir.exists()) {
//            workDir.mkdir();
//        }

        mNavigationDrawerFragment = (HpListNavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onStart() {
        Alog.i("HpListActivity : onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Alog.i("HpListActivity : onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Alog.i("HpListActivity : onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        Alog.i("HpListActivity : onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Alog.i("HpListActivity : onDestroy");
        if(mServiceManager.isBound()) {
            mServiceManager.getRunningRunnable(new ARunnableServiceManager.onGetRunningRunnableListener() {
                @Override
                public void onGetRunningRunnable(ARunnable runnable) {
                    if(runnable == null) {
                        mServiceManager.stopService();
                    } else {
                        mServiceManager.unbindService();
                    }
                }
            });
        } else {
            mServiceManager.unbindService();
        }

        super.onDestroy();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, String directory) {
        Alog.i("onNavigationDrawerItemSelected position = %d", position);

        FragmentManager fragmentManager = getFragmentManager();

        if (position == 0) {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.container,
                            GridDecryptoFragment.newInstance(), GridDecryptoFragment.class.getName())
                    .commit();
        } else {
            mTitle = directory;
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.container,
                            GridTnFragment.newInstance(directory), GridTnFragment.class.getName())
                    .commit();

        }
    }

    private static final String TITLE_SEPARATER = " > ";

    public void onSectionAttached(String title) {
        ArrayList<String> root = AFileUtil.getInstance().getSdCardArrayList();
        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(File.separatorChar);
        splitter.setString(title);

        StringBuffer sb = new StringBuffer();

        int i = 0;
        for(String dir : splitter) {
            if(dir.length() > 0) {
                if(root.size() > i) {
                    if(!root.get(i).equals(dir)) {
                        sb.append(dir+TITLE_SEPARATER);
                    }
                } else {
                    sb.append(dir+TITLE_SEPARATER);
                }
                i++;
            }
        }
        if(sb.length() > TITLE_SEPARATER.length()) {
        	mTitle = sb.substring(0, sb.length() - TITLE_SEPARATER.length());
        }
    }

    @SuppressWarnings("deprecation")
    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//        if(v instanceof GridView) {
//            GridView gv = (GridView) v;
//            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
//            Cursor cursor = (Cursor) gv.getItemAtPosition(acmi.position);
//
//            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE));
//            menu.add(0, 0, 0, title);
//        }
//        super.onCreateContextMenu(menu, v, menuInfo);
//    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        return super.onContextItemSelected(item);
    }

    private static final int HANDLER_NAVIGATIONDRAWER_DATASETCHANGED = 0x0001;
    private static final int HANDLER_ENCRYPTO_PROGRESS  = 0x0002;
    private static final int HANDLER_ENCRYPTO_COMPLETE  = 0x0003;
    private static final int HANDLER_DECRYPTO_PROGRESS  = 0x0004;
    private static final int HANDLER_DECRYPTO_COMPLETE  = 0x0005;
    private static final int HANDLER_FILE_PROGRESS      = 0x0006;
    private static final int HANDLER_FILE_COMPLETE      = 0x0007;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case HANDLER_NAVIGATIONDRAWER_DATASETCHANGED : {
                String out = (String) msg.obj;
                if(mNavigationDrawerFragment.selectItem(out)) {
                    _dissmissDialog(DIALOG_ID_WAITING);
                } else {
                    if(msg.arg1 < 5) {
                        Message _msg = mHandler.obtainMessage();
                        _msg.what = HANDLER_NAVIGATIONDRAWER_DATASETCHANGED;
                        _msg.arg1 = 0;
                        _msg.obj = out;
                        mHandler.sendMessageDelayed(_msg, 500);
                    } else {
                        _dissmissDialog(DIALOG_ID_WAITING);
                    }
                }
            } break;

            case HANDLER_ENCRYPTO_PROGRESS : {
                TdialogProgress tdialogProgress = (TdialogProgress) _getDialog(DIALOG_ID_ENCRYPTO_PROGRESS);
                if(tdialogProgress != null) {
                    tdialogProgress.setCount(msg.arg1);
                    tdialogProgress.setProgress(msg.arg2);
                    if(msg.obj != null) {
                        tdialogProgress.setMessage(AFileUtil.getFileName((String) msg.obj));
                    }
                }
            } break;
            case HANDLER_ENCRYPTO_COMPLETE : {
                if(msg.arg1 == Crypto.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), R.string.Encryption_completed, Toast.LENGTH_SHORT).show();
                    _fragmentDatasetChanged();
                } else if(msg.arg1 == Crypto.RESULT_CANCEL) {
                    Toast.makeText(getApplicationContext(), R.string.Encryption_canceled, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.Encryption_error, Toast.LENGTH_SHORT).show();
                }
                _dissmissDialog(DIALOG_ID_ENCRYPTO_PROGRESS);
            } break;

            case HANDLER_DECRYPTO_PROGRESS : {
                TdialogProgress tdialogProgress = (TdialogProgress) _getDialog(DIALOG_ID_DECRYPTO_PROGRESS);
                if(tdialogProgress != null) {
                    tdialogProgress.setCount(msg.arg1);
                    tdialogProgress.setProgress(msg.arg2);
                    if(msg.obj != null) {
                        tdialogProgress.setMessage(AFileUtil.getFileName((String) msg.obj));
                    }
                }
            } break;
            case HANDLER_DECRYPTO_COMPLETE : {
                if(msg.arg1 == Crypto.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), R.string.Decryption_completed, Toast.LENGTH_SHORT).show();
                    String item = mNavigationDrawerFragment.getSelectItem();
                    mNavigationDrawerFragment.refresh();
                    mNavigationDrawerFragment.selectItem(item);
                } else if(msg.arg1 == Crypto.RESULT_CANCEL) {
                    Toast.makeText(getApplicationContext(), R.string.Decryption_canceled, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.Decryption_error, Toast.LENGTH_SHORT).show();
                }
                _dissmissDialog(DIALOG_ID_DECRYPTO_PROGRESS);
            } break;

            case HANDLER_FILE_PROGRESS : {
                TdialogProgress tdialogProgress = (TdialogProgress) _getDialog(DIALOG_ID_FILE_PROGRESS);
                if(tdialogProgress != null) {
                    tdialogProgress.setCount(msg.arg1);
                    tdialogProgress.setProgress(msg.arg2);
                    if(msg.obj != null) {
                        tdialogProgress.setMessage(AFileUtil.getFileName((String) msg.obj));
                    }
                }
            } break;
            case HANDLER_FILE_COMPLETE : {
                Alog.i("onFileComplete msg.arg1 = %x msg.arg2 = %x",msg.arg1, msg.arg2);
                if(msg.arg1 == AFileRunnable.COPY) {
                    if(msg.arg2 == AFileRunnable.RESULT_OK) {
                        String out = (String) msg.obj;
                        if(!mNavigationDrawerFragment.selectItem(out)) {
                            TdialogTask tdialogTask = new TdialogTask(DIALOG_ID_WAITING);
                            tdialogTask.show(getFragmentManager());

                            Message _msg = mHandler.obtainMessage();
                            _msg.what = HANDLER_NAVIGATIONDRAWER_DATASETCHANGED;
                            _msg.arg1 = 0;
                            _msg.obj = out;
                            mHandler.sendMessageDelayed(_msg, 500);
                        }
                        Toast.makeText(getApplicationContext(), R.string.Copy_completed, Toast.LENGTH_SHORT).show();
                    } else if(msg.arg2 == AFileRunnable.RESULT_CANCEL) {
                        Toast.makeText(getApplicationContext(), R.string.Copy_canceled, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.Copy_error, Toast.LENGTH_SHORT).show();
                    }
                } else if(msg.arg1 == AFileRunnable.MOVE) {
                    if(msg.arg2 == AFileRunnable.RESULT_OK) {
                        Toast.makeText(getApplicationContext(), R.string.Move_completed, Toast.LENGTH_SHORT).show();
                    } else if(msg.arg2 == AFileRunnable.RESULT_CANCEL) {
                        Toast.makeText(getApplicationContext(), R.string.Move_canceled, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.Move_error, Toast.LENGTH_SHORT).show();
                    }
                } else if(msg.arg1 == AFileRunnable.DELETE) {
                    Alog.i("onFileComplete DELETE msg.arg2 = %x",msg.arg2);
                    if(msg.arg2 == AFileRunnable.RESULT_OK) {
                        Toast.makeText(getApplicationContext(), R.string.Delete_completed, Toast.LENGTH_SHORT).show();
                        _fragmentDatasetChanged();
                    } else if(msg.arg2 == AFileRunnable.RESULT_CANCEL) {
                        Toast.makeText(getApplicationContext(), R.string.Delete_canceled, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.Delete_error, Toast.LENGTH_SHORT).show();
                    }
                }

                _dissmissDialog(DIALOG_ID_FILE_PROGRESS);
            } break;
            }
        }
    };

    @Override
    public void onDialogAttach(Adialog dialog) {
        Alog.d("onDialogAttach dialog.getDialogId() = %d",dialog.getDialogId());
        _addDialog(dialog);

        switch(dialog.getDialogId()) {
        case DIALOG_ID_WAITING : {

        } break;
        case DIALOG_ID_ENCRYPTO_PROGRESS : {
            _EncryptoProgressDialogAttach();
        } break;
        case DIALOG_ID_DECRYPTO_PROGRESS : {
            _DecryptoProgressDialogAttach();
        } break;
        case DIALOG_ID_FILE_PROGRESS : {
            _FileProgressDialogAttach();
        } break;
        case DIALOG_ID_FILE_COPY_BROWSER :
        case DIALOG_ID_FILE_MOVE_BROWSER : {

        } break;
        default :
            Alog.w("onDialogAttach not found id %d", dialog.getDialogId());
            dialog.dismiss();
        }
    }

    @Override
    public void onDialogDetach(Adialog dialog) {
        Alog.d("onDialogDetach dialog.getDialogId() = %d",dialog.getDialogId());
        switch(dialog.getDialogId()) {
        case DIALOG_ID_ENCRYPTO_PROGRESS : {
            _EncryptoProgressDialogDetach();
        } break;
        case DIALOG_ID_DECRYPTO_PROGRESS : {
            _DecryptoProgressDialogDetach();
        } break;
        case DIALOG_ID_FILE_PROGRESS : {
            _FileProgressDialogDetach();
        }
        }
        _removeDialog(dialog);
    }

    @Override
    public void onDialogEvent(Adialog dialog, int which) {
        Alog.i("onDialogEvent dialog.getDialogId() = %d which = %d",dialog.getDialogId(), which);
        switch(dialog.getDialogId()) {
        case DIALOG_ID_ENCRYPTO_PROGRESS : {
            if(which == Adialog.EVENT_NEGATIVE || which == Adialog.EVENT_CANCEL) {
                mServiceManager.getRunningRunnable(new ARunnableServiceManager.onGetRunningRunnableListener() {
                    @Override
                    public void onGetRunningRunnable(ARunnable runnable) {
                        if(runnable != null && runnable instanceof EncryptoARunnable) {
                            ((EncryptoARunnable) runnable).cancel();
                        }
                    }
                });
            }
        } break;
        case DIALOG_ID_DECRYPTO_PROGRESS : {
            if(which == Adialog.EVENT_NEGATIVE || which == Adialog.EVENT_CANCEL) {
                mServiceManager.getRunningRunnable(new ARunnableServiceManager.onGetRunningRunnableListener() {
                    @Override
                    public void onGetRunningRunnable(ARunnable runnable) {
                        if(runnable != null && runnable instanceof DecryptoARunnable) {
                            ((DecryptoARunnable) runnable).cancel();
                        }
                    }
                });
            }
        } break;
        case DIALOG_ID_FILE_PROGRESS : {
            if(which == Adialog.EVENT_NEGATIVE || which == Adialog.EVENT_CANCEL) {
                mServiceManager.getRunningRunnable(new ARunnableServiceManager.onGetRunningRunnableListener() {
                    @Override
                    public void onGetRunningRunnable(ARunnable runnable) {
                        if(runnable != null && runnable instanceof AFileRunnable) {
                            ((AFileRunnable) runnable).cancel();
                        }
                    }
                });
            }
        } break;
        case DIALOG_ID_FILE_COPY_BROWSER : {
            TdialogFolderBrowser tDialogFolderBrowser = (TdialogFolderBrowser) dialog;

            String in[] = tDialogFolderBrowser.getParam();
            String out = tDialogFolderBrowser.getPath();
            tDialogFolderBrowser.dismiss();

            TdialogProgress progressDialog = new TdialogProgress(DIALOG_ID_FILE_PROGRESS);
            progressDialog.setTitle(getString(R.string.Delete));
            progressDialog.setMessage(AFileUtil.getFileName(in[0]));
            progressDialog.setNegativeButton(getString(R.string.Cancel));
            if(in.length > 1) {
                progressDialog.setMaxCount(in.length);
            }
            progressDialog.show(getFragmentManager());

            mServiceManager.exectue(new AFileRunnable(AFileRunnable.COPY, in, out));
        }
        }
    }

    private Adialog _getDialog(int id) {
        for(Adialog dialog : mAdialog) {
            if(dialog.getDialogId() == id) {
                return dialog;
            }
        }
        return null;
    }

    private boolean _addDialog(Adialog dialog) {
        return mAdialog.add(dialog);
    };

    private boolean _removeDialog(Adialog dialog) {
        return mAdialog.remove(dialog);
    };

    private void _dissmissDialog(int id) {
        Adialog dialog = _getDialog(id);
        if(dialog != null) {
            dialog.dismiss();
        }
    };
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
//            ((HpListActivity) activity).onSectionAttached(""getArguments().getInt(
//                    ARG_SECTION_NUMBER));
        }
    }

    private class CryptoResultParam {
        String[] in;
        String[] out;

        CryptoResultParam(String[] in, String[] out) {
            this.in = in;
            this.out = out;
        }
    }

    @Override
    public void DoEncrypto(String[] in) {
        TdialogProgress progressDialog = new TdialogProgress(DIALOG_ID_ENCRYPTO_PROGRESS);
        progressDialog.setTitle(getString(R.string.Encryption));
        progressDialog.setMessage(new File(in[0]).getName());
        progressDialog.setNegativeButton(getString(R.string.Cancel));
        if(in.length > 1) {
            progressDialog.setMaxCount(in.length);
        }
        progressDialog.show(getFragmentManager());

        mServiceManager.exectue(Crypto.getEncryptoRunnable(in));
    }

    private void _EncryptoProgressDialogAttach() {
        TdialogProgress tdialogProgress = (TdialogProgress) _getDialog(DIALOG_ID_ENCRYPTO_PROGRESS);
        if(tdialogProgress != null) {
            mServiceManager.getRunningRunnable(new ARunnableServiceManager.onGetRunningRunnableListener() {
                @Override
                public void onGetRunningRunnable(ARunnable runnable) {
                    if(runnable != null && runnable instanceof EncryptoARunnable) {
                        ((EncryptoARunnable) runnable).setCryptoListener(mEncryptoListener);
                    }
                    else {
                        _getDialog(DIALOG_ID_ENCRYPTO_PROGRESS).dismiss();
                    }
                }
            });
        } else {
            _getDialog(DIALOG_ID_ENCRYPTO_PROGRESS).dismiss();
        }
    }

    private void _EncryptoProgressDialogDetach() {
        TdialogProgress tdialogProgress = (TdialogProgress) _getDialog(DIALOG_ID_ENCRYPTO_PROGRESS);
        if(tdialogProgress != null) {
            mServiceManager.getRunningRunnable(new ARunnableServiceManager.onGetRunningRunnableListener() {
                @Override
                public void onGetRunningRunnable(ARunnable runnable) {
                    if(runnable != null && runnable instanceof EncryptoARunnable) {
                        ((EncryptoARunnable) runnable).setCryptoListener(null);
                    }
                }
            });
        }
    }

    private CryptoListener mEncryptoListener = new CryptoListener() {
        @Override
        public void onCryptoProgress(String message, int count, int percent) {
            mHandler.removeMessages(HANDLER_ENCRYPTO_PROGRESS);

            Message msg = mHandler.obtainMessage();
            msg.what = HANDLER_ENCRYPTO_PROGRESS;
            msg.arg1 = count;
            msg.arg2 = percent;
            msg.obj = message;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onCryptoComplete(int result, String[] in, String[] out) {
            Message msg = mHandler.obtainMessage();
            msg.what = HANDLER_ENCRYPTO_COMPLETE;
            msg.arg1 = result;
            msg.obj = new CryptoResultParam(in, out);
            mHandler.sendMessage(msg);
        }
    };

    @Override
    public void DoDecrypto(String[] in) {
        TdialogProgress progressDialog = new TdialogProgress(DIALOG_ID_DECRYPTO_PROGRESS);
        progressDialog.setTitle(getString(R.string.Decryption));
        progressDialog.setMessage(new File(in[0]).getName());
        progressDialog.setNegativeButton(getString(R.string.Cancel));
        if(in.length > 1) {
            progressDialog.setMaxCount(in.length);
        }
        progressDialog.show(getFragmentManager());
        if(mServiceManager == null)
            mServiceManager = new ARunnableServiceManager(this);
        mServiceManager.exectue(Crypto.getDecryptoARunnable(in));
    }

    private void _DecryptoProgressDialogAttach() {
        TdialogProgress tdialogProgress = (TdialogProgress) _getDialog(DIALOG_ID_DECRYPTO_PROGRESS);
        if(tdialogProgress != null) {
            mServiceManager.getRunningRunnable(new ARunnableServiceManager.onGetRunningRunnableListener() {
                @Override
                public void onGetRunningRunnable(ARunnable runnable) {
                    if(runnable != null && runnable instanceof DecryptoARunnable)
                        ((DecryptoARunnable) runnable).setCryptoListener(mDecryptoListener);
                    else
                        _getDialog(DIALOG_ID_DECRYPTO_PROGRESS).dismiss();
                }
            });
        } else {
            _getDialog(DIALOG_ID_DECRYPTO_PROGRESS).dismiss();
        }
    }

    private void _DecryptoProgressDialogDetach() {
        TdialogProgress tdialogProgress = (TdialogProgress) _getDialog(DIALOG_ID_DECRYPTO_PROGRESS);
        if(tdialogProgress != null) {
            mServiceManager.getRunningRunnable(new ARunnableServiceManager.onGetRunningRunnableListener() {
                @Override
                public void onGetRunningRunnable(ARunnable runnable) {
                    if(runnable != null && runnable instanceof DecryptoARunnable) {
                        ((DecryptoARunnable) runnable).setCryptoListener(null);
                    }
                }
            });
        }
    }

    private CryptoListener mDecryptoListener = new CryptoListener() {
        @Override
        public void onCryptoProgress(String message, int count, int percent) {
            mHandler.removeMessages(HANDLER_DECRYPTO_PROGRESS);

            Message msg = mHandler.obtainMessage();
            msg.what = HANDLER_DECRYPTO_PROGRESS;
            msg.arg1 = count;
            msg.arg2 = percent;
            msg.obj = message;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onCryptoComplete(int result, String[] in, String[] out) {
            Message msg = mHandler.obtainMessage();
            msg.what = HANDLER_DECRYPTO_COMPLETE;
            msg.arg1 = result;
            msg.obj = new CryptoResultParam(in, out);
            mHandler.sendMessage(msg);
        }
    };

    @Override
    public void DoCopy(String[] in) {
        TdialogFolderBrowser dialog = new TdialogFolderBrowser(DIALOG_ID_FILE_COPY_BROWSER);
        dialog.setPath(new File(in[0]).getParent());
        dialog.setParam(in);
        dialog.setPositiveButton(getString(R.string.Here));
        dialog.show(getFragmentManager());
    }

    @Override
    public void DoMove(String[] in) {

    }

    @Override
    public void DoDelete(String[] in) {
        TdialogProgress progressDialog = new TdialogProgress(DIALOG_ID_FILE_PROGRESS);
        progressDialog.setTitle(getString(R.string.Delete));
        progressDialog.setMessage(AFileUtil.getFileName(in[0]));
        progressDialog.setNegativeButton(getString(R.string.Cancel));
        if(in.length > 1) {
            progressDialog.setMaxCount(in.length);
        }
        progressDialog.show(getFragmentManager());

        mServiceManager.exectue(new AFileRunnable(AFileRunnable.DELETE, in, null));
    }

    private void _FileProgressDialogAttach() {
        TdialogProgress tdialogProgress = (TdialogProgress) _getDialog(DIALOG_ID_FILE_PROGRESS);
        if(tdialogProgress != null) {
            mServiceManager.getRunningRunnable(new ARunnableServiceManager.onGetRunningRunnableListener() {
                @Override
                public void onGetRunningRunnable(ARunnable runnable) {
                    if(runnable != null && runnable instanceof AFileRunnable)
                        ((AFileRunnable) runnable).setFileListener(mFileListener);
                    else
                        _getDialog(DIALOG_ID_FILE_PROGRESS).dismiss();
                }
            });
        } else {
            _getDialog(DIALOG_ID_FILE_PROGRESS).dismiss();
        }
    }

    private void _FileProgressDialogDetach() {
        TdialogProgress tdialogProgress = (TdialogProgress) _getDialog(DIALOG_ID_FILE_PROGRESS);
        if(tdialogProgress != null) {
            mServiceManager.getRunningRunnable(new ARunnableServiceManager.onGetRunningRunnableListener() {
                @Override
                public void onGetRunningRunnable(ARunnable runnable) {
                    if(runnable != null && runnable instanceof AFileRunnable) {
                        ((AFileRunnable) runnable).setFileListener(null);
                    }
                }
            });
        }
    }

    private AFileListener mFileListener = new AFileListener() {
        @Override
        public void onFileProgress(String message, int count, int percent) {
            Alog.d("onFileProgress percent = %d", percent);
            mHandler.removeMessages(HANDLER_FILE_PROGRESS);

            Message msg = mHandler.obtainMessage();
            msg.what = HANDLER_FILE_PROGRESS;
            msg.arg1 = count;
            msg.arg2 = percent;
            msg.obj = message;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onFileComplete(int result, String out) {
            Alog.d("onFileComplete result = %x", result);
            int act = 0xffff0000 & result;
            int ret = 0x0000ffff & result;

            Message msg = mHandler.obtainMessage();
            msg.what = HANDLER_FILE_COMPLETE;
            msg.arg1 = act;
            msg.arg2 = ret;
            msg.obj = out;
            mHandler.sendMessage(msg);
        }
    };

    private void _fragmentDatasetChanged() {
        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.container);
        if(f instanceof GridTnFragment) {
            ((GridTnFragment) f).dataSetChanged();
        } else if(f instanceof GridDecryptoFragment) {
            ((GridDecryptoFragment) f).dataSetChanged();
        }
    }
}
