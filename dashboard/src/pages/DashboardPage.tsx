import React, { useEffect, useState } from 'react'
import { fetchStats } from '../api'
import { Stats, NotificationEvent } from '../types'
import { theme } from '../theme'
import KpiCard from '../components/KpiCard'
import StatusBadge from '../components/StatusBadge'
import ChannelIcon from '../components/ChannelIcon'

interface Props { recentEvents: NotificationEvent[] }

export default function DashboardPage({ recentEvents }: Props) {
  const [stats, setStats] = useState<Stats | null>(null)
  const [error, setError] = useState<string | null>(null)

  const load = async () => {
    try { setStats(await fetchStats()); setError(null) }
    catch { setError('Cannot reach API Gateway at http://localhost:8080') }
  }

  useEffect(() => { load(); const id = setInterval(load, 5000); return () => clearInterval(id) }, [])

  const total = stats?.totalNotifications ?? 0
  const sent  = stats?.sent ?? 0
  const rate  = total > 0 ? ((sent / total) * 100).toFixed(1) : '0.0'
  const rateColor = parseFloat(rate) >= 90 ? theme.success : parseFloat(rate) >= 70 ? theme.warning : theme.error

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: 20, fontWeight: 600, color: theme.text }}>Dashboard</h1>
        <p style={{ fontSize: 12, color: theme.textDim, marginTop: 4 }}>System overview · auto-refreshes every 5s</p>
      </div>

      {error && (
        <div style={{ padding: '12px 16px', background: `${theme.error}22`, border: `1px solid ${theme.error}44`, borderRadius: 6, color: theme.error, fontSize: 12, marginBottom: 20 }}>
          ⚠️ {error}
        </div>
      )}

      {/* KPI row */}
      <div style={{ display: 'flex', gap: 16, marginBottom: 24, flexWrap: 'wrap' }}>
        <KpiCard title="Total Sent"   value={total.toLocaleString()} subtitle="All time" />
        <KpiCard title="Success Rate" value={`${rate}%`} color={rateColor} subtitle={`${sent} delivered`} />
        <KpiCard title="Failed"       value={(stats?.failed ?? 0).toLocaleString()} color={theme.error} subtitle="Delivery failures" />
        <KpiCard title="Rate Limited" value={(stats?.rateLimited ?? 0).toLocaleString()} color={theme.warning} subtitle="Throttled" />
      </div>

      {/* Channel breakdown */}
      <div style={{ display: 'flex', gap: 16, marginBottom: 24 }}>
        {(['EMAIL', 'SMS', 'PUSH'] as const).map(ch => (
          <div key={ch} style={{ flex: 1, background: theme.card, border: `1px solid ${theme.cardBorder}`, borderRadius: 8, padding: '16px 20px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
              <ChannelIcon channel={ch} size={18} />
              <span style={{ fontSize: 13, fontWeight: 600 }}>{ch}</span>
            </div>
            <div style={{ fontSize: 28, fontWeight: 700, color: theme.accent }}>{(stats?.byChannel?.[ch] ?? 0).toLocaleString()}</div>
            <div style={{ fontSize: 11, color: theme.textDim, marginTop: 4 }}>notifications</div>
          </div>
        ))}
      </div>

      {/* Recent events table */}
      <div style={{ background: theme.card, border: `1px solid ${theme.cardBorder}`, borderRadius: 8, overflow: 'hidden' }}>
        <div style={{ padding: '16px 20px', borderBottom: `1px solid ${theme.cardBorder}` }}>
          <span style={{ fontSize: 13, fontWeight: 600 }}>Recent Notifications</span>
        </div>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: '#0d0d16' }}>
                {['ID', 'User', 'Channel', 'Status', 'Time', 'Latency'].map(h => (
                  <th key={h} style={{ padding: '10px 16px', textAlign: 'left', fontSize: 10, color: theme.textDim, letterSpacing: 1 }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {recentEvents.slice(0, 10).map((ev, i) => (
                <tr key={ev.id} style={{ borderTop: `1px solid ${theme.cardBorder}`, background: i % 2 ? '#0d0d1610' : 'transparent' }}>
                  <td style={{ padding: '10px 16px', fontSize: 11, color: theme.textDim }}>{ev.id.substring(0, 8)}…</td>
                  <td style={{ padding: '10px 16px', fontSize: 12 }}>{ev.userId}</td>
                  <td style={{ padding: '10px 16px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <ChannelIcon channel={ev.channel} size={14} />
                      <span style={{ fontSize: 11 }}>{ev.channel}</span>
                    </div>
                  </td>
                  <td style={{ padding: '10px 16px' }}><StatusBadge status={ev.status} /></td>
                  <td style={{ padding: '10px 16px', fontSize: 11, color: theme.textDim }}>{new Date(ev.timestamp).toLocaleTimeString()}</td>
                  <td style={{ padding: '10px 16px', fontSize: 11, color: theme.accent }}>{ev.processingTimeMs}ms</td>
                </tr>
              ))}
              {recentEvents.length === 0 && (
                <tr><td colSpan={6} style={{ padding: '32px', textAlign: 'center', color: theme.textDim, fontSize: 12 }}>
                  No notifications yet — click ⚡ SIMULATE to generate test data
                </td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
