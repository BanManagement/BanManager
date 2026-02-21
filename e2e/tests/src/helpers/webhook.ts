const WEBHOOK_SINK_URL = process.env.WEBHOOK_SINK_URL ?? 'http://localhost:18080'

type WebhookRequest = {
  method: string
  url: string
  headers: Record<string, string | string[] | undefined>
  body: string
  receivedAt: number
}

type WebhookResponse = {
  count: number
  requests: WebhookRequest[]
}

async function parseResponse<T> (response: Response): Promise<T> {
  if (!response.ok) {
    const text = await response.text()
    throw new Error(`Webhook sink request failed (${response.status}): ${text}`)
  }

  return await response.json() as T
}

export async function resetWebhookSink (): Promise<void> {
  const response = await fetch(`${WEBHOOK_SINK_URL}/requests`, {
    method: 'DELETE'
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(`Failed to reset webhook sink (${response.status}): ${text}`)
  }
}

export async function getWebhookRequests (): Promise<WebhookRequest[]> {
  const response = await fetch(`${WEBHOOK_SINK_URL}/requests`)
  const data = await parseResponse<WebhookResponse>(response)
  return data.requests
}
