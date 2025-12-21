import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  reloadPlugin,
  mutePlayer,
  unmutePlayer,
  sendCommand
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

describe('BanManager E2E Tests', () => {
  let bot: TestBot
  const BOT_USERNAME = 'E2ETestPlayer'

  beforeAll(async () => {
    // Connect to RCON first
    await connectRcon()

    // Connect the bot
    bot = await createBot(BOT_USERNAME)

    // Wait for bot to be fully registered (poll for player in list)
    await waitFor(
      async () => {
        const list = await sendCommand('list')
        return list.includes(BOT_USERNAME)
      },
      { timeout: 10000, interval: 500, message: 'Bot not in player list' }
    )
  }, 60000)

  afterAll(async () => {
    // Clean up
    try {
      await unmutePlayer(BOT_USERNAME)
    } catch {
      // Ignore errors during cleanup
    }

    if (bot != null) {
      await bot.disconnect()
    }

    await disconnectRcon()
  })

  beforeEach(async () => {
    // Clear chat history before each test
    bot.clearChatHistory()

    // Ensure player is not muted
    try {
      await unmutePlayer(BOT_USERNAME)
    } catch {
      // Player might not be muted, ignore
    }

    // Small delay to ensure unmute is processed
    await sleep(200)
  })

  test('plugin is loaded and RCON works', async () => {
    const response = await sendCommand('plugins')
    expect(response).toContain('BanManager')
  })

  test('bmreload command works', async () => {
    const response = await reloadPlugin()
    // The reload command should complete without error
    expect(response).toBeDefined()
  })

  test('chat works when not muted', async () => {
    // Send a chat message
    const uniqueId = Date.now().toString()
    await bot.sendChat(`Hello world ${uniqueId}`)

    // Wait for chat to appear using polling
    await waitFor(
      () => bot.getChatMessages().some(m => m.message.includes(uniqueId)),
      { timeout: 3000, interval: 100, message: 'Chat message not received' }
    )

    const messages = bot.getChatMessages()
    const found = messages.some(m => m.message.includes('Hello world'))
    expect(found).toBe(true)
  })

  test('muted player chat is blocked', async () => {
    // Mute the player
    await mutePlayer(BOT_USERNAME, 'E2E test mute')

    // Small delay for mute to be applied
    await sleep(200)

    // Clear chat history after mute notification
    bot.clearChatHistory()

    // Send a chat message
    await bot.sendChat('This should be blocked')

    // Wait and verify no chat appeared from this player (shorter timeout)
    await bot.expectNoChatFrom(BOT_USERNAME, 1500)
  })

  test('unmuted player can chat again', async () => {
    // Mute first to ensure we're testing unmute
    await mutePlayer(BOT_USERNAME, 'E2E test mute')
    await sleep(200)

    // Unmute them
    await unmutePlayer(BOT_USERNAME)
    await sleep(200)

    // Clear chat history
    bot.clearChatHistory()

    // Send a chat message
    const uniqueId = Date.now().toString()
    await bot.sendChat(`After unmute ${uniqueId}`)

    // Wait for chat using polling
    await waitFor(
      () => bot.getChatMessages().some(m => m.message.includes(uniqueId)),
      { timeout: 3000, interval: 100, message: 'Chat not received after unmute' }
    )

    const messages = bot.getChatMessages()
    const found = messages.some(m => m.message.includes('After unmute'))
    expect(found).toBe(true)
  })

  test('reload does not break mute functionality', async () => {
    // Mute the player
    await mutePlayer(BOT_USERNAME, 'E2E test mute')
    await sleep(200)

    // Reload the plugin
    await reloadPlugin()

    // Wait for reload to complete (poll for plugin)
    await waitFor(
      async () => {
        const plugins = await sendCommand('plugins')
        return plugins.includes('BanManager')
      },
      { timeout: 5000, interval: 200, message: 'Plugin not loaded after reload' }
    )

    // Clear chat history
    bot.clearChatHistory()

    // Send a chat message - should still be blocked
    await bot.sendChat('Should still be blocked after reload')

    // Wait and verify no chat appeared
    await bot.expectNoChatFrom(BOT_USERNAME, 1500)
  })
})
