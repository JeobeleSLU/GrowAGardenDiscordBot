# 🌿 Grow a Garden Stock Bot

A lightweight **Discord bot** that tracks and posts real-time stock updates (seeds, gear, eggs) for the **Grow a Garden** game via WebSocket.

---

## 📦 Requirements

- Java 17
- Maven
- A Discord **bot token**
- Your personal **api token**
---

## 🚀 Getting Started

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/yourusername/grow-a-garden-stock-bot.git
cd grow-a-garden-stock-bot
```
2️⃣ Build the Project
Using Maven:

```bash

mvn clean package
```

```
3️⃣ Run the Bot
The bot requires one arguments:
java -jar target/grow-a-garden-stock-bot.jar <BOT_TOKEN> <API_TOKEN> -Denv=prod
```
You can get the api token in this Grow A Garden | API & WebSocket discord channel https://discord.com/invite/growagardenapi 
