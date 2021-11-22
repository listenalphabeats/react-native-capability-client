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

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
