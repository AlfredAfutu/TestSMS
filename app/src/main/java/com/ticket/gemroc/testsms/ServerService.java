package com.ticket.gemroc.testsms;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * Created by cted on 7/10/15.
 */
public class ServerService extends IntentService {

     private String TAG = getClass().getSimpleName();
     ServerSocket serverSocket;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Service Started");
        try {
            serverSocket = new ServerSocket(8080);
            Log.i(TAG, "TCPServer Waiting for client on port 8080");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while(true) {
                HTTPServer httpServer = (new HTTPServer(serverSocket.accept()));
                httpServer.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*try {
            serverSocket = new ServerSocket();
            Socket socket = serverSocket.accept();
            socket.
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private class HTTPServer extends Thread{
        Socket connectedClient = null;
        BufferedReader inFromClient = null;
        DataOutputStream outToClient = null;


        public HTTPServer(Socket client) {
            connectedClient = client;
        }

        @Override
        public void run() {
            super.run();
            Log.i(TAG, "The Client " +
                    connectedClient.getInetAddress() + ":" + connectedClient.getPort() + " is connected");

            try {
                inFromClient = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
                outToClient = new DataOutputStream(connectedClient.getOutputStream());

                String requestString = inFromClient.readLine();


                Log.i(TAG, "Request String 1: " + requestString);


                StringTokenizer tokenizer = new StringTokenizer(requestString);
                String httpMethod = tokenizer.nextToken();
                String httpQueryString = tokenizer.nextToken().replace("+", " ");

                while(inFromClient.ready()){
                    Log.i(TAG, "Request String 2: " + requestString);
                    requestString = inFromClient.readLine();
                    Log.i(TAG, "Request String 3: " + requestString);
                }
                Log.i(TAG, "HTTP Method: " + httpMethod);
                Log.i(TAG, "HTTP Query String: " + httpQueryString);

                String recipientNumber = httpQueryString.substring((httpQueryString.indexOf("to")+3), (httpQueryString.indexOf("text") - 1));
                String text_message = httpQueryString.substring((httpQueryString.indexOf("text")+5), httpQueryString.length());

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(recipientNumber, null, text_message, null, null);
                Log.i(TAG, "Receipient : " + recipientNumber);
                Log.i(TAG, "Text Message: " + text_message);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
