package com.zpdl.api.dialog;

import java.util.ArrayList;

import com.zpdl.api.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

class TdialogContainer extends ViewGroup {
    private Context mContext;

    private View mTitle;
    private View mView;
    private View mButton;

    public  onDialogButtonClickListener mDialogButtonClickListener;

    public interface onDialogButtonClickListener {
        public void onDialogButtonClick(int which);
    }

    public TdialogContainer(Context context) {
        this(context, null);
    }

    public TdialogContainer(Context context, AttributeSet attrs) {
        this(context, attrs , 0);
    }

    public TdialogContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mContext = context;

        mView = null;
        mButton = null;
        mDialogButtonClickListener = null;

//        setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
//            @Override
//            public void onChildViewRemoved(View parent, View child) {
//            }
//
//            @Override
//            public void onChildViewAdded(View parent, View child) {
//            }
//        });
    }

    @SuppressLint("InflateParams")
    public void setTitle(String title) {
        if(title == null) {
            if(mTitle != null)
                removeView(mTitle);
            mTitle = null;
        } else {
            if(mTitle != null) {
                removeView(mTitle);
            }
            mTitle = LayoutInflater.from(mContext).inflate(R.layout.tdialog_title, null);
            mTitle.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ((TextView) mTitle.findViewById(R.id.tdialog_title_tv)).setText(title);

            addView(mTitle);
        }
    }

    public void setView(View view) {
        if(view != null) {
            if(mView != null) {
                removeView(mView);
            }

            mView = view;
            mView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            addView(mView);
        }
    }

    public void setButton(String positive, String neutral, String negative, onDialogButtonClickListener listener) {
        if(positive == null && neutral == null && negative == null) {
            return;
        }

        if(mButton != null) {
            removeView(mButton);
        }

        ArrayList<String> alTitile = new ArrayList<String>();
        ArrayList<Integer> alId = new ArrayList<Integer>();

        if(positive != null) {
            alTitile.add(positive);
            alId.add(Integer.valueOf(Adialog.EVENT_POSITIVE));
        }
        if(neutral != null) {
            alTitile.add(neutral);
            alId.add(Integer.valueOf(Adialog.EVENT_NEUTRAL));
        }
        if(negative != null) {
            alTitile.add(negative);
            alId.add(Integer.valueOf(Adialog.EVENT_NEGATIVE));
        }

        mDialogButtonClickListener = listener;

        float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mContext.getResources().getDisplayMetrics());

        LinearLayout ll = new LinearLayout(mContext);
        ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mContext.getResources().getDimensionPixelSize(R.dimen.dialog_btn_height)));
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(0, (int)(5 * dip), 0, 0);

        for(int i = 0; i < alTitile.size(); i++) {
            Button btn = new Button(mContext);
            btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            btn.setBackgroundResource(R.drawable.tdialog_btn_selector);
            btn.setTextAppearance(mContext, R.style.TextAppearance_tdialog_btn);
            btn.setMaxLines(2);
            btn.setText(alTitile.get(i));
            btn.setTag(alId.get(i));

            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mDialogButtonClickListener != null) {
                        mDialogButtonClickListener.onDialogButtonClick(((Integer) v.getTag()).intValue());
                    }
                }
            });
            ll.addView(btn);

            if(i + 1 < alTitile.size()) {
                ImageView iv = new ImageView(mContext);
                iv.setLayoutParams(new LinearLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
                iv.setPadding(0, (int)(10 * dip), 0, (int)(10 * dip));
                iv.setImageResource(R.drawable.tdialog_shape_btn_divider);
                ll.addView(iv);
            }
        }
        mButton = ll;
        addView(mButton);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int cwidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                                    MeasureSpec.getSize(widthMeasureSpec),
                                    MeasureSpec.EXACTLY);
//        int cwidthMeasureSpec = widthMeasureSpec;
//        if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
//            Alog.i("onMeasure mode MeasureSpec.UNSPECIFIED = %x", MeasureSpec.UNSPECIFIED);
//        } else if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
//            Alog.i("onMeasure mode MeasureSpec.EXACTLY = %x ", MeasureSpec.EXACTLY);
//        } else if(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
//            Alog.i("onMeasure mode MeasureSpec.AT_MOST = %x", MeasureSpec.AT_MOST);
//        } else {
//            Alog.i("onMeasure mode Unknown = %x", MeasureSpec.getMode(widthMeasureSpec));
//        }

        int childheight = getPaddingTop() + getPaddingBottom();
        int heightSize = MeasureSpec.getSize(heightMeasureSpec) - childheight;

        if(mTitle != null) {
            measureChild(mTitle, cwidthMeasureSpec, heightMeasureSpec);
            heightSize -= mTitle.getMeasuredHeight();
            childheight += mTitle.getMeasuredHeight();
        }

        if(mButton != null) {
            measureChild(mButton, cwidthMeasureSpec, heightMeasureSpec);
            heightSize -= mButton.getMeasuredHeight();
            childheight += mButton.getMeasuredHeight();
        }

        if(mView != null) {
            int bodyHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.getMode(heightMeasureSpec));
            measureChild(mView, cwidthMeasureSpec, bodyHeightMeasureSpec);
            childheight += mView.getMeasuredHeight();
        }

        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(childheight, MeasureSpec.getMode(heightMeasureSpec)));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int heightPos = 0;
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if(mTitle != null) {
            mTitle.layout(paddingLeft, paddingTop + heightPos, paddingLeft + mTitle.getMeasuredWidth(), paddingTop + heightPos + mTitle.getMeasuredHeight());
            heightPos += mTitle.getMeasuredHeight();
        }

        if(mView != null) {
            mView.layout(paddingLeft, paddingTop + heightPos, paddingLeft + mView.getMeasuredWidth(), paddingTop + heightPos + mView.getMeasuredHeight());
            heightPos += mView.getMeasuredHeight();
        }

        if(mButton != null) {
            mButton.layout(paddingLeft, paddingTop + heightPos, paddingLeft + mButton.getMeasuredWidth(), paddingTop + heightPos + mButton.getMeasuredHeight());
        }
    }

}
