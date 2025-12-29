import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  warnPlayer,
  opPlayer,
  sendCommand,
  clearWarnings,
  isPlayerInList
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

describe('Warning Actions E2E Tests', () => {
  let staffBot: TestBot
  let targetBot: TestBot
  const STAFF_USERNAME = 'WarnStaff'
  const TARGET_USERNAME = 'WarnTarget'

  beforeAll(async () => {
    await connectRcon()

    staffBot = await createBot(STAFF_USERNAME)
    await waitFor(
      async () => isPlayerInList(STAFF_USERNAME),
      { timeout: 10000, interval: 500, message: 'Staff bot not in player list' }
    )

    await opPlayer(STAFF_USERNAME)
    await sleep(3500)

    targetBot = await createBot(TARGET_USERNAME)
    await waitFor(
      async () => isPlayerInList(TARGET_USERNAME),
      { timeout: 10000, interval: 500, message: 'Target bot not in player list' }
    )

    await sleep(1000)
  }, 120000)

  afterAll(async () => {
    // Clean up warnings
    try { await clearWarnings(TARGET_USERNAME) } catch { /* ignore */ }

    if (targetBot != null) await targetBot.disconnect()
    if (staffBot != null) await staffBot.disconnect()

    await disconnectRcon()
  })

  beforeEach(async () => {
    staffBot.clearChatHistory()
    staffBot.clearSystemMessages()

    // Clear any existing warnings for target player
    try { await clearWarnings(TARGET_USERNAME) } catch { /* ignore */ }

    await sleep(500)
  })

  test('warning is recorded', async () => {
    staffBot.clearSystemMessages()
    await warnPlayer(TARGET_USERNAME, 'Testing warning recording')

    await waitFor(
      () => staffBot.getSystemMessages().some(m =>
        m.message.toLowerCase().includes('warn') &&
        m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
      ),
      { timeout: 10000, interval: 200, message: 'Warn notification not received by staff' }
    )

    const warnNotification = staffBot.getSystemMessages().find(m =>
      m.message.toLowerCase().includes('warn') &&
      m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
    )
    expect(warnNotification).toBeDefined()
  }, 30000)

  test('multiple warnings trigger action', async () => {
    staffBot.clearSystemMessages()

    // Issue 3 warnings to trigger the warning action
    // Use longer delays to avoid "warned too recently" rate limiting
    await warnPlayer(TARGET_USERNAME, 'First warning')
    await sleep(1500)
    await warnPlayer(TARGET_USERNAME, 'Second warning')
    await sleep(1500)
    await warnPlayer(TARGET_USERNAME, 'Third warning')

    // Check for the warning action message
    await waitFor(
      () => staffBot.getSystemMessages().some(m =>
        m.message.toLowerCase().includes('warning-action-triggered')
      ),
      { timeout: 15000, interval: 300, message: 'Warning action not triggered after 3 warnings' }
    )

    const actionMessage = staffBot.getSystemMessages().find(m =>
      m.message.toLowerCase().includes('warning-action-triggered')
    )
    expect(actionMessage).toBeDefined()
  }, 60000)
})
