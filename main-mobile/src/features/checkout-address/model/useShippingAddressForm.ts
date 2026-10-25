'use client'

import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { addressFormSchema, EMPTY_ADDRESS_FORM, type AddressFormValues } from './addressFormSchema'

/**
 * 배송지 폼 상태 훅.
 * 상위(widget)에서 생성해 폼 UI 와 결제 버튼에 같은 인스턴스를 전달한다.
 */
export const useShippingAddressForm = () =>
  useForm<AddressFormValues>({
    resolver: zodResolver(addressFormSchema),
    defaultValues: EMPTY_ADDRESS_FORM,
    mode: 'onTouched',
  })

export type ShippingAddressFormApi = ReturnType<typeof useShippingAddressForm>
