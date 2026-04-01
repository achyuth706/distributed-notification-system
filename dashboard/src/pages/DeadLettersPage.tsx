import React from 'react'
import { NotificationEvent } from '../types'
import { theme } from '../theme'
import StatusBadge from '../components/StatusBadge'
import ChannelIcon from '../components/ChannelIcon'

interface Props { recentEvents: NotificationEvent[] }

export default function DeadLettersPage({ recentEvents }: Props) {
  const dead = recentEvents.filter(e => e.status === 'DEAD_LETTERED' || e.status === 'FAILED')

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: 20, fontWeight: 600, color: theme.error }}>Dead Letters</h1>
        <p style={{ fontSize: 12, color: theme.textDim, marginTop: 4 }}>
          Failed notifications that exhausted all retry attempts · {dead.length} total
        </p>
      </div>

      <div style={{ background: theme.card, border: `1px solid ${theme.cardBorder}`, borderRadius: 8, overflow: 'hidden' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ background: '#0d0d16' }}>
              {['ID', 'User', 'Channel', 'Status', 'Last Attempt'].map(h => (
                <th key={h} style={{ padding: '10px 16px', textAlign: 'left', fontSize: 10, color: theme.textDim, letterSpacing: 1 }}>{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {dead.map((ev, i) => (
              <tr key={ev.id} style={{ borderTop: `1px solid ${theme.cardBorder}`, background: i % 2 ? '#0d0d1610' : 'transparent' }}>
                <td style={{ padding: '10px 16px', fontSize: 11, color: theme.textDim }}>{ev.id.substring(0, 12)}…</td>
                <td style={{ padding: '10px 16px', fontSize: 12 }}>{ev.userId}</td>
                <td style={{ padding: '10px 16px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                    <ChannelIcon channel={ev.channel} size={14} />
                    <span style={{ fontSize: 11 }}>{ev.channel}</span>
                  </div>
                </td>
                <td style={{ padding: '10px 16px' }}><StatusBadge status={ev.status} /></td>
                <td style={{ padding: '10px 16px', fontSize: 11, color: theme.textDim }}>{new Date(ev.timestamp).toLocaleString()}</td>
              </tr>
            ))}
            {dead.length === 0 && (
              <tr><td colSpan={5} style={{ padding: '48px', textAlign: 'center', color: theme.textDim, fontSize: 12 }}>
                ✅ No dead letters — all notifications delivered successfully
              </td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
