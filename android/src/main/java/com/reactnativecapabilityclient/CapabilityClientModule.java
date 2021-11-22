package com.reactnativecapabilityclient;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import java.util.Base64;


import java.util.Set;
import java.util.List;

@ReactModule(name = CapabilityClientModule.NAME)
public class CapabilityClientModule extends ReactContextBaseJavaModule {
    public static final String NAME = "CapabilityClient";

    private final CapabilityClient capabilityClient;
    private final MessageClient messageClient;
    private final NodeClient nodeClient;

    public CapabilityClientModule(ReactApplicationContext reactContext) {
        super(reactContext);

        this.capabilityClient = Wearable.getCapabilityClient(reactContext.getApplicationContext());
        this.messageClient = Wearable.getMessageClient(reactContext.getApplicationContext());
        this.nodeClient = Wearable.getNodeClient(reactContext.getApplicationContext());
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void getConnectedNodes(Promise promise) {
      try {
        List<Node> nodes = Tasks.await(
          this.nodeClient.getConnectedNodes()
        );

        WritableMap nodeIdNameMap = Arguments.createMap();

        for (Node node : nodes) {
          nodeIdNameMap.putString(node.getId(), node.getDisplayName());
        }

        promise.resolve(nodeIdNameMap);
      } catch (Exception e) {
        promise.reject(e);
      }
    }

    @ReactMethod
    public void discoverNodes(final String capability, Promise promise) {
      try {
        CapabilityInfo info = Tasks.await(
          this.capabilityClient.getCapability(capability, CapabilityClient.FILTER_REACHABLE)
        );

        WritableMap nodeIdNameMap = Arguments.createMap();

        for (Node node : info.getNodes()) {
          nodeIdNameMap.putString(node.getId(), node.getDisplayName());
        }

        promise.resolve(nodeIdNameMap);
      } catch (Exception e) {
        promise.reject(e);
      }
    }

    @ReactMethod
    public void isReachable(final String nodeId, final String capability, Promise promise) {
      try {
        CapabilityInfo info = Tasks.await(
          this.capabilityClient.getCapability(capability, CapabilityClient.FILTER_REACHABLE)
        );

        promise.resolve(containsNodeWithId(info.getNodes(), nodeId));
      } catch (Exception e) {
        promise.reject(e);
      }
    }

    @ReactMethod
    public void addMessageListener(final String nodeId, Promise promise) {
      this.messageClient.addListener(messageEvent -> {
        if (messageEvent.getSourceNodeId() != null && messageEvent.getSourceNodeId().equals(nodeId)) {
          WritableMap payload = Arguments.createMap();
          String base64data = Base64.getEncoder().encodeToString(messageEvent.getData());

          payload.putString("nodeId", messageEvent.getSourceNodeId());
          payload.putString("path", messageEvent.getPath());
          payload.putString("data", base64data);

          this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(
            "message", payload
          );
        }
      });
    }

    @ReactMethod
    public  void addDiscoveryListener(final String capability, Promise promise) {
      this.capabilityClient.addListener(capabilityInfo -> {
        WritableMap nodeIdNameMap = Arguments.createMap();

        for (Node node : capabilityInfo.getNodes()) {
          nodeIdNameMap.putString(node.getId(), node.getDisplayName());
        }

        this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(
          "discovery", nodeIdNameMap
        );
      }, capability);

      promise.resolve(null);
    }

    @ReactMethod
    public void addReachabilityListener(final String nodeId, final String capability, Promise promise) {
      this.capabilityClient.addListener(capabilityInfo -> {
        boolean isReachable = false;
        for (Node node : capabilityInfo.getNodes()) {
          isReachable = nodeId == node.getId() || isReachable;
        }

        WritableMap payload = Arguments.createMap();

        payload.putString("nodeId", nodeId);
        payload.putBoolean("reachable", isReachable);

        this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(
          "reachable", payload
        );
      }, capability);
    }

    @ReactMethod
    public void addNearbyListener(final String nodeId, final String capability, Promise promise) {
      this.capabilityClient.addListener(capabilityInfo -> {
        boolean isNearby = false;
        for (Node node : capabilityInfo.getNodes()) {
          isNearby = (nodeId == node.getId()&& node.isNearby()) || isNearby;
        }

        WritableMap payload = Arguments.createMap();

        payload.putString("nodeId", nodeId);
        payload.putBoolean("nearby", isNearby);

        this.getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(
          "nearby", payload
        );

      }, capability);
    }

    @ReactMethod
    public void sendMessageToAllNodes(final String capability, final String messagePath, byte[] message, Promise promise) {
      try {
        CapabilityInfo info = Tasks.await(
          this.capabilityClient.getCapability(capability, CapabilityClient.FILTER_REACHABLE)
        );

        for (Node node : info.getNodes()) {
          Tasks.await(
            this.messageClient.sendMessage(node.getId(), messagePath, message)
          );
        }

        promise.resolve(0);
      } catch (Exception e) {
        promise.reject(e);
      }
    }

    @ReactMethod
    public void sendMessage(final String nodeId, final String messagePath, final String message, Promise promise) {
      try {
        byte[] byteBuffer = Base64.getDecoder().decode(new String(message).getBytes("UTF-8"));

        Integer result = Tasks.await(
          this.messageClient.sendMessage(nodeId, messagePath, byteBuffer)
        );

        promise.resolve(result);
      } catch (Exception e) {
        promise.reject(e);
      }
    }


    private boolean containsNodeWithId(final Set<Node> nodes, final String id) {
      for (Node node : nodes) {
        if (node.getId() == id) {
          return true;
        }
      }
      return false;
    }
}
