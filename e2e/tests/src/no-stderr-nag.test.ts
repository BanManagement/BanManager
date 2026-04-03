import { readFileSync, existsSync } from 'fs'

const SERVER_LOG_PATH = '/server-data/logs/latest.log'

// Only runs on Bukkit (Paper) — proxy platforms won't have the log file
const describeIfBukkit = existsSync(SERVER_LOG_PATH) ? describe : describe.skip

describeIfBukkit('No stderr nag', () => {
  test('server log should not contain Paper System.out/err nag for BanManager', () => {
    const log = readFileSync(SERVER_LOG_PATH, 'utf-8')
    const nagLines = log.split('\n').filter(line =>
      line.includes('Please use your plugin\'s logger instead') &&
      line.includes('BanManager')
    )
    expect(nagLines).toHaveLength(0)
  })

  test('server log should not contain [STDERR] lines from BanManager packages', () => {
    const log = readFileSync(SERVER_LOG_PATH, 'utf-8')
    const stderrLines = log.split('\n').filter(line =>
      line.includes('[STDERR]') &&
      line.includes('me.confuser.banmanager')
    )
    expect(stderrLines).toHaveLength(0)
  })
})
