package com.diwen.android.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by zhaishaoping on 21/11/2016.
 */

public class BaseFragment extends Fragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    public OnCameraShowLinstener onCameraShowLinstener;

    public void setOnCameraShowLinstener(OnCameraShowLinstener onCameraShowLinstener) {
        this.onCameraShowLinstener = onCameraShowLinstener;
    }

    public interface OnCameraShowLinstener {
        /**
         * Camera是否显示
         * @param isShow
         */
        void onCameraShow(boolean isShow);

        /**
         * 返回Camera的缩小值
         * @return
         */
        int onCameraZoomMinus();

        int onCameraZoomAdd();
    }
}
