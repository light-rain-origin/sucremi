import { describe, it, expect } from 'vitest'
import { formatDateTime } from '../datetime'

describe('formatDateTime', () => {
  it('returns a dash for empty input', () => {
    expect(formatDateTime(null)).toBe('—')
    expect(formatDateTime(undefined)).toBe('—')
    expect(formatDateTime('')).toBe('—')
  })

  it('formats an ISO-ish local datetime for display', () => {
    const text = formatDateTime('2026-10-12T11:34:37')
    expect(text).toContain('2026')
    expect(text).toContain('11:34:37')
  })

  it('falls back to the raw string when it cannot be parsed', () => {
    expect(formatDateTime('not-a-date')).toBe('not-a-date')
  })
})
