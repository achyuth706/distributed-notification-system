import React from 'react'
import { theme } from '../theme'

const statusColors: Record<string, string> = {
  SENT: theme.success,
  PENDING: theme.warning,
  PROCESSING: theme.accent,
  FAILED: theme.error,
  RATE_LIMITED: '#ff8800',
  DEAD_LETTERED: '#aa0044',
}

export default function StatusBadge({ status }: { status: string }) {
  const color = statusColors[status] || theme.textDim
  return (
    <span style={{
      display: 'inline-block', padding: '2px 8px', borderRadius: 4,
      background: `${color}22`, border: `1px solid ${color}44`,
      color, fontSize: 10, fontWeight: 600, letterSpacing: 1,
    }}>{status}</span>
  )
}
