/************************************************************
 *  * Hyphenate CONFIDENTIAL 
 * __________________ 
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved. 
 *  
 * NOTICE: All information contained herein is, and remains 
 * the property of Hyphenate Inc.
 * Dissemination of this information or reproduction of this material 
 * is strictly forbidden unless prior written permission is obtained
 * from Hyphenate Inc.
 */
package com.hyphenate.chatuidemo.chat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseUI;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.EasyUtils;

import java.util.HashSet;
import java.util.List;

/**
 * new message notifier class
 * 
 * this class is subject to be inherited and implement the relative APIs
 */
public class MessageNotifier {
    private final static String TAG = "notify";
    private Ringtone ringtone = null;

    private final static String[] msg_eng = { "sent a message", "sent a picture", "sent a voice",
                                                "sent location message", "sent a video", "sent a file", "%1 contacts sent %2 messages"
                                              };

    private static int notifyID = 0525; // start notification id

    private NotificationManager notificationManager = null;

    private HashSet<String> fromUsers = new HashSet<String>();
    private int notificationNum = 0;

    private Context appContext;
    private String packageName;
    private String[] msgs;
    private long lastNotifyTime;
    private AudioManager audioManager;
    private Vibrator vibrator;
    private EaseNotificationInfoProvider notificationInfoProvider;

    public MessageNotifier() {
    }
    
    /**
     * this function can be override
     * @param context
     * @return
     */
    public MessageNotifier init(Context context){
        appContext = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        packageName = appContext.getApplicationInfo().packageName;
        msgs = msg_eng;

        audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
        
        return this;
    }
    
    /**
     * this function can be override
     */
    public void reset(){
        resetNotificationCount();
        cancelNotification();
    }

    void resetNotificationCount() {
        notificationNum = 0;
        fromUsers.clear();
    }
    
    void cancelNotification() {
        if (notificationManager != null)
            notificationManager.cancel(notifyID);
    }

    /**
     * handle the new message
     * this function can be override
     * 
     * @param message
     */
    public synchronized void onNewMsg(EMMessage message) {
        EaseUI.EaseSettingsProvider settingsProvider = EaseUI.getInstance().getSettingsProvider();
        if(!settingsProvider.isMsgNotifyAllowed(message)){
            return;
        }

        // check if app running background
        if (!EasyUtils.isAppRunningForeground(appContext)) {
            EMLog.d(TAG, "app is running in background");
            sendNotification(message, false);
        } else {
            sendNotification(message, true);

        }

        vibrateAndPlayTone(message);
    }
    
    public synchronized void onNewMsg(List<EMMessage> messages) {
        EaseUI.EaseSettingsProvider settingsProvider = EaseUI.getInstance().getSettingsProvider();
        if(!settingsProvider.isMsgNotifyAllowed(null)){
            return;
        }
        // check if app running background
        if (!EasyUtils.isAppRunningForeground(appContext)) {
            EMLog.d(TAG, "app is running in background");
            sendNotification(messages, false);
        } else {
            sendNotification(messages, true);
        }
        vibrateAndPlayTone(messages.get(messages.size()-1));
    }

    public synchronized void onNewMsg(String alert){
        notify(false, alert, null, null, alert, 0);
        vibrateAndPlayTone(null);
    }

    /**
     * send it to notification bar
     * This can be override by subclass to provide customer implementation
     * @param messages
     * @param isForeground
     */
    private void sendNotification(List<EMMessage> messages, boolean isForeground){
        for(EMMessage message : messages){
            if(!isForeground){
                notificationNum++;
                fromUsers.add(message.getFrom());
            }
        }
        sendNotification(messages.get(messages.size()-1), isForeground, false);
    }
    
    private void sendNotification(EMMessage message, boolean isForeground){
        sendNotification(message, isForeground, true);
    }
    
    /**
     * send it to notification bar
     * This can be override by subclass to provide customer implementation
     * @param message
     */
    private void sendNotification(EMMessage message, boolean isForeground, boolean numIncrease) {
        String username = message.getFrom();
        try {
            String notifyText = username + " ";
            switch (message.getType()) {
            case TXT:
                notifyText += msgs[0];
                break;
            case IMAGE:
                notifyText += msgs[1];
                break;
            case VOICE:
                notifyText += msgs[2];
                break;
            case LOCATION:
                notifyText += msgs[3];
                break;
            case VIDEO:
                notifyText += msgs[4];
                break;
            case FILE:
                notifyText += msgs[5];
                break;
            }
            
            PackageManager packageManager = appContext.getPackageManager();

            // set notification title
            String contentTitle = (String) packageManager.getApplicationLabel(appContext.getApplicationInfo());
            if (notificationInfoProvider != null) {
                String customNotifyText = notificationInfoProvider.getDisplayedText(message);
                String customContentTitle = notificationInfoProvider.getTitle(message);
                if (customNotifyText != null){
                    notifyText = customNotifyText;
                }
                    
                if (customContentTitle != null){
                    contentTitle = customContentTitle;
                }   
            }

            Intent msgIntent = appContext.getPackageManager().getLaunchIntentForPackage(packageName);
            if (notificationInfoProvider != null) {
                msgIntent = notificationInfoProvider.getLaunchIntent(message);
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(appContext, notifyID, msgIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            if(numIncrease){
                // prepare latest event info section
                if(!isForeground){
                    notificationNum++;
                    fromUsers.add(message.getFrom());
                }
            }

            int fromUsersNum = fromUsers.size();
            String summaryBody = msgs[6].replaceFirst("%1", Integer.toString(fromUsersNum)).replaceFirst("%2",
                    Integer.toString(notificationNum));

            int smallIcon = 0;
            if (notificationInfoProvider != null) {
                // latest text
                String customSummaryBody = notificationInfoProvider.getLatestText(message, fromUsersNum,notificationNum);
                if (customSummaryBody != null){
                    summaryBody = customSummaryBody;
                }
                
                // small icon
                smallIcon = notificationInfoProvider.getSmallIcon(message);
            }
            notify(isForeground, notifyText, contentTitle, pendingIntent, summaryBody, smallIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notify(boolean isForeground, String notifyText, String contentTitle,
            PendingIntent pendingIntent, String summaryBody, int smallIcon) {
        PackageManager packageManager = appContext.getPackageManager();

        if(contentTitle == null){
            contentTitle = (String) packageManager.getApplicationLabel(appContext.getApplicationInfo());
        }
        if(pendingIntent == null) {
            Intent msgIntent = appContext.getPackageManager().getLaunchIntentForPackage(packageName);
            pendingIntent = PendingIntent.getActivity(appContext, notifyID, msgIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // create and send notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(appContext)
                .setSmallIcon(appContext.getApplicationInfo().icon)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);

        mBuilder.setContentTitle(contentTitle);
        mBuilder.setTicker(notifyText);
        mBuilder.setContentText(summaryBody);
        mBuilder.setContentIntent(pendingIntent);
        if (smallIcon != 0){
            mBuilder.setSmallIcon(smallIcon);
        }
        // mBuilder.setNumber(notificationNum);
        Notification notification = mBuilder.build();

        if (isForeground) {
            int foregroundNotifyID = 0555;
            notificationManager.notify(foregroundNotifyID, notification);
            notificationManager.cancel(foregroundNotifyID);
        } else {
            notificationManager.notify(notifyID, notification);
        }
    }

    /**
     * vibrate and  play tone
     */
    public void vibrateAndPlayTone(EMMessage message) {
        if (System.currentTimeMillis() - lastNotifyTime < 1000) {
            // received new messages within 2 seconds, skip play ringtone
            return;
        }
        
        try {
            lastNotifyTime = System.currentTimeMillis();
            
            // check if in silent mode
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                EMLog.e(TAG, "in silent mode now");
                return;
            }
            EaseUI.EaseSettingsProvider settingsProvider = EaseUI.getInstance().getSettingsProvider();
            if(settingsProvider.isMsgVibrateAllowed(message)){
                long[] pattern = new long[] { 0, 180, 80, 120 };
                vibrator.vibrate(pattern, -1);
            }

            if(settingsProvider.isMsgSoundAllowed(message)){
                if (ringtone == null) {
                    Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    ringtone = RingtoneManager.getRingtone(appContext, notificationUri);
                    if (ringtone == null) {
                        EMLog.d(TAG, "can't find ringtone at: " + notificationUri.getPath());
                        return;
                    }
                }
                
                if (!ringtone.isPlaying()) {
                    String vendor = Build.MANUFACTURER;
                    
                    ringtone.play();
                    // for samsung S3, we encounter a bug that the phone will continue ringing without stop
                    // add the following handler to stop it after 3s if needed
                    if (vendor != null && vendor.toLowerCase().contains("samsung")) {
                        Thread ctlThread = new Thread() {
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                    if (ringtone.isPlaying()) {
                                        ringtone.stop();
                                    }
                                } catch (Exception e) {
                                }
                            }
                        };
                        ctlThread.run();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * set notification info Provider
     * 
     * @param provider
     */
    public void setNotificationInfoProvider(EaseNotificationInfoProvider provider) {
        notificationInfoProvider = provider;
    }

    public interface EaseNotificationInfoProvider {
        /**
         * set the notification content, such as "you received a new image from xxx"
         * 
         * @param message
         * @return null-will use the default text
         */
        String getDisplayedText(EMMessage message);

        /**
         * set the notification content: such as "you received 5 message from 2 contacts"
         * 
         * @param message
         * @param fromUsersNum- number of message sender
         * @param messageNum -number of messages
         * @return null-will use the default text
         */
        String getLatestText(EMMessage message, int fromUsersNum, int messageNum);

        /**
         * set notification title
         * 
         * @param message
         * @return null- will use the default text
         */
        String getTitle(EMMessage message);

        /**
         * set the small icon
         * 
         * @param message
         * @return 0- will use the default icon
         */
        int getSmallIcon(EMMessage message);

        /**
         * set the intent when notification is pressed
         * 
         * @param message
         * @return null- will use the default icon
         */
        Intent getLaunchIntent(EMMessage message);
    }
}
