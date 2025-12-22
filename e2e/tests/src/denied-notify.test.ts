import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  banPlayer,
  unbanPlayer,
  opPlayer,
  sendCommand
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

describe('Denied Notification Exemption Tests', () => {
  let staffBot: TestBot
  const STAFF_USERNAME = 'StaffPlayer'
  // Regular banned player - NO exemption
  const BANNED_USERNAME = 'BannedPlayer'
  // Exempt banned player - HAS deniedNotify exemption in exemptions.yml
  const EXEMPT_BANNED_USERNAME = 'ExemptBannedPlayer'

  beforeAll(async () => {
    // Connect to RCON first
    await connectRcon()

    // Connect the staff bot
    staffBot = await createBot(STAFF_USERNAME)

    // Wait for bot to be fully registered
    await waitFor(
      async () => {
        const list = await sendCommand('list')
        return list.includes(STAFF_USERNAME)
      },
      { timeout: 10000, interval: 500, message: 'Staff bot not in player list' }
    )

    // Op the staff bot so they can receive denied notifications
    await opPlayer(STAFF_USERNAME)
    await sleep(500)

    // Pre-register the test players by connecting them briefly
    // This ensures they exist in the database for the ban command
    try {
      const bannedBot = await createBot(BANNED_USERNAME)
      await sleep(1000)
      await bannedBot.disconnect()
    } catch {
      // May fail, that's ok
    }

    // Wait to avoid connection throttling
    await sleep(3000)

    try {
      const exemptBot = await createBot(EXEMPT_BANNED_USERNAME)
      await sleep(1000)
      await exemptBot.disconnect()
    } catch {
      // May fail, that's ok
    }

    // Wait to avoid connection throttling
    await sleep(3000)
  }, 120000)

  afterAll(async () => {
    // Clean up
    try {
      await unbanPlayer(BANNED_USERNAME)
    } catch {
      // Ignore errors during cleanup
    }
    try {
      await unbanPlayer(EXEMPT_BANNED_USERNAME)
    } catch {
      // Ignore errors during cleanup
    }

    if (staffBot != null) {
      await staffBot.disconnect()
    }

    await disconnectRcon()
  })

  beforeEach(async () => {
    // Clear message history before each test
    staffBot.clearChatHistory()
    staffBot.clearSystemMessages()

    // Ensure banned players are not banned
    try {
      await unbanPlayer(BANNED_USERNAME)
    } catch {
      // Player might not be banned, ignore
    }
    try {
      await unbanPlayer(EXEMPT_BANNED_USERNAME)
    } catch {
      // Player might not be banned, ignore
    }

    await sleep(500)
  })

  test('staff receives denied notification when banned player tries to join', async () => {
    // Ban the player (they should now exist in the database)
    await banPlayer(BANNED_USERNAME, 'E2E test ban')
    await sleep(1000)

    // Clear system messages right before the connection attempt
    staffBot.clearSystemMessages()

    // Attempt to connect with the banned player
    const bannedBot = new TestBot(BANNED_USERNAME)
    const connectPromise = bannedBot.connect().catch(() => {
      // Expected to fail - player is banned
    })

    // Small delay to let the connection attempt and notification happen
    await sleep(2000)

    // Wait for the denied notification to arrive (poll the system messages)
    await waitFor(
      () => staffBot.getSystemMessages().some(m => m.message.includes('attempted to join')),
      { timeout: 10000, interval: 200, message: 'Denied notification not received' }
    )

    // Verify the message content
    const messages = staffBot.getSystemMessages()
    const deniedMessage = messages.find(m =>
      m.message.includes('attempted to join') && m.message.includes(BANNED_USERNAME)
    )
    expect(deniedMessage).toBeDefined()

    // Wait for connection to complete/fail
    await connectPromise
    await bannedBot.disconnect().catch(() => {})
  }, 60000)

  test('staff does NOT receive denied notification when exempt player tries to join', async () => {
    // This player has deniedNotify: true in exemptions.yml
    // UUID: 47d8e47b-11c1-393b-8611-7c34449ba1b1 (offline UUID for "ExemptBannedPlayer")

    // Ban the exempt player
    await banPlayer(EXEMPT_BANNED_USERNAME, 'E2E test ban for exemption test')
    await sleep(1000)

    // Clear system messages right before the connection attempt
    staffBot.clearSystemMessages()

    // Attempt to connect with the exempt banned player
    const bannedBot = new TestBot(EXEMPT_BANNED_USERNAME)
    const connectPromise = bannedBot.connect().catch(() => {
      // Expected to fail - player is banned
    })

    // Wait for the connection to fail
    await connectPromise

    // Give some time for any notification to arrive
    await sleep(3000)

    // Verify NO denied notification was received for the exempt player
    const messages = staffBot.getSystemMessages()
    const deniedMessage = messages.find(m =>
      m.message.includes('attempted to join') && m.message.includes(EXEMPT_BANNED_USERNAME)
    )
    expect(deniedMessage).toBeUndefined()

    await bannedBot.disconnect().catch(() => {})
  }, 60000)
})
