package io.agora.chatdemo.chat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;

import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.agora.Error;
import io.agora.MessageListener;
import io.agora.ValueCallBack;
import io.agora.chat.ChatRoom;
import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chat.Group;
import io.agora.chat.ChatMessage;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.bus.BusEvent;
import io.agora.chatdemo.bus.LiveDataBus;
import io.agora.chatdemo.chatroom.AgoraChatRoomChangeListener;
import io.agora.chatdemo.chatroom.ChatRoomDetailsActivity;
import io.agora.chatdemo.group.AgoraGroupChangeListener;
import io.agora.chatdemo.group.GroupDetailsActivity;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.chatdemo.ui.MapsCurrentPlaceActivity;
import io.agora.chatdemo.ui.widget.ChatInputView;
import io.agora.chatdemo.ui.widget.VoiceRecordDialog;
import io.agora.chatdemo.ui.widget.VoiceRecordView;
import io.agora.chatdemo.ui.widget.chatrow.ChatRowCall;
import io.agora.chatdemo.user.model.UserEntity;
import io.agora.easeui.EaseConstant;
import io.agora.easeui.utils.EaseCommonUtils;
import io.agora.easeui.utils.EaseCompat;
import io.agora.easeui.utils.EaseFileUtils;
import io.agora.easeui.utils.Utils;
import io.agora.easeui.widget.EaseChatExtendMenu;
import io.agora.easeui.widget.EaseMessageListView;
import io.agora.easeui.widget.chatrow.EaseChatRow;
import io.agora.easeui.widget.chatrow.EaseCustomChatRowProvider;
import io.agora.util.EMLog;
import io.agora.util.PathUtil;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.agora.util.VersionUtils;

import static io.agora.easeui.EaseConstant.CHATTYPE_CHATROOM;
import static io.agora.easeui.EaseConstant.CHATTYPE_GROUP;
import static io.agora.easeui.EaseConstant.CHATTYPE_SINGLE;

/**
 * Chat with someone in this activity
 */
public class ChatActivity extends BaseActivity {

    private String TAG = this.getClass().getSimpleName();
    @BindView(R.id.input_view) ChatInputView mInputView;
    @BindView(R.id.message_list) EaseMessageListView mMessageListView;
    @BindView(R.id.pb_loading_message) ProgressBar mLoadingProgressBar;

    // Group change listener
    private DefaultAgoraGroupChangeListener groupChangeListener;
    private DefaultAgoraChatRoomChangeListener chatRoomChangeListener;

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
            R.string.attach_take_pic, R.string.attach_picture, /*R.string.attach_location,*/ R.string.attach_file
    };
    protected int[] itemdrawables = {
            R.drawable.ease_chat_takepic_selector, R.drawable.ease_chat_image_selector, /*R.drawable.ease_chat_location_selector,*/
            R.drawable.em_chat_file_selector
    };
    protected int[] itemIds = { ITEM_TAKE_PICTURE, ITEM_PICTURE, /*ITEM_LOCATION,*/ ITEM_FILE };

    protected File mCameraFile;

    /**
     * to chat user id or group id
     */
    protected String toChatUsername;

    /**
     * chat type, single chat or group chat
     */
    protected int chatType;

    protected Conversation mConversation;
    private ChatMessage mToDeleteMessage;

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
            groupChangeListener = new DefaultAgoraGroupChangeListener();
            ChatClient.getInstance().groupManager().addGroupChangeListener(groupChangeListener);
        } else if (chatType == CHATTYPE_CHATROOM) {
            chatRoomChangeListener = new DefaultAgoraChatRoomChangeListener();
            ChatClient.getInstance().chatroomManager().addChatRoomChangeListener(chatRoomChangeListener);
            initChatRoom();
        }
        // received messages code in onResume() method
    }

    private void initView() {
        MyItemClickListener extendMenuItemClickListener = new MyItemClickListener();
        for (int i = 0; i < itemStrings.length; i++) {
            mInputView.registerExtendMenuItem(itemStrings[i], itemdrawables[i], itemIds[i], extendMenuItemClickListener);
        }
//        if (chatType == CHATTYPE_SINGLE) {
//            mInputView.registerExtendMenuItem(R.string.attach_voice_call, R.drawable.em_chat_voice_call_selector, ITEM_VOICE_CALL,
//                    extendMenuItemClickListener);
//            mInputView.registerExtendMenuItem(R.string.attach_video_call, R.drawable.em_chat_video_call_selector, ITEM_VIDEO_CALL,
//                    extendMenuItemClickListener);
//        }
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

        LiveDataBus.get().with(BusEvent.REFRESH_GROUP, Boolean.class).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean != null) {
                    setToolbarTitle();
                }
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
            Group group = ChatClient.getInstance().groupManager().getGroup(toChatUsername);
            if (group != null) {
                nick = group.getGroupName();
            }
        } else if (chatType == EaseConstant.CHATTYPE_CHATROOM) { // chatroom
            ChatRoom chatRoom = ChatClient.getInstance().chatroomManager().getChatRoom(toChatUsername);
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
        mConversation = ChatClient.getInstance()
                .chatManager()
                .getConversation(toChatUsername, EaseCommonUtils.getConversationType(chatType), true);
        mConversation.markAllMessagesAsRead();
        // the number of messages loaded into mConversation is getChatOptions().getNumberOfMessagesLoaded
        // you can change this number
        final List<ChatMessage> msgs = mConversation.getAllMessages();
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
            @Override public void onResendClick(ChatMessage message) {
                resendMessage(message);
            }

            @Override public boolean onBubbleClick(ChatMessage message) {
                // override you want click groupChangeListener and return true
                return false;
            }

            @Override public void onBubbleLongClick(ChatMessage message) {
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
        ChatClient.getInstance().chatroomManager().joinChatRoom(toChatUsername, new ValueCallBack<ChatRoom>() {

            @Override public void onSuccess(final ChatRoom chatRoom) {
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
                        switch (error) {
                            case Error.CHATROOM_INVALID_ID:
                                Toast.makeText(activityInstance, "Invalid chatroom id", Toast.LENGTH_LONG).show();
                                break;
                            case Error.CHATROOM_PERMISSION_DENIED:
                                Toast.makeText(activityInstance, "user has no permission for the operation", Toast.LENGTH_LONG)
                                        .show();
                                break;
                            case Error.CHATROOM_MEMBERS_FULL:
                                Toast.makeText(activityInstance, "chat room members full", Toast.LENGTH_LONG).show();
                                break;
                        }
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
                    final List<ChatMessage> messages;
                    ChatMessage firstMsg = mConversation.getAllMessages().get(0);
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
                        //startVideoCall();
                        break;
                    case R.id.menu_voice_call:
                        //startVoiceCall();
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
        ChatMessage message = ChatMessage.createTxtSendMessage(content, toChatUsername);

        // send message
        sendMessage(message);
    }

    protected void sendVoiceMessage(String filePath, int length) {
        ChatMessage message = ChatMessage.createVoiceSendMessage(filePath, length, toChatUsername);
        sendMessage(message);
    }

    protected void sendImageMessage(String imagePath) {
        ChatMessage message = ChatMessage.createImageSendMessage(imagePath, false, toChatUsername);
        sendMessage(message);
    }

    protected void sendImageMessage(Uri imagePath) {
        ChatMessage message = ChatMessage.createImageSendMessage(imagePath, false, toChatUsername);
        sendMessage(message);
    }

    protected void sendLocationMessage(double latitude, double longitude, String locationAddress) {
        ChatMessage message = ChatMessage.createLocationSendMessage(latitude, longitude, locationAddress, toChatUsername);
        sendMessage(message);
    }

    protected void sendVideoMessage(String videoPath, String thumbPath, int videoLength) {
        ChatMessage message = ChatMessage.createVideoSendMessage(videoPath, thumbPath, videoLength, toChatUsername);
        sendMessage(message);
    }

    protected void sendFileMessage(String filePath) {
        ChatMessage message = ChatMessage.createFileSendMessage(filePath, toChatUsername);
        sendMessage(message);
    }

    protected void sendFileMessage(Uri filePath) {
        ChatMessage message = ChatMessage.createFileSendMessage(filePath, toChatUsername);
        sendMessage(message);
    }

    protected void sendMessage(ChatMessage message) {

        if (message == null) {
            return;
        }

        onSetMessageAttributes(message);

        if (chatType == EaseConstant.CHATTYPE_GROUP) {
            message.setChatType(ChatMessage.ChatType.GroupChat);
        } else if (chatType == EaseConstant.CHATTYPE_CHATROOM) {
            message.setChatType(ChatMessage.ChatType.ChatRoom);
        }

        // send message
        ChatClient.getInstance().chatManager().sendMessage(message);

        // refresh ui
        mMessageListView.refreshSelectLast();
    }

    public void resendMessage(ChatMessage message) {
        message.setStatus(ChatMessage.Status.CREATE);
        ChatClient.getInstance().chatManager().sendMessage(message);
        mMessageListView.refresh();
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
                ChatClient.getInstance().getCurrentUser() + System.currentTimeMillis() + ".jpg");
        mCameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, EaseCompat.getUriForFile(this, mCameraFile)),
                REQUEST_CODE_CAMERA);
    }

    /**
     * select local image
     */
    protected void selectPicFromLocal() {
        EaseCompat.openImage(this, REQUEST_CODE_LOCAL);
    }

    /**
     * select a file
     */
    protected void selectFileFromLocal() {
        Intent intent = new Intent();
        if(VersionUtils.isTargetQ(this)) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }else {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.setAction(Intent.ACTION_GET_CONTENT);
            }else {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }

    protected void selectLocation() {
        MapsCurrentPlaceActivity.actionStartForResult(this, REQUEST_CODE_MAP);
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) { // capture new image
                onActivityResultForCamera(data);
            } else if (requestCode == REQUEST_CODE_LOCAL) { // send local image
                onActivityResultForLocalPhotos(data);
            } else if (requestCode == REQUEST_CODE_MAP) { // location
                onActivityResultForLocation(data);
            } else if (requestCode == REQUEST_CODE_SELECT_FILE) { //send the file
                onActivityResultForLocalFiles(data);
            }
        }
    }

    private void onActivityResultForLocation(Intent data) {
        double latitude = data.getDoubleExtra("lat", 0);
        double longitude = data.getDoubleExtra("lon", 0);
        String locationAddress = data.getStringExtra("address");
        if(TextUtils.isEmpty(locationAddress)) {
            locationAddress = "sample location";
        }

        if (!TextUtils.isEmpty(locationAddress) && latitude != 0 && longitude != 0) {
            sendLocationMessage(latitude, longitude, locationAddress);
        } else {
            Toast.makeText(this, R.string.unable_to_get_location, Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResultForCamera(Intent data) {
        if (mCameraFile != null && mCameraFile.exists()) {
            sendImageMessage(mCameraFile.getAbsolutePath());
        }
    }

    protected void onActivityResultForLocalPhotos(@Nullable Intent data) {
        if (data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                String filePath = EaseFileUtils.getFilePath(this, selectedImage);
                if(!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                    sendImageMessage(Uri.parse(filePath));
                }else {
                    EaseFileUtils.saveUriPermission(this, selectedImage, data);
                    sendImageMessage(selectedImage);
                }
            }
        }
    }

    protected void onActivityResultForLocalFiles(@Nullable Intent data) {
        if (data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String filePath = EaseFileUtils.getFilePath(this, uri);
                if(!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                    sendFileMessage(Uri.parse(filePath));
                }else {
                    EaseFileUtils.saveUriPermission(this, uri, data);
                    sendFileMessage(uri);
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
                    selectLocation();
                    break;
                //case ITEM_VIDEO:
                //    Intent intent = new Intent(chatType.this, ImageGridActivity.class);
                //    startActivityForResult(intent, REQUEST_CODE_SELECT_VIDEO);
                //    break;
                case ITEM_FILE: //file
                    selectFileFromLocal();
                    break;
                case ITEM_VOICE_CALL:
                    //startVoiceCall();
                    break;
                case ITEM_VIDEO_CALL:
                    //startVideoCall();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * create a chat row provider
     */
    private EaseCustomChatRowProvider newCustomChatRowProvider() {
        return new EaseCustomChatRowProvider() {
            @Override public int getCustomChatRowTypeCount() {
                return 2;
            }

            @Override public int getCustomChatRowType(ChatMessage message) {
                if (message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_VIDEO_CALL, false) || message.getBooleanAttribute(
                        EaseConstant.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    return message.direct() == ChatMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_CALL : MESSAGE_TYPE_SENT_CALL;
                }
                return 0;
            }

            @Override public EaseChatRow getCustomChatRow(ChatMessage message, int position, BaseAdapter adapter) {
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
    protected void onSetMessageAttributes(ChatMessage message) {

    }

    MessageListener mMessageListener = new MessageListener() {
        @Override public void onMessageReceived(List<ChatMessage> list) {
            for (ChatMessage message : list) {
                String username = null;
                // group message
                if (message.getChatType() == ChatMessage.ChatType.GroupChat
                        || message.getChatType() == ChatMessage.ChatType.ChatRoom) {
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

        @Override public void onCmdMessageReceived(List<ChatMessage> list) {
            //cmd messages do not save to the cache in sdk
        }

        @Override public void onMessageRead(List<ChatMessage> list) {
            mMessageListView.refresh();
        }

        @Override public void onMessageDelivered(List<ChatMessage> list) {
            mMessageListView.refresh();
        }

        @Override public void onMessageRecalled(List<ChatMessage> messages) {
            mMessageListView.refresh();
        }

        @Override public void onMessageChanged(ChatMessage emMessage, Object o) {
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
            ChatClient.getInstance().chatManager().addMessageListener(mMessageListener);
        }
        mMessageListView.refresh();
    }

    @Override protected void onStop() {
        super.onStop();
        if (mMessageListener != null) {
            // unregister this event groupChangeListener when this activity enters the background
            ChatClient.getInstance().chatManager().removeMessageListener(mMessageListener);
        }
        // remove activity from foreground activity list
        DemoHelper.getInstance().popActivity(this);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (groupChangeListener != null) {
            ChatClient.getInstance().groupManager().removeGroupChangeListener(groupChangeListener);
        }
        if (chatRoomChangeListener != null) {
            ChatClient.getInstance().chatroomManager().removeChatRoomListener(chatRoomChangeListener);
        }
        if (chatType == EaseConstant.CHATTYPE_CHATROOM) {
            ChatClient.getInstance().chatroomManager().leaveChatRoom(toChatUsername);
        }
    }

    @Override public void onBackPressed() {
        if (mInputView.onBackPressed()) {
            finish();
            if (chatType == EaseConstant.CHATTYPE_CHATROOM) {
                ChatClient.getInstance().chatroomManager().leaveChatRoom(toChatUsername);
            }
        }
    }

    /**
     * Group change listener
     */
    private class DefaultAgoraGroupChangeListener extends AgoraGroupChangeListener {
        @Override public void onUserRemoved(String s, String s1) {
            super.onUserRemoved(s, s1);
            finish();
        }

        @Override public void onGroupDestroyed(String s, String s1) {
            super.onGroupDestroyed(s, s1);
            finish();
        }

        @Override
        public void onWhiteListAdded(String groupId, List<String> whitelist) {

        }

        @Override
        public void onWhiteListRemoved(String groupId, List<String> whitelist) {

        }

        @Override
        public void onAllMemberMuteStateChanged(String groupId, boolean isMuted) {

        }

        @Override public void onOwnerChanged(String groupId, String newOwner, String oldOwner) {
            super.onOwnerChanged(groupId, newOwner, oldOwner);
        }

        @Override public void onMemberJoined(String groupId, final String member) {
            super.onMemberJoined(groupId, member);
        }
    }

    private class DefaultAgoraChatRoomChangeListener extends AgoraChatRoomChangeListener {
        @Override public void onChatRoomDestroyed(String roomId, String roomName) {
            super.onChatRoomDestroyed(roomId, roomName);
            finish();
        }

        @Override public void onMemberJoined(String roomId, final String participant) {
            super.onMemberJoined(roomId, participant);
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    Toast.makeText(activityInstance, participant + " joined", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override public void onMemberExited(String roomId, String roomName, final String participant) {
            super.onMemberExited(roomId, roomName, participant);
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    Toast.makeText(activityInstance, participant + " exited", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onRemovedFromChatRoom(int reason, String roomId, String roomName, String participant) {
            super.onRemovedFromChatRoom(reason, roomId, roomName, participant);
            finish();
        }

        @Override public void onMuteListAdded(String chatRoomId, final List<String> mutes, long expireTime) {
            super.onMuteListAdded(chatRoomId, mutes, expireTime);
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    Toast.makeText(activityInstance, mutes.get(0) + " is muted", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override public void onMuteListRemoved(String chatRoomId, final List<String> mutes) {
            super.onMuteListRemoved(chatRoomId, mutes);
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    Toast.makeText(activityInstance, mutes.get(0) + " is unmuted", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override public void onAdminAdded(String chatRoomId, final String admin) {
            super.onAdminAdded(chatRoomId, admin);
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    Toast.makeText(activityInstance, admin + "  administrator privileges are added", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override public void onAdminRemoved(String chatRoomId, final String admin) {
            super.onAdminRemoved(chatRoomId, admin);
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    Toast.makeText(activityInstance, admin + " administrator privileges are canceled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override public void onOwnerChanged(String chatRoomId, final String newOwner, final String oldOwner) {
            super.onOwnerChanged(chatRoomId, newOwner, oldOwner);
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    Toast.makeText(activityInstance, oldOwner + " transferred ownership to " + newOwner, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
