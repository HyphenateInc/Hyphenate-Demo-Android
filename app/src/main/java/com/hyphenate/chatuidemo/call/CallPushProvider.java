package com.hyphenate.chatuidemo.call;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.Constant;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lzan13 on 2017/5/5.
 *
 * Send the push message when offline
 */
public class CallPushProvider implements EMCallManager.EMCallPushProvider {
    @Override public void onRemoteOffline(final String username) {
        final EMMessage message = EMMessage.createTxtSendMessage("offline call", username);
        message.setAttribute(Constant.MESSAGE_ATTR_IS_CALL_PUSH, true);
        message.setAttribute("em_force_notification", "true");
        JSONObject extObj = new JSONObject();
        try {
            extObj.put("em_push_title", "Hi! I'm calling ~");
            extObj.put("extern", "push ext");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        message.setAttribute("em_apns_ext", extObj);
        message.setMessageStatusCallback(new EMCallBack() {
            @Override public void onSuccess() {
                EMClient.getInstance().chatManager().getConversation(username).removeMessage(message.getMsgId());
            }

            @Override public void onError(int i, String s) {
                EMClient.getInstance().chatManager().getConversation(username).removeMessage(message.getMsgId());
            }

            @Override public void onProgress(int i, String s) {

            }
        });
        EMClient.getInstance().chatManager().sendMessage(message);
    }
}
