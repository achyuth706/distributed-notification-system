export interface NotificationEvent {
  id: string
  userId: string
  channel: 'EMAIL' | 'SMS' | 'PUSH'
  status: 'PENDING' | 'PROCESSING' | 'SENT' | 'FAILED' | 'RATE_LIMITED' | 'DEAD_LETTERED'
  timestamp: string
  processingTimeMs: number
}

export interface Stats {
  totalNotifications: number
  pending: number
  sent: number
  failed: number
  rateLimited: number
  byChannel: Record<string, number>
}

export interface TimeSeriesPoint {
  time: string
  email: number
  sms: number
  push: number
  total: number
}

export type Page = 'dashboard' | 'livefeed' | 'analytics' | 'deadletters' | 'settings'
