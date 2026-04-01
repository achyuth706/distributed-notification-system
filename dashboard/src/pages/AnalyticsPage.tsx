import React, { useEffect, useMemo, useState } from 'react'
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts'
import { fetchStats } from '../api'
import { Stats, NotificationEvent, TimeSeriesPoint } from '../types'
import { theme } from '../theme'

interface Props { recentEvents: NotificationEvent[] }
const PIE_COLORS = [theme.accent, theme.success, theme.warning]
const ttStyle = { background: theme.card, border: `1px solid ${theme.cardBorder}`, borderRadius: 6, fontSize: 11 }

export default function AnalyticsPage({ recentEvents }: Props) {
  const [stats, setStats] = useState<Stats | null>(null)

  useEffect(() => {
    const load = () => fetchStats().then(setStats).catch(() => {})
    load()
    const id = setInterval(load, 10000)
    return () => clearInterval(id)
  }, [])

  const timeSeriesData = useMemo((): TimeSeriesPoint[] => {
    const now = Date.now()
    const buckets: Record<string, TimeSeriesPoint> = {}
    for (let i = 59; i >= 0; i--) {
      const t = new Date(now - i * 60000)
      const key = `${t.getHours().toString().padStart(2,'0')}:${t.getMinutes().toString().padStart(2,'0')}`
      buckets[key] = { time: key, email: 0, sms: 0, push: 0, total: 0 }
    }
    recentEvents.forEach(ev => {
      const t = new Date(ev.timestamp)
      const key = `${t.getHours().toString().padStart(2,'0')}:${t.getMinutes().toString().padStart(2,'0')}`
      if (buckets[key]) {
        buckets[key].total++
        if (ev.channel === 'EMAIL') buckets[key].email++
        else if (ev.channel === 'SMS') buckets[key].sms++
        else buckets[key].push++
      }
    })
    return Object.values(buckets)
  }, [recentEvents])

  const barData = [
    { channel: 'EMAIL', count: stats?.byChannel?.EMAIL ?? 0 },
    { channel: 'SMS',   count: stats?.byChannel?.SMS   ?? 0 },
    { channel: 'PUSH',  count: stats?.byChannel?.PUSH  ?? 0 },
  ]
  const pieData = barData.filter(d => d.count > 0)

  const card = { background: theme.card, border: `1px solid ${theme.cardBorder}`, borderRadius: 8, padding: 20, marginBottom: 20 }

  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: 20, fontWeight: 600 }}>Analytics</h1>
        <p style={{ fontSize: 12, color: theme.textDim, marginTop: 4 }}>Performance metrics · updates every 10s</p>
      </div>

      <div style={card}>
        <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 16 }}>Notifications Over Time (last 60 min)</div>
        <ResponsiveContainer width="100%" height={220}>
          <LineChart data={timeSeriesData}>
            <CartesianGrid strokeDasharray="3 3" stroke={theme.cardBorder} />
            <XAxis dataKey="time" tick={{ fill: theme.textDim, fontSize: 10 }} interval={9} />
            <YAxis tick={{ fill: theme.textDim, fontSize: 10 }} allowDecimals={false} />
            <Tooltip contentStyle={ttStyle} />
            <Legend wrapperStyle={{ fontSize: 11 }} />
            <Line type="monotone" dataKey="email" stroke={theme.accent}   strokeWidth={2} dot={false} name="Email" />
            <Line type="monotone" dataKey="sms"   stroke={theme.success}  strokeWidth={2} dot={false} name="SMS" />
            <Line type="monotone" dataKey="push"  stroke={theme.warning}  strokeWidth={2} dot={false} name="Push" />
          </LineChart>
        </ResponsiveContainer>
      </div>

      <div style={{ display: 'flex', gap: 20 }}>
        <div style={{ ...card, flex: 1, marginBottom: 0 }}>
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 16 }}>Sent per Channel</div>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={barData}>
              <CartesianGrid strokeDasharray="3 3" stroke={theme.cardBorder} />
              <XAxis dataKey="channel" tick={{ fill: theme.textDim, fontSize: 10 }} />
              <YAxis tick={{ fill: theme.textDim, fontSize: 10 }} allowDecimals={false} />
              <Tooltip contentStyle={ttStyle} />
              <Bar dataKey="count" fill={theme.accent} radius={[4,4,0,0]} name="Count" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div style={{ ...card, flex: 1, marginBottom: 0 }}>
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 16 }}>Channel Distribution</div>
          {pieData.length === 0
            ? <div style={{ height: 200, display: 'flex', alignItems: 'center', justifyContent: 'center', color: theme.textDim, fontSize: 12 }}>No data yet</div>
            : (
              <ResponsiveContainer width="100%" height={200}>
                <PieChart>
                  <Pie data={pieData} dataKey="count" nameKey="channel" cx="50%" cy="50%" outerRadius={72}
                    label={({ channel, percent }: { channel: string; percent: number }) => `${channel} ${(percent*100).toFixed(0)}%`}
                    labelLine={false}>
                    {pieData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                  </Pie>
                  <Tooltip contentStyle={ttStyle} />
                </PieChart>
              </ResponsiveContainer>
            )
          }
        </div>
      </div>
    </div>
  )
}
