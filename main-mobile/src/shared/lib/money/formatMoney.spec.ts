import { formatMoney } from './formatMoney'

describe('formatMoney', () => {
  it('USD 금액을 통화 형식으로 표시한다', () => {
    expect(formatMoney({ amount: '52.90', currency: 'USD' })).toBe('$52.90')
    expect(formatMoney({ amount: '1234.5', currency: 'USD' })).toBe('$1,234.50')
  })

  it('값이 없으면 대시를 표시한다', () => {
    expect(formatMoney(null)).toBe('-')
  })
})
