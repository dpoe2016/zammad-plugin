# Zammad Branch Creator Plugin for IntelliJ IDEA

This plugin integrates with the Zammad ticketing system to create Git branches for tickets directly from your IntelliJ IDEA IDE.

## Features

- View open tickets from your Zammad instance
- Select a ticket to create a feature branch
- Automatically formats branch names based on ticket ID and title
- Seamless integration with Git

## Requirements

- IntelliJ IDEA 2025.1 or newer
- Git4Idea plugin (bundled with IntelliJ IDEA)
- Access to a Zammad ticketing system instance

## Installation

### From JetBrains Marketplace

1. Open IntelliJ IDEA
2. Go to Settings/Preferences → Plugins → Marketplace
3. Search for "Zammad Branch Creator"
4. Click "Install"

### Manual Installation

1. Download the plugin ZIP file from the [Releases](https://github.com/dp-coding/zammad-plugin/releases) page
2. Open IntelliJ IDEA
3. Go to Settings/Preferences → Plugins
4. Click the gear icon and select "Install Plugin from Disk..."
5. Navigate to the downloaded ZIP file and select it
6. Restart IntelliJ IDEA when prompted

## Configuration

Before using the plugin, you need to configure your Zammad API connection:

1. Go to VCS → Zammad → Zammad Settings
2. Enter your Zammad instance URL (e.g., https://your-zammad-instance.com)
3. Enter your API token (can be generated in your Zammad user profile)
4. Click "Test Connection" to verify your settings
5. Click "Save" to store your settings

## Usage

1. Go to VCS → Zammad → Create Branch from Zammad Ticket
2. Select a ticket from the list of open tickets
3. The plugin will automatically create a Git branch with a name based on the ticket ID and title

## Building from Source

This project uses Gradle as its build system. To build the plugin from source:

1. Clone the repository:
   ```
   git clone https://github.com/dp-coding/zammad-plugin.git
   cd zammad-plugin
   ```

2. Build the plugin:
   ```
   ./gradlew build
   ```

3. Create a distribution ZIP file:
   ```
   ./gradlew buildPlugin
   ```

The plugin ZIP file will be created in the `build/distributions` directory.

## Development

The plugin is built using:
- Kotlin
- IntelliJ Platform Plugin SDK
- Retrofit for API communication
- Gson for JSON parsing

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.