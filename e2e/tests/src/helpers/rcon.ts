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
export async function reloadPlugin (): Promise<string> {
  return await sendCommand('bmreload')
}

export async function mutePlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`mute ${player} ${reason}`)
}

export async function unmutePlayer (player: string): Promise<string> {
  return await sendCommand(`unmute ${player}`)
}

export async function getPlayerList (): Promise<string> {
  return await sendCommand('list')
}

export async function banPlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`ban ${player} ${reason}`)
}

export async function unbanPlayer (player: string): Promise<string> {
  return await sendCommand(`unban ${player}`)
}

export async function opPlayer (player: string): Promise<string> {
  return await sendCommand(`op ${player}`)
}

export async function deopPlayer (player: string): Promise<string> {
  return await sendCommand(`deop ${player}`)
}

export async function warnPlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`warn ${player} ${reason}`)
}

export async function addNote (player: string, message: string = 'E2E Test Note'): Promise<string> {
  return await sendCommand(`addnote ${player} ${message}`)
}

export async function kickPlayer (player: string, reason: string = 'E2E Test'): Promise<string> {
  return await sendCommand(`kick ${player} ${reason}`)
}

export async function reportPlayer (player: string, reason: string = 'E2E Test Report'): Promise<string> {
  return await sendCommand(`report ${player} ${reason}`)
}
