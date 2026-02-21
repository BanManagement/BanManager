import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  opPlayer,
  sendCommand,
  isPlayerInList,
  waitForBanManagerCommands
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'
import {
  getWebhookRequests,
  resetWebhookSink
} from './helpers/webhook'

describe('Global banall webhook E2E', () => {
  let staffBot: TestBot
  let targetBot: TestBot
  const STAFF_USERNAME = 'GlobalBanStaff'
  const TARGET_USERNAME = 'GlobalBanTarget'
  const BAN_REASON = 'E2E_global_banall_webhook'

  beforeAll(async () => {
    await connectRcon()
    await waitForBanManagerCommands({ timeoutMs: 90000, intervalMs: 1000 })

    await resetWebhookSink()

    staffBot = await createBot(STAFF_USERNAME)
    targetBot = await createBot(TARGET_USERNAME)

    await waitFor(
      async () => await isPlayerInList(STAFF_USERNAME),
      { timeout: 20000, interval: 500, message: 'Staff bot not in player list' }
    )

    await waitFor(
      async () => await isPlayerInList(TARGET_USERNAME),
      { timeout: 20000, interval: 500, message: 'Target bot not in player list' }
    )

    await opPlayer(STAFF_USERNAME)
    await sleep(3500)
  }, 120000)

  beforeEach(async () => {
    await resetWebhookSink()
    try { await sendCommand(`unbanall ${TARGET_USERNAME}`) } catch { /* ignore */ }
    try { await sendCommand(`bmunban ${TARGET_USERNAME}`) } catch { /* ignore */ }
    await sleep(1000)
  })

  afterAll(async () => {
    try { await sendCommand(`unbanall ${TARGET_USERNAME}`) } catch { /* ignore */ }
    try { await sendCommand(`bmunban ${TARGET_USERNAME}`) } catch { /* ignore */ }

    if (targetBot != null) {
      try { await targetBot.disconnect() } catch { /* ignore */ }
    }

    if (staffBot != null) {
      try { await staffBot.disconnect() } catch { /* ignore */ }
    }

    await disconnectRcon()
  })

  test('banall emits exactly one webhook payload', async () => {
    await sendCommand(`banall ${TARGET_USERNAME} ${BAN_REASON}`)

    await waitFor(
      async () => {
        const requests = await getWebhookRequests()
        return requests.some(request =>
          request.body.includes(TARGET_USERNAME) && request.body.includes(BAN_REASON)
        )
      },
      { timeout: 15000, interval: 250, message: 'No webhook payload received for banall command' }
    )

    const initialMatchingCount = (await getWebhookRequests())
      .filter(request => request.body.includes(TARGET_USERNAME) && request.body.includes(BAN_REASON))
      .length

    if (initialMatchingCount === 0) {
      throw new Error(`Expected exactly 1 webhook for banall, but received 0 for ${TARGET_USERNAME}`)
    }

    if (initialMatchingCount > 1) {
      throw new Error(`Expected exactly 1 webhook for banall, but received ${initialMatchingCount} for ${TARGET_USERNAME}`)
    }

    await sleep(4000)

    const finalMatchingCount = (await getWebhookRequests())
      .filter(request => request.body.includes(TARGET_USERNAME) && request.body.includes(BAN_REASON))
      .length

    if (finalMatchingCount === 0) {
      throw new Error(`Expected exactly 1 webhook after sync window, but received 0 for ${TARGET_USERNAME}`)
    }

    if (finalMatchingCount > 1) {
      throw new Error(`Expected exactly 1 webhook after sync window, but received ${finalMatchingCount} for ${TARGET_USERNAME}`)
    }
  }, 90000)
})
