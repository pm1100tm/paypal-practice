import { clearStoredCartId, getStoredCartId, setStoredCartId } from './cartIdStorage'

const CART_ID = '49977c28-16cd-4dcc-b614-fd8f483c5bd5'

describe('cartIdStorage', () => {
  beforeEach(() => {
    window.localStorage.clear()
    clearStoredCartId()
  })

  it('저장한 cartId 를 다시 읽는다', () => {
    expect(getStoredCartId()).toBeNull()
    setStoredCartId(CART_ID)
    expect(getStoredCartId()).toBe(CART_ID)
  })

  it('UUID 형식이 아닌 값은 무시한다', () => {
    window.localStorage.setItem('shopswd:cart-id', 'tampered')
    expect(getStoredCartId()).toBeNull()
  })

  it('삭제하면 null', () => {
    setStoredCartId(CART_ID)
    clearStoredCartId()
    expect(getStoredCartId()).toBeNull()
  })
})
