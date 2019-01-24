package com.hejcompany.administrator.tubedownloader;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.Window;
import android.widget.ProgressBar;

/**
 * Created by Administrator on 2018-11-01.
 */

public class CustomCircleProgressDialog extends Dialog {
    public CustomCircleProgressDialog(@NonNull Context context){
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.progressbar);
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_circle);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#bb0000"), PorterDuff.Mode.SRC_IN);

    }
}
