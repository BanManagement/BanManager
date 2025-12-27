import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  opPlayer,
  sendCommand
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

// Use BanManager-specific commands to avoid conflicts with Sponge's built-in ban service
async function bmBanPlayer (player: string, reason: string): Promise<string> {
  return await sendCommand(`bmban ${player} ${reason}`)
}

async function bmUnbanPlayer (player: string): Promise<string> {
  return await sendCommand(`bmunban ${player}`)
}

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
      await bmUnbanPlayer(BANNED_USERNAME)
    } catch {
      // Ignore errors during cleanup
    }
    try {
      await bmUnbanPlayer(EXEMPT_BANNED_USERNAME)
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

    // Ensure banned players are not banned (use BanManager command)
    try {
      await bmUnbanPlayer(BANNED_USERNAME)
    } catch {
      // Player might not be banned, ignore
    }
    try {
      await bmUnbanPlayer(EXEMPT_BANNED_USERNAME)
    } catch {
      // Player might not be banned, ignore
    }

    await sleep(500)
  })

  test('staff receives denied notification when banned player tries to join', async () => {
    // Ban the player using BanManager (not vanilla ban)
    await bmBanPlayer(BANNED_USERNAME, 'E2E test ban')

    // Wait for ban to be fully processed and synced
    await sleep(2000)

    // Clear system messages right before the connection attempt
    staffBot.clearSystemMessages()

    // Attempt to connect with the banned player - this will be rejected
    const bannedBot = new TestBot(BANNED_USERNAME)

    // Start the connection attempt (will fail because player is banned)
    bannedBot.connect().catch(() => {
      // Expected to fail - player is banned
    })

    // Wait for the denied notification to arrive
    // The notification is sent when the auth check fails
    await waitFor(
      () => staffBot.getSystemMessages().some(m =>
        m.message.includes('attempted to join') ||
        m.message.toLowerCase().includes('denied')
      ),
      { timeout: 15000, interval: 300, message: 'Denied notification not received' }
    )

    // Verify the message content
    const messages = staffBot.getSystemMessages()
    const deniedMessage = messages.find(m =>
      (m.message.includes('attempted to join') || m.message.toLowerCase().includes('denied')) &&
      m.message.toLowerCase().includes(BANNED_USERNAME.toLowerCase())
    )
    expect(deniedMessage).toBeDefined()

    // Clean up the banned bot connection
    await bannedBot.disconnect().catch(() => {})
  }, 60000)

  test('staff does NOT receive denied notification when exempt player tries to join', async () => {
    // This player has deniedNotify: true in exemptions.yml
    // UUID: 47d8e47b-11c1-393b-8611-7c34449ba1b1 (offline UUID for "ExemptBannedPlayer")

    // Ban the exempt player using BanManager
    await bmBanPlayer(EXEMPT_BANNED_USERNAME, 'E2E test ban for exemption test')

    // Wait for ban to be fully processed
    await sleep(2000)

    // Clear system messages right before the connection attempt
    staffBot.clearSystemMessages()

    // Attempt to connect with the exempt banned player
    const bannedBot = new TestBot(EXEMPT_BANNED_USERNAME)
    bannedBot.connect().catch(() => {
      // Expected to fail - player is banned
    })

    // Give time for connection attempt and any potential notification
    await sleep(5000)

    // Verify NO denied notification was received for the exempt player
    const messages = staffBot.getSystemMessages()
    const deniedMessage = messages.find(m =>
      (m.message.includes('attempted to join') || m.message.toLowerCase().includes('denied')) &&
      m.message.toLowerCase().includes(EXEMPT_BANNED_USERNAME.toLowerCase())
    )
    expect(deniedMessage).toBeUndefined()

    await bannedBot.disconnect().catch(() => {})
  }, 60000)
})
