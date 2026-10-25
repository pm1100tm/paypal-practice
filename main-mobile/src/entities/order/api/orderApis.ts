import { fetchJson } from '@/shared/api'
import {
  CheckoutSummarySchema,
  CreateOrderResponseSchema,
  OrderSchema,
  type CheckoutSummary,
  type CreateOrderRequest,
  type CreateOrderResponse,
  type Order,
} from '../model/types'

/** 체크아웃 금액 미리보기 (POST /api/v2/orders/checkout) */
export const previewCheckout = (cartId: string): Promise<CheckoutSummary> =>
  fetchJson('/api/v2/orders/checkout', CheckoutSummarySchema, {
    method: 'POST',
    json: { cartId },
  })

/**
 * 결제 대기 주문 생성 (POST /api/v2/orders)
 * 금액은 보내지 않는다 — 서버가 장바구니 기준으로 계산한다.
 */
export const createOrder = (request: CreateOrderRequest): Promise<CreateOrderResponse> =>
  fetchJson('/api/v2/orders', CreateOrderResponseSchema, { method: 'POST', json: request })

/** 주문 조회 (GET /api/v2/orders/{orderId}) */
export const getOrder = (orderId: string): Promise<Order> =>
  fetchJson(`/api/v2/orders/${encodeURIComponent(orderId)}`, OrderSchema)
