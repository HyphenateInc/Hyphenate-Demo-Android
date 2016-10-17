/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hyphenate.chatuidemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.ui.chat.call.CallStatus;
import com.hyphenate.chatuidemo.ui.chat.call.VideoCallActivity;
import com.hyphenate.chatuidemo.ui.chat.call.VoiceCallActivity;
import com.hyphenate.easeui.EaseConstant;

public class CallReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
        // 判断环信是否登录成功
        if (!EMClient.getInstance().isLoggedInBefore()) {
            return;
        }

        // 呼叫方的usernmae
        String callFrom = intent.getStringExtra(EaseConstant.EXTRA_FROM);
        // 呼叫类型，有语音和视频两种
        String callType = intent.getStringExtra(EaseConstant.EXTRA_TYPE);
        // 呼叫接收方
        String callTo = intent.getStringExtra(EaseConstant.EXTRA_TO);

        // 判断下当前被呼叫的为自己的时候才启动通话界面 TODO 这个当不同appkey下相同的username时就无效了
        if (callTo.equals(EMClient.getInstance().getCurrentUser())) {
            Intent callIntent = new Intent();
            // 根据通话类型跳转到语音通话或视频通话界面
            if (callType.equals("video")) {
                callIntent.setClass(context, VideoCallActivity.class);
                // 设置当前通话类型为视频通话
                CallStatus.getInstance().setCallType(CallStatus.CALL_TYPE_VIDEO);
            } else if (callType.equals("voice")) {
                callIntent.setClass(context, VoiceCallActivity.class);
                // 设置当前通话类型为语音通话
                CallStatus.getInstance().setCallType(CallStatus.CALL_TYPE_VOICE);
            }
            // 设置 activity 启动方式
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // 设置呼叫方 username 参数
            callIntent.putExtra(EaseConstant.EXTRA_USER_ID, callFrom);
            // 设置通话为对方打来
            callIntent.putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, true);
            context.startActivity(callIntent);
        }
    }
}
