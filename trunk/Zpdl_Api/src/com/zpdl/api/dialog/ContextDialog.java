package com.zpdl.api.dialog;

import java.util.ArrayList;

import com.zpdl.api.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ContextDialog extends DialogFragment {
    private static final String ARG_FRAGMENT_TAG            = "contextdialog";
    private static final String ARG_CALLING_FRAGMENT_TAG    = "contextdialog_fragment_tag";

    private static final String ARG_SELECTED_ITEM           = "contextdialog_selected_item";

    protected ContextDialogListener mListener;

    public ContextDialog() {
        super();

        mListener = null;
    }

    public ContextDialog(String selectedItem, String callingfragmentTag) {
        super();

        Bundle args = new Bundle();

        if(selectedItem != null) {
            args.putString(ARG_SELECTED_ITEM, selectedItem);
        }
        if(callingfragmentTag != null) {
            args.putString(ARG_CALLING_FRAGMENT_TAG, callingfragmentTag);
        }
        setArguments(args);
    }

    public void show(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(ARG_FRAGMENT_TAG);

        if (prev != null) {
            ft.remove(prev);
        }

        show(ft, ARG_FRAGMENT_TAG);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            String callingFragmentTag = getArguments().getString(ARG_CALLING_FRAGMENT_TAG, null);
            if(callingFragmentTag == null) {
                mListener = (ContextDialogListener) activity;
            } else {
                Fragment f = activity.getFragmentManager().findFragmentByTag(callingFragmentTag);
                mListener = (ContextDialogListener) f;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ContextDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getActivity().getResources().getDisplayMetrics());

        ArrayList<ContextDialogMenu> menu = new ArrayList<ContextDialogMenu>();

        mListener.onCreateContextDialog(menu, getArguments().getString(ARG_SELECTED_ITEM, null));
        if(menu.isEmpty()) {
            dismiss();
            return null;
        }

        ContextDialogListAdapter adapter = new ContextDialogListAdapter(getActivity(), menu);

        ListView lv = new ListView(getActivity(), null, android.R.attr.absListViewStyle);
        lv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        lv.setBackgroundResource(R.drawable.contextdialog_background);
        lv.setAdapter(adapter);
        lv.setSelector(R.drawable.tdialog_btn_selector);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContextDialogMenu item = (ContextDialogMenu) parent.getItemAtPosition(position);
                mListener.onContextDialogItemSelected(item.id, getArguments().getString(ARG_SELECTED_ITEM, null));
                dismiss();
            }
        });

        TdialogContainer dialogContainer = new TdialogContainer(getActivity());
        dialogContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialogContainer.setPadding((int)(10 * dip), (int)(10 * dip), (int)(10 * dip), 0);

        dialogContainer.setView(lv);

        return dialogContainer;
    }

    private class ContextDialogListAdapter extends ArrayAdapter<ContextDialogMenu> {

        public ContextDialogListAdapter(Context context, ArrayList<ContextDialogMenu> objects) {
            super(context, 0, objects);
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if(view == null) {
                float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getContext().getResources().getDisplayMetrics());

                TextView tv = new TextView(getContext());
                tv.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setPadding((int)(10 * dip), (int)(10 * dip), (int)(10 * dip), (int)(10 * dip));
                tv.setTextAppearance(getContext(), R.style.TextAppearance_tdialog_medium);
                tv.setGravity(Gravity.START);
                tv.setBackgroundColor(Color.TRANSPARENT);
                view = tv;
            }

            final ContextDialogMenu contextDialogMenu = this.getItem(position);
            if(contextDialogMenu != null) {
                TextView tv = (TextView) view;
                tv.setText(contextDialogMenu.title);
            }
            return view;
        }
    }
}
