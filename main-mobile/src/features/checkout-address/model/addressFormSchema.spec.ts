import { addressFormSchema, EMPTY_ADDRESS_FORM } from './addressFormSchema'

const valid = {
  ...EMPTY_ADDRESS_FORM,
  email: 'guest@example.com',
  fullName: 'Jane Doe',
  addressLine1: '1 Main St',
  city: 'San Jose',
  state: 'CA',
  postalCode: '95131',
  countryCode: 'US',
}

describe('addressFormSchema', () => {
  it('유효한 미국 주소를 통과시킨다', () => {
    expect(addressFormSchema.safeParse(valid).success).toBe(true)
  })

  it('미국은 주(state)와 우편번호가 필수다', () => {
    const result = addressFormSchema.safeParse({ ...valid, state: '', postalCode: '' })
    expect(result.success).toBe(false)
    const paths = result.error?.issues.map((issue) => issue.path.join('.'))
    expect(paths).toEqual(expect.arrayContaining(['state', 'postalCode']))
  })

  it('홍콩은 우편번호 없이도 통과한다', () => {
    const result = addressFormSchema.safeParse({ ...valid, countryCode: 'HK', state: '', postalCode: '' })
    expect(result.success).toBe(true)
  })

  it('ASCII 가 아닌 문자는 거부한다', () => {
    const result = addressFormSchema.safeParse({ ...valid, fullName: '홍길동' })
    expect(result.success).toBe(false)
  })
})
