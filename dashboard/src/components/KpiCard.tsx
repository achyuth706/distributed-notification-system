import React from 'react'
import { theme } from '../theme'

interface Props {
  title: string
  value: string | number
  subtitle?: string
  color?: string
}

export default function KpiCard({ title, value, subtitle, color = theme.accent }: Props) {
  return (
    <div style={{
      background: theme.card, border: `1px solid ${theme.cardBorder}`,
      borderRadius: 8, padding: '20px 24px', flex: 1, minWidth: 180,
    }}>
      <div style={{ fontSize: 11, color: theme.textDim, letterSpacing: 2, marginBottom: 8 }}>
        {title.toUpperCase()}
      </div>
      <div style={{ fontSize: 32, fontWeight: 700, color, lineHeight: 1 }}>{value}</div>
      {subtitle && <div style={{ fontSize: 12, color: theme.textDim, marginTop: 6 }}>{subtitle}</div>}
    </div>
  )
}
