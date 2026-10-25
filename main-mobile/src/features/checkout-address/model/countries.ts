/**
 * 배송 국가 목록 (ISO 3166-1 alpha-2).
 * 실제 배송 가능 국가 정책에 맞게 조정해서 사용한다.
 */
export const SHIPPING_COUNTRIES = [
  { code: 'US', name: 'United States' },
  { code: 'CA', name: 'Canada' },
  { code: 'GB', name: 'United Kingdom' },
  { code: 'AU', name: 'Australia' },
  { code: 'NZ', name: 'New Zealand' },
  { code: 'DE', name: 'Germany' },
  { code: 'FR', name: 'France' },
  { code: 'IT', name: 'Italy' },
  { code: 'ES', name: 'Spain' },
  { code: 'NL', name: 'Netherlands' },
  { code: 'IE', name: 'Ireland' },
  { code: 'SE', name: 'Sweden' },
  { code: 'NO', name: 'Norway' },
  { code: 'PL', name: 'Poland' },
  { code: 'JP', name: 'Japan' },
  { code: 'SG', name: 'Singapore' },
  { code: 'MY', name: 'Malaysia' },
  { code: 'PH', name: 'Philippines' },
  { code: 'TH', name: 'Thailand' },
  { code: 'HK', name: 'Hong Kong' },
  { code: 'TW', name: 'Taiwan' },
  { code: 'AE', name: 'United Arab Emirates' },
  { code: 'MX', name: 'Mexico' },
  { code: 'BR', name: 'Brazil' },
] as const

/** 주/도(admin_area_1) 입력이 필요한 국가 */
export const STATE_REQUIRED_COUNTRIES: ReadonlySet<string> = new Set(['US', 'CA', 'AU', 'MX', 'BR'])

/** 우편번호가 없는 국가 */
export const POSTAL_CODE_OPTIONAL_COUNTRIES: ReadonlySet<string> = new Set(['HK', 'AE', 'IE'])
