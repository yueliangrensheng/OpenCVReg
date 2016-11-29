package com.diwen.android.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.diwen.android.R;

import butterknife.Bind;

/**
 * Created by zhaishaoping on 20/11/2016.
 */

public class PatientDataFragment extends BaseFragment implements View.OnClickListener {

  /*  @Bind(R.id.action_minus)
    ImageView action_minus;*/
   /* @Bind(R.id.action_add)
    ImageView action_add;*/
    /*@Bind(R.id.action_zoom)
    TextView action_zoom_show;
    @Bind(R.id.action_selected_body_part)
    ImageView action_selected_body_part;
    @Bind(R.id.action_selected_model)
    ImageView action_selected_model;
    @Bind(R.id.action_reg_main)
    ImageView action_reg_main;
    @Bind(R.id.action_reg_red_dot)
    ImageView action_reg_red_dot;
    @Bind(R.id.action_link_line)
    ImageView action_link_line;
    @Bind(R.id.action_back)
    ImageView action_back;*/
	@Bind(R.id.iv_ultransound)
	ImageView vUltransoud;
	@Bind(R.id.iv_rf)
	ImageView vRf;
	@Bind(R.id.ultransoud_right)
	ImageView ultransoudMark;
	@Bind(R.id.rf_right)
	ImageView rfMark;
	
    @Bind(R.id.fragment_content)
    LinearLayout fragment_content;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.layout_fragment_patient_data, container, false);
        return inflate;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerListener();
        initData();

    }

    private void registerListener() {
        /*action_minus.setOnClickListener(this);
        action_add.setOnClickListener(this);
        action_selected_body_part.setOnClickListener(this);
        action_selected_model.setOnClickListener(this);
        action_reg_main.setOnClickListener(this);
        action_reg_red_dot.setOnClickListener(this);
        action_link_line.setOnClickListener(this);
        action_back.setOnClickListener(this);*/
    	vUltransoud.setOnClickListener(this);
    	vRf.setOnClickListener(this);
    }

    private void initData() {
        //显示Camera区
        onCameraShowLinstener.onCameraShow(false);
    }

    @Override
    public void onClick(View view) {
    	switch (view.getId()) {
		case R.id.iv_ultransound:
			changeUltransoud();
			onCameraShowLinstener.onChangeTab(1);
			break;
		case R.id.iv_rf:
			changeRf();
			onCameraShowLinstener.onChangeTab(2);
			break;
		default:
			break;
		}
       /* switch (view.getId()) {
            case R.id.action_minus://缩放
                int zoomMinusValue = onCameraShowLinstener.onCameraZoomMinus();
                action_zoom_show.setText(String.valueOf(zoomMinusValue));
                break;
            case R.id.action_add://缩放
                int zoomAddValue = onCameraShowLinstener.onCameraZoomAdd();
                action_zoom_show.setText(String.valueOf(zoomAddValue));
                break;
            case R.id.action_selected_body_part://选择身体部位
                //隐藏Camera
                onCameraShowLinstener.onCameraShow(false);

                break;
            case R.id.action_selected_model://选择治疗模式
                break;
            case R.id.action_reg_main://识别1
                break;
            case R.id.action_reg_red_dot://识别2
                break;
            case R.id.action_link_line://连线
                break;
            case R.id.action_back://返回
                break;

            default:

                break;
        }*/

    }
    private void changeRf(){
    	rfMark.setVisibility(View.VISIBLE);
    	ultransoudMark.setVisibility(View.GONE);
    }
    private void changeUltransoud(){
    	rfMark.setVisibility(View.GONE);
    	ultransoudMark.setVisibility(View.VISIBLE);
    	
    }
}
