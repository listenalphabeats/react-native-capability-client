# react-native-capability-client

The Android Capability Client wrapped in a React Native library. This project is still under active development but offers basic connectivity functionality to pair an Android device with a WearOS device.

## Installation

```sh
npm install @alphabeats/react-native-capability-client
```

## Usage

```js
import { getConnectedNodes, sendMessage } from "@alphabeats/react-native-capability-client";

// ...
const nodes = await getConnectedNodes()
const result = await sendMessage(nodes[0], '/some/path', 'WgQKAiAx')
```

## Cavaets

Android's capability client enables a direct communications line between an app on your phone with an app on your watch. Two important conditions must be met however for this to work:

- The Phone app and the WearOS app must share the same applicationId
- The Phone app and the WearOS app must be signed with the same keystore

Failure to perform either of those steps will result in the phone and the watch app not being able to exchange messages with one another.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
