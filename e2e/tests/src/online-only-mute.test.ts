import { TestBot, createBot } from './helpers/bot'
import {
  connectRcon,
  disconnectRcon,
  tempMutePlayerOnlineOnly,
  unmutePlayer,
  opPlayer,
  isPlayerInList
} from './helpers/rcon'
import { sleep, waitFor } from './helpers/config'

describe('Online-Only Temp Mute E2E Tests', () => {
  let staffBot: TestBot
  let targetBot: TestBot | null = null
  const STAFF_USERNAME = 'OnlineMuteStaff'
  const TARGET_USERNAME = 'OnlineMuteTarget'

  beforeAll(async () => {
    await connectRcon()

    staffBot = await createBot(STAFF_USERNAME)
    await waitFor(
      async () => await isPlayerInList(STAFF_USERNAME),
      { timeout: 10000, interval: 500, message: 'Staff bot not in player list' }
    )

    await opPlayer(STAFF_USERNAME)
    await sleep(3500)
  }, 120000)

  afterAll(async () => {
    try { await unmutePlayer(TARGET_USERNAME) } catch { /* ignore */ }

    if (targetBot != null) await targetBot.disconnect()
    if (staffBot != null) await staffBot.disconnect()

    await disconnectRcon()
  })

  beforeEach(async () => {
    staffBot.clearChatHistory()
    staffBot.clearSystemMessages()

    try { await unmutePlayer(TARGET_USERNAME) } catch { /* ignore */ }

    await sleep(500)
  })

  test('online-only mute blocks chat when player is online', async () => {
    // Create target bot
    targetBot = await createBot(TARGET_USERNAME)
    await waitFor(
      async () => await isPlayerInList(TARGET_USERNAME),
      { timeout: 10000, interval: 500, message: 'Target not in player list' }
    )

    await sleep(1000)
    staffBot.clearSystemMessages()

    // Apply online-only temp mute for 1 minute
    await tempMutePlayerOnlineOnly(TARGET_USERNAME, '1m', 'Testing online-only mute')
    await sleep(2000)

    // Target should receive mute notification with "online time" text
    const targetMessages = targetBot.getSystemMessages()
    const muteNotification = targetMessages.find(m =>
      m.message.includes('temporarily muted') && m.message.includes('online time')
    )
    expect(muteNotification).toBeDefined()

    // Staff should receive notification with "online time" text
    const staffMessages = staffBot.getSystemMessages()
    const staffNotification = staffMessages.find(m =>
      m.message.includes(TARGET_USERNAME) &&
      m.message.includes('temporarily muted') &&
      m.message.includes('online time')
    )
    expect(staffNotification).toBeDefined()

    // Target tries to chat - should be blocked
    targetBot.clearSystemMessages()
    await targetBot.sendChat('This should be blocked')
    await sleep(1000)

    // Target should receive mute denial message
    const denialMessages = targetBot.getSystemMessages()
    const hasDenial = denialMessages.some(m =>
      m.message.includes('muted') || m.message.includes('cannot')
    )
    expect(hasDenial).toBe(true)

    // Staff should NOT see the blocked message in chat
    await staffBot.expectNoChatFrom(TARGET_USERNAME, 2000)

    // Cleanup
    await targetBot.disconnect()
    targetBot = null
  }, 60000)

  test('online-only mute timer pauses on disconnect and resumes on reconnect', async () => {
    // Create target bot
    targetBot = await createBot(TARGET_USERNAME)
    await waitFor(
      async () => await isPlayerInList(TARGET_USERNAME),
      { timeout: 10000, interval: 500, message: 'Target not in player list' }
    )

    await sleep(1000)

    // Apply online-only temp mute for 20 seconds
    await tempMutePlayerOnlineOnly(TARGET_USERNAME, '20s', 'Testing pause/resume')
    await sleep(2000)

    // Wait 5 seconds with the player online (timer counting down)
    await sleep(5000)

    // Disconnect the player - this should pause the timer
    console.log('Disconnecting target to pause mute timer...')
    await targetBot.disconnect()
    targetBot = null

    await waitFor(
      async () => !(await isPlayerInList(TARGET_USERNAME)),
      { timeout: 10000, interval: 500, message: 'Target still in player list after disconnect' }
    )

    // Wait 10 seconds while offline (timer should NOT count down)
    console.log('Waiting 10 seconds while player is offline (timer should be paused)...')
    await sleep(10000)

    // Reconnect the player - timer should resume with ~15 seconds remaining
    console.log('Reconnecting target to resume mute timer...')
    targetBot = await createBot(TARGET_USERNAME)
    await waitFor(
      async () => await isPlayerInList(TARGET_USERNAME),
      { timeout: 10000, interval: 500, message: 'Target not in player list after reconnect' }
    )

    await sleep(1000)

    // Player should still be muted (if timer wasn't paused, it would have expired)
    targetBot.clearSystemMessages()
    await targetBot.sendChat('Should still be muted after reconnect')
    await sleep(1000)

    // Target should receive mute denial
    const denialMessages = targetBot.getSystemMessages()
    const stillMuted = denialMessages.some(m =>
      m.message.includes('muted') || m.message.includes('cannot')
    )

    expect(stillMuted).toBe(true)

    // Staff should NOT see the message
    await staffBot.expectNoChatFrom(TARGET_USERNAME, 2000)

    // Cleanup
    await targetBot.disconnect()
    targetBot = null
  }, 120000)

  test('online-only mute expires after sufficient online time', async () => {
    // Create target bot
    targetBot = await createBot(TARGET_USERNAME)
    await waitFor(
      async () => await isPlayerInList(TARGET_USERNAME),
      { timeout: 10000, interval: 500, message: 'Target not in player list' }
    )

    await sleep(1000)

    // Apply online-only temp mute for 10 seconds
    // Note: Expiry depends on ExpiresSync schedule (typically runs every 30-60s)
    await tempMutePlayerOnlineOnly(TARGET_USERNAME, '10s', 'Testing expiry')
    await sleep(2000)

    // Verify muted initially
    targetBot.clearSystemMessages()
    await targetBot.sendChat('Should be blocked initially')
    await sleep(1000)

    const initialDenial = targetBot.getSystemMessages().some(m =>
      m.message.includes('muted') || m.message.includes('cannot')
    )
    expect(initialDenial).toBe(true)

    // Wait for mute to expire + time for ExpiresSync to process
    // ExpiresSync runs every 5 seconds in test environment (schedules.yml)
    console.log('Waiting for online-only mute to expire and be processed by ExpiresSync...')
    await sleep(20000) // 10s mute + ~10s buffer for ExpiresSync cycles

    // Now player should be able to chat
    targetBot.clearSystemMessages()
    staffBot.clearChatHistory()

    // Start waiting for chat BEFORE sending, to ensure we catch the message
    // waitForChat uses startIndex at call time, so we need to call it first
    const chatPromise = staffBot.waitForChat(TARGET_USERNAME, 'can chat now', 10000)
      .catch(err => {
        // If chat message wasn't received, check if there's any mute denial
        if (targetBot != null) {
          const stillDenied = targetBot.getSystemMessages().some(m =>
            m.message.includes('muted') || m.message.includes('cannot')
          )
          if (stillDenied) {
            throw new Error('Player is still muted after expiry time - ExpiresSync may not have processed it yet')
          }
        }
        throw err
      })

    // Small delay to ensure waitForChat's startIndex is captured
    await sleep(100)

    // Now send the chat
    await targetBot.sendChat('I can chat now after mute expired')

    // Wait for the chat to be received
    await chatPromise

    // Cleanup
    await targetBot.disconnect()
    targetBot = null
  }, 60000)
})
