package com.zpdl.encryptionphoto.griddecrypto;

import java.io.File;
import java.util.ArrayList;

import com.zpdl.encryptionphoto.R;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.zpdl.api.dialog.ContextDialog;
import com.zpdl.api.dialog.ContextDialogListener;
import com.zpdl.api.dialog.ContextDialogMenu;
import com.zpdl.api.file.AFileUtil;
import com.zpdl.api.util.Alog;
import com.zpdl.encryptionphoto.HpDoInterface;
import com.zpdl.encryptionphoto.HpListActivity;
import com.zpdl.encryptionphoto.crypto.Crypto;

public class GridDecryptoFragment extends Fragment implements ContextDialogListener {
    private GridView mGrid;

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static GridDecryptoFragment newInstance() {
        GridDecryptoFragment fragment = new GridDecryptoFragment();

        return fragment;
    }

    public GridDecryptoFragment() {
        mGrid = null;
    }

    public void dataSetChanged() {
        GridDecryptoAdapter gridDecryptoAdapter = new GridDecryptoAdapter(getActivity(), _createFileList());
        mGrid.setAdapter(gridDecryptoAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Alog.i("GridDecryptoFragment : onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Alog.i("GridDecryptoFragment : onCreateView");

        View rootView = inflater.inflate(R.layout.gridtn_fragment, container, false);

        TextView tv = (TextView) rootView.findViewById(R.id.gridtn_tv_path);
        tv.setText(getString(R.string.title_Encrypted_Photo));

        mGrid = (GridView) rootView.findViewById(R.id.gridtn_grid);
        mGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Alog.i("GridView : setOnItemClickListener - position = %d id = %d", position, id);
            }
        });
        mGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ContextDialog dialog = new ContextDialog((String) parent.getItemAtPosition(position), getTag());
                dialog.show(getFragmentManager());
                return true;
            }
        });

        GridDecryptoAdapter gridDecryptoAdapter = new GridDecryptoAdapter(getActivity(), _createFileList());
        mGrid.setAdapter(gridDecryptoAdapter);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        unregisterForContextMenu(mGrid);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Alog.i("GridTnFragment : onAttach");

        ((HpListActivity) activity).onSectionAttached(getString(R.string.title_Encrypted_Photo));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private ArrayList<String> _createFileList() {
        ArrayList<String> filelist = new  ArrayList<String>();

        File[] files = new File(Crypto.WORKING_DIRECTORY).listFiles();
        if(files != null) {
        	files = AFileUtil.getInstance().sortFileList(files, AFileUtil.COMPARETYPE_DATE, true);

            for(File file: files) {
                if(file.exists() && file.isFile()) {
                    filelist.add(file.getAbsolutePath());
                }
            }
        }
        return filelist;
    }

    private static final int CONTEXTMENU_ID_VIEW             = 0x0001;
    private static final int CONTEXTMENU_ID_DECRYPTION       = 0x0002;
    private static final int CONTEXTMENU_ID_DELETE           = 0x0003;
    private static final int CONTEXTMENU_ID_DETAIL           = 0x0004;

    @Override
    public void onCreateContextDialog(ArrayList<ContextDialogMenu> menu, String selectedItem) {
        menu.add(new ContextDialogMenu(CONTEXTMENU_ID_VIEW,         CONTEXTMENU_ID_VIEW,        getString(R.string.View)));
        menu.add(new ContextDialogMenu(CONTEXTMENU_ID_DECRYPTION,   CONTEXTMENU_ID_DECRYPTION,  getString(R.string.Decryption)));
        menu.add(new ContextDialogMenu(CONTEXTMENU_ID_DELETE,       CONTEXTMENU_ID_DELETE,      getString(R.string.Delete)));
        menu.add(new ContextDialogMenu(CONTEXTMENU_ID_DETAIL,       CONTEXTMENU_ID_DETAIL,      getString(R.string.Detail)));
    }

    @Override
    public void onContextDialogItemSelected(int id, String selectedItem) {
        switch(id) {
        case CONTEXTMENU_ID_VIEW : {
            Alog.i("GridDecryptoFragment : CONTEXTMENU_ID_VIEW");
        } break;
        case CONTEXTMENU_ID_DECRYPTION : {
            Alog.i("GridDecryptoFragment : CONTEXTMENU_ID_DECRYPTION Tag = "+getTag());
            ((HpDoInterface) getActivity()).DoDecrypto(new String[] {selectedItem});
        } break;
        case CONTEXTMENU_ID_DELETE : {
            Alog.i("GridDecryptoFragment : CONTEXTMENU_ID_DELETE");
            ((HpDoInterface) getActivity()).DoDelete(new String[] {selectedItem});
        } break;
        case CONTEXTMENU_ID_DETAIL : {
            Alog.i("GridDecryptoFragment : CONTEXTMENU_ID_DETAIL");
        } break;
        }
    }
}
