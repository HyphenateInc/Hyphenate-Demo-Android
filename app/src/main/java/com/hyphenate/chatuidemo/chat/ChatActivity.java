package com.hyphenate.chatuidemo.chat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.call.VideoCallActivity;
import com.hyphenate.chatuidemo.call.VoiceCallActivity;
import com.hyphenate.chatuidemo.chatroom.ChatRoomChangeListener;
import com.hyphenate.chatuidemo.chatroom.ChatRoomDetailsActivity;
import com.hyphenate.chatuidemo.group.GroupChangeListener;
import com.hyphenate.chatuidemo.group.GroupDetailsActivity;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.ui.widget.ChatInputView;
import com.hyphenate.chatuidemo.ui.widget.VoiceRecordDialog;
import com.hyphenate.chatuidemo.ui.widget.VoiceRecordView;
import com.hyphenate.chatuidemo.ui.widget.chatrow.ChatRowCall;
import com.hyphenate.chatuidemo.user.model.UserEntity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.Utils;
import com.hyphenate.easeui.widget.EaseChatExtendMenu;
import com.hyphenate.easeui.widget.EaseMessageListView;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;
import com.hyphenate.easeui.widget.chatrow.EaseCustomChatRowProvider;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.PathUtil;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hyphenate.easeui.EaseConstant.CHATTYPE_CHATROOM;
import static com.hyphenate.easeui.EaseConstant.CHATTYPE_GROUP;
import static com.hyphenate.easeui.EaseConstant.CHATTYPE_SINGLE;

/**
 * Chat with someone in this activity
 */
public class ChatActivity extends BaseActivity {

    private String TAG = this.getClass().getSimpleName();
    @BindView(R.id.input_view) ChatInputView mInputView;
    @BindView(R.id.message_list) EaseMessageListView mMessageListView;
    @BindView(R.id.pb_loading_message) ProgressBar mLoadingProgressBar;

    // Group change listener
    private DefaultGroupChangeListener groupChangeListener;
    private DefaultChatRoomChangeListener chatRoomChangeListener;

    protected static final int REQUEST_CODE_MAP = 1;
    protected static final int REQUEST_CODE_CAMERA = 2;
    protected static final int REQUEST_CODE_LOCAL = 3;
    protected static final int REQUEST_CODE_SELECT_VIDEO = 11;
    protected static final int REQUEST_CODE_SELECT_FILE = 12;
    protected static final int REQUEST_CODE_GROUP_DETAIL = 13;

    protected static final int MESSAGE_TYPE_RECV_CALL = 1;
    protected static final int MESSAGE_TYPE_SENT_CALL = 2;

    static final int ITEM_TAKE_PICTURE = 1;
    static final int ITEM_PICTURE = 2;
    static final int ITEM_LOCATION = 3;
    static final int ITEM_FILE = 4;
    static final int ITEM_VOICE_CALL = 5;
    static final int ITEM_VIDEO_CALL = 6;

    protected int[] itemStrings = {
            R.string.attach_take_pic, R.string.attach_picture, R.string.attach_location, R.string.attach_file
    };
    protected int[] itemdrawables = {
            R.drawable.ease_chat_takepic_selector, R.drawable.ease_chat_image_selector, R.drawable.ease_chat_location_selector,
            R.drawable.em_chat_file_selector
    };
    protected int[] itemIds = { ITEM_TAKE_PICTURE, ITEM_PICTURE, ITEM_LOCATION, ITEM_FILE };

    protected File mCameraFile;

    /**
     * to chat user id or group id
     */
    protected String toChatUsername;

    /**
     * chat type, single chat or group chat
     */
    protected int chatType;

    protected EMConversation mConversation;
    private EMMessage mToDeleteMessage;

    /**
     * load 20 messages at one time
     */
    protected int pageSize = 20;
    protected boolean isLoading;
    protected boolean isFirstLoad = true;
    protected boolean haveMoreData = true;

    public static ChatActivity activityInstance;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_chat);
        ButterKnife.bind(this);
        activityInstance = this;

        toChatUsername = getIntent().getStringExtra(EaseConstant.EXTRA_USER_ID);
        chatType = getIntent().getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, CHATTYPE_SINGLE);

        setToolbarTitle();
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
        initView();

        // If it is not a chat room, to initialize
        if (chatType != CHATTYPE_CHATROOM) {
            initConversation();
        }
        if (chatType == CHATTYPE_GROUP) {
            groupChangeListener = new DefaultGroupChangeListener();
            EMClient.getInstance().groupManager().addGroupChangeListener(groupChangeListener);
        } else if (chatType == CHATTYPE_CHATROOM) {
            chatRoomChangeListener = new DefaultChatRoomChangeListener();
            EMClient.getInstance().chatroomManager().addChatRoomChangeListener(chatRoomChangeListener);
            initChatRoom();
        }
        // received messages code in onResume() method
    }

    private void initView() {
        MyItemClickListener extendMenuItemClickListener = new MyItemClickListener();
        for (int i = 0; i < itemStrings.length; i++) {
            mInputView.registerExtendMenuItem(itemStrings[i], itemdrawables[i], itemIds[i], extendMenuItemClickListener);
        }
        if (chatType == CHATTYPE_SINGLE) {
            mInputView.registerExtendMenuItem(R.string.attach_voice_call, R.drawable.em_chat_voice_call_selector, ITEM_VOICE_CALL,
                    extendMenuItemClickListener);
            mInputView.registerExtendMenuItem(R.string.attach_video_call, R.drawable.em_chat_video_call_selector, ITEM_VIDEO_CALL,
                    extendMenuItemClickListener);
        }
        mInputView.init();
        mInputView.setViewEventListener(new ChatInputView.ChatInputViewEventListener() {
            @Override public void onSendMessage(CharSequence content) {
                if (!TextUtils.isEmpty(content)) {
                    sendTextMessage(content.toString());
                }
            }

            @Override public void onMicClick() {
                final VoiceRecordDialog dialog = new VoiceRecordDialog(ChatActivity.this);
                dialog.setRecordCallback(new VoiceRecordView.VoiceRecordCallback() {
                    @Override public void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength) {
                        dialog.dismiss();
                        sendVoiceMessage(voiceFilePath, voiceTimeLength);
                    }
                });
                dialog.show();
            }
        });
    }

    private void setToolbarTitle() {
        String nick = toChatUsername;
        if (chatType == CHATTYPE_SINGLE) { //p2p chat
            UserEntity user = DemoHelper.getInstance().getUserManager().getContactList().get(toChatUsername);
            if (user != null) {
                nick = user.getNickname();
            }
        } else if (chatType == EaseConstant.CHATTYPE_GROUP) { //group chat
            EMGroup group = EMClient.getInstance().groupManager().getGroup(toChatUsername);
            if (group != null) {
                nick = group.getGroupName();
            }
        } else if (chatType == EaseConstant.CHATTYPE_CHATROOM) { // chatroom
            EMChatRoom chatRoom = EMClient.getInstance().chatroomManager().getChatRoom(toChatUsername);
            if (chatRoom != null) {
                nick = chatRoom.getName();
            }
        }
        getSupportActionBar().setTitle(nick);
    }

    /**
     * init conversation
     * If it is a chat room, Need to join the chat room after the success of initialization
     */
    private void initConversation() {
        //get the mConversation
        mConversation = EMClient.getInstance()
                .chatManager()
                .getConversation(toChatUsername, EaseCommonUtils.getConversationType(chatType), true);
        mConversation.markAllMessagesAsRead();
        // the number of messages loaded into mConversation is getChatOptions().getNumberOfMessagesLoaded
        // you can change this number
        final List<EMMessage> msgs = mConversation.getAllMessages();
        int msgCount = msgs != null ? msgs.size() : 0;
        if (msgCount < mConversation.getAllMsgCount() && msgCount < pageSize) {
            String msgId = null;
            if (msgs != null && msgs.size() > 0) {
                msgId = msgs.get(0).getMsgId();
            }
            mConversation.loadMoreMsgFromDB(msgId, pageSize - msgCount);
        }

        initMessageListView();
    }

    /**
     * init message listview
     * If it is a chat room, Need to join the chat room after the success of initialization
     */
    private void initMessageListView() {
        // init message list view
        mMessageListView.init(toChatUsername, chatType, newCustomChatRowProvider());
        // show user nick in group chat
        if (mConversation.isGroup()) {
            mMessageListView.setShowUserNick(true);
        }
        mMessageListView.setOnScrollListener(new MsgListScrollListener());
        //register context menu for message listView
        registerForContextMenu(mMessageListView);
        mMessageListView.setItemClickListener(new EaseMessageListView.MessageListItemClicksListener() {
            @Override public void onResendClick(EMMessage message) {
                resendMessage(message);
            }

            @Override public boolean onBubbleClick(EMMessage message) {
                // override you want click groupChangeListener and return true
                return false;
            }

            @Override public void onBubbleLongClick(EMMessage message) {
                mToDeleteMessage = message;
                mMessageListView.showContextMenu();
            }

            @Override public void onUserAvatarClick(String username) {

            }

            @Override public void onUserAvatarLongClick(String username) {

            }
        });
        mMessageListView.setOnTouchListener(new View.OnTouchListener() {

            @Override public boolean onTouch(View v, MotionEvent event) {
                Utils.hideKeyboard(mInputView.getEditText());
                mInputView.hideExtendMenuContainer();
                return false;
            }
        });
    }

    /**
     *
     */
    protected void initChatRoom() {
        final ProgressDialog pd = ProgressDialog.show(this, "", "Joining......");
        EMClient.getInstance().chatroomManager().joinChatRoom(toChatUsername, new EMValueCallBack<EMChatRoom>() {

            @Override public void onSuccess(final EMChatRoom chatRoom) {
                EMLog.d(TAG, "join room success: " + chatRoom.getId());
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (isFinishing() || !toChatUsername.equals(chatRoom.getId())) {
                            return;
                        }
                        pd.dismiss();
                        setToolbarTitle();
                        initConversation();
                    }
                });
            }

            @Override public void onError(final int error, String errorMsg) {
                // TODO Auto-generated method stub
                EMLog.d(TAG, "join room failure : " + error);
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        pd.dismiss();
                    }
                });
                finish();
            }
        });
    }

    /**
     * message list on sroll groupChangeListener
     */
    private class MsgListScrollListener implements AbsListView.OnScrollListener {

        @Override public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            synchronized (mMessageListView) {
                if (firstVisibleItem == 0 && !isLoading && haveMoreData && mConversation.getAllMessages().size() != 0) {
                    isLoading = true;

                    if (!isFirstLoad) mLoadingProgressBar.setVisibility(View.VISIBLE);
                    isFirstLoad = false;
                    final List<EMMessage> messages;
                    EMMessage firstMsg = mConversation.getAllMessages().get(0);
                    try {
                        // load more messages from db
                        messages = mConversation.loadMoreMsgFromDB(firstMsg.getMsgId(), pageSize);
                    } catch (Exception e1) {
                        mLoadingProgressBar.setVisibility(View.INVISIBLE);
                        return;
                    }
                    new Thread(new Runnable() {
                        @Override public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    if (messages.size() != 0) {
                                        if (messages.size() > 0) {
                                            mMessageListView.refreshSeekTo(messages.size() - 1);
                                        }

                                        if (messages.size() != pageSize) haveMoreData = false;
                                    } else {
                                        haveMoreData = false;
                                    }
                                    mLoadingProgressBar.setVisibility(View.INVISIBLE);
                                    isLoading = false;
                                }
                            });
                        }
                    }).start();
                }
            }
        }
    }

    @Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.em_delete_message, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_message) {
            if (mConversation != null) {
                //delete selected message
                mConversation.removeMessage(mToDeleteMessage.getMsgId());
                mMessageListView.refresh();
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //add the action buttons to toolbar
        Toolbar toolbar = getActionBarToolbar();
        toolbar.inflateMenu(R.menu.em_chat_menu);

        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            menu.findItem(R.id.menu_voice_call).setVisible(false);
            menu.findItem(R.id.menu_video_call).setVisible(false);
            menu.findItem(R.id.menu_group_detail).setVisible(true);
            menu.findItem(R.id.menu_chatroom_detail).setVisible(false);
        }
        if (chatType == EaseConstant.CHATTYPE_CHATROOM) {
            menu.findItem(R.id.menu_voice_call).setVisible(false);
            menu.findItem(R.id.menu_video_call).setVisible(false);
            menu.findItem(R.id.menu_group_detail).setVisible(false);
            menu.findItem(R.id.menu_chatroom_detail).setVisible(true);
        }

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent();
                switch (item.getItemId()) {
                    case R.id.menu_video_call:
                        intent.setClass(ChatActivity.this, VideoCallActivity.class);
                        intent.putExtra(EaseConstant.EXTRA_USER_ID, toChatUsername);
                        intent.putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false);
                        startActivity(intent);
                        break;
                    case R.id.menu_voice_call:
                        intent.setClass(ChatActivity.this, VoiceCallActivity.class);
                        intent.putExtra(EaseConstant.EXTRA_USER_ID, toChatUsername);
                        intent.putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false);
                        startActivity(intent);
                        break;

                    case R.id.menu_group_detail:
                        intent.setClass(ChatActivity.this, GroupDetailsActivity.class);
                        intent.putExtra(EaseConstant.EXTRA_GROUP_ID, toChatUsername);
                        startActivity(intent);
                        break;
                    case R.id.menu_chatroom_detail:
                        intent.setClass(ChatActivity.this, ChatRoomDetailsActivity.class);
                        intent.putExtra(EaseConstant.EXTRA_CHATROOM_ID, toChatUsername);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });

        return true;
    }

    //methods of send various types message
    protected void sendTextMessage(String content) {
        // create a message
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        // send message
        sendMessage(message);
    }

    protected void sendVoiceMessage(String filePath, int length) {
        EMMessage message = EMMessage.createVoiceSendMessage(filePath, length, toChatUsername);
        sendMessage(message);
    }

    protected void sendImageMessage(String imagePath) {
        EMMessage message = EMMessage.createImageSendMessage(imagePath, false, toChatUsername);
        sendMessage(message);
    }

    protected void sendLocationMessage(double latitude, double longitude, String locationAddress) {
        EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, toChatUsername);
        sendMessage(message);
    }

    protected void sendVideoMessage(String videoPath, String thumbPath, int videoLength) {
        EMMessage message = EMMessage.createVideoSendMessage(videoPath, thumbPath, videoLength, toChatUsername);
        sendMessage(message);
    }

    protected void sendFileMessage(String filePath) {
        EMMessage message = EMMessage.createFileSendMessage(filePath, toChatUsername);
        sendMessage(message);
    }

    protected void sendMessage(EMMessage message) {
        if (message == null) {
            return;
        }
        onSetMessageAttributes(message);
        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            message.setChatType(EMMessage.ChatType.GroupChat);
        } else if (chatType == EaseConstant.CHATTYPE_CHATROOM) {
            message.setChatType(EMMessage.ChatType.ChatRoom);
        }
        //send message
        EMClient.getInstance().chatManager().sendMessage(message);
        //refresh ui
        mMessageListView.refreshSelectLast();
    }

    public void resendMessage(EMMessage message) {
        message.setStatus(EMMessage.Status.CREATE);
        EMClient.getInstance().chatManager().sendMessage(message);
        mMessageListView.refresh();
    }

    /**
     * send image
     */
    protected void sendPicByUri(Uri selectedImage) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            cursor = null;

            if (picturePath == null || picturePath.equals("null")) {
                Toast toast = Toast.makeText(this, R.string.cant_find_pictures, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendImageMessage(picturePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Toast toast = Toast.makeText(this, R.string.cant_find_pictures, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendImageMessage(file.getAbsolutePath());
        }
    }

    /**
     * send file
     */
    protected void sendFileByUri(Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;

            try {
                cursor = this.getContentResolver().query(uri, filePathColumn, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        File file = new File(filePath);
        if (file == null || !file.exists()) {
            Toast.makeText(this, R.string.File_does_not_exist, Toast.LENGTH_SHORT).show();
            return;
        }
        //limit the size < 10M
        if (file.length() > 10 * 1024 * 1024) {
            Toast.makeText(this, R.string.The_file_is_not_greater_than_10_m, Toast.LENGTH_SHORT).show();
            return;
        }
        sendFileMessage(filePath);
    }

    /**
     * capture new image
     */
    protected void selectPicFromCamera() {
        if (!EaseCommonUtils.isSdcardExist()) {
            Toast.makeText(this, R.string.sd_card_does_not_exist, Toast.LENGTH_SHORT).show();
            return;
        }

        mCameraFile = new File(PathUtil.getInstance().getImagePath(),
                EMClient.getInstance().getCurrentUser() + System.currentTimeMillis() + ".jpg");
        mCameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraFile)),
                REQUEST_CODE_CAMERA);
    }

    /**
     * select local image
     */
    protected void selectPicFromLocal() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);
    }

    /**
     * select a file
     */
    protected void selectFileFromLocal() {
        Intent intent = null;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        } else {
            //after version 19, this api is not available, demo here simply handle into the gallery to select the picture
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }

    protected void selectLoaction() {
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            // Start the Intent by requesting a result, identified by a request code.
            startActivityForResult(intent, REQUEST_CODE_MAP);
        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Google Play Services is not available.", Toast.LENGTH_LONG).show();
        }
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) { // capture new image
                if (mCameraFile != null && mCameraFile.exists()) {
                    sendImageMessage(mCameraFile.getAbsolutePath());
                }
            } else if (requestCode == REQUEST_CODE_LOCAL) { // send local image
                if (data != null) {
                    Uri selectedImage = data.getData();
                    if (selectedImage != null) {
                        sendPicByUri(selectedImage);
                    }
                }
            } else if (requestCode == REQUEST_CODE_MAP) { // location
                final Place place = PlacePicker.getPlace(data, this);
                double latitude = place.getLatLng().latitude;
                double longitude = place.getLatLng().longitude;
                String locationAddress = (String) place.getAddress();

                if (locationAddress != null && !locationAddress.equals("")) {
                    sendLocationMessage(latitude, longitude, locationAddress);
                } else {
                    Toast.makeText(this, R.string.unable_to_get_location, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_CODE_SELECT_FILE) { //send the file
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        sendFileByUri(uri);
                    }
                }
            }
        }
    }

    /**
     * handle the click event for extend menu
     */
    class MyItemClickListener implements EaseChatExtendMenu.EaseChatExtendMenuItemClickListener {

        @Override public void onClick(int itemId, View view) {
            switch (itemId) {
                case ITEM_TAKE_PICTURE:
                    selectPicFromCamera();
                    break;
                case ITEM_PICTURE:
                    selectPicFromLocal();
                    break;
                case ITEM_LOCATION:
                    selectLoaction();
                    break;
                //case ITEM_VIDEO:
                //    Intent intent = new Intent(chatType.this, ImageGridActivity.class);
                //    startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
                //    break;
                case ITEM_FILE: //file
                    selectFileFromLocal();
                    break;
                case ITEM_VOICE_CALL:
                    startVoiceCall();
                    break;
                case ITEM_VIDEO_CALL:
                    startVideoCall();
                    break;
                default:
                    break;
            }
        }
    }

    private void startVoiceCall() {
        if (!EMClient.getInstance().isConnected()) {
            Toast.makeText(this, R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
        } else {
            Intent voiceCallIntent = new Intent();
            voiceCallIntent.setClass(ChatActivity.this, VoiceCallActivity.class);
            voiceCallIntent.putExtra(EaseConstant.EXTRA_USER_ID, toChatUsername);
            voiceCallIntent.putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false);
            startActivity(voiceCallIntent);
        }
        mInputView.hideExtendMenuContainer();
    }

    private void startVideoCall() {
        if (!EMClient.getInstance().isConnected()) {
            Toast.makeText(this, R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
        } else {
            Intent videoCallIntent = new Intent();
            videoCallIntent.setClass(ChatActivity.this, VideoCallActivity.class);
            videoCallIntent.putExtra(EaseConstant.EXTRA_USER_ID, toChatUsername);
            videoCallIntent.putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false);
            startActivity(videoCallIntent);
        }
        mInputView.hideExtendMenuContainer();
    }

    /**
     * create a chat row provider
     */
    private EaseCustomChatRowProvider newCustomChatRowProvider() {
        return new EaseCustomChatRowProvider() {
            @Override public int getCustomChatRowTypeCount() {
                return 2;
            }

            @Override public int getCustomChatRowType(EMMessage message) {
                if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false) || message.getBooleanAttribute(
                        EaseConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_CALL : MESSAGE_TYPE_SENT_CALL;
                }
                return 0;
            }

            @Override public EaseChatRow getCustomChatRow(EMMessage message, int position, BaseAdapter adapter) {
                if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false) || message.getBooleanAttribute(
                        EaseConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    return new ChatRowCall(ChatActivity.this, message, position, adapter);
                }
                return null;
            }
        };
    }

    /**
     * set message Extension attributes
     */
    protected void onSetMessageAttributes(EMMessage message) {

    }

    EMMessageListener mMessageListener = new EMMessageListener() {
        @Override public void onMessageReceived(List<EMMessage> list) {
            for (EMMessage message : list) {
                String username = null;
                // group message
                if (message.getChatType() == EMMessage.ChatType.GroupChat
                        || message.getChatType() == EMMessage.ChatType.ChatRoom) {
                    username = message.getTo();
                } else {
                    // single chat message
                    username = message.getFrom();
                }

                // if the message is for current mConversation
                if (username.equals(toChatUsername)) {
                    mMessageListView.refreshSelectLast();
                    DemoHelper.getInstance().getNotifier().vibrateAndPlayTone(message);
                } else {
                    DemoHelper.getInstance().getNotifier().onNewMsg(message);
                }
            }
        }

        @Override public void onCmdMessageReceived(List<EMMessage> list) {
            //cmd messages do not save to the cache in sdk
        }

        @Override public void onMessageRead(List<EMMessage> list) {
            mMessageListView.refresh();
        }

        @Override public void onMessageDelivered(List<EMMessage> list) {
            mMessageListView.refresh();
        }

        @Override public void onMessageChanged(EMMessage emMessage, Object o) {
            mMessageListView.refresh();
        }
    };

    @Override protected void onNewIntent(Intent intent) {
        //Ensure that only a chat activity
        String username = intent.getStringExtra(EaseConstant.EXTRA_USER_ID);
        if (toChatUsername.equals(username)) {
            super.onNewIntent(intent);
        } else {
            finish();
            startActivity(intent);
        }
    }

    @Override protected void onResume() {
        super.onResume();
        DemoHelper.getInstance().pushActivity(this);
        if (mMessageListener != null) {
            // register the event groupChangeListener when enter the foreground
            // remember to remove this groupChangeListener in onStop()
            EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
        }
        mMessageListView.refresh();
    }

    @Override protected void onStop() {
        super.onStop();
        if (mMessageListener != null) {
            // unregister this event groupChangeListener when this activity enters the background
            EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
        }
        // remove activity from foreground activity list
        DemoHelper.getInstance().popActivity(this);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (groupChangeListener != null) {
            EMClient.getInstance().groupManager().removeGroupChangeListener(groupChangeListener);
        }
        if (chatType == EaseConstant.CHATTYPE_CHATROOM) {
            EMClient.getInstance().chatroomManager().leaveChatRoom(toChatUsername);
        }
    }

    @Override public void onBackPressed() {
        if (mInputView.onBackPressed()) {
            finish();
            if (chatType == EaseConstant.CHATTYPE_CHATROOM) {
                EMClient.getInstance().chatroomManager().leaveChatRoom(toChatUsername);
            }
        }
    }

    /**
     * Group change listener
     */
    private class DefaultGroupChangeListener extends GroupChangeListener {
        @Override public void onUserRemoved(String s, String s1) {
            super.onUserRemoved(s, s1);
            finish();
        }

        @Override public void onGroupDestroyed(String s, String s1) {
            super.onGroupDestroyed(s, s1);
            finish();
        }
    }

    private class DefaultChatRoomChangeListener extends ChatRoomChangeListener {

    }
}
