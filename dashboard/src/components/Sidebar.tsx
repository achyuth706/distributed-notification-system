import React from 'react'
import { theme } from '../theme'
import { Page } from '../types'

interface Props {
  currentPage: Page
  onNavigate: (page: Page) => void
}

const navItems: { page: Page; label: string; icon: string }[] = [
  { page: 'dashboard',   label: 'Dashboard',   icon: '⬛' },
  { page: 'livefeed',    label: 'Live Feed',    icon: '📡' },
  { page: 'analytics',   label: 'Analytics',    icon: '📊' },
  { page: 'deadletters', label: 'Dead Letters', icon: '💀' },
  { page: 'settings',    label: 'Settings',     icon: '⚙️' },
]

export default function Sidebar({ currentPage, onNavigate }: Props) {
  return (
    <div style={{
      width: 220, minHeight: '100vh', background: theme.sidebar,
      borderRight: `1px solid ${theme.sidebarBorder}`,
      display: 'flex', flexDirection: 'column',
      position: 'fixed', top: 0, left: 0, zIndex: 100,
    }}>
      <div style={{ padding: '24px 20px', borderBottom: `1px solid ${theme.sidebarBorder}` }}>
        <div style={{ fontSize: 22, fontWeight: 700, color: theme.accent, letterSpacing: 1 }}>⚡ NDS</div>
        <div style={{ fontSize: 10, color: theme.textDim, marginTop: 4, letterSpacing: 2 }}>NOTIFICATION DELIVERY</div>
      </div>
      <nav style={{ flex: 1, padding: '12px 0' }}>
        {navItems.map(item => (
          <button key={item.page} onClick={() => onNavigate(item.page)} style={{
            display: 'flex', alignItems: 'center', gap: 10, width: '100%',
            padding: '12px 20px',
            background: currentPage === item.page ? `${theme.accent}15` : 'transparent',
            border: 'none',
            borderLeft: currentPage === item.page ? `3px solid ${theme.accent}` : '3px solid transparent',
            color: currentPage === item.page ? theme.accent : theme.textDim,
            fontSize: 13, fontFamily: 'inherit', cursor: 'pointer', textAlign: 'left',
            transition: 'all 0.15s',
          }}>
            <span style={{ fontSize: 14 }}>{item.icon}</span>
            {item.label}
          </button>
        ))}
      </nav>
      <div style={{
        padding: '16px 20px', borderTop: `1px solid ${theme.sidebarBorder}`,
        display: 'flex', alignItems: 'center', gap: 8,
      }}>
        <div style={{ width: 8, height: 8, borderRadius: '50%', background: theme.success, boxShadow: `0 0 6px ${theme.success}` }} />
        <span style={{ fontSize: 11, color: theme.textDim }}>All Systems Operational</span>
      </div>
    </div>
  )
}
