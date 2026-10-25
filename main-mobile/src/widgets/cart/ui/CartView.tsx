'use client'

import Link from 'next/link'
import { CartEmpty, CartLineItem, useCartQuery } from '@/entities/cart'
import { QuantityStepper, RemoveCartItemButton } from '@/features/cart'
import { routes } from '@/shared/config'
import { cn } from '@/shared/lib/cn'
import { formatMoney } from '@/shared/lib/money'
import { Spinner } from '@/shared/ui'

/**
 * 장바구니 화면 — 비회원 cartId 기반.
 */
export function CartView() {
  const { data: cart, isLoading, isError } = useCartQuery()

  if (isLoading) return <Spinner />
  if (isError) {
    return <p className="py-16 text-center text-sm text-neutral-600">Your bag could not be loaded.</p>
  }
  if (!cart || cart.items.length === 0) return <CartEmpty />

  return (
    <div className="flex flex-col gap-6 pb-28">
      <ul className="divide-y divide-neutral-200">
        {cart.items.map((item) => (
          <CartLineItem
            key={item.productId}
            item={item}
            quantityControl={
              <QuantityStepper productId={item.productId} quantity={item.quantity} disabled={!item.purchasable} />
            }
            removeAction={<RemoveCartItemButton productId={item.productId} productName={item.name} />}
          />
        ))}
      </ul>

      {!cart.checkoutAvailable && (
        <p className="rounded-md bg-amber-50 px-4 py-3 text-sm text-amber-800">
          Some items are unavailable. Remove them to continue to checkout.
        </p>
      )}

      <div className="fixed inset-x-0 bottom-0 z-30 border-t border-neutral-200 bg-white">
        <div className="mx-auto flex max-w-md flex-col gap-3 px-4 py-4">
          <div className="flex justify-between text-sm">
            <span className="text-neutral-600">Subtotal ({cart.totalQuantity} items)</span>
            <span className="font-semibold">{formatMoney(cart.subtotal)}</span>
          </div>
          <Link
            href={routes.checkout}
            aria-disabled={!cart.checkoutAvailable}
            className={cn(
              'flex h-12 items-center justify-center rounded-md text-sm font-semibold text-white',
              cart.checkoutAvailable ? 'bg-brand' : 'pointer-events-none bg-neutral-300',
            )}
          >
            Checkout
          </Link>
        </div>
      </div>
    </div>
  )
}
