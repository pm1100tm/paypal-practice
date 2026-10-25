'use client'

import { useMutation, useQueryClient } from '@tanstack/react-query'
import {
  addCartItem,
  cartQueryKeys,
  changeCartItemQuantity,
  createCart,
  getStoredCartId,
  removeCartItem,
  setStoredCartId,
  type Cart,
} from '@/entities/cart'
import { isApiError } from '@/shared/api'

/**
 * 저장된 cartId 로 작업을 수행하고, cartId 가 없거나 서버에서 사라졌으면(404)
 * 새 장바구니를 발급받아 한 번 재시도한다.
 */
const withCart = async (action: (cartId: string) => Promise<Cart>): Promise<Cart> => {
  const issueCart = async () => {
    const cart = await createCart()
    setStoredCartId(cart.cartId)
    return cart.cartId
  }

  const storedCartId = getStoredCartId()
  if (!storedCartId) {
    return action(await issueCart())
  }
  try {
    return await action(storedCartId)
  } catch (error) {
    if (isApiError(error) && error.code === 'CART_NOT_FOUND') {
      return action(await issueCart())
    }
    throw error
  }
}

const useCartMutation = <TVariables>(mutationFn: (variables: TVariables) => Promise<Cart>) => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn,
    onSuccess: (cart) => {
      queryClient.setQueryData(cartQueryKeys.all, cart)
    },
  })
}

export const useAddToCart = () =>
  useCartMutation(({ productId, quantity }: { productId: number; quantity: number }) =>
    withCart((cartId) => addCartItem(cartId, productId, quantity)),
  )

export const useChangeCartItemQuantity = () =>
  useCartMutation(({ productId, quantity }: { productId: number; quantity: number }) =>
    withCart((cartId) => changeCartItemQuantity(cartId, productId, quantity)),
  )

export const useRemoveCartItem = () =>
  useCartMutation(({ productId }: { productId: number }) =>
    withCart((cartId) => removeCartItem(cartId, productId)),
  )
