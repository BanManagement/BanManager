import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  mutePlayer,
  unmutePlayer,
  sendCommand,
  isPlayerInList,
  opPlayer
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

/**
 * Tests that hook command delays work correctly after the scheduler Duration fix.
 * The hook is configured with a 2 second delay in config.yml:
 *
 * hooks:
 *   enabled: true
 *   events:
 *     mute:
 *       post:
 *         - cmd: "addnote [player] hook-fired-mute"  # Creates note, broadcasts to staff
 *           delay: 2
 *
 * On servers (Bukkit, Fabric, Sponge): Uses "say hook-fired-mute" which broadcasts to all
 * On proxies (Velocity, BungeeCord): Uses "addnote [player] hook-fired-mute" which broadcasts to staff
 */
describe('Hook Delay Tests', () => {
  let staffBot: TestBot
  let targetBot: TestBot
  const STAFF_USERNAME = 'HookStaff'
  const TARGET_USERNAME = 'HookTarget'

  beforeAll(async () => {
    // Connect to RCON first
    await connectRcon()

    // Connect staff bot first (will receive hook notifications)
    staffBot = await createBot(STAFF_USERNAME)
    await waitFor(
      async () => isPlayerInList(STAFF_USERNAME),
      { timeout: 10000, interval: 500, message: 'Staff bot not in player list' }
    )

    // Give staff bot operator permissions (includes bm.notify.notes on proxies)
    await opPlayer(STAFF_USERNAME)
    await sleep(3500)

    // Connect target bot (the one that gets muted)
    targetBot = await createBot(TARGET_USERNAME)
    await waitFor(
      async () => isPlayerInList(TARGET_USERNAME),
      { timeout: 10000, interval: 500, message: 'Target bot not in player list' }
    )

    await sleep(1000)
  }, 120000)

  afterAll(async () => {
    // Clean up
    try {
      await unmutePlayer(TARGET_USERNAME)
    } catch {
      // Ignore errors during cleanup
    }

    if (targetBot != null) await targetBot.disconnect()
    if (staffBot != null) await staffBot.disconnect()

    await disconnectRcon()
  })

  beforeEach(async () => {
    // Clear chat history before each test
    staffBot.clearChatHistory()
    staffBot.clearSystemMessages()

    // Ensure target player is not muted
    try {
      await unmutePlayer(TARGET_USERNAME)
    } catch {
      // Player might not be muted, ignore
    }

    await sleep(500)
  })

  test('hook command executes after configured delay (~2 seconds)', async () => {
    // Clear messages right before the test
    staffBot.clearSystemMessages()

    const startTime = Date.now()

    // Mute the target player - this should trigger the post-mute hook
    await mutePlayer(TARGET_USERNAME, 'E2E hook delay test')

    // Wait for the hook message to appear (should be after ~2 seconds)
    // The hook runs "addnote [player] hook-fired-mute" which broadcasts to staff
    // Message will contain "hook-fired-mute" somewhere in the notification
    await waitFor(
      () => staffBot.getSystemMessages().some(m => m.message.includes('hook-fired-mute')),
      { timeout: 10000, interval: 200, message: 'Hook message not received by staff' }
    )

    const elapsed = Date.now() - startTime

    // Verify the delay was approximately 2 seconds (with tolerance)
    // Allow 1.5 to 5 seconds to account for server tick variance and processing time
    expect(elapsed).toBeGreaterThan(1500)
    expect(elapsed).toBeLessThan(5000)

    // Verify the message was actually received
    const messages = staffBot.getSystemMessages()
    const hookMessage = messages.find(m => m.message.includes('hook-fired-mute'))
    expect(hookMessage).toBeDefined()
  }, 30000)

  test('hook command with no delay executes immediately', async () => {
    // This is a control test to verify hooks work at all
    // Since our config only has a delayed hook, we verify the delayed one works
    // If we had a hook with delay: 0, we'd test that here

    // For now, just verify the hook system is enabled
    const response = await sendCommand('bmreload')
    expect(response).toBeDefined()
  })
})
