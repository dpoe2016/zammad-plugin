# Zammad Branch Creator Plugin for IntelliJ IDEA

This plugin integrates with the Zammad ticketing system to create Git branches for tickets directly from your IntelliJ IDEA IDE.

## Features

- View open tickets from your Zammad instance
- Select a ticket to create a feature branch
- Automatically formats branch names based on ticket ID and title
- Open tickets directly in your web browser
- Access settings directly from the tool window
- Seamless integration with Git
- View time accounting entries for tickets
- Record time spent working on tickets

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

1. Access the settings dialog by either:
   - Clicking the "Settings" button in the Zammad Tickets tool window toolbar, or
   - Going to VCS → Zammad → Show Zammad Tickets, then clicking the "Settings" button
2. Enter your Zammad instance URL (e.g., https://your-zammad-instance.com)
3. Enter your API token (can be generated in your Zammad user profile)
4. Click "Test Connection" to verify your settings
5. Click "Save" to store your settings

## Usage

1. Open the Zammad Tickets tool window (View → Tool Windows → Zammad Tickets)
2. You'll see a list of your open tickets from Zammad
3. To create a branch for a ticket, either:
   - Double-click on a ticket in the list, or
   - Select a ticket and click the "Create Branch" button in the toolbar
4. The plugin will automatically create a Git branch with a name based on the ticket ID and title (format: `{ticket-id}-{sanitized-title}`)
5. Additional actions available in the toolbar:
   - Click the "Open in Browser" button to view the selected ticket in your web browser
   - Click the "Settings" button to configure your Zammad API connection
   - Click the "Refresh" button to update the ticket list
   - Click the "Start Time Recording" button to start recording time for the selected ticket
   - Click the "Stop Time Recording" button to stop recording time and save the entry to Zammad
   - Click the "Show Time Entries" button to view all time accounting entries for the selected ticket

Alternatively, you can access the tool window from VCS → Zammad → Show Zammad Tickets

## Building from Source

This project uses Gradle as its build system. To build the plugin from source:

1. Clone the repository:
   ```
   git clone https://github.com/dpoe2016/zammad-plugin.git
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
- Java
- IntelliJ Platform Plugin SDK
- Retrofit for API communication
- Gson for JSON parsing

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
