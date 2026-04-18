import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  banPlayer,
  unbanPlayer,
  opPlayer,
  reloadPlugin,
  sendCommand,
  isPlayerInList,
  isProxy
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

describe('Locale Smoke Tests', () => {
  let staffBot: TestBot
  const STAFF_USERNAME = 'LocaleStaff'
  const TARGET_USERNAME = 'LocaleTarget'

  beforeAll(async () => {
    await connectRcon()

    staffBot = await createBot(STAFF_USERNAME)
    await waitFor(
      async () => isPlayerInList(STAFF_USERNAME),
      { timeout: 10000, interval: 500, message: 'Staff bot not in player list' }
    )

    await opPlayer(STAFF_USERNAME)
    await sleep(3500)
  }, 120000)

  afterAll(async () => {
    try { await unbanPlayer(TARGET_USERNAME) } catch { /* ignore */ }
    if (staffBot != null) await staffBot.disconnect()
    await disconnectRcon()
  })

  beforeEach(async () => {
    staffBot.clearChatHistory()
    staffBot.clearSystemMessages()

    try { await unbanPlayer(TARGET_USERNAME) } catch { /* ignore */ }
    await sleep(3000)
  })

  test('ban notification uses default locale messages', async () => {
    staffBot.clearSystemMessages()

    await banPlayer(TARGET_USERNAME, 'locale-test')

    await waitFor(
      () => staffBot.getSystemMessages().some(m =>
        m.message.toLowerCase().includes('banned') &&
        m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
      ),
      { timeout: 10000, interval: 200, message: 'Ban notification not received' }
    )

    const banNotification = staffBot.getSystemMessages().find(m =>
      m.message.toLowerCase().includes('banned') &&
      m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
    )

    expect(banNotification).toBeDefined()
    expect(banNotification!.message).toContain('permanently banned')
    expect(banNotification!.message).toContain('locale-test')
  }, 30000)

  test('messages survive bmreload', async () => {
    await reloadPlugin()
    await sleep(2000)

    staffBot.clearSystemMessages()

    await banPlayer(TARGET_USERNAME, 'post-reload')

    await waitFor(
      () => staffBot.getSystemMessages().some(m =>
        m.message.toLowerCase().includes('banned') &&
        m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
      ),
      { timeout: 10000, interval: 200, message: 'Ban notification not received after reload' }
    )

    const banNotification = staffBot.getSystemMessages().find(m =>
      m.message.toLowerCase().includes('banned') &&
      m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
    )

    expect(banNotification).toBeDefined()
    expect(banNotification!.message).toContain('permanently banned')
    expect(banNotification!.message).toContain('post-reload')
  }, 30000)

  test('non-default locale messages loaded after reload', async () => {
    // Reload to ensure messages/ directory is scanned (including messages_de.yml)
    const reloadResponse = await reloadPlugin()
    await sleep(2000)

    // The reload should succeed without error, confirming the i18n
    // message loading pipeline works with multiple locale files
    const isSponge = (process.env.SERVER_HOST ?? 'localhost').toLowerCase().includes('sponge')
    if (!isSponge) {
      expect(reloadResponse.toLowerCase()).toContain('reloaded')
    }

    // Verify the server still functions correctly after loading multiple locales
    staffBot.clearSystemMessages()
    await banPlayer(TARGET_USERNAME, 'multi-locale')

    await waitFor(
      () => staffBot.getSystemMessages().some(m =>
        m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
      ),
      { timeout: 10000, interval: 200, message: 'Ban notification not received with multiple locales loaded' }
    )

    const notification = staffBot.getSystemMessages().find(m =>
      m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase()) &&
      m.message.toLowerCase().includes('multi-locale')
    )

    expect(notification).toBeDefined()
  }, 30000)
})
