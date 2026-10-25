import { z } from 'zod'
import { POSTAL_CODE_OPTIONAL_COUNTRIES, STATE_REQUIRED_COUNTRIES } from './countries'

const EMAIL_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s]+$/
/** PayPal 배송지는 라틴 문자만 안정적으로 처리되므로 ASCII 로 제한 */
const ASCII_PATTERN = /^[\x20-\x7E]*$/
const ASCII_MESSAGE = 'Please use English letters and numbers only'

const asciiText = (label: string, max: number) =>
  z
    .string()
    .trim()
    .min(1, `${label} is required`)
    .max(max, `${label} is too long`)
    .regex(ASCII_PATTERN, ASCII_MESSAGE)

const optionalAsciiText = (max: number) =>
  z.string().trim().max(max, 'Too long').regex(ASCII_PATTERN, ASCII_MESSAGE)

/**
 * 비회원 체크아웃 배송지 폼 스키마.
 * 필드 길이 제한은 PayPal Orders v2 shipping.address 제약을 따른다.
 */
export const addressFormSchema = z
  .object({
    email: z.string().trim().min(1, 'Email is required').max(255).regex(EMAIL_PATTERN, 'Enter a valid email'),
    phone: optionalAsciiText(50),
    fullName: asciiText('Full name', 300),
    addressLine1: asciiText('Address', 300),
    addressLine2: optionalAsciiText(300),
    city: asciiText('City', 120),
    state: optionalAsciiText(300),
    postalCode: optionalAsciiText(60),
    countryCode: z.string().regex(/^[A-Z]{2}$/, 'Select a country'),
  })
  .superRefine((values, ctx) => {
    if (STATE_REQUIRED_COUNTRIES.has(values.countryCode) && !values.state) {
      ctx.addIssue({ code: 'custom', path: ['state'], message: 'State / Province is required' })
    }
    if (!POSTAL_CODE_OPTIONAL_COUNTRIES.has(values.countryCode) && !values.postalCode) {
      ctx.addIssue({ code: 'custom', path: ['postalCode'], message: 'Postal code is required' })
    }
  })

export type AddressFormValues = z.infer<typeof addressFormSchema>

export const EMPTY_ADDRESS_FORM: AddressFormValues = {
  email: '',
  phone: '',
  fullName: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  state: '',
  postalCode: '',
  countryCode: 'US',
}
