import { Stats, NotificationEvent } from './types'

const BASE = 'http://localhost:8080'

export async function fetchStats(): Promise<Stats> {
  const res = await fetch(`${BASE}/api/v1/notifications/stats`)
  if (!res.ok) throw new Error('Failed to fetch stats')
  return res.json()
}

export async function fetchStream(): Promise<NotificationEvent[]> {
  const res = await fetch(`${BASE}/api/v1/notifications/stream`)
  if (!res.ok) throw new Error('Failed to fetch stream')
  const raw: string[] = await res.json()
  return raw.map(s => {
    try { return JSON.parse(s) } catch { return null }
  }).filter(Boolean) as NotificationEvent[]
}

export async function sendTestNotification(): Promise<void> {
  const channels = ['EMAIL', 'SMS', 'PUSH']
  const priorities = ['CRITICAL', 'HIGH', 'NORMAL', 'LOW']
  const templates = ['WELCOME', 'ALERT', 'TRANSACTION']
  const randomChannel = channels[Math.floor(Math.random() * channels.length)]
  const randomPriority = priorities[Math.floor(Math.random() * priorities.length)]
  const randomTemplate = templates[Math.floor(Math.random() * templates.length)]
  const userId = `user_${Math.floor(Math.random() * 1000).toString().padStart(4, '0')}`

  await fetch(`${BASE}/api/v1/notifications`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      userId,
      channels: [randomChannel],
      templateId: randomTemplate,
      priority: randomPriority,
      subject: `Test ${randomTemplate} notification`,
      body: `Hello, this is a test ${randomTemplate.toLowerCase()} notification for ${userId}.`,
      variables: {
        firstName: userId,
        amount: '$' + (Math.random() * 1000).toFixed(2),
        transactionId: 'TXN' + Math.random().toString(36).substring(2, 10).toUpperCase(),
        date: new Date().toLocaleDateString(),
        alertCode: 'ALT' + Math.floor(Math.random() * 9999)
      }
    })
  })
}
