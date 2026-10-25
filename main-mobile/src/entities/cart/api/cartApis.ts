import { fetchJson } from '@/shared/api'
import { CartSchema, type Cart } from '../model/types'

/** 비회원 장바구니 발급 (POST /api/v2/carts) */
export const createCart = (): Promise<Cart> =>
  fetchJson('/api/v2/carts', CartSchema, { method: 'POST' })

/** 장바구니 조회 (GET /api/v2/carts/{cartId}) */
export const getCart = (cartId: string): Promise<Cart> =>
  fetchJson(`/api/v2/carts/${encodeURIComponent(cartId)}`, CartSchema)

/** 상품 담기 (POST /api/v2/carts/{cartId}/items) — 이미 담긴 상품이면 수량 합산 */
export const addCartItem = (cartId: string, productId: number, quantity: number): Promise<Cart> =>
  fetchJson(`/api/v2/carts/${encodeURIComponent(cartId)}/items`, CartSchema, {
    method: 'POST',
    json: { productId, quantity },
  })

/** 수량 변경 (PATCH /api/v2/carts/{cartId}/items/{productId}) */
export const changeCartItemQuantity = (
  cartId: string,
  productId: number,
  quantity: number,
): Promise<Cart> =>
  fetchJson(`/api/v2/carts/${encodeURIComponent(cartId)}/items/${productId}`, CartSchema, {
    method: 'PATCH',
    json: { quantity },
  })

/** 상품 삭제 (DELETE /api/v2/carts/{cartId}/items/{productId}) */
export const removeCartItem = (cartId: string, productId: number): Promise<Cart> =>
  fetchJson(`/api/v2/carts/${encodeURIComponent(cartId)}/items/${productId}`, CartSchema, {
    method: 'DELETE',
  })
