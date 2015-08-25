package com.zpdl.encryptionphoto.gridthumbnail;

import java.util.ArrayList;

import com.zpdl.encryptionphoto.R;
import android.app.Activity;
import android.app.Fragment;
import android.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
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

public class GridTnFragment extends Fragment implements ContextDialogListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_DIRECTORY = "directory";

    private GridView mGrid;
    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static GridTnFragment newInstance(String directory) {
        GridTnFragment fragment = new GridTnFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DIRECTORY, directory);
        fragment.setArguments(args);
        return fragment;
    }

    public GridTnFragment() {
        mGrid = null;
    }

    public void dataSetChanged() {
        ((GridTnAdapter) mGrid.getAdapter()).changeCursor(_loadCursor());
        ((GridTnAdapter) mGrid.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Alog.i("GridTnFragment : onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Alog.i("GridTnFragment : onCreateView directory = %s", getArguments().getString(ARG_DIRECTORY));

        View rootView = inflater.inflate(R.layout.gridtn_fragment, container, false);
        String directory = getArguments().getString(ARG_DIRECTORY);

        TextView tv = (TextView) rootView.findViewById(R.id.gridtn_tv_path);
        tv.setText(directory);

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
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                ContextDialog dialog = new ContextDialog(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)), getTag());
                dialog.show(getFragmentManager());
                return true;
            }
        });

        GridTnAdapter gridTnAdapter = new GridTnAdapter(getActivity(), _loadCursor());
        mGrid.setAdapter(gridTnAdapter);

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

        ((HpListActivity) activity).onSectionAttached(getArguments().getString(ARG_DIRECTORY));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private static final int CONTEXTMENU_ID_VIEW             = 0x0001;
    private static final int CONTEXTMENU_ID_ENCRYPTION       = 0x0002;
    private static final int CONTEXTMENU_ID_COPY             = 0x0003;
    private static final int CONTEXTMENU_ID_MOVE             = 0x0004;
    private static final int CONTEXTMENU_ID_DELETE           = 0x0005;
    private static final int CONTEXTMENU_ID_DETAIL           = 0x0006;

    @Override
    public void onCreateContextDialog(ArrayList<ContextDialogMenu> menu, String selectedItem) {
        if(AFileUtil.getInstance().isSdCard(selectedItem)) {
            menu.add(new ContextDialogMenu(CONTEXTMENU_ID_VIEW,       CONTEXTMENU_ID_VIEW,       getString(R.string.View)));
            menu.add(new ContextDialogMenu(CONTEXTMENU_ID_ENCRYPTION, CONTEXTMENU_ID_ENCRYPTION, getString(R.string.Encryption)));
            menu.add(new ContextDialogMenu(CONTEXTMENU_ID_COPY,       CONTEXTMENU_ID_COPY,       getString(R.string.Copy)));
            menu.add(new ContextDialogMenu(CONTEXTMENU_ID_MOVE,       CONTEXTMENU_ID_MOVE,       getString(R.string.Move)));
            menu.add(new ContextDialogMenu(CONTEXTMENU_ID_DELETE,     CONTEXTMENU_ID_DELETE,     getString(R.string.Delete)));
            menu.add(new ContextDialogMenu(CONTEXTMENU_ID_DETAIL,     CONTEXTMENU_ID_DETAIL,     getString(R.string.Detail)));
        } else {
            menu.add(new ContextDialogMenu(CONTEXTMENU_ID_VIEW,       CONTEXTMENU_ID_VIEW,       getString(R.string.View)));
            menu.add(new ContextDialogMenu(CONTEXTMENU_ID_ENCRYPTION, CONTEXTMENU_ID_ENCRYPTION, getString(R.string.Encryption)));
            menu.add(new ContextDialogMenu(CONTEXTMENU_ID_COPY,       CONTEXTMENU_ID_COPY,       getString(R.string.Copy)));
            menu.add(new ContextDialogMenu(CONTEXTMENU_ID_DETAIL,     CONTEXTMENU_ID_DETAIL,     getString(R.string.Detail)));
        }
    }

    @Override
    public void onContextDialogItemSelected(int id, String selectedItem) {
        switch(id) {
        case CONTEXTMENU_ID_VIEW : {
            Alog.i("GridTnFragment : CONTEXTMENU_ID_VIEW");
        } break;
        case CONTEXTMENU_ID_ENCRYPTION : {
            Alog.i("GridTnFragment : CONTEXTMENU_ID_ENCRYPTION");
            ((HpDoInterface) getActivity()).DoEncrypto(new String[] {selectedItem});
        } break;
        case CONTEXTMENU_ID_COPY : {
            Alog.i("GridTnFragment : CONTEXTMENU_ID_COPY");
            ((HpDoInterface) getActivity()).DoCopy(new String[] {selectedItem});
        } break;
        case CONTEXTMENU_ID_MOVE : {
            Alog.i("GridTnFragment : CONTEXTMENU_ID_MOVE");
            ((HpDoInterface) getActivity()).DoMove(new String[] {selectedItem});
        } break;
        case CONTEXTMENU_ID_DELETE : {
            Alog.i("GridTnFragment : CONTEXTMENU_ID_DELETE");
            ((HpDoInterface) getActivity()).DoDelete(new String[] {selectedItem});
        } break;
        case CONTEXTMENU_ID_DETAIL : {
            Alog.i("GridTnFragment : CONTEXTMENU_ID_DETAIL");
        } break;
        }
    }

    private Cursor _loadCursor() {
        String[] imageColumns = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.ORIENTATION};
        String where = MediaStore.Images.Media.DATA + " like ?";
        String whereArgs[] = {getArguments().getString(ARG_DIRECTORY)+"%"};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                           imageColumns,
                                                           where,
                                                           whereArgs,
                                                           MediaStore.Images.Media.DATE_MODIFIED + " desc");
        Cursor cursor = cursorLoader.loadInBackground();

        return cursor;
    }
}
