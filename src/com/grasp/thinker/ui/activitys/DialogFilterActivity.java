package com.grasp.thinker.ui.activitys;

import com.grasp.thinker.R;
import com.grasp.thinker.ThinkerConstant;
import com.grasp.thinker.adapters.FilterAdapter;
import com.grasp.thinker.utils.MusicUtils;
import com.grasp.thinker.utils.PreferenceUtils;
import com.grasp.thinker.utils.ThemeUtils;
import com.grasp.thinker.utils.ThinkerUtils;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by qiuzhangzhi on 15/2/3.
 */
public class DialogFilterActivity extends Activity implements View.OnClickListener{

    private ListView mListView;

    private FilterAdapter mFilterAdapter;

    private ArrayList<String> mdata ;

    private View mHeader;

    private View mFooter;

    private ImageView mImageViewHeaderNum;

    private ImageView mImageViewHeaderLetter;

    private ImageView mImageViewTitleAdd;

    private ImageView mImageViewEditerAdd;

    private EditText mEditTextAdd;

    private View mTitleDivider;

    private PreferenceUtils mPreferenceUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferenceUtils = PreferenceUtils.getInstance(this);
        initData();
        setContentView(R.layout.dialog_filter);



        mListView = (ListView)findViewById(R.id.filter_list);
        mImageViewTitleAdd = (ImageView)findViewById(R.id.filter_add);

        mFilterAdapter = new FilterAdapter(this);
        mHeader = LayoutInflater.from(this).inflate(R.layout.listview_filter_header,null);
        mFooter = LayoutInflater.from(this).inflate(R.layout.listview_filter_footer,null);
        mImageViewHeaderNum = (ImageView)mHeader.findViewById(R.id.filter_num_editer);
        mImageViewHeaderLetter =(ImageView)mHeader.findViewById(R.id.filter_letter_editer);
        mImageViewEditerAdd = (ImageView)mFooter.findViewById(R.id.filter_editer_add);
        mEditTextAdd = (EditText)mFooter.findViewById(R.id.filer_editer);
        mTitleDivider = findViewById(R.id.filter_divider);
        init();

    }

    private void init(){

        mListView.addHeaderView(mHeader);
        mListView.setAdapter(mFilterAdapter);
        mFilterAdapter.addAll(mdata);

        mImageViewHeaderNum.setOnClickListener(this);
        mImageViewHeaderLetter.setOnClickListener(this);
        mImageViewTitleAdd.setOnClickListener(this);
        mImageViewEditerAdd.setOnClickListener(this);

        if(mPreferenceUtils.getIsFilterNum()){
            mImageViewHeaderNum.setImageResource(R.drawable.dialog_filter_remove_ic);
        }else {
            mImageViewHeaderNum.setImageResource(R.drawable.dialog_filter_add_ic);
        }
        if(mPreferenceUtils.getIsFilterLetter()){
            mImageViewHeaderLetter.setImageResource(R.drawable.dialog_filter_remove_ic);
        }else {
            mImageViewHeaderLetter.setImageResource(R.drawable.dialog_filter_add_ic);
        }
        mTitleDivider.setBackgroundColor(ThinkerConstant.mThemeColor);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.filter_num_editer:
                if(mPreferenceUtils.getIsFilterNum()){
                    mImageViewHeaderNum.setImageResource(R.drawable.dialog_filter_add_ic);
                    mPreferenceUtils.setIsFilterNum(false);
                }else {
                    mImageViewHeaderNum.setImageResource(R.drawable.dialog_filter_remove_ic);
                    mPreferenceUtils.setIsFilterNum(true);
                }
                break;
            case R.id.filter_letter_editer:
                if(mPreferenceUtils.getIsFilterLetter()){
                    mImageViewHeaderLetter.setImageResource(R.drawable.dialog_filter_add_ic);
                    mPreferenceUtils.setIsFilterLetter(false);
                }else {
                    mImageViewHeaderLetter.setImageResource(R.drawable.dialog_filter_remove_ic);
                    mPreferenceUtils.setIsFilterLetter(true);
                }
                break;
            case R.id.filter_add:
                if(mListView.getFooterViewsCount() == 0) {
                    mListView.addFooterView(mFooter);
                    mEditTextAdd.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                }
                break;
            case R.id.filter_editer_add:
                mListView.removeFooterView(mFooter);
                if(!ThinkerUtils.isStringEmpty(mEditTextAdd.getText().toString())){
                    mPreferenceUtils.setSongFilters(mEditTextAdd.getText().toString());
                    mFilterAdapter.addItem(mEditTextAdd.getText().toString());
                    mEditTextAdd.setText("");
                }
                break;


        }
    }

    private void initData(){
        mdata = new ArrayList<String>();
        if(mPreferenceUtils.getSongFilter()!=null){
            Set<String> filterSet = mPreferenceUtils.getSongFilter();
            mdata.addAll(filterSet);
        }

    }
}