package com.adam.tastylog.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.adam.tastylog.R
//import com.adam.tastylog.model.FcmToken
import com.adam.tastylog.ui.activity.MainActivity
import com.adam.tastylog.utils.RetrofitBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
//import com.google.firebase.messaging.FirebaseMessagingService
//import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//class MyFirebaseMessagingService : FirebaseMessagingService() {
//
//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//        Log.d("FCM", "New token: $token")
//        saveTokenToLocal(token)
//    }
//
//    private fun saveTokenToLocal(token: String) {
////        sendTokenToServer(token)
//        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
//        with(sharedPreferences.edit()) {
//            putString("fcmToken", token)
//            apply()
//        }
//    }
//}




//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//        // 메시지 처리 로직 구현
//        if (remoteMessage.notification != null) {
//            sendNotification(remoteMessage.notification!!.title, remoteMessage.notification!!.body)
//        }
//    }
//
//    private fun sendNotification(title: String?, messageBody: String?) {
//        // 알림 관련 코드
//        // ...
//    }
//}



//    private fun sendNotification(title: String?, messageBody: String?) {
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val channelId = "your_channel_id"
//
//        // Android Oreo 이상에서는 채널이 필요합니다.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channelName = "Your Channel Name"
//            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        // 알림을 눌렀을 때 실행될 인텐트 설정
//        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
//
//        // 알림 빌더를 사용해 알림 설정
//        val notificationBuilder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification) // 알림 아이콘 설정
//            .setContentTitle(title) // 알림 제목
//            .setContentText(messageBody) // 알림 내용
//            .setAutoCancel(true) // 터치 시 자동으로 닫힘
//            .setContentIntent(pendingIntent) // 알림을 눌렀을 때의 인텐트
//
//        // 알림 표시
//        notificationManager.notify(0, notificationBuilder.build())
//    }


//    private fun sendTokenToServer(token: String) {
//        val call = RetrofitBuilder.restaurantService.sendTokenToServer(FcmToken(token))
//        call.enqueue(object : Callback<Void> {
//            override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                if (response.isSuccessful) {
//                    Log.d("FCM", "Token sent successfully")
//                } else {
//                    Log.e("FCM", "Failed to send token")
//                }
//            }
//
//            override fun onFailure(call: Call<Void>, t: Throwable) {
//                Log.e("FCM", "Error sending token: ${t.message}")
//            }
//        })
//    }

//}
