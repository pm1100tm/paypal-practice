'use client'

import { useId } from 'react'
import { cn } from '@/shared/lib/cn'
import { TextField } from '@/shared/ui'
import { SHIPPING_COUNTRIES, STATE_REQUIRED_COUNTRIES } from '../model/countries'
import type { ShippingAddressFormApi } from '../model/useShippingAddressForm'

interface ShippingAddressFormProps {
  form: ShippingAddressFormApi
  disabled?: boolean
}

/**
 * 비회원 배송지/연락처 입력 폼.
 * PayPal 에는 SET_PROVIDED_ADDRESS 로 이 주소가 그대로 전달된다.
 */
export function ShippingAddressForm({ form, disabled }: ShippingAddressFormProps) {
  const countryId = useId()
  const {
    register,
    watch,
    formState: { errors },
  } = form
  const countryCode = watch('countryCode')

  return (
    <form noValidate onSubmit={(event) => event.preventDefault()} className="flex flex-col gap-4">
      <fieldset disabled={disabled} className="flex flex-col gap-3">
        <legend className="mb-2 text-sm font-semibold">Contact</legend>
        <TextField
          label="Email"
          type="email"
          autoComplete="email"
          required
          error={errors.email?.message}
          {...register('email')}
        />
        <TextField
          label="Phone (optional)"
          type="tel"
          autoComplete="tel"
          error={errors.phone?.message}
          {...register('phone')}
        />
      </fieldset>

      <fieldset disabled={disabled} className="flex flex-col gap-3">
        <legend className="mb-2 text-sm font-semibold">Shipping address</legend>

        <div className="flex flex-col gap-1">
          <label htmlFor={countryId} className="text-xs font-medium text-neutral-700">
            Country / Region<span className="ml-0.5 text-brand-accent">*</span>
          </label>
          <select
            id={countryId}
            autoComplete="country"
            className={cn(
              'h-11 rounded-md border bg-white px-3 text-sm',
              errors.countryCode ? 'border-red-500' : 'border-neutral-300',
            )}
            {...register('countryCode')}
          >
            {SHIPPING_COUNTRIES.map((country) => (
              <option key={country.code} value={country.code}>
                {country.name}
              </option>
            ))}
          </select>
          {errors.countryCode && <p className="text-xs text-red-600">{errors.countryCode.message}</p>}
        </div>

        <TextField
          label="Full name"
          autoComplete="name"
          required
          error={errors.fullName?.message}
          {...register('fullName')}
        />
        <TextField
          label="Address"
          autoComplete="address-line1"
          required
          error={errors.addressLine1?.message}
          {...register('addressLine1')}
        />
        <TextField
          label="Apartment, suite, etc. (optional)"
          autoComplete="address-line2"
          error={errors.addressLine2?.message}
          {...register('addressLine2')}
        />
        <TextField
          label="City"
          autoComplete="address-level2"
          required
          error={errors.city?.message}
          {...register('city')}
        />
        <div className="grid grid-cols-2 gap-3">
          <TextField
            label="State / Province"
            autoComplete="address-level1"
            required={STATE_REQUIRED_COUNTRIES.has(countryCode)}
            error={errors.state?.message}
            {...register('state')}
          />
          <TextField
            label="Postal code"
            autoComplete="postal-code"
            error={errors.postalCode?.message}
            {...register('postalCode')}
          />
        </div>
      </fieldset>
    </form>
  )
}
