import type { Money } from '@/shared/api'

/**
 * 금액 표시용 포맷 — 계산에는 사용하지 않는다 (금액 계산은 항상 서버).
 */
export const formatMoney = (money: Money | null | undefined, locale = 'en-US'): string => {
  if (!money) return '-'
  const value = Number(money.amount)
  if (Number.isNaN(value)) return `${money.currency} ${money.amount}`
  return new Intl.NumberFormat(locale, { style: 'currency', currency: money.currency }).format(value)
}
