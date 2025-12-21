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
