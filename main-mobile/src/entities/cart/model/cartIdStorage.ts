/**
 * 비회원 장바구니 ID 보관소.
 *
 * 로그인/세션이 없으므로 서버가 발급한 cartId(UUID) 를 브라우저 localStorage 에 보관한다.
 * (사생활 보호 모드 등 storage 접근 실패 시에는 메모리에만 유지)
 */

const STORAGE_KEY = 'shopswd:cart-id'
const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

let memoryCartId: string | null = null

export const getStoredCartId = (): string | null => {
  if (typeof window === 'undefined') return null
  try {
    const value = window.localStorage.getItem(STORAGE_KEY)
    return value && UUID_PATTERN.test(value) ? value : memoryCartId
  } catch {
    return memoryCartId
  }
}

export const setStoredCartId = (cartId: string): void => {
  memoryCartId = cartId
  try {
    window.localStorage.setItem(STORAGE_KEY, cartId)
  } catch {
    // storage 사용 불가 — 메모리 값만 유지
  }
}

export const clearStoredCartId = (): void => {
  memoryCartId = null
  try {
    window.localStorage.removeItem(STORAGE_KEY)
  } catch {
    // ignore
  }
}
