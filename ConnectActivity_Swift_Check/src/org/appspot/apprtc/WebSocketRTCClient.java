/*
 * libjingle
 * Copyright 2014 Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.appspot.apprtc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.appspot.apprtc.util.LooperExecutor;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import yuan.client.ClientRoomService;
import yuan.client.SignalClient;
import yuan.client.SignalClient.SignalEvents;
import yuan.client.util.JsonUtil;
import yuan.message.SignalMsg;
import android.util.Log;

/**
 * 
 * @author lenovo
 *
 */
public class WebSocketRTCClient implements AppRTCClient, SignalEvents {
	private static final String TAG = "WSRTCClient";

	private enum ConnectionState {
		NEW, CONNECTED, CLOSED, ERROR
	};

	private final LooperExecutor executor;
	private boolean initiator;
	private SignalingEvents events;
	private ConnectionState roomState;
	// 想要连接的房间的信息，这边只会用到RoomID
	private RoomConnectionParameters connectionParameters;
	// 信令客户端
	private SignalClient signalclient;
	// 业务的主要操纵类
	private ClientRoomService service;

	public WebSocketRTCClient(SignalingEvents events, LooperExecutor executor) {
		this.events = events;
		this.executor = executor;
		roomState = ConnectionState.NEW;
		executor.requestStart();
	}

	// --------------------------------------------------------------------
	// AppRTCClient interface implementation.
	// Asynchronously connect to an AppRTC room URL using supplied connection
	// parameters, retrieves room parameters and connect to WebSocket server.
	@Override
	public void connectToRoom(RoomConnectionParameters connectionParameters) {
		this.connectionParameters = connectionParameters;
		executor.execute(new Runnable() {
			@Override
			public void run() {
				connectToRoomInternal();
			}
		});
	}

	@Override
	public void disconnectFromRoom() {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				disconnectFromRoomInternal();
			}
		});
		executor.requestStop();
	}

	// Connects to room - function runs on a local looper thread.
	/**
	 * 加入房间的内函数
	 */
	private void connectToRoomInternal() {
		roomState = ConnectionState.NEW;
		Log.i("myTag", "real ip is:" + connectionParameters.roomId);
		/**
		 * 这边加入房间
		 */
		// 初始化signalclient
		signalclient = new SignalClient(this);
		// 初始化service
		service = new ClientRoomService(signalclient.getChannel());
		// 加入房间
		service.joinRoom(connectionParameters.roomId);

	}

	// Disconnect from room and send bye messages - runs on a local looper
	// thread.
	private void disconnectFromRoomInternal() {
		Log.d(TAG, "Disconnect. Room state: " + roomState);
		if (roomState == ConnectionState.CONNECTED) {
			Log.d(TAG, "Closing room.");
			/**
			 * 这边主动离开房间
			 */
			service.leaveRoom(connectionParameters.roomId);

			service = null;

		}else if(roomState ==ConnectionState.NEW){
			Log.w("myTag", "销毁信令客户端资源");
			signalclient.disconnect();
		}
		
		roomState = ConnectionState.CLOSED;
		//销毁客户端对象以便gc回收
		if(signalclient!=null){
			signalclient=null;
		}
		
	}

	// Callback issued when room parameters are extracted. Runs on local
	// looper thread.
	private void signalingParametersReady(
			final SignalingParameters signalingParameters) {
		Log.d(TAG, "Room connection completed.");
		if (connectionParameters.loopback
				&& (!signalingParameters.initiator || signalingParameters.offerSdp != null)) {
			reportError("Loopback room is busy.");
			return;
		}
		if (!connectionParameters.loopback && !signalingParameters.initiator
				&& signalingParameters.offerSdp == null) {
			Log.w(TAG, "No offer SDP in room response.");
		}
		initiator = signalingParameters.initiator;
		roomState = ConnectionState.CONNECTED;
		events.onConnectedToRoom(signalingParameters);
	}

	// Send local offer SDP 
	@Override
	public void sendOfferSdp(final SessionDescription sdp) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				if (roomState != ConnectionState.CONNECTED) {
					reportError("Sending offer SDP in non connected state.");
					return;
				}
				/**
				 * 这边发送offer
				 */
				service.sendOffSdp(sdp.description);
				// sendPostMessage(MessageType.MESSAGE, messageUrl,
				// json.toString());
				// if (connectionParameters.loopback) {
				// // In loopback mode rename this offer to answer and route it
				// back.
				// SessionDescription sdpAnswer = new SessionDescription(
				// SessionDescription.Type.fromCanonicalForm("answer"),
				// sdp.description);
				// events.onRemoteDescription(sdpAnswer);
				// }
			}
		});
	}

	// Send local answer SDP to the other participant.
	@Override
	public void sendAnswerSdp(final SessionDescription sdp) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				// if (connectionParameters.loopback) {
				// Log.e(TAG, "Sending answer in loopback mode.");
				// return;
				// }
				/**
				 * 这边发送answer
				 */
				service.sendAnswerSdp(sdp.description);
			}
		});
	}

	// Send Ice candidate to the other participant.
	@Override
	public void sendLocalIceCandidate(final IceCandidate candidate) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				// JSONObject json = new JSONObject();
				// jsonPut(json, "type", "candidate");
				// jsonPut(json, "label", candidate.sdpMLineIndex);
				// jsonPut(json, "id", candidate.sdpMid);
				// jsonPut(json, "candidate", candidate.sdp);
				/**
				 * 发送本地candidate
				 */
				service.sendCandidate(candidate.sdpMid,
						candidate.sdpMLineIndex, candidate.sdp);
			}
		});
	}

	// --------------------------------------------------------------------
	// Helper functions.
	private void reportError(final String errorMessage) {
		Log.e(TAG, errorMessage);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				if (roomState != ConnectionState.ERROR) {
					roomState = ConnectionState.ERROR;
					events.onChannelError(errorMessage);
				}
			}
		});
	}

	// 我在这边添加自己写的代码

	/**
	 * 创建只有initiator属性的signalingparameter 必须保留 2016/4/19因为callactivity也要用
	 * 
	 * @return
	 */
	private SignalingParameters createInitiatorSignalPara(boolean isinitiator,
			List<IceCandidate> icecandidates, SessionDescription sdp) {
		Log.i("myTag", "在创建空的signalparameters");
		//write your stunserver here
		PeerConnection.IceServer iceserver = new PeerConnection.IceServer(
				"stun:127.0.0.1:3478");
		//write your turnserver here
		PeerConnection.IceServer iceserver2 = new PeerConnection.IceServer(
				"turn:127.0.0.1:3478", "username", "password");
		LinkedList<PeerConnection.IceServer> iceservers = new LinkedList<PeerConnection.IceServer>();
		iceservers.add(iceserver);
		iceservers.add(iceserver2);
		SignalingParameters para = new SignalingParameters(iceservers,
				isinitiator, null, null, null, sdp, icecandidates);

		return para;
	}

	// 这边是signalclient回调的函数
	@Override
	public void joinRoomResult(SignalMsg resultmsg) {
		int membersize = (Integer) resultmsg.getBody();
		if (membersize == 1) {
			// 传回来的房间号是1，表示该客户端是发起者
			signalingParametersReady(createInitiatorSignalPara(true, null, null));
		} else if (membersize > 1) {
			// 传回来的房间号>1,表示该客户端是参与者
			// 取得房间sdpmap的jsonstring
			String jsonSdp = (String) resultmsg.getHeader().getAttachment()
					.get("offer");
			// 取得房间candidatesmap的jsonstring
			// 2016/4/19 下面误写成offer了
			String jsoncdlist = (String) resultmsg.getHeader().getAttachment()
					.get("candidatelist");
			// 分别将它们转化成对应的map，如果只是两个人的话，Map里面只有一条内容
			// 这边为了扩展还是使用Map,等多人的时候再做修改
			Map<String, String> sdpmap = JsonUtil.toCommonMap(jsonSdp);
			Map<String, List<Map<String, String>>> candidatelistmap = JsonUtil
					.toListMap(jsoncdlist);
			String sdpString = null;
			List<IceCandidate> icecandidates = new ArrayList<IceCandidate>();
			// 这边的循环只会执行一次
			for (Map.Entry<String, String> tempmap : sdpmap.entrySet()) {
				// 取出sdp
				sdpString = tempmap.getValue();
			}
			Log.i("newTag", "************\n" + sdpString);
			// 根据sdpString构建sdp
			SessionDescription sdp = new SessionDescription(
					SessionDescription.Type.fromCanonicalForm("offer"),
					sdpString);
			for (Map.Entry<String, List<Map<String, String>>> tempmap : candidatelistmap
					.entrySet()) {
				List<Map<String, String>> templist = tempmap.getValue();
				Log.i("tesst", "getcandidatesize--:"+templist.size());
				for (Map<String, String> temp : templist) {
					// 根据Map构建candidate
					IceCandidate icecandidate = new IceCandidate(
							temp.get("id"),
							Integer.parseInt(temp.get("label")),
							temp.get("candidate"));
					icecandidates.add(icecandidate);
				}
			}
			// 2016/4/18忘记false了
			signalingParametersReady(createInitiatorSignalPara(false,
					icecandidates, sdp));

		} else {
			// 这边表示加入房间失败，或者房间人数已经满了
			events.onChannelError("加入房间失败");
		}
	}

	// 有任意客户端掉线会回调到这里
	@Override
	public void leaveRoomResult(SignalMsg resultmsg) {
		events.onClientClose((String) resultmsg.getBody());
	}

	// 其它客户端转发过来的sdpanswer
	@Override
	public void forWardAnswer(SignalMsg resultmsg) {
		String sdpString = (String) resultmsg.getBody();
		SessionDescription sdp = new SessionDescription(
				SessionDescription.Type.fromCanonicalForm("answer"), sdpString);
		// 回调保存answer
		events.onRemoteDescription(sdp);
	}

	// 其它客户端转发过来的candidate
	@Override
	public void forWardCandidate(SignalMsg resultmsg) {
		String id = (String) resultmsg.getHeader().getAttachment().get("id");
		int label = (Integer) resultmsg.getHeader().getAttachment()
				.get("label");
		String candidate = (String) resultmsg.getHeader().getAttachment()
				.get("candidate");
		IceCandidate icecandidate = new IceCandidate(id, label, candidate);
		// 回调保存candidate
		events.onRemoteIceCandidate(icecandidate);
	}

	@Override
	public void onChannelClose() {
		if (roomState != ConnectionState.CLOSED) {
			roomState = ConnectionState.CLOSED;
		}
		events.onChannelClose();
	}

}
