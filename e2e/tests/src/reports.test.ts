import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  opPlayer,
  sendCommand,
  isPlayerInList,
  isProxy
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

// Reports are disabled on proxies (Velocity, BungeeCord) - skip these tests
const describeOrSkip = isProxy() ? describe.skip : describe

describeOrSkip('Reports E2E Tests', () => {
  let staffBot: TestBot
  let targetBot: TestBot
  const STAFF_USERNAME = 'ReportStaff'
  const TARGET_USERNAME = 'ReportTarget'

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
    if (targetBot != null) await targetBot.disconnect()
    if (staffBot != null) await staffBot.disconnect()

    await disconnectRcon()
  })

  beforeEach(async () => {
    staffBot.clearChatHistory()
    staffBot.clearSystemMessages()

    await sleep(500)
  })

  test('player can create report via RCON', async () => {
    staffBot.clearSystemMessages()

    const response = await sendCommand(`report ${TARGET_USERNAME} Testing report creation`)

    expect(response.toLowerCase()).not.toContain('error')
    expect(response.toLowerCase()).not.toContain('unknown command')
  }, 30000)

  test('staff receives report notification', async () => {
    staffBot.clearSystemMessages()

    await sendCommand(`report ${TARGET_USERNAME} Testing report notification for staff`)

    await waitFor(
      () => staffBot.getSystemMessages().some(m =>
        m.message.toLowerCase().includes('report')
      ),
      { timeout: 15000, interval: 300, message: 'Report notification not received by staff' }
    )

    const reportNotification = staffBot.getSystemMessages().find(m =>
      m.message.toLowerCase().includes('report')
    )
    expect(reportNotification).toBeDefined()
  }, 30000)
})
