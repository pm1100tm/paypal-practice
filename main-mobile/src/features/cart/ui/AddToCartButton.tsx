'use client'

import { getErrorMessage } from '@/shared/api'
import { toast } from '@/shared/lib/toast'
import { Button } from '@/shared/ui'
import { useAddToCart } from '../model/useCartMutations'

interface AddToCartButtonProps {
  productId: number
  disabled?: boolean
}

export function AddToCartButton({ productId, disabled }: AddToCartButtonProps) {
  const { mutate, isPending } = useAddToCart()

  const handleClick = () => {
    mutate(
      { productId, quantity: 1 },
      {
        onSuccess: () => toast.success('Added to your bag'),
        onError: (error) => toast.error(getErrorMessage(error, 'Could not add to bag. Please try again.')),
      },
    )
  }

  return (
    <Button variant="outline" block onClick={handleClick} disabled={disabled || isPending}>
      {isPending ? 'Adding…' : disabled ? 'Sold out' : 'Add to bag'}
    </Button>
  )
}
