'use client'

/**
 * PayPal 결제 버튼.
 *
 * `createOrder` 콜백에서 주문+PayPal 주문을 생성하고, PayPal Order ID 를 SDK 에 반환합니다.
 * 구매자 승인 후 `onApprove` 에서 백엔드가 캡처를 수행합니다.
 */

import { FUNDING, PayPalButtons, usePayPalScriptReducer } from '@paypal/react-paypal-js'
import { useMemo } from 'react'
import type { CreateOrderRequest } from '@/entities/order'
import { usePayPalSDKEventHandlers } from '../lib/usePayPalSDKEventHandlers'

interface PayPalCheckoutButtonProps {
  getOrderRequest: () => Promise<CreateOrderRequest | null>
  disabled?: boolean
}

export function PayPalCheckoutButton({ getOrderRequest, disabled }: PayPalCheckoutButtonProps) {
  const [{ isResolved, isPending, isRejected }] = usePayPalScriptReducer()

  const {
    handlePayPalClick,
    handlePayPalCreateOrder,
    handlePayPalApprove,
    handlePayPalCancel,
    handlePayPalError,
  } = usePayPalSDKEventHandlers({ getOrderRequest, disabled })

  const style = useMemo(
    () => ({
      layout: 'vertical' as const,
      color: 'gold' as const,
      shape: 'rect' as const,
      label: 'paypal' as const,
      height: 48,
    }),
    [],
  )

  if (isRejected) {
    return (
      <p role="alert" className="rounded-md bg-red-50 px-4 py-3 text-center text-sm text-red-700">
        PayPal could not be loaded. Please refresh the page.
      </p>
    )
  }

  if (!isResolved || isPending) {
    return (
      <div
        aria-label="PayPal loading"
        className="h-12 w-full animate-pulse rounded-md"
        style={{ backgroundColor: '#ffc439', opacity: 0.4 }}
      />
    )
  }

  return (
    <div className="min-h-12">
      <PayPalButtons
        fundingSource={FUNDING.PAYPAL}
        style={style}
        disabled={disabled}
        onClick={handlePayPalClick}
        createOrder={handlePayPalCreateOrder}
        onApprove={handlePayPalApprove}
        onCancel={handlePayPalCancel}
        onError={handlePayPalError}
      />
    </div>
  )
}
