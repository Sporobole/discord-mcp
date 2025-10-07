<div align="center">
  <img src="assets/img/Discord_MCP_full_logo.svg" width="60%" alt="DeepSeek-V3" />
</div>
<hr>
<div align="center" style="line-height: 1;">
    <a href="https://github.com/modelcontextprotocol/servers" target="_blank" style="margin: 2px;">
        <img alt="MCP Server" src="https://badge.mcpx.dev?type=server" style="display: inline-block; vertical-align: middle;"/>
    </a>
    <a href="https://smithery.ai/server/@SaseQ/discord-mcp" target="_blank" style="margin: 2px;">
        <img alt="Smithery Badge" src="https://camo.githubusercontent.com/ee5c6c6dc502821f4d57313b2885f7878af52be14142dd98526ea12aedf9b260/68747470733a2f2f736d6974686572792e61692f62616467652f40646d6f6e74676f6d65727934302f646565707365656b2d6d63702d736572766572" data-canonical-src="https://smithery.ai/server/@SaseQ/discord-mcp" style="display: inline-block; vertical-align: middle;"/>
    </a>
    <a href="https://discord.gg/5Uvxe5jteM" target="_blank" style="margin: 2px;">
        <img alt="Discord" src="https://img.shields.io/discord/936242526120194108?color=7389D8&label&logo=discord&logoColor=ffffff" style="display: inline-block; vertical-align: middle;"/>
    </a>
    <a href="https://github.com/SaseQ/discord-mcp/blob/main/LICENSE" target="_blank" style="margin: 2px;">
        <img alt="MIT License" src="https://img.shields.io/github/license/SaseQ/discord-mcp" style="display: inline-block; vertical-align: middle;"/>
    </a>
</div>


## 📖 Description

A [Model Context Protocol (MCP)](https://modelcontextprotocol.io/introduction) server for the Discord API [(JDA)](https://jda.wiki/),
allowing seamless integration of Discord Bot with MCP-compatible applications.

This server supports **HTTP/SSE (Server-Sent Events)** transport with **Bearer token authentication**, making it suitable for production deployments behind reverse proxies and platforms like Coolify.

Enable your AI assistants to seamlessly interact with Discord. Manage channels, send messages, and retrieve server information effortlessly. Enhance your Discord experience with powerful automation capabilities.


## 🔬 Installation

### 🔑 Prerequisites

Before installation, you'll need:

1. **Discord Bot Token**: Create a bot at [Discord Developer Portal](https://discordjs.guide/preparations/setting-up-a-bot-application.html#creating-your-bot)
2. **Bearer Token**: Generate a secure random token for authentication:

```bash
# Linux/macOS
openssl rand -base64 32

# Or using Python
python3 -c "import secrets; print(secrets.token_urlsafe(32))"

# Windows PowerShell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

⚠️ **Security**: Keep your tokens secure and never commit them to version control!

### ► 🐳 Docker Compose Deployment (Recommended)

This is the recommended method for production deployments, especially with platforms like Coolify.

#### 1. Clone the repository
```bash
git clone https://github.com/yourusername/discord-mcp
cd discord-mcp
```

#### 2. Create environment file
```bash
cat > .env << EOF
DISCORD_TOKEN=your_discord_bot_token_here
DISCORD_GUILD_ID=optional_default_server_id
MCP_BEARER_TOKEN=your_generated_bearer_token_here
EOF
```

#### 3. Deploy with Docker Compose
```bash
docker-compose up -d
```

The server will be available at `http://localhost:8085` with the following endpoints:
- `/sse` - SSE connection endpoint
- `/mcp/message` - Message endpoint
- `/actuator/health` - Health check endpoint

### ► 🌐 HTTP/SSE Client Configuration

Configure your MCP client to connect via HTTP/SSE:

```json
{
  "mcpServers": {
    "discord-mcp": {
      "url": "https://your-domain.com:8085/sse",
      "transport": {
        "type": "sse"
      },
      "headers": {
        "Authorization": "Bearer YOUR_MCP_BEARER_TOKEN"
      }
    }
  }
}
```

<details>
    <summary style="font-size: 1.35em; font-weight: bold;">
        🔧 Manual Build and Run
    </summary>

#### Build the project
> NOTE: Maven installation is required. Full instructions can be found [here](https://www.baeldung.com/install-maven-on-windows-linux-mac).

```bash
git clone https://github.com/yourusername/discord-mcp
cd discord-mcp
mvn clean package
```

#### Run locally
```bash
export DISCORD_TOKEN="your_discord_bot_token"
export DISCORD_GUILD_ID="optional_default_server_id"
export MCP_BEARER_TOKEN="your_generated_bearer_token"

java -jar target/discord-mcp-0.0.1.jar
```

The server will start on `http://localhost:8085`

</details>

<details>
    <summary style="font-size: 1.35em; font-weight: bold;">
        ☁️ Coolify Deployment
    </summary>

Deploy to Coolify with these steps:

1. **Import the repository** into Coolify from Git
2. **Set deployment type** to Docker Compose
3. **Configure environment variables** in Coolify UI:
   - `DISCORD_TOKEN`: Your Discord bot token
   - `DISCORD_GUILD_ID`: (Optional) Default Discord server ID
   - `MCP_BEARER_TOKEN`: Your generated bearer token
4. **Configure domain**: Set as `https://your-domain.com:8085`
   - ⚠️ **Important**: Include port `:8085` in the domain configuration
5. **Deploy**: Coolify will build and deploy the service

</details>

## 🛠️ Available Tools

#### Server Information
 - [`get_server_info`](): Get detailed discord server information

#### User Management
- [`get_user_id_by_name`](): Get a Discord user's ID by username in a guild for ping usage `<@id>`
- [`send_private_message`](): Send a private message to a specific user
- [`edit_private_message`](): Edit a private message from a specific user
- [`delete_private_message`](): Delete a private message from a specific user
- [`read_private_messages`](): Read recent message history from a specific user

#### Message Management
 - [`send_message`](): Send a message to a specific channel
 - [`edit_message`](): Edit a message from a specific channel
 - [`delete_message`](): Delete a message from a specific channel
 - [`read_messages`](): Read recent message history from a specific channel
 - [`add_reaction`](): Add a reaction (emoji) to a specific message
 - [`remove_reaction`](): Remove a specified reaction (emoji) from a message

#### Channel Management
 - [`create_text_channel`](): Create text a channel
 - [`delete_channel`](): Delete a channel
 - [`find_channel`](): Find a channel type and ID using name and server ID
 - [`list_channels`](): List of all channels

#### Category Management
 - [`create_category`](): Create a new category for channels
 - [`delete_category`](): Delete a category
 - [`find_category`](): Find a category ID using name and server ID
 - [`list_channels_in_category`](): List of channels in a specific category

#### Webhook Management
 - [`create_webhook`](): Create a new webhook on a specific channel
 - [`delete_webhook`](): Delete a webhook
 - [`list_webhooks`](): List of webhooks on a specific channel
 - [`send_webhook_message`](): Send a message via webhook

>If `DISCORD_GUILD_ID` is set, the `guildId` parameter becomes optional for all tools above.

## 🔐 Security

### Authentication

All HTTP/SSE endpoints (`/sse` and `/mcp/message`) are protected with Bearer token authentication. Clients must include the authorization header:

```
Authorization: Bearer YOUR_MCP_BEARER_TOKEN
```

### Best Practices

- **Generate strong tokens**: Use the provided commands to generate cryptographically secure tokens
- **Rotate tokens regularly**: Change your `MCP_BEARER_TOKEN` periodically
- **Use HTTPS in production**: Always deploy behind SSL/TLS (Coolify handles this automatically)
- **Store tokens securely**: Use environment variables or secrets management systems
- **Never commit tokens**: Add `.env` to `.gitignore`

### Health Check

The `/actuator/health` endpoint is publicly accessible for monitoring and does not require authentication.

## 🏗️ Architecture

- **Framework**: Spring Boot 3.3.6
- **MCP Integration**: Spring AI MCP Server with WebMVC SSE transport
- **Discord API**: JDA (Java Discord API) 5.6.1
- **Security**: Spring Security with custom Bearer token authentication
- **Transport**: HTTP/SSE (Server-Sent Events)
- **Port**: 8085

## 📚 Additional Resources

A more detailed examples can be found in the [Wiki](https://github.com/SaseQ/discord-mcp/wiki).
