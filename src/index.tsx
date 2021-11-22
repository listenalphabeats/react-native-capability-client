import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-capability-client' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const CapabilityClient = NativeModules.CapabilityClient
  ? NativeModules.CapabilityClient
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function getConnectedNodes(): Promise<Record<string, string>> {
  return CapabilityClient.getConnectedNodes();
}

export function sendMessage(
  id: string,
  path: string,
  message: string
): Promise<Record<string, string>> {
  return CapabilityClient.sendMessage(id, path, message);
}

export function addMessageListener(nodeId: string): Promise<void> {
  return CapabilityClient.addMessageListener(nodeId);
}

export function discoverNodes(
  capability: string
): Promise<Record<string, string>> {
  return CapabilityClient.discoverNodes(capability);
}

export function addDiscoveryListener(capability: string): Promise<void> {
  return CapabilityClient.addDiscoveryListener(capability);
}

export function isReachable(
  nodeId: string,
  capability: string
): Promise<boolean> {
  return CapabilityClient.isReachable(nodeId, capability);
}
