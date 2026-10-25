'use client'

import Link from 'next/link'
import { OrderSummary, useOrderQuery, type OrderStatus } from '@/entities/order'
import { routes } from '@/shared/config'
import { Spinner } from '@/shared/ui'

const STATUS_MESSAGE: Record<OrderStatus, { title: string; description: string }> = {
  PAID: {
    title: 'Thank you for your order!',
    description: 'Your payment was successful. Please keep your order number for reference.',
  },
  PENDING_PAYMENT: {
    title: 'We are confirming your payment',
    description: 'If you were charged, please contact customer service with your order number.',
  },
  CANCELLED: {
    title: 'Payment was not completed',
    description: 'Your order was cancelled and you were not charged. Your bag has been kept.',
  },
}

export function OrderCompleteView({ orderId }: { orderId: string | null }) {
  const { data: order, isLoading, isError } = useOrderQuery(orderId)

  if (!orderId) {
    return <p className="py-16 text-center text-sm text-neutral-600">Order number is missing.</p>
  }
  if (isLoading) return <Spinner />
  if (isError || !order) {
    return <p className="py-16 text-center text-sm text-neutral-600">The order could not be found.</p>
  }

  const message = STATUS_MESSAGE[order.status]

  return (
    <div className="flex flex-col gap-6 py-6">
      <div className="flex flex-col gap-2 text-center">
        <h1 className="text-xl font-bold">{message.title}</h1>
        <p className="text-sm text-neutral-600">{message.description}</p>
        <p className="mt-2 break-all text-xs text-neutral-500">
          Order number <span className="font-mono text-neutral-800">{order.orderId}</span>
        </p>
      </div>

      <div className="rounded-lg border border-neutral-200 p-4">
        <p className="mb-3 text-xs text-neutral-500">
          Ship to {order.recipientName} ({order.countryCode})
        </p>
        <OrderSummary
          items={order.items}
          itemTotal={order.itemTotal}
          shippingCost={order.shippingCost}
          totalAmount={order.totalAmount}
        />
      </div>

      <Link
        href={order.status === 'CANCELLED' ? routes.cart : routes.home}
        className="flex h-12 items-center justify-center rounded-md bg-brand text-sm font-semibold text-white"
      >
        {order.status === 'CANCELLED' ? 'Back to bag' : 'Continue shopping'}
      </Link>
    </div>
  )
}
