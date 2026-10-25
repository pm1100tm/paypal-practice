'use client'

import { getErrorMessage } from '@/shared/api'
import { toast } from '@/shared/lib/toast'
import { useRemoveCartItem } from '../model/useCartMutations'

export function RemoveCartItemButton({ productId, productName }: { productId: number; productName: string }) {
  const { mutate, isPending } = useRemoveCartItem()

  return (
    <button
      type="button"
      aria-label={`Remove ${productName}`}
      disabled={isPending}
      className="shrink-0 text-xs text-neutral-500 underline underline-offset-2 disabled:text-neutral-300"
      onClick={() =>
        mutate(
          { productId },
          { onError: (error) => toast.error(getErrorMessage(error, 'Could not remove the item.')) },
        )
      }
    >
      Remove
    </button>
  )
}
