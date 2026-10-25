'use client'

import { CART_ITEM_MAX_QUANTITY, CART_ITEM_MIN_QUANTITY } from '@/entities/cart'
import { getErrorMessage } from '@/shared/api'
import { toast } from '@/shared/lib/toast'
import { useChangeCartItemQuantity } from '../model/useCartMutations'

interface QuantityStepperProps {
  productId: number
  quantity: number
  disabled?: boolean
}

export function QuantityStepper({ productId, quantity, disabled }: QuantityStepperProps) {
  const { mutate, isPending } = useChangeCartItemQuantity()

  const change = (next: number) => {
    mutate(
      { productId, quantity: next },
      { onError: (error) => toast.error(getErrorMessage(error, 'Could not update quantity.')) },
    )
  }

  const isDisabled = disabled || isPending
  return (
    <div className="inline-flex h-9 items-center rounded-md border border-neutral-300">
      <button
        type="button"
        aria-label="Decrease quantity"
        className="h-full w-9 text-lg disabled:text-neutral-300"
        disabled={isDisabled || quantity <= CART_ITEM_MIN_QUANTITY}
        onClick={() => change(quantity - 1)}
      >
        −
      </button>
      <span className="w-8 text-center text-sm tabular-nums" aria-live="polite">
        {quantity}
      </span>
      <button
        type="button"
        aria-label="Increase quantity"
        className="h-full w-9 text-lg disabled:text-neutral-300"
        disabled={isDisabled || quantity >= CART_ITEM_MAX_QUANTITY}
        onClick={() => change(quantity + 1)}
      >
        +
      </button>
    </div>
  )
}
