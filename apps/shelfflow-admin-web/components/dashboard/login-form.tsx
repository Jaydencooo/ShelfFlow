"use client"

import { zodResolver } from "@hookform/resolvers/zod"
import { useRouter } from "next/navigation"
import { useEffect, useState } from "react"
import { useForm } from "react-hook-form"
import { LockKeyhole, User2 } from "lucide-react"

import { DASHBOARD_DEFAULT_ROUTE } from "@/lib/constants"
import { loginRequest } from "@/lib/client/api"
import { glassCard, glassInputClassName, primaryGradient, primaryShadow } from "@/lib/glass-styles"
import type { LoginFormValues } from "@/lib/validation"
import { loginSchema } from "@/lib/validation"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { Spinner } from "@/components/ui/spinner"

export function LoginForm() {
  const router = useRouter()
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [isHydrated, setIsHydrated] = useState(false)
  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: "",
      password: ""
    }
  })

  useEffect(() => {
    setIsHydrated(true)
  }, [])

  const onSubmit = form.handleSubmit(async (values) => {
    setSubmitError(null)

    try {
      await loginRequest(values)
      router.replace(DASHBOARD_DEFAULT_ROUTE)
      router.refresh()
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "登录失败，请稍后重试")
    }
  })

  return (
    <Card className="border-0" style={glassCard}>
      <CardHeader className="space-y-3">
        <div
          className="inline-flex h-12 w-12 items-center justify-center rounded-xl text-white"
          style={{ background: primaryGradient, boxShadow: primaryShadow }}
        >
          <LockKeyhole className="h-5 w-5" />
        </div>
        <div className="space-y-1">
          <CardTitle className="text-2xl font-semibold text-slate-900">登录 ShelfFlow 管理端</CardTitle>
          <CardDescription className="text-slate-600">使用运营账号访问临期库存运营台。</CardDescription>
        </div>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form className="space-y-5" data-hydrated={isHydrated ? "true" : "false"} data-testid="admin-login-form" noValidate onSubmit={onSubmit}>
            <FormField
              control={form.control}
              name="username"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>账号</FormLabel>
                  <FormControl>
                    <div className="relative">
                      <User2 className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                      <Input
                        autoComplete="username"
                        className={`${glassInputClassName} pl-9`}
                        data-testid="admin-login-username"
                        placeholder="请输入运营账号"
                        {...field}
                      />
                    </div>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="password"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>密码</FormLabel>
                  <FormControl>
                    <Input
                      autoComplete="current-password"
                      className={glassInputClassName}
                      data-testid="admin-login-password"
                      placeholder="请输入密码"
                      type="password"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            {submitError ? (
              <Alert variant="destructive">
                <AlertDescription>{submitError}</AlertDescription>
              </Alert>
            ) : null}

            <Button
              className="w-full border-0 text-white hover:opacity-90"
              data-testid="admin-login-submit"
              disabled={form.formState.isSubmitting}
              style={{ background: primaryGradient, boxShadow: primaryShadow }}
              type="submit"
            >
              {form.formState.isSubmitting ? (
                <>
                  <Spinner className="mr-2" />
                  登录中
                </>
              ) : (
                "登录"
              )}
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  )
}
