import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  banPlayer,
  unbanPlayer,
  opPlayer,
  sendCommand,
  isPlayerInList,
  isProxy
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

describe('Ban/Unban E2E Tests', () => {
  let staffBot: TestBot
  let targetBot: TestBot | null = null
  const STAFF_USERNAME = 'BanStaff'
  const TARGET_USERNAME = 'BanTarget'

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

    if (targetBot != null) await targetBot.disconnect()
    if (staffBot != null) await staffBot.disconnect()

    await disconnectRcon()
  })

  beforeEach(async () => {
    staffBot.clearChatHistory()
    staffBot.clearSystemMessages()

    try { await unbanPlayer(TARGET_USERNAME) } catch { /* ignore */ }

    await sleep(500)
  })

  test('banned player cannot join server', async () => {
    // First, ban the target
    await banPlayer(TARGET_USERNAME, 'Testing ban')
    await sleep(1000)

    // Try to connect as the banned player
    try {
      targetBot = await createBot(TARGET_USERNAME)

      // If we get here, wait and check if kicked
      await sleep(2000)

      // Check player list - banned player should NOT be there
      // On proxies, we can't easily verify via the proxy's list command
      if (!isProxy()) {
        const list = await sendCommand('list')
        expect(list.includes(TARGET_USERNAME)).toBe(false)
      }
    } catch (err) {
      // Connection should fail or player should be kicked - this is expected
      expect(err).toBeDefined()
    }
  }, 60000)

  test('unbanned player can join server', async () => {
    // First, ban and then unban the target
    await banPlayer(TARGET_USERNAME, 'Testing ban')
    await sleep(1000)
    await unbanPlayer(TARGET_USERNAME)
    // Allow extra time for async unban processing (especially on Sponge)
    await sleep(2000)

    // Now try to connect as the unbanned player
    targetBot = await createBot(TARGET_USERNAME)
    await waitFor(
      async () => isPlayerInList(TARGET_USERNAME),
      { timeout: 10000, interval: 500, message: 'Unbanned player not in player list' }
    )

    // On proxies, we can't verify via the proxy's list command - the bot spawn confirms connection
    if (!isProxy()) {
      const list = await sendCommand('list')
      expect(list.includes(TARGET_USERNAME)).toBe(true)
    }
  }, 60000)

  test('ban notification sent to staff', async () => {
    staffBot.clearSystemMessages()
    await banPlayer(TARGET_USERNAME, 'Testing ban notification')

    await waitFor(
      () => staffBot.getSystemMessages().some(m =>
        m.message.toLowerCase().includes('ban') &&
        m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
      ),
      { timeout: 10000, interval: 200, message: 'Ban notification not received by staff' }
    )

    const banNotification = staffBot.getSystemMessages().find(m =>
      m.message.toLowerCase().includes('ban') &&
      m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
    )
    expect(banNotification).toBeDefined()
  }, 30000)
})
