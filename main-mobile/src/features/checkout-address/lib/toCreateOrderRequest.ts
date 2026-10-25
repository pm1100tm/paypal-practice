import type { CreateOrderRequest } from '@/entities/order'
import type { AddressFormValues } from '../model/addressFormSchema'

const emptyToUndefined = (value: string | undefined) => {
  const trimmed = value?.trim()
  return trimmed ? trimmed : undefined
}

/** 배송지 폼 값 → 주문 생성 요청 */
export const toCreateOrderRequest = (cartId: string, values: AddressFormValues): CreateOrderRequest => ({
  cartId,
  email: values.email.trim(),
  phone: emptyToUndefined(values.phone),
  shippingAddress: {
    fullName: values.fullName.trim(),
    addressLine1: values.addressLine1.trim(),
    addressLine2: emptyToUndefined(values.addressLine2),
    city: values.city.trim(),
    state: emptyToUndefined(values.state),
    postalCode: emptyToUndefined(values.postalCode),
    countryCode: values.countryCode,
  },
})
