'use client'

import { useQuery } from '@tanstack/react-query'
import { getOrder, previewCheckout } from './orderApis'

export const orderQueryKeys = {
  checkout: (cartId: string) => ['checkout', cartId] as const,
  detail: (orderId: string) => ['order', orderId] as const,
}

export const useCheckoutSummaryQuery = (cartId: string | null | undefined) =>
  useQuery({
    queryKey: orderQueryKeys.checkout(cartId ?? ''),
    queryFn: () => previewCheckout(cartId as string),
    enabled: !!cartId,
  })

export const useOrderQuery = (orderId: string | null | undefined) =>
  useQuery({
    queryKey: orderQueryKeys.detail(orderId ?? ''),
    queryFn: () => getOrder(orderId as string),
    enabled: !!orderId,
  })
