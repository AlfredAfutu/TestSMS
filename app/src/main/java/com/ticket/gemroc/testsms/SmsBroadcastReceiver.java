package com.ticket.gemroc.testsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;


/**
 * Created by cted on 7/9/15.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {
    private String TAG = getClass().getSimpleName();
    private SmsManager SmsManager = android.telephony.SmsManager.getDefault();

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        //String data = intent.getData().toString();
        Log.i(TAG, "Bundle object is " + bundle);
        //Log.i(TAG, "Data is " + data);
        String senderNumber = null, message = null;
        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    senderNumber = currentMessage.getDisplayOriginatingAddress();

                    //String senderNum = phoneNumber;
                    message = currentMessage.getDisplayMessageBody();

                    Log.i(TAG, "Sender Number is " + senderNumber);
                    Log.i(TAG, "Message is " + message);



                    // Show alert
                    /*int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, "senderNum: "+ senderNum + ", message: " + message, duration);
                    toast.show();*/

                } // end for loop

            }

            TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            String imsi = "IMSI" + telephonyManager.getSubscriberId();

            if(senderNumber != null && message != null && imsi != null)
                makePostCallToServer(senderNumber, "767", imsi, message, context);

        } catch (Exception e) {
            Log.e(TAG, "Exception smsReceiver " +e);

        }
    }

    private void makePostCallToServer(String phoneNumber, String destinationNumber
            , String imsiNumber, String message, final Context context){
        try {
            Log.i(TAG, "Inside Server call");
            Ion.with(context)

                    .load("http://192.168.42.75:8000/")
                    .setLogging("Logs", Log.VERBOSE)
                    //.addHeader("content-type", "application/x-www-form-urlencoded")
                    .setLogging("Logs", Log.VERBOSE)
                    .setBodyParameter("from_name", phoneNumber)
                    .setBodyParameter("destination", destinationNumber)
                    .setBodyParameter("from_number", imsiNumber)
                    .setBodyParameter("body", message)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {

                            Log.i(TAG, "Message successfully sent to server");
                            Log.i(TAG, "Result: " + result);

                            Intent serverIntent = new Intent(context, ServerService.class);
                            context.startService(serverIntent);
                        }

                    });
        }catch(Exception exception){
            Log.e(TAG, "Exception is " + exception);
        }

    }
}
