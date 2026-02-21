const http = require('node:http')

const PORT = Number.parseInt(process.env.PORT || '8080', 10)
const requests = []

function sendJson (res, statusCode, body) {
  res.writeHead(statusCode, { 'Content-Type': 'application/json' })
  res.end(JSON.stringify(body))
}

const server = http.createServer((req, res) => {
  if (req.method === 'GET' && req.url === '/health') {
    return sendJson(res, 200, { ok: true })
  }

  if (req.method === 'GET' && req.url === '/requests') {
    return sendJson(res, 200, { count: requests.length, requests })
  }

  if (req.method === 'DELETE' && req.url === '/requests') {
    requests.length = 0
    return sendJson(res, 200, { ok: true })
  }

  if (req.method === 'POST' && req.url === '/webhook') {
    let body = ''
    req.on('data', chunk => {
      body += chunk
    })
    req.on('end', () => {
      requests.push({
        method: req.method,
        url: req.url,
        headers: req.headers,
        body,
        receivedAt: Date.now()
      })
      sendJson(res, 200, { ok: true })
    })
    return
  }

  sendJson(res, 404, { error: 'Not found' })
})

server.listen(PORT, '0.0.0.0', () => {
  // eslint-disable-next-line no-console
  console.log(`webhook sink listening on ${PORT}`)
})
