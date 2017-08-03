/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.wolfposd.tmchat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.CloseReason;

import com.github.jtendermint.crypto.ByteUtil;
import com.github.jtendermint.jabci.api.ICheckTx;
import com.github.jtendermint.jabci.api.ICommit;
import com.github.jtendermint.jabci.api.IDeliverTx;
import com.github.jtendermint.jabci.socket.TSocket;
import com.github.jtendermint.jabci.types.Types.CodeType;
import com.github.jtendermint.jabci.types.Types.RequestCheckTx;
import com.github.jtendermint.jabci.types.Types.RequestCommit;
import com.github.jtendermint.jabci.types.Types.RequestDeliverTx;
import com.github.jtendermint.jabci.types.Types.ResponseCheckTx;
import com.github.jtendermint.jabci.types.Types.ResponseCommit;
import com.github.jtendermint.jabci.types.Types.ResponseDeliverTx;
import com.github.jtmsp.websocket.Websocket;
import com.github.jtmsp.websocket.WebsocketStatus;
import com.github.jtmsp.websocket.jsonrpc.JSONRPC;
import com.github.jtmsp.websocket.jsonrpc.Method;
import com.github.jtmsp.websocket.jsonrpc.calls.StringParam;
import com.github.wolfposd.tmchat.frontend.FrontendListener;
import com.github.wolfposd.tmchat.frontend.ISendMessage;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bouncycastle.util.encoders.Hex;

public class NodeCommunication implements ICheckTx, IDeliverTx, ICommit, ISendMessage, WebsocketStatus {

    private Websocket wsClient;
    private TSocket socket;

    private Gson gson = new Gson();
    private int hashCount = 0;

    private Map<String, FrontendListener> frontends = new HashMap<>();

    public NodeCommunication() {

        wsClient = new Websocket(this);


        socket = new TSocket();
        socket.registerListener(this);
        new Thread(socket::start).start();
        System.out.println("Started TMSP Socket");

        // wait 10 seconds before connecting the websocket
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        executorService.schedule(() -> reconnectWS(), 10, TimeUnit.SECONDS);
    }

    private void reconnectWS() {
        System.out.println("Trying to connect to Websocket...");
        try {
            wsClient.reconnectWebsocket();
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    public void registerFrontend(String username, FrontendListener f) {
        frontends.put(username, f);
    }

    @Override
    public ResponseDeliverTx receivedDeliverTx(RequestDeliverTx req) {

//        String s = new String(req.getTx().toByteArray());//new String(req.getTx().toByteArray())

        //byte[] bts = req.getTx().toByteArray();
//        for(byte b:bts){
//
//            System.out.println("byte "+new String("0xec"));
//        }

        //Message msg = new Message("Left","Right","hello");

        //System.out.println(ByteUtil.toString00(req.getTx().toByteArray()));
        // Message msg2 = gson.fromJson(new String(req.getTx().toByteArray()), Message.class);
        //System.out.println("msg : "+msg2.toString());
        System.out.println("got deliver tx, with" + TSocket.byteArrayToString(req.getTx().toByteArray()));

        byte[] tx =req.getTx().toByteArray();

        System.out.println("got deliver tx, with" +new String(tx));



        // String test ="{\"Message\": {\"sender\" :Left, \"receiver\" :Right, \"message\" :a, \"timestamp\" :1501733610660}}";
        // String test ="{\"sender\" :Left, \"receiver\" :Right, \"message\" :a, \"timestamp\" :1501733610660}";

        // Message mtest = gson.fromJson(test,Message.class);
        //验证 gson.fromJson 可用
//        String sMsg =msg.toString();
//        Message test = gson.fromJson(sMsg,Message.class);
        // System.out.println("test sender : "+mtest.sender);


        // System.out.println(msg.toString());
        Message msg = gson.fromJson(new String(tx) ,Message.class);
        FrontendListener l = frontends.get(msg.receiver);
        System.out.println("receiver "+ msg.receiver);
        if (l != null) {
            l.messageIncoming(msg);
        }

        return ResponseDeliverTx.newBuilder().setCode(CodeType.OK).build();
    }

    @Override
    public ResponseCheckTx requestCheckTx(RequestCheckTx req) {
        return ResponseCheckTx.newBuilder().setCode(CodeType.OK).build();
    }

    @Override
    public ResponseCommit requestCommit(RequestCommit requestCommit) {
        hashCount += 1;


        ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE);
        buf.putInt(hashCount);
        return ResponseCommit.newBuilder().setCode(CodeType.OK).setData(ByteString.copyFrom(buf)).build();
    }

    @Override
    public void sendMessage(Message m) {
        //System.out.println(Charset.defaultCharset().name());
//        final byte[] byteData = m.toString().getBytes();
//        System.out.println("sendMessage" +  byteData);
//        //JSONRPC rpc = new StringParam(Method.BROADCAST_TX_ASYNC, gson.toJson(m).getBytes());
//        //JSONRPC rpc = new NewStringParam(Method.BROADCAST_TX_COMMIT, gson.toJson(m).getBytes());
//        JSONRPC rpc = new StringParam(Method.BROADCAST_TX_COMMIT, byteData);
//
//        System.out.println("GSON "+ gson.toJson(rpc));
//        wsClient.sendMessage(rpc, e -> {
//            // no interest
//            System.out.println("error send message");
//        });


        byte[] byteData = m.toString().getBytes();
        JSONRPC rpc = new StringParam(Method.BROADCAST_TX_COMMIT,  m.toString().getBytes());
        System.out.println("GSON "+ gson.toJson(rpc));
        System.out.println("Send data: "+ Hex.toHexString(byteData));
//        wsClient.sendMessage(rpc, e -> {
//            // no interest
//        });

        try {

            sendHTTPRequest(Hex.toHexString(byteData));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void wasClosed(CloseReason cr) {
        if (!"Manual Close".equals(cr.getReasonPhrase())) {
            System.out.println("Websocket closed... reconnecting");
            reconnectWS();
        }
    }

    /*
    * CURL 函数
    * 发送 HTTP GET请求
    * */

    public void sendHTTPRequest(String data)
            throws  IOException {
        String url = "http://localhost:46657/broadcast_tx_commit?tx=0x"+data;
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        HttpResponse response = client.execute(request);
        System.out.println("Response Code: " + response.getStatusLine().getStatusCode());


    }

}
