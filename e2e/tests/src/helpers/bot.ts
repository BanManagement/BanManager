import mineflayer, { Bot } from 'mineflayer'

const SERVER_HOST = process.env.SERVER_HOST ?? 'localhost'
const SERVER_PORT = parseInt(process.env.SERVER_PORT ?? '25565', 10)

export interface ChatMessage {
  username: string
  message: string
  timestamp: number
}

export class TestBot {
  private bot: Bot | null = null
  private chatMessages: ChatMessage[] = []
  private readonly username: string

  constructor (username: string) {
    this.username = username
  }

  async connect (): Promise<void> {
    return await new Promise((resolve, reject) => {
      console.log(`Connecting bot ${this.username} to ${SERVER_HOST}:${SERVER_PORT}`)

      this.bot = mineflayer.createBot({
        host: SERVER_HOST,
        port: SERVER_PORT,
        username: this.username,
        auth: 'offline', // Offline mode for testing
        hideErrors: false
      })

      const timeout = setTimeout(() => {
        reject(new Error('Bot connection timeout'))
      }, 30000)

      this.bot.once('spawn', () => {
        clearTimeout(timeout)
        console.log(`Bot ${this.username} spawned successfully`)
        resolve()
      })

      this.bot.once('error', (err) => {
        clearTimeout(timeout)
        reject(err)
      })

      this.bot.once('kicked', (reason) => {
        console.log(`Bot ${this.username} was kicked: ${String(reason)}`)
      })

      // Listen for chat messages
      this.bot.on('chat', (username, message) => {
        this.chatMessages.push({
          username,
          message,
          timestamp: Date.now()
        })
        console.log(`Chat: <${username}> ${message}`)
      })

      // Listen for system messages (including mute notifications)
      this.bot.on('message', (jsonMsg) => {
        const text = jsonMsg.toString()
        if (text.trim() !== '') {
          console.log(`System: ${text}`)
        }
      })
    })
  }

  async disconnect (): Promise<void> {
    if (this.bot != null) {
      this.bot.quit()
      this.bot = null
      console.log(`Bot ${this.username} disconnected`)
    }
  }

  async sendChat (message: string): Promise<void> {
    if (this.bot == null) {
      throw new Error('Bot not connected')
    }
    console.log(`Bot ${this.username} sending: ${message}`)
    this.bot.chat(message)
  }

  clearChatHistory (): void {
    this.chatMessages = []
  }

  getChatMessages (): ChatMessage[] {
    return [...this.chatMessages]
  }

  /**
   * Wait for a chat message from a specific user containing specific text
   */
  async waitForChat (
    fromUsername: string,
    containsText: string,
    timeoutMs: number = 5000
  ): Promise<ChatMessage> {
    const startTime = Date.now()
    const startIndex = this.chatMessages.length

    return await new Promise((resolve, reject) => {
      const checkInterval = setInterval(() => {
        // Check messages received after we started waiting
        for (let i = startIndex; i < this.chatMessages.length; i++) {
          const msg = this.chatMessages[i]
          if (msg.username === fromUsername && msg.message.includes(containsText)) {
            clearInterval(checkInterval)
            resolve(msg)
            return
          }
        }

        if (Date.now() - startTime > timeoutMs) {
          clearInterval(checkInterval)
          reject(new Error(`Timeout waiting for chat from ${fromUsername} containing "${containsText}"`))
        }
      }, 100)
    })
  }

  /**
   * Wait for any chat message from a specific user
   */
  async waitForAnyChat (fromUsername: string, timeoutMs: number = 5000): Promise<ChatMessage> {
    const startTime = Date.now()
    const startIndex = this.chatMessages.length

    return await new Promise((resolve, reject) => {
      const checkInterval = setInterval(() => {
        for (let i = startIndex; i < this.chatMessages.length; i++) {
          const msg = this.chatMessages[i]
          if (msg.username === fromUsername) {
            clearInterval(checkInterval)
            resolve(msg)
            return
          }
        }

        if (Date.now() - startTime > timeoutMs) {
          clearInterval(checkInterval)
          reject(new Error(`Timeout waiting for chat from ${fromUsername}`))
        }
      }, 100)
    })
  }

  /**
   * Assert that no chat is received within a timeout period
   * Useful for testing mutes - expecting the chat to be blocked
   */
  async expectNoChatFrom (fromUsername: string, timeoutMs: number = 3000): Promise<void> {
    const startIndex = this.chatMessages.length

    await this.sleep(timeoutMs)

    for (let i = startIndex; i < this.chatMessages.length; i++) {
      const msg = this.chatMessages[i]
      if (msg.username === fromUsername) {
        throw new Error(`Unexpected chat from ${fromUsername}: "${msg.message}"`)
      }
    }
  }

  private async sleep (ms: number): Promise<void> {
    return await new Promise((resolve) => setTimeout(resolve, ms))
  }

  isConnected (): boolean {
    return this.bot !== null
  }
}

export async function createBot (username: string): Promise<TestBot> {
  const bot = new TestBot(username)
  await bot.connect()
  return bot
}
