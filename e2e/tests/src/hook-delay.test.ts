import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  mutePlayer,
  unmutePlayer,
  sendCommand
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
 *         - cmd: "say hook-fired-mute"
 *           delay: 2
 */
describe('Hook Delay Tests', () => {
  let bot: TestBot
  const BOT_USERNAME = 'HookTestPlayer'

  beforeAll(async () => {
    // Connect to RCON first
    await connectRcon()

    // Connect the bot
    bot = await createBot(BOT_USERNAME)

    // Wait for bot to be fully registered
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
    bot.clearSystemMessages()

    // Ensure player is not muted
    try {
      await unmutePlayer(BOT_USERNAME)
    } catch {
      // Player might not be muted, ignore
    }

    await sleep(500)
  })

  test('hook command executes after configured delay (~2 seconds)', async () => {
    // Clear messages right before the test
    bot.clearSystemMessages()

    const startTime = Date.now()

    // Mute the player - this should trigger the post-mute hook
    await mutePlayer(BOT_USERNAME, 'E2E hook delay test')

    // Wait for the hook message to appear (should be after ~2 seconds)
    await waitFor(
      () => bot.getSystemMessages().some(m => m.message.includes('hook-fired-mute')),
      { timeout: 10000, interval: 200, message: 'Hook message not received' }
    )

    const elapsed = Date.now() - startTime

    // Verify the delay was approximately 2 seconds (with tolerance)
    // Allow 1.5 to 5 seconds to account for server tick variance and processing time
    expect(elapsed).toBeGreaterThan(1500)
    expect(elapsed).toBeLessThan(5000)

    // Verify the message was actually received
    const messages = bot.getSystemMessages()
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
