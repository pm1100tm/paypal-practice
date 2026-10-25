'use client'

import { useQuery } from '@tanstack/react-query'
import { isApiError } from '@/shared/api'
import { clearStoredCartId, getStoredCartId } from '../model/cartIdStorage'
import type { Cart } from '../model/types'
import { getCart } from './cartApis'

export const cartQueryKeys = {
  all: ['cart'] as const,
}

/**
 * 현재 브라우저의 장바구니 조회.
 * cartId 가 없거나 서버에서 찾을 수 없으면(404) null 을 반환한다 (빈 장바구니).
 */
export const fetchCurrentCart = async (): Promise<Cart | null> => {
  const cartId = getStoredCartId()
  if (!cartId) return null
  try {
    return await getCart(cartId)
  } catch (error) {
    if (isApiError(error) && (error.status === 404 || error.code === 'INVALID_INPUT')) {
      clearStoredCartId()
      return null
    }
    throw error
  }
}

export const useCartQuery = () =>
  useQuery({
    queryKey: cartQueryKeys.all,
    queryFn: fetchCurrentCart,
    staleTime: 0,
  })
