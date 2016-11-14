package com.hyphenate.chatuidemo.ui.settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.ui.sign.SignInActivity;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseImageView;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by lzan13 on 2016/10/21.
 * Account info activity
 */

public class AccountActivity extends BaseActivity {

    private static final int REQUEST_CODE_PICK = 1;
    private static final int REQUEST_CODE_CUTTING = 2;

    @BindView(R.id.img_account_avatar) EaseImageView mAvatarView;
    @BindView(R.id.fab_edit_avatar) FloatingActionButton mEditAvatarFab;
    @BindView(R.id.text_hyphenate_id) TextView mHyphenateID;
    @BindView(R.id.text_user_nick) TextView mNickView;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_account);

        ButterKnife.bind(this);

        init();
    }

    /**
     * Init layout view
     */
    private void init() {
        mHyphenateID.setText(EMClient.getInstance().getCurrentUser());
        getSupportActionBar().setTitle("Account");
        getActionBarToolbar().setNavigationIcon(R.drawable.em_ic_back);
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        EaseUserUtils.setUserNick(EMClient.getInstance().getCurrentUser(), mNickView);
        EaseUserUtils.setUserAvatar(this, EMClient.getInstance().getCurrentUser(), mAvatarView);

    }

    @OnClick(R.id.layout_avatar_container) void changeAvatar(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.account_title_upload_photo);
        builder.setItems(new String[] { getString(R.string.account_msg_take_photo), getString(R.string.account_msg_local_upload) },
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Toast.makeText(AccountActivity.this, "Not supported at this time",
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Intent pickIntent = new Intent(Intent.ACTION_PICK,null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, REQUEST_CODE_PICK);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    @OnClick(R.id.btn_sign_out) void signOut() {
        DemoHelper.getInstance().signOut(true, new EMCallBack() {
            @Override public void onSuccess() {
                List<Activity> list = DemoHelper.getInstance().getActivityList();
                for (Activity activity : list) {
                    activity.finish();
                }
                startActivity(new Intent(AccountActivity.this, SignInActivity.class));
            }

            @Override public void onError(int i, String s) {

            }

            @Override public void onProgress(int i, String s) {

            }
        });
    }

    //public void asyncFetchUserInfo(String username){
    //    DemoHelper.getInstance().getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<UserEntity>() {
    //
    //        @Override
    //        public void onSuccess(UserEntity user) {
    //            if (user != null) {
    //                DemoHelper.getInstance().saveContact(user);
    //                if(isFinishing()){
    //                    return;
    //                }
    //                mNickView.setText(user.getNickname());
    //                if(!TextUtils.isEmpty(user.getAvatar())){
    //                    Glide.with(AccountActivity.this).load(user.getAvatar()).placeholder(R.drawable.ease_default_avatar).into(mAvatarView);
    //                }else{
    //                    Glide.with(AccountActivity.this).load(R.drawable.ease_default_avatar).into(mAvatarView);
    //                }
    //            }
    //        }
    //
    //        @Override
    //        public void onError(int error, String errorMsg) {
    //        }
    //    });
    //}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case REQUEST_CODE_CUTTING:
                if (data != null) {
                    setPicToView(data);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUEST_CODE_CUTTING);
    }

    /**
     * save the picture data
     *
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(getResources(), photo);
            mAvatarView.setImageDrawable(drawable);
            uploadUserAvatar(Bitmap2Bytes(photo));
        }

    }

    private void uploadUserAvatar(final byte[] data) {
        final ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.account_update_photo), getString(R.string.account_waiting));
        new Thread(new Runnable() {

            @Override
            public void run() {
                final String avatarUrl = DemoHelper.getInstance().getUserProfileManager().uploadUserAvatar(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        if (avatarUrl != null) {
                            Toast.makeText(AccountActivity.this, getString(R.string.toast_update_photo_success),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccountActivity.this, getString(R.string.toast_update_photo_fail),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        }).start();

        dialog.show();
    }

    public byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
