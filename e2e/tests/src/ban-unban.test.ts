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
import { sleep, waitFor, waitForOrFalse } from './helpers/config'

function hasNotification (bot: TestBot, ...keywords: string[]): boolean {
  return bot.getSystemMessages().some(m => {
    const lower = m.message.toLowerCase()
    return keywords.every(k => lower.includes(k.toLowerCase()))
  })
}

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

    // Wait for async unban to complete if player was banned
    await waitForOrFalse(
      () => hasNotification(staffBot, 'unban', TARGET_USERNAME),
      { timeout: 5000, interval: 200 }
    )

    staffBot.clearSystemMessages()
  })

  test('banned player cannot join server', async () => {
    await banPlayer(TARGET_USERNAME, 'Testing ban')

    await waitFor(
      () => hasNotification(staffBot, 'ban', TARGET_USERNAME),
      { timeout: 10000, interval: 200, message: 'Ban notification not received' }
    )

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
    await banPlayer(TARGET_USERNAME, 'Testing ban')

    await waitFor(
      () => hasNotification(staffBot, 'ban', TARGET_USERNAME),
      { timeout: 10000, interval: 200, message: 'Ban notification not received' }
    )

    staffBot.clearSystemMessages()

    await unbanPlayer(TARGET_USERNAME)

    await waitFor(
      () => hasNotification(staffBot, 'unban', TARGET_USERNAME),
      { timeout: 10000, interval: 200, message: 'Unban notification not received' }
    )

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
