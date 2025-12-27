import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  banPlayer,
  unbanPlayer,
  mutePlayer,
  unmutePlayer,
  warnPlayer,
  addNote,
  reportPlayer,
  opPlayer,
  sendCommand
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

describe('Notification Broadcast Tests', () => {
  let staffBot: TestBot
  let targetBot: TestBot
  const STAFF_USERNAME = 'NotifyStaff'
  const TARGET_USERNAME = 'NotifyTarget'

  beforeAll(async () => {
    await connectRcon()

    staffBot = await createBot(STAFF_USERNAME)
    await waitFor(
      async () => {
        const list = await sendCommand('list')
        return list.includes(STAFF_USERNAME)
      },
      { timeout: 10000, interval: 500, message: 'Staff bot not in player list' }
    )

    await opPlayer(STAFF_USERNAME)
    await sleep(3500)

    targetBot = await createBot(TARGET_USERNAME)
    await waitFor(
      async () => {
        const list = await sendCommand('list')
        return list.includes(TARGET_USERNAME)
      },
      { timeout: 10000, interval: 500, message: 'Target bot not in player list' }
    )

    await sleep(1000)
  }, 120000)

  afterAll(async () => {
    try { await unbanPlayer(TARGET_USERNAME) } catch { /* ignore */ }
    try { await unmutePlayer(TARGET_USERNAME) } catch { /* ignore */ }

    if (targetBot != null) await targetBot.disconnect()
    if (staffBot != null) await staffBot.disconnect()

    await disconnectRcon()
  })

  beforeEach(async () => {
    staffBot.clearChatHistory()
    staffBot.clearSystemMessages()

    try { await unbanPlayer(TARGET_USERNAME) } catch { /* ignore */ }
    try { await unmutePlayer(TARGET_USERNAME) } catch { /* ignore */ }

    await sleep(500)
  })

  test('staff receives notification when a player is banned', async () => {
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

  test('staff receives notification when a player is muted', async () => {
    staffBot.clearSystemMessages()
    await mutePlayer(TARGET_USERNAME, 'Testing mute notification')

    await waitFor(
      () => staffBot.getSystemMessages().some(m =>
        m.message.toLowerCase().includes('mute') &&
        m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
      ),
      { timeout: 10000, interval: 200, message: 'Mute notification not received by staff' }
    )

    const muteNotification = staffBot.getSystemMessages().find(m =>
      m.message.toLowerCase().includes('mute') &&
      m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
    )
    expect(muteNotification).toBeDefined()
  }, 30000)

  test('staff receives notification when a player is warned', async () => {
    staffBot.clearSystemMessages()
    await warnPlayer(TARGET_USERNAME, 'Testing warn notification')

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

  test('staff receives notification when a note is added', async () => {
    staffBot.clearSystemMessages()
    await addNote(TARGET_USERNAME, 'Testing note notification')

    await waitFor(
      () => staffBot.getSystemMessages().some(m =>
        m.message.toLowerCase().includes('note') &&
        m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
      ),
      { timeout: 10000, interval: 200, message: 'Note notification not received by staff' }
    )

    const noteNotification = staffBot.getSystemMessages().find(m =>
      m.message.toLowerCase().includes('note') &&
      m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
    )
    expect(noteNotification).toBeDefined()
  }, 30000)

  test.skip('staff receives notification when a player is reported', async () => {
    staffBot.clearSystemMessages()

    // Small delay before reporting to ensure previous test cleanup is complete
    await sleep(500)

    await reportPlayer(TARGET_USERNAME, 'Testing report notification')

    await waitFor(
      () => staffBot.getSystemMessages().some(m =>
        m.message.toLowerCase().includes('report') &&
        m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
      ),
      { timeout: 15000, interval: 300, message: 'Report notification not received by staff' }
    )

    const reportNotification = staffBot.getSystemMessages().find(m =>
      m.message.toLowerCase().includes('report') &&
      m.message.toLowerCase().includes(TARGET_USERNAME.toLowerCase())
    )
    expect(reportNotification).toBeDefined()
  }, 30000)
})
