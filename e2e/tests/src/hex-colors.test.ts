import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  opPlayer,
  isPlayerInList
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

describe('Hex Color Support E2E Tests', () => {
  let bot: TestBot
  const BOT_USERNAME = 'HexColorBot'

  beforeAll(async () => {
    await connectRcon()

    bot = await createBot(BOT_USERNAME)
    await waitFor(
      async () => isPlayerInList(BOT_USERNAME),
      { timeout: 10000, interval: 500, message: 'Bot not in player list' }
    )

    await opPlayer(BOT_USERNAME)
    await sleep(1000)
  }, 60000)

  afterAll(async () => {
    if (bot != null) {
      await bot.disconnect()
    }
    await disconnectRcon()
  })

  beforeEach(async () => {
    bot.clearChatHistory()
    bot.clearSystemMessages()
  })

  test('hex color message is received correctly', async () => {
    // configReloaded in messages.yml uses &#rrggbb hex colors
    await bot.sendChat('/bmreload')

    await waitFor(
      () => bot.getSystemMessages().some(m =>
        m.message.toLowerCase().includes('configuration') &&
        m.message.toLowerCase().includes('reloaded') &&
        m.message.toLowerCase().includes('successfully')
      ),
      { timeout: 10000, interval: 200, message: 'Hex color reload message not received' }
    )

    const reloadMessage = bot.getSystemMessages().find(m =>
      m.message.toLowerCase().includes('configuration') &&
      m.message.toLowerCase().includes('reloaded')
    )

    expect(reloadMessage).toBeDefined()
    expect(reloadMessage?.message.toLowerCase()).toContain('configuration')
    expect(reloadMessage?.message.toLowerCase()).toContain('reloaded')
    expect(reloadMessage?.message.toLowerCase()).toContain('successfully')

    // Raw hex codes (&#rrggbb format) should be parsed, not displayed as text
    expect(reloadMessage?.message).not.toContain('&#00ff00')
    expect(reloadMessage?.message).not.toContain('&#ff5733')
  }, 30000)

  test('hex colors are processed correctly', async () => {
    // configReloaded: '&#00ff00Configuration &#ff5733reloaded &asuccessfully!'
    // - Bukkit/Bungee/Sponge: hex downsampled to legacy codes (&2, &c)
    // - Velocity/Fabric: full hex preserved in JSON
    await bot.sendChat('/bmreload')

    await waitFor(
      () => bot.getSystemMessages().some(m =>
        m.message.toLowerCase().includes('successfully')
      ),
      { timeout: 10000, interval: 200, message: 'Color message not received' }
    )

    const message = bot.getSystemMessages().find(m =>
      m.message.toLowerCase().includes('configuration')
    )

    expect(message).toBeDefined()

    // Raw hex codes should always be parsed (never shown as text)
    expect(message?.message).not.toContain('&#00ff00')
    expect(message?.message).not.toContain('&#ff5733')

    // Text content preserved
    expect(message?.message.toLowerCase()).toContain('configuration')
    expect(message?.message.toLowerCase()).toContain('reloaded')
    expect(message?.message.toLowerCase()).toContain('successfully')
  }, 30000)
})
