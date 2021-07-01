package io.agora.chatdemo.settings;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chat.ChatMessage;
import io.agora.chat.TextMessageBody;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.sign.SignInActivity;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.easeui.utils.EaseUserUtils;
import io.agora.easeui.utils.Utils;
import io.agora.easeui.widget.EaseImageView;
import io.agora.util.EMLog;

import java.io.ByteArrayOutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by lzan13 on 2016/10/21.
 * Account info activity
 */

public class AccountActivity extends BaseActivity {

    private BaseActivity mActivity;

    private static final int REQUEST_CODE_PICK = 1;
    private static final int REQUEST_CODE_CUTTING = 2;

    @BindView(R.id.img_account_avatar) EaseImageView mAvatarView;
    @BindView(R.id.fab_edit_avatar) FloatingActionButton mEditAvatarFab;
    @BindView(R.id.text_hyphenate_id) TextView mHyphenateID;
    @BindView(R.id.text_user_nick) TextView mNickView;
    @BindView(R.id.snackbar_action)    RelativeLayout snackbar_action;
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_account);

        ButterKnife.bind(this);
        mActivity = this;
        init();
    }

    /**
     * Init layout view
     */
    private void init() {
        mHyphenateID.setText(ChatClient.getInstance().getCurrentUser());
        getSupportActionBar().setTitle("Account");
        getActionBarToolbar().setNavigationIcon(R.drawable.em_ic_back);
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        EaseUserUtils.setUserNick(ChatClient.getInstance().getCurrentUser(), mNickView);
        EaseUserUtils.setUserAvatar(this, ChatClient.getInstance().getCurrentUser(), mAvatarView);
    }

    @OnClick(R.id.layout_avatar_container) void setAvatar() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(R.string.account_title_upload_photo);
//        builder.setItems(new String[] {
//                getString(R.string.account_msg_take_photo),
//                getString(R.string.account_msg_local_upload)
//        }, new DialogInterface.OnClickListener() {
//
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                switch (which) {
//                    case 0:
//                        Toast.makeText(AccountActivity.this, "Not supported at this time",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                    case 1:
//                        Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
//                        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                                "image/*");
//                        startActivityForResult(pickIntent, REQUEST_CODE_PICK);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });
//        builder.create().show();
    }

    @OnClick(R.id.layout_nick_container) void setNick() {
        final EditText editText = new EditText(this);
        final String nick =
                DemoHelper.getInstance().getUserManager().getCurrentUserInfo().getNickname();
        editText.setText(nick);
        AlertDialog alertDialog =
                new AlertDialog.Builder(this).setTitle(R.string.account_set_nickname)
                        .setView(editText)
                        .setPositiveButton(R.string.common_ok,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String nickString = editText.getText().toString();
                                        if (TextUtils.isEmpty(nickString)) {
                                            Toast.makeText(AccountActivity.this,
                                                    getString(R.string.toast_nick_not_isnull),
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        if (nickString.equals(nick)) {
                                            return;
                                        }
                                        updateRemoteNick(nickString);
                                    }
                                })
                        .setNegativeButton(R.string.common_cancel, null)
                        .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override public void onShow(DialogInterface dialog) {
                Editable editable = editText.getText();
                Selection.setSelection(editable, editable.length());
                Utils.showKeyboard(editText);
            }
        });
        alertDialog.show();
    }

    @OnClick({R.id.btn_sign_out, R.id.btn_send, R.id.btn_verify}) void signOut(View view) {
        switch (view.getId()) {
            case R.id.btn_sign_out:

                final ProgressDialog dialog = new ProgressDialog(mActivity);
                dialog.setMessage(mActivity.getString(R.string.em_hint_loading));
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                DemoHelper.getInstance().signOut(true, new CallBack() {
                    @Override public void onSuccess() {
                        Intent intent = new Intent(AccountActivity.this, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override public void onError(final int i, final String s) {
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                dialog.dismiss();
                                Toast.makeText(mActivity, "Sign out failed " + i + ", " + s,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override public void onProgress(int i, String s) {

                    }
                });
                break;
            case R.id.btn_send:
                send();
                break;
            case R.id.btn_verify:
                DemoHelper.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        verify();
                    }
                });
                break;
            default:
                break;
        }
    }

    void send() {
        String to = "gm";
        for (int i = 0; i < 1000; i++) {
            ChatMessage message = ChatMessage.createTxtSendMessage("" + i, to);
            message.setChatType(ChatMessage.ChatType.Chat);
            ChatClient.getInstance().chatManager().sendMessage(message);
            if (i % 100 == 0) {
                Snackbar.make(snackbar_action, "" + i, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    void verify() {
        // loadAllMessages
        String msgId = "";
        List<ChatMessage> msgs;
        // Using the three-parameter method to prevent conversation from being null
        Conversation conversation = ChatClient.getInstance().chatManager().getConversation("gm1",
                Conversation.ConversationType.Chat, true);
        do {
            msgs = conversation.loadMoreMsgFromDB(msgId, 100);
            if (msgs != null && msgs.size() > 0) {
                msgId = msgs.get(msgs.size() -1).getMsgId();
            }
        } while(msgs.size() > 0);

        final List<ChatMessage> allMessages =  conversation.getAllMessages();
        ChatMessage prev = null;
        boolean result = true;
        for (ChatMessage msg : allMessages) {
            if (prev != null) {
                String prevText = ((TextMessageBody) prev.getBody()).getMessage();
                String msgText = ((TextMessageBody)prev.getBody()).getMessage();
                if ((new Integer(prevText)).intValue() > (new Integer(msgText)).intValue()) {
                    EMLog.d("ASSERT FAIL:", "disorder, prev:" + prevText + " msg:" + msgText);
                    result = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(snackbar_action, "verify fail", Snackbar.LENGTH_INDEFINITE).show();

                        }
                    });
                    break;
                }
            }
            prev = msg;
        }

        if (result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(snackbar_action, "verify ok, allMessage.size:" + allMessages.size(), Snackbar.LENGTH_INDEFINITE).show();
                }
            });
        }

    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    private void updateRemoteNick(final String nickName) {
        final ProgressDialog dialog =
                ProgressDialog.show(this, getString(R.string.account_update_nick),
                        getString(R.string.account_waiting));
        new Thread(new Runnable() {

            @Override public void run() {
                boolean updatenick = true;
                if (AccountActivity.this.isFinishing()) {
                    return;
                }
                if (!updatenick) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(AccountActivity.this,
                                    getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT)
                                    .show();
                            dialog.dismiss();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            dialog.dismiss();
                            Toast.makeText(AccountActivity.this,
                                    getString(R.string.toast_updatenick_success),
                                    Toast.LENGTH_SHORT).show();
                            mNickView.setText(nickName);
                        }
                    });
                }
            }
        }).start();
    }

    private void uploadUserAvatar(final byte[] data) {
        final ProgressDialog dialog =
                ProgressDialog.show(this, getString(R.string.account_update_photo),
                        getString(R.string.account_waiting));
        new Thread(new Runnable() {

            @Override public void run() {
                final String avatarUrl = "";
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        dialog.dismiss();
                        if (avatarUrl != null) {
                            Toast.makeText(AccountActivity.this,
                                    getString(R.string.toast_update_photo_success),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccountActivity.this,
                                    getString(R.string.toast_update_photo_fail), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
            }
        }).start();

        dialog.show();
    }

    public byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
