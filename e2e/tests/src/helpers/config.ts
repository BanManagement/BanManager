/**
 * Sleep utility
 */
export async function sleep (ms: number): Promise<void> {
  return await new Promise((resolve) => setTimeout(resolve, ms))
}

/**
 * Wait for a condition to be true, polling at intervals
 * Much faster than fixed sleeps when condition is met early
 */
export async function waitFor (
  condition: () => boolean | Promise<boolean>,
  options: { timeout?: number, interval?: number, message?: string } = {}
): Promise<void> {
  const { timeout = 5000, interval = 100, message = 'Condition not met' } = options
  const start = Date.now()

  while (Date.now() - start < timeout) {
    const result = await condition()
    if (result) {
      return
    }
    await sleep(interval)
  }

  throw new Error(`Timeout after ${timeout}ms: ${message}`)
}

/**
 * Wait for condition with no throw - returns true/false
 */
export async function waitForOrFalse (
  condition: () => boolean | Promise<boolean>,
  options: { timeout?: number, interval?: number } = {}
): Promise<boolean> {
  try {
    await waitFor(condition, options)
    return true
  } catch {
    return false
  }
}
