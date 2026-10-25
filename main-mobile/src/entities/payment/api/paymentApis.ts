/**
 * 결제 관련 API 모듈 (PayPal).
 */

import { fetchJson } from '@/shared/api'
import {
  CancelPaymentResponseSchema,
  CapturePaymentResponseSchema,
  CreatePaymentResponseSchema,
  type CancelPaymentResponse,
  type CapturePaymentResponse,
  type CreatePaymentResponse,
  type PaymentMethodType,
} from '../model/types'

/**
 * 결제 API 전용 타임아웃 (30초).
 * 백엔드가 PayPal 재시도(최대 3회, 백오프)를 수행하므로 클라이언트가 먼저 끊지 않도록 길게 둔다.
 */
const PAYMENT_TIMEOUT_MS = 30_000

/**
 * 결제 생성 (POST /api/v2/payments)
 * 생성된 주문 ID 로 PayPal 주문을 만들고, PayPal Order ID(paymentId) 를 받는다.
 */
export const createPayment = (
  orderId: string,
  paymentMethod: PaymentMethodType,
): Promise<CreatePaymentResponse> =>
  fetchJson('/api/v2/payments', CreatePaymentResponseSchema, {
    method: 'POST',
    json: { orderId, paymentMethod },
    timeoutMs: PAYMENT_TIMEOUT_MS,
  })

/**
 * 결제 확정/캡처 (POST /api/v2/payments/capture)
 * PayPal onApprove 콜백에서 호출한다. 캡처는 항상 서버에서 수행한다.
 */
export const capturePayment = (orderId: string, paymentId: string): Promise<CapturePaymentResponse> =>
  fetchJson('/api/v2/payments/capture', CapturePaymentResponseSchema, {
    method: 'POST',
    json: { orderId, paymentId },
    timeoutMs: PAYMENT_TIMEOUT_MS,
  })

/** 결제 취소 (POST /api/v2/payments/cancel/paypal) — PayPal 팝업에서 사용자가 취소 */
export const cancelPayment = (orderId: string): Promise<CancelPaymentResponse> =>
  fetchJson('/api/v2/payments/cancel/paypal', CancelPaymentResponseSchema, {
    method: 'POST',
    json: { orderId },
  })

/** 결제 에러 기록 (POST /api/v2/payments/error/paypal) — PayPal SDK 에러 */
export const errorPayment = (
  orderId: string,
  errorName?: string,
  errorMessage?: string,
): Promise<CancelPaymentResponse> =>
  fetchJson('/api/v2/payments/error/paypal', CancelPaymentResponseSchema, {
    method: 'POST',
    json: { orderId, errorName, errorMessage: errorMessage?.slice(0, 1000) },
  })
