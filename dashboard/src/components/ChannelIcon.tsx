import React from 'react'
const icons: Record<string, string> = { EMAIL: '✉️', SMS: '📱', PUSH: '🔔' }
export default function ChannelIcon({ channel, size = 16 }: { channel: string; size?: number }) {
  return <span style={{ fontSize: size }}>{icons[channel] || '❓'}</span>
}
