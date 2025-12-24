import { TestBot, createBot } from './helpers/bot'
import { connectRcon, disconnectRcon, sendCommand } from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

/**
 * Multiaccounts Denial E2E Tests
 *
 * These tests verify that the maxMultiaccountsRecently feature works correctly.
 * Players from the same IP are limited to a maximum number of accounts within a time window.
 *
 * REQUIREMENTS:
 * To run these tests, you must update e2e/platforms/bukkit/configs/config.yml:
 *   maxMultiaccountsRecently: 2
 *   multiaccountsTime: 300
 *
 * If maxMultiaccountsRecently is 0 (default), these tests will be skipped.
 */

// Check if multiaccounts testing is enabled via environment variable
const MULTIACCOUNTS_ENABLED = process.env.TEST_MULTIACCOUNTS === 'true'

const describeOrSkip = MULTIACCOUNTS_ENABLED ? describe : describe.skip

describeOrSkip('Multiaccounts Denial Tests', () => {
  const PLAYER1_USERNAME = 'MultiPlayer1'
  const PLAYER2_USERNAME = 'MultiPlayer2'
  const PLAYER3_USERNAME = 'MultiPlayer3'

  let player1Bot: TestBot | null = null
  let player2Bot: TestBot | null = null

  beforeAll(async () => {
    await connectRcon()
  }, 30000)

  afterAll(async () => {
    // Clean up all bots
    if (player1Bot != null) {
      await player1Bot.disconnect().catch(() => {})
    }
    if (player2Bot != null) {
      await player2Bot.disconnect().catch(() => {})
    }
    await disconnectRcon()
  })

  afterEach(async () => {
    // Disconnect bots between tests
    if (player1Bot != null) {
      await player1Bot.disconnect().catch(() => {})
      player1Bot = null
    }
    if (player2Bot != null) {
      await player2Bot.disconnect().catch(() => {})
      player2Bot = null
    }
    // Wait to avoid connection throttling
    await sleep(3000)
  })

  test('should allow players within the multiaccounts limit', async () => {
    // Connect first player
    player1Bot = await createBot(PLAYER1_USERNAME)

    await waitFor(
      async () => {
        const list = await sendCommand('list')
        return list.includes(PLAYER1_USERNAME)
      },
      { timeout: 10000, interval: 500, message: 'Player 1 not in player list' }
    )

    // Wait to avoid throttling
    await sleep(2000)

    // Connect second player (should succeed - within limit of 2)
    player2Bot = await createBot(PLAYER2_USERNAME)

    await waitFor(
      async () => {
        const list = await sendCommand('list')
        return list.includes(PLAYER2_USERNAME)
      },
      { timeout: 10000, interval: 500, message: 'Player 2 not in player list' }
    )

    // Both players should be online
    const finalList = await sendCommand('list')
    expect(finalList).toContain(PLAYER1_USERNAME)
    expect(finalList).toContain(PLAYER2_USERNAME)
  }, 60000)

  test('should deny player exceeding the multiaccounts limit', async () => {
    // Connect first two players to reach the limit
    player1Bot = await createBot(PLAYER1_USERNAME)
    await waitFor(
      async () => {
        const list = await sendCommand('list')
        return list.includes(PLAYER1_USERNAME)
      },
      { timeout: 10000, interval: 500, message: 'Player 1 not in player list' }
    )

    await sleep(2000)

    player2Bot = await createBot(PLAYER2_USERNAME)
    await waitFor(
      async () => {
        const list = await sendCommand('list')
        return list.includes(PLAYER2_USERNAME)
      },
      { timeout: 10000, interval: 500, message: 'Player 2 not in player list' }
    )

    await sleep(2000)

    // Third player should be denied (exceeds limit of 2)
    const player3Bot = new TestBot(PLAYER3_USERNAME)
    let connectionFailed = false
    let kickReason = ''

    try {
      await player3Bot.connect()
      // If we get here, connection succeeded when it shouldn't have
      await player3Bot.disconnect().catch(() => {})
    } catch (error: unknown) {
      connectionFailed = true
      if (error instanceof Error) {
        kickReason = error.message
      }
    }

    expect(connectionFailed).toBe(true)
    // The kick message should contain the denial reason
    // This depends on the messages.yml configuration
  }, 90000)
})

// If not enabled, provide a helpful message
if (!MULTIACCOUNTS_ENABLED) {
  describe('Multiaccounts Denial Tests (SKIPPED)', () => {
    test.skip('tests skipped - set TEST_MULTIACCOUNTS=true and configure maxMultiaccountsRecently in config.yml', () => {
      // This test is intentionally skipped
      // To enable:
      // 1. Set maxMultiaccountsRecently: 2 in e2e/platforms/bukkit/configs/config.yml
      // 2. Run with TEST_MULTIACCOUNTS=true npm test
    })
  })
}
