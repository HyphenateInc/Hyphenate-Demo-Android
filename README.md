# Agora chat demo

This repository will help you learn how to use Agora chat SDK to implement a simple chat android application, like whatsapp or wechat.

With this sample app, you can:

- Login chat server
- Start a chat
- Manage conversation list
- Add contacts
- Join group chats
- Join chat rooms
- Add your contacts to your blacklist
- Send various types of messages, Such as: text, expression, picture, voice, file and so on
- Logout

## Prerequisites
* Make sure you have made the preparations mentioned in the [Agora Chat Sdk Introduction](https://hyphenateinc.github.io/android_product_overview.html).
* Prepare the development environment:
    * JDK
    * Android Studio 3.2 or later
## Run the sample project

Follow these steps to run the sample project:\
### 1. Clone the repository to your local machine.
```java
    git clone https://github.com/HyphenateInc/Hyphenate-Demo-Android.git
```

### 2. Open the Android project with Android Studio.

### 3. Configure keys.
Set your appkey applied from [Agora Developer Console](http://console.easemob.com) before calling ChatClient#init().
```java
ChatOptions options = new ChatOptions();
// Set your appkey
options.setAppKey("Your appkey");
...
//initialization
ChatClient.getInstance().init(applicationContext, options);
```
For details, see the [prerequisites](https://hyphenateinc.github.io/android_product_overview.html) in Agora Chat SDK Guide.

## Contact Us
- You can find full API document at [Document Center](https://hyphenateinc.github.io/android_product_overview.html)
- You can file bugs about this demo at [issue](https://github.com/HyphenateInc/Hyphenate-Demo-Android/issues)

## License
The MIT License (MIT).