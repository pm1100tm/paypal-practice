'use client'

/**
 * PayPal JavaScript SDK 이벤트 전용 훅.
 *
 * ## 결제 흐름
 * ```
 * onClick → createOrder → [사용자 PayPal 팝업 승인] → onApprove → capturePayment → 주문 완료
 *                                                    → onCancel  → cancelPayment  → 체크아웃 유지
 *                                                    → onError   → errorPayment   → 체크아웃 유지
 * ```
 * 장바구니는 결제 완료 전까지 서버에서 비우지 않으므로, 취소/오류 후 바로 다시 결제할 수 있습니다.
 *
 * ## Ref 사용 이유
 * PayPal SDK 는 각 이벤트를 별도 콜백으로 호출하고, 버튼 렌더 시점의 콜백을 보관할 수 있으므로
 * 콜백 간 데이터 전달과 최신 props 참조에 ref 를 사용합니다.
 * - `optionsRef`: 최신 옵션(getOrderRequest, disabled 등)
 * - `validatedRequestRef`: onClick 에서 검증한 주문 요청 → createOrder 까지 전달
 * - `pendingRef`: createOrder 에서 생성된 orderId/paymentId → onApprove/onCancel/onError 까지 전달
 */

import type { PayPalButtonsComponentProps } from '@paypal/react-paypal-js'
import { useQueryClient } from '@tanstack/react-query'
import { useRouter } from 'next/navigation'
import { useRef } from 'react'
import { cartQueryKeys } from '@/entities/cart'
import type { CreateOrderRequest } from '@/entities/order'
import { cancelPayment, capturePayment, errorPayment } from '@/entities/payment'
import { getErrorMessage, isApiError } from '@/shared/api'
import { routes } from '@/shared/config'
import { toast } from '@/shared/lib/toast'
import { createOrderAndPayment } from './createOrderAndPayment'

type OnClick = NonNullable<PayPalButtonsComponentProps['onClick']>
type CreateOrder = NonNullable<PayPalButtonsComponentProps['createOrder']>
type OnApprove = NonNullable<PayPalButtonsComponentProps['onApprove']>
type OnCancel = NonNullable<PayPalButtonsComponentProps['onCancel']>
type OnError = NonNullable<PayPalButtonsComponentProps['onError']>

export interface UsePayPalSDKEventHandlersOptions {
  /**
   * 배송지 폼 검증 후 주문 생성 요청을 만든다. 검증 실패 시 null.
   * onClick 에서 호출되며, 실패하면 PayPal 팝업을 열지 않는다.
   */
  getOrderRequest: () => Promise<CreateOrderRequest | null>
  /** 결제 불가 상태 (금액 미리보기 로딩 중 등) */
  disabled?: boolean
}

export const usePayPalSDKEventHandlers = (options: UsePayPalSDKEventHandlersOptions) => {
  const router = useRouter()
  const queryClient = useQueryClient()

  const optionsRef = useRef(options)
  optionsRef.current = options

  const validatedRequestRef = useRef<CreateOrderRequest | null>(null)
  const pendingRef = useRef<{ orderId: string; paymentId: string | null } | null>(null)
  // onClick 에서 reject 했는데도 createOrder 가 호출되는 SDK race condition 방어
  const clickRejectedRef = useRef(false)
  // createOrder 실패 원인 — onError 에는 SDK 의 일반 에러만 전달되므로 실제 원인 메시지를 보관
  const lastErrorRef = useRef<unknown>(null)

  const handlePayPalClick: OnClick = async (_data, actions) => {
    clickRejectedRef.current = false
    lastErrorRef.current = null

    if (optionsRef.current.disabled) {
      clickRejectedRef.current = true
      return actions.reject()
    }
    const orderRequest = await optionsRef.current.getOrderRequest()
    if (!orderRequest) {
      clickRejectedRef.current = true
      toast.error('Please check your contact and shipping details.')
      return actions.reject()
    }
    validatedRequestRef.current = orderRequest
    return actions.resolve()
  }

  /** 주문/결제 생성 → PayPal SDK 에 PayPal Order ID 반환 */
  const handlePayPalCreateOrder: CreateOrder = async () => {
    if (clickRejectedRef.current) {
      throw new Error('Payment validation failed. Please try again.')
    }
    try {
      const orderRequest = validatedRequestRef.current ?? (await optionsRef.current.getOrderRequest())
      if (!orderRequest) {
        throw new Error('Please check your contact and shipping details.')
      }
      const { orderId, paymentId } = await createOrderAndPayment({
        orderRequest,
        onOrderCreated: (createdOrderId) => {
          pendingRef.current = { orderId: createdOrderId, paymentId: null }
        },
      })
      validatedRequestRef.current = null
      pendingRef.current = { orderId, paymentId }
      return paymentId
    } catch (error) {
      lastErrorRef.current = error
      throw error
    }
  }

  /**
   * PayPal SDK 의 내부 통신이 완료될 때까지 기다립니다.
   * 캡처는 백엔드에서 처리하므로 actions.order.capture() 는 호출하지 않습니다.
   */
  const waitForPayPalSDKCommunication = async (actions: Parameters<OnApprove>[1]) => {
    const orderActions = actions?.order
    if (!orderActions || typeof orderActions.get !== 'function') return
    try {
      await orderActions.get()
    } catch {
      // SDK 통신은 완료되었을 가능성이 높으므로 계속 진행합니다.
    }
  }

  /** 구매자 승인 → 백엔드 캡처 → 주문 완료 페이지 */
  const handlePayPalApprove: OnApprove = async (data, actions) => {
    const pending = pendingRef.current
    if (!pending?.paymentId) {
      toast.error('Order information is missing. Please try again.')
      return
    }
    if (data.orderID && data.orderID !== pending.paymentId) {
      toast.error('Payment information does not match. Please try again.')
      return
    }

    try {
      await waitForPayPalSDKCommunication(actions)
      await capturePayment(pending.orderId, pending.paymentId)
      pendingRef.current = null
      await queryClient.invalidateQueries({ queryKey: cartQueryKeys.all })
      router.push(routes.orderComplete(pending.orderId))
    } catch (error) {
      pendingRef.current = null
      if (isApiError(error) && error.code === 'PAYMENT_REVIEW_REQUIRED') {
        // 금전 이동 여부 확인이 필요한 상태 — 주문 페이지에서 상태를 안내
        router.push(routes.orderComplete(pending.orderId))
        return
      }
      toast.error(getErrorMessage(error, 'Payment approval failed. Please try again.'))
      await queryClient.invalidateQueries({ queryKey: cartQueryKeys.all })
    }
  }

  /** 사용자가 PayPal 팝업에서 취소 → 백엔드에 취소 기록, 체크아웃 화면 유지 */
  const handlePayPalCancel: OnCancel = () => {
    const pending = pendingRef.current
    pendingRef.current = null
    if (pending) {
      void cancelPayment(pending.orderId).catch(() => undefined)
    }
    toast.info('Payment was cancelled. You can try again.')
  }

  /** PayPal SDK 에러 → 백엔드에 에러 기록, 체크아웃 화면 유지 */
  const handlePayPalError: OnError = (err) => {
    const pending = pendingRef.current
    pendingRef.current = null

    // onClick reject 는 사용자 안내를 이미 했으므로 무시
    if (clickRejectedRef.current) {
      clickRejectedRef.current = false
      return
    }

    if (pending) {
      const errorMessage = err instanceof Error ? err.message : JSON.stringify(err)
      void errorPayment(pending.orderId, 'PAYPAL_SDK_ERROR', errorMessage).catch(() => undefined)
    }
    toast.error(getErrorMessage(lastErrorRef.current ?? err, 'PayPal payment failed. Please try again.'))
    lastErrorRef.current = null
  }

  return {
    handlePayPalClick,
    handlePayPalCreateOrder,
    handlePayPalApprove,
    handlePayPalCancel,
    handlePayPalError,
  }
}
