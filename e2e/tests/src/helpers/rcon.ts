import { Rcon } from 'rcon-client'

const RCON_HOST = process.env.RCON_HOST ?? 'localhost'
const RCON_PORT = parseInt(process.env.RCON_PORT ?? '25575', 10)
const RCON_PASSWORD = process.env.RCON_PASSWORD ?? 'testing'

let rconClient: Rcon | null = null

export async function connectRcon (): Promise<Rcon> {
  if (rconClient?.authenticated === true) {
    return rconClient
  }

  rconClient = await Rcon.connect({
    host: RCON_HOST,
    port: RCON_PORT,
    password: RCON_PASSWORD
  })

  console.log(`Connected to RCON at ${RCON_HOST}:${RCON_PORT}`)
  return rconClient
}

export async function sendCommand (command: string): Promise<string> {
  const client = await connectRcon()
  console.log(`RCON: ${command}`)
  const response = await client.send(command)
  console.log(`RCON Response: ${response}`)
  return response
}

export async function disconnectRcon (): Promise<void> {
  if (rconClient != null) {
    await rconClient.end()
    rconClient = null
    console.log('Disconnected from RCON')
  }
}

// Helper functions for common commands
// Use BanManager-prefixed commands (bmban, bmmute, etc.) for cross-platform compatibility
// This avoids conflicts with Sponge's built-in ban/mute services

export async function reloadPlugin (): Promise<string> {
  return await sendCommand('bmreload')
}

export async function mutePlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`bmmute ${player} ${reason}`)
}

export async function unmutePlayer (player: string): Promise<string> {
  return await sendCommand(`bmunmute ${player}`)
}

export async function getPlayerList (): Promise<string> {
  return await sendCommand('list')
}

export async function banPlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`bmban ${player} ${reason}`)
}

export async function unbanPlayer (player: string): Promise<string> {
  // First unban from BanManager
  const bmResult = await sendCommand(`bmunban ${player}`)
  // Also pardon from Sponge's built-in ban service (needed for Sponge compatibility)
  // The pardon command may fail if the player isn't in Sponge's ban list, which is fine
  try {
    await sendCommand(`pardon ${player}`)
  } catch {
    // Ignore errors from pardon - player may not be in Sponge's ban list
  }
  return bmResult
}

export async function opPlayer (player: string): Promise<string> {
  return await sendCommand(`op ${player}`)
}

export async function deopPlayer (player: string): Promise<string> {
  return await sendCommand(`deop ${player}`)
}

export async function warnPlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`bmwarn ${player} ${reason}`)
}

export async function addNote (player: string, message: string = 'E2E Test Note'): Promise<string> {
  // addnote is BanManager-only, no conflict with Sponge
  return await sendCommand(`addnote ${player} ${message}`)
}

export async function kickPlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`bmkick ${player} ${reason}`)
}

export async function reportPlayer (player: string, reason: string = 'E2E Test Report'): Promise<string> {
  return await sendCommand(`bmreport ${player} ${reason}`)
}

export async function tempBanPlayer (player: string, duration: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`bmtempban ${player} ${duration} ${reason}`)
}

export async function tempMutePlayer (player: string, duration: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`bmtempmute ${player} ${duration} ${reason}`)
}

export async function clearWarnings (player: string): Promise<string> {
  return await sendCommand(`bmclear ${player} warnings`)
}

// Legacy aliases kept for backwards compatibility
export async function bmBanPlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await banPlayer(player, reason)
}

export async function bmUnbanPlayer (player: string): Promise<string> {
  return await unbanPlayer(player)
}
