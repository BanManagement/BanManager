import { Rcon } from 'rcon-client'

const RCON_HOST = process.env.RCON_HOST ?? 'localhost'
const RCON_PORT = parseInt(process.env.RCON_PORT ?? '25575', 10)
const RCON_PASSWORD = process.env.RCON_PASSWORD ?? 'testing'

// Proxy mode: When testing on a proxy (Velocity, BungeeCord, etc.), some commands
// like 'list' and 'op' need to be sent to the backend server instead
const IS_PROXY = process.env.IS_PROXY === 'true'

// Backend server RCON for commands that don't exist on proxies
const BACKEND_RCON_HOST = process.env.BACKEND_RCON_HOST ?? ''
const BACKEND_RCON_PORT = parseInt(process.env.BACKEND_RCON_PORT ?? '25575', 10)
const BACKEND_RCON_PASSWORD = process.env.BACKEND_RCON_PASSWORD ?? 'testing'

let rconClient: Rcon | null = null
let backendRconClient: Rcon | null = null

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

/**
 * Connect to the backend server's RCON (used on proxies for 'list', 'op' commands)
 */
async function connectBackendRcon (): Promise<Rcon> {
  if (backendRconClient?.authenticated === true) {
    return backendRconClient
  }

  backendRconClient = await Rcon.connect({
    host: BACKEND_RCON_HOST,
    port: BACKEND_RCON_PORT,
    password: BACKEND_RCON_PASSWORD
  })

  console.log(`Connected to backend RCON at ${BACKEND_RCON_HOST}:${BACKEND_RCON_PORT}`)
  return backendRconClient
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
  if (backendRconClient != null) {
    await backendRconClient.end()
    backendRconClient = null
    console.log('Disconnected from backend RCON')
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
  // On proxies, use the backend server's RCON for 'list' command
  if (IS_PROXY && BACKEND_RCON_HOST !== '') {
    const client = await connectBackendRcon()
    return await client.send('list')
  }
  return await sendCommand('list')
}

/**
 * Check if a player is in the player list.
 * On proxies, we query the backend server's RCON since proxies don't have the 'list' command.
 */
export async function isPlayerInList (playerName: string): Promise<boolean> {
  if (IS_PROXY && BACKEND_RCON_HOST !== '') {
    // Query backend server's RCON for the player list
    const client = await connectBackendRcon()
    const list = await client.send('list')
    console.log(`Backend RCON list response: ${list}`)
    return list.includes(playerName)
  }
  const list = await sendCommand('list')
  return list.includes(playerName)
}

/**
 * Check if running on a proxy (Velocity, BungeeCord, etc.)
 */
export function isProxy (): boolean {
  return IS_PROXY
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
  // Proxies don't have 'op' command - send to backend server
  // Note: On Velocity, BanManager permissions are pre-configured via LuckPerms default group
  if (IS_PROXY && BACKEND_RCON_HOST !== '') {
    const client = await connectBackendRcon()
    const response = await client.send(`op ${player}`)
    console.log(`Backend RCON op response: ${response}`)
    return response
  }
  return await sendCommand(`op ${player}`)
}

export async function deopPlayer (player: string): Promise<string> {
  // Proxies don't have 'deop' command - send to backend server
  if (IS_PROXY && BACKEND_RCON_HOST !== '') {
    const client = await connectBackendRcon()
    const response = await client.send(`deop ${player}`)
    console.log(`Backend RCON deop response: ${response}`)
    return response
  }
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
