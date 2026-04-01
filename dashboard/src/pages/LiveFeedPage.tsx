import React, { useEffect, useRef, useState, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { NotificationEvent } from '../types'
import { theme } from '../theme'
import StatusBadge from '../components/StatusBadge'
import ChannelIcon from '../components/ChannelIcon'

type ConnStatus = 'CONNECTED' | 'DISCONNECTED' | 'RECONNECTING'
interface Props { onEvent: (e: NotificationEvent) => void }

export default function LiveFeedPage({ onEvent }: Props) {
  const [events, setEvents] = useState<NotificationEvent[]>([])
  const [status, setStatus] = useState<ConnStatus>('DISCONNECTED')
  const [flashId, setFlashId] = useState<string | null>(null)
  const clientRef = useRef<Client | null>(null)

  const handleMsg = useCallback((raw: string) => {
    try {
      const ev: NotificationEvent = JSON.parse(raw)
      setEvents(prev => [ev, ...prev].slice(0, 100))
      setFlashId(ev.id)
      onEvent(ev)
      setTimeout(() => setFlashId(null), 500)
    } catch { /* ignore malformed */ }
  }, [onEvent])

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      reconnectDelay: 3000,
      onConnect: () => {
        setStatus('CONNECTED')
        client.subscribe('/topic/notifications', msg => handleMsg(msg.body))
      },
      onDisconnect: () => setStatus('DISCONNECTED'),
      onStompError:    () => setStatus('RECONNECTING'),
      onWebSocketError: () => setStatus('RECONNECTING'),
    })
    client.activate()
    clientRef.current = client
    return () => { client.deactivate() }
  }, [handleMsg])

  const sc: Record<ConnStatus, string> = { CONNECTED: theme.success, DISCONNECTED: theme.error, RECONNECTING: theme.warning }

  return (
    <div>
      <style>{`@keyframes ndsPulse{0%,100%{opacity:1}50%{opacity:.3}}`}</style>

      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <div>
          <h1 style={{ fontSize: 20, fontWeight: 600 }}>Live Feed</h1>
          <p style={{ fontSize: 12, color: theme.textDim, marginTop: 4 }}>Real-time WebSocket stream from /topic/notifications</p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          {status === 'CONNECTED' && (
            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <div style={{ width: 8, height: 8, borderRadius: '50%', background: theme.error, animation: 'ndsPulse 1.4s infinite' }} />
              <span style={{ color: theme.error, fontSize: 11, fontWeight: 700, letterSpacing: 1 }}>LIVE</span>
            </div>
          )}
          <div style={{ padding: '6px 14px', background: `${sc[status]}22`, border: `1px solid ${sc[status]}44`, borderRadius: 6, color: sc[status], fontSize: 11, fontWeight: 600 }}>
            {status}
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        {events.length === 0 && (
          <div style={{ padding: '48px', textAlign: 'center', color: theme.textDim, fontSize: 13, background: theme.card, border: `1px solid ${theme.cardBorder}`, borderRadius: 8 }}>
            {status === 'CONNECTED' ? '✅ Connected — click ⚡ SIMULATE to generate events' : `Connecting to ws://localhost:8080/ws…`}
          </div>
        )}
        {events.map(ev => (
          <div key={ev.id} style={{
            background: flashId === ev.id ? `${theme.success}12` : theme.card,
            border: `1px solid ${flashId === ev.id ? theme.success : theme.cardBorder}`,
            borderRadius: 6, padding: '12px 16px',
            display: 'flex', alignItems: 'center', gap: 16,
            transition: 'border-color 0.3s, background 0.3s',
          }}>
            <ChannelIcon channel={ev.channel} size={22} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 2 }}>
                <span style={{ fontSize: 12, fontWeight: 600 }}>{ev.userId}</span>
                <StatusBadge status={ev.status} />
              </div>
              <div style={{ fontSize: 10, color: theme.textDim }}>{ev.id.substring(0, 16)}… · {ev.channel}</div>
            </div>
            <div style={{ textAlign: 'right', flexShrink: 0 }}>
              <div style={{ fontSize: 11, color: theme.accent, fontWeight: 600 }}>{ev.processingTimeMs}ms</div>
              <div style={{ fontSize: 10, color: theme.textDim }}>{new Date(ev.timestamp).toLocaleTimeString()}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
