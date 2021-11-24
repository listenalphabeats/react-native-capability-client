import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  NativeEventEmitter,
  NativeModules,
  Button,
} from 'react-native';
import {
  getConnectedNodes,
  sendMessage,
  addDiscoveryListener,
  addMessageListener,
  isReachable,
  getAllCapabilities,
  hasNodeWithCapability,
  addReachabilityListener,
  isNearby,
} from '@alphabeats/react-native-capability-client';

export default function App() {
  const [result, setResult] = React.useState<
    Record<string, string> | undefined
  >();
  const [capabilities, setCapabilities] = React.useState<any>({});
  const [reachable, setReachable] = React.useState<boolean | undefined>();

  let eventListener = null;
  let messageListener = null;

  React.useEffect(() => {
    const a = async () => {
      const nodes = await getConnectedNodes();
      setResult(nodes);
    };

    a();

    const eventEmitter = new NativeEventEmitter(NativeModules.ToastExample);
    eventListener = eventEmitter.addListener('discovery', (event) => {
      console.log('DISCOVERY', event); // "someValue"
    });
    eventListener = eventEmitter.addListener('reachable', (event) => {
      console.log('REACHABILITY', event); // "someValue"
    });
    messageListener = eventEmitter.addListener('message', (event) => {
      console.log('MESSAGE', event);

      if (event.data === 'KgIIAQ==') {
        setReachable(true);
      }
    });

    addDiscoveryListener('alphabeats-wearos');
    addReachabilityListener('8d6fc96e', 'alphabeats-wearos');
  }, []);

  React.useEffect(() => {
    if (result && Object.keys(result).length > 0) {
      const [id] = Object.keys(result);

      console.debug('Adding message listener from node', id);
      addMessageListener(id);

      sendMessage(id, '/alphabeats/v1', 'aaab');
      isReachable(id, 'alphabeats-wearos')
        .then(setReachable)
        .catch(console.debug);
    }
  }, [result]);

  console.debug(reachable);

  return (
    <View style={styles.container}>
      <Button
        title="Check has capability"
        onPress={() => {
          hasNodeWithCapability('alphabeats-wearos')
            .then((nearby: boolean) => console.debug('IS REACHABLE', nearby))
            .catch(console.debug);
        }}
      />
      <Button
        title="Check reachability"
        onPress={() => {
          isReachable('8d6fc96e', 'alphabeats-wearos')
            .then((nearby: boolean) => console.debug('IS REACHABLE', nearby))
            .catch(console.debug);
        }}
      />
      <Button
        title="Check Nearby"
        onPress={() => {
          isNearby('8d6fc96e', 'alphabeats-wearos')
            .then((nearby: boolean) => console.debug('IS NEARBY', nearby))
            .catch(console.debug);
        }}
      />
      <Button
        title="Get capabilities"
        onPress={() => {
          getAllCapabilities().then(setCapabilities).catch(console.debug);
        }}
      />
      <Button
        title="Send Message"
        onPress={() => {
          if (!result) {
            return;
          }

          console.debug('Sending message');

          const [id] = Object.keys(result);
          sendMessage(id, '/alphabeats/v1', 'WgQKAiAx')
            .then(console.debug)
            .catch(console.debug);

          isReachable(id, 'alphabeats-wearos')
            .then(console.debug, console.debug)
            .catch(console.debug);
        }}
      />
      <Button
        title="Verify Reachability"
        onPress={() => {
          if (!result) {
            return;
          }

          console.debug('Sending message');

          const [id] = Object.keys(result);

          setReachable(false);
          sendMessage(id, '/alphabeats/v1', 'KgIIAQ==')
            .then(console.debug)
            .catch(console.debug);
        }}
      />
      <Text>Result: {JSON.stringify(result)}</Text>
      <Text>Capabilities: {JSON.stringify(capabilities)}</Text>
      <Text>Reacability: {reachable ? 'Reachable' : 'Not reachable'}</Text>
      <Text>Result: {reachable}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
