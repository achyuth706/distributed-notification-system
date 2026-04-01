import React, { useState, useCallback, useEffect } from 'react'
import { Page, NotificationEvent } from './types'
import { theme } from './theme'
import Sidebar from './components/Sidebar'
import DashboardPage from './pages/DashboardPage'
import LiveFeedPage from './pages/LiveFeedPage'
import AnalyticsPage from './pages/AnalyticsPage'
import DeadLettersPage from './pages/DeadLettersPage'
import SettingsPage from './pages/SettingsPage'
import { sendTestNotification, fetchStream } from './api'

export default function App() {
  const [page, setPage] = useState<Page>('dashboard')
  const [events, setEvents] = useState<NotificationEvent[]>([])
  const [simulating, setSimulating] = useState(false)

  // Bootstrap with last 50 events from Redis cache
  useEffect(() => {
    fetchStream().then(setEvents).catch(() => {})
  }, [])

  const handleNewEvent = useCallback((ev: NotificationEvent) => {
    setEvents(prev => [ev, ...prev].slice(0, 200))
  }, [])

  const handleSimulate = async () => {
    setSimulating(true)
    try {
      await Promise.all(Array.from({ length: 5 }, () => sendTestNotification()))
    } finally {
      setSimulating(false)
    }
  }

  const renderPage = () => {
    switch (page) {
      case 'dashboard':    return <DashboardPage recentEvents={events} />
      case 'livefeed':     return <LiveFeedPage onEvent={handleNewEvent} />
      case 'analytics':    return <AnalyticsPage recentEvents={events} />
      case 'deadletters':  return <DeadLettersPage recentEvents={events} />
      case 'settings':     return <SettingsPage />
    }
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: theme.bg, color: theme.text }}>
      <Sidebar currentPage={page} onNavigate={setPage} />

      <div style={{ marginLeft: 220, flex: 1, display: 'flex', flexDirection: 'column' }}>
        {/* Top bar */}
        <div style={{
          padding: '0 24px', height: 56,
          borderBottom: `1px solid ${theme.cardBorder}`,
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          background: theme.card, position: 'sticky', top: 0, zIndex: 50,
        }}>
          <div style={{ fontSize: 11, color: theme.textDim, letterSpacing: 1 }}>
            NOTIFICATION DELIVERY SYSTEM · {new Date().toLocaleDateString()}
          </div>
          <button
            onClick={handleSimulate}
            disabled={simulating}
            style={{
              padding: '8px 20px', border: 'none', borderRadius: 6,
              background: simulating ? `${theme.accent}55` : theme.accent,
              color: '#000', fontSize: 12, fontWeight: 700,
              fontFamily: 'inherit', letterSpacing: 1,
              cursor: simulating ? 'not-allowed' : 'pointer',
              transition: 'background 0.2s',
            }}
          >
            {simulating ? '⚡ SENDING...' : '⚡ SIMULATE'}
          </button>
        </div>

        {/* Page content */}
        <main style={{ flex: 1, padding: 24, overflowY: 'auto' }}>
          {renderPage()}
        </main>
      </div>
    </div>
  )
}
