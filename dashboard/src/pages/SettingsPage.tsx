import React from 'react'
import { theme } from '../theme'

const links = [
  ['API Gateway',        'http://localhost:8080'],
  ['Swagger UI',         'http://localhost:8080/swagger-ui.html'],
  ['WebSocket endpoint', 'ws://localhost:8080/ws'],
  ['Notification Svc',   'http://localhost:8081'],
  ['Email Service',      'http://localhost:8082'],
  ['SMS Service',        'http://localhost:8083/api/v1/sms/sent'],
  ['Push Service',       'http://localhost:8084/api/v1/push/tokens'],
  ['Prometheus',         'http://localhost:9090'],
  ['Grafana',            'http://localhost:3001'],
  ['Mailhog UI',         'http://localhost:8025'],
]

export default function SettingsPage() {
  return (
    <div>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: 20, fontWeight: 600 }}>Settings</h1>
        <p style={{ fontSize: 12, color: theme.textDim, marginTop: 4 }}>Service endpoints and configuration</p>
      </div>
      <div style={{ background: theme.card, border: `1px solid ${theme.cardBorder}`, borderRadius: 8, padding: 24 }}>
        {links.map(([label, url]) => (
          <div key={label} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 0', borderBottom: `1px solid ${theme.cardBorder}`, fontSize: 13 }}>
            <span style={{ color: theme.textDim }}>{label}</span>
            <a href={url} target="_blank" rel="noopener noreferrer" style={{ color: theme.accent, textDecoration: 'none', fontFamily: 'inherit', fontSize: 12 }}>{url}</a>
          </div>
        ))}
      </div>
    </div>
  )
}
