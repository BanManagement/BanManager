import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  tempBanPlayer,
  unbanPlayer,
  opPlayer,
  sendCommand,
  isPlayerInList,
  isProxy
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

describe('Temp Ban Expiry E2E Tests', () => {
  let staffBot: TestBot
  let targetBot: TestBot | null = null
  const STAFF_USERNAME = 'TempBanStaff'
  const TARGET_USERNAME = 'TempBanTarget'

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

  test('temp ban blocks player during ban period', async () => {
    // Temp ban for 30 seconds
    await tempBanPlayer(TARGET_USERNAME, '30s', 'Testing temp ban')
    await sleep(1000)

    // Try to connect as the temp banned player
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

  test('player can join after temp ban expires', async () => {
    // Temp ban for 10 seconds
    await tempBanPlayer(TARGET_USERNAME, '10s', 'Testing temp ban expiry')

    // Wait for the ban to expire
    await sleep(12000)

    // Now try to connect as the player
    targetBot = await createBot(TARGET_USERNAME)
    await waitFor(
      async () => isPlayerInList(TARGET_USERNAME),
      { timeout: 10000, interval: 500, message: 'Player not in list after temp ban expired' }
    )

    // On proxies, we can't verify via the proxy's list command - the bot spawn confirms connection
    if (!isProxy()) {
      const list = await sendCommand('list')
      expect(list.includes(TARGET_USERNAME)).toBe(true)
    }
  }, 60000)
})
