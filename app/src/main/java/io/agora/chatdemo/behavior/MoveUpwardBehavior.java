package io.agora.chatdemo.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

public class MoveUpwardBehavior extends AppBarLayout.ScrollingViewBehavior {
    private int lastHeight;
    private boolean isShow;

    public MoveUpwardBehavior() {
        super();
    }

    public MoveUpwardBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        return dependency instanceof AppBarLayout || dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        if(dependency instanceof AppBarLayout) {
            return super.onDependentViewChanged(parent, child, dependency);
        }else {
            if(child instanceof RecyclerView) {
                int height = child.getMeasuredHeight() - dependency.getHeight();
                ViewGroup.LayoutParams params = child.getLayoutParams();
                params.height = Math.max(lastHeight, height);
                lastHeight = params.height;
            }else {
                float translationY = Math.min(0, ViewCompat.getTranslationY(dependency) - dependency.getHeight());
                child.setTranslationY(translationY);
            }
            return true;
        }
    }

    @Override
    public void onDependentViewRemoved(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        if(dependency instanceof AppBarLayout) {
            super.onDependentViewRemoved(parent, child, dependency);
        }else {
            ViewCompat.animate(child).translationY(0).start();
        }
    }
}

