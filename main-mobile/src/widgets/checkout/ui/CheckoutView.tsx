'use client'

import Link from 'next/link'
import { useCallback } from 'react'
import { getStoredCartId, useCartQuery } from '@/entities/cart'
import { OrderSummary, useCheckoutSummaryQuery } from '@/entities/order'
import { ShippingAddressForm, toCreateOrderRequest, useShippingAddressForm } from '@/features/checkout-address'
import { PayPalCheckoutButton, PayPalProvider } from '@/features/paypal-checkout'
import { getErrorMessage } from '@/shared/api'
import { routes } from '@/shared/config'
import { formatMoney } from '@/shared/lib/money'
import { Spinner } from '@/shared/ui'

interface CheckoutViewProps {
  /** PayPal JS SDK client-id (없으면 결제 버튼 대신 설정 안내를 표시) */
  paypalClientId: string | null
}

/**
 * 비회원 체크아웃 — 연락처/배송지 입력 → 금액 확인 → PayPal 결제.
 */
export function CheckoutView({ paypalClientId }: CheckoutViewProps) {
  const { data: cart, isLoading: isCartLoading } = useCartQuery()
  const cartId = cart?.cartId ?? null
  const summaryQuery = useCheckoutSummaryQuery(cart && cart.items.length > 0 ? cartId : null)
  const form = useShippingAddressForm()

  /** PayPal onClick 에서 호출 — 폼 검증 후 주문 생성 요청 구성 */
  const getOrderRequest = useCallback(async () => {
    const currentCartId = getStoredCartId()
    if (!currentCartId) return null
    const isValid = await form.trigger(undefined, { shouldFocus: true })
    if (!isValid) return null
    return toCreateOrderRequest(currentCartId, form.getValues())
  }, [form])

  if (isCartLoading || summaryQuery.isLoading) return <Spinner />

  if (!cart || cart.items.length === 0) {
    return (
      <div className="flex flex-col items-center gap-4 py-16 text-center text-sm text-neutral-600">
        <p>Your bag is empty.</p>
        <Link href={routes.home} className="font-semibold underline underline-offset-4">
          Continue shopping
        </Link>
      </div>
    )
  }

  if (summaryQuery.isError || !summaryQuery.data) {
    return (
      <div className="flex flex-col items-center gap-4 py-16 text-center text-sm text-neutral-600">
        <p>{getErrorMessage(summaryQuery.error, 'Checkout is not available right now.')}</p>
        <Link href={routes.cart} className="font-semibold underline underline-offset-4">
          Back to bag
        </Link>
      </div>
    )
  }

  const summary = summaryQuery.data

  return (
    <div className="flex flex-col gap-8 pb-10">
      <ShippingAddressForm form={form} />

      <section className="flex flex-col gap-3">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold">Order summary</h2>
          <Link href={routes.cart} className="text-xs text-neutral-500 underline underline-offset-2">
            Edit bag
          </Link>
        </div>
        <OrderSummary
          items={summary.items}
          itemTotal={summary.itemTotal}
          shippingCost={summary.shippingCost}
          totalAmount={summary.totalAmount}
        />
        {summary.freeShippingThreshold && Number(summary.shippingCost.amount) > 0 && (
          <p className="text-xs text-neutral-500">
            Free shipping on orders over {formatMoney(summary.freeShippingThreshold)}
          </p>
        )}
      </section>

      <section className="flex flex-col gap-3">
        <h2 className="text-sm font-semibold">Payment</h2>
        {paypalClientId ? (
          <PayPalProvider clientId={paypalClientId}>
            <PayPalCheckoutButton getOrderRequest={getOrderRequest} disabled={!cart.checkoutAvailable} />
          </PayPalProvider>
        ) : (
          <p role="alert" className="rounded-md bg-amber-50 px-4 py-3 text-sm text-amber-800">
            PayPal is not configured. Set <code>PAYPAL_CLIENT_ID</code> for this app.
          </p>
        )}
        <p className="text-xs text-neutral-500">
          You will complete your payment securely with PayPal. The final amount is confirmed by our server.
        </p>
      </section>
    </div>
  )
}
