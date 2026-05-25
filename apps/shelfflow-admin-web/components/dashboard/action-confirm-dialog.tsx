"use client"

import type { ReactNode } from "react"

import { glassDialog, primaryGradient, primaryShadow } from "@/lib/glass-styles"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"

export interface ActionConfirmState {
  title: string
  description: string
  confirmLabel?: string
  onConfirm: () => void | Promise<void>
}

export function ActionConfirmDialog(props: {
  action: ActionConfirmState | null
  onOpenChange: (open: boolean) => void
  children?: ReactNode
}) {
  return (
    <AlertDialog open={Boolean(props.action)} onOpenChange={props.onOpenChange}>
      <AlertDialogContent className="border-0" style={glassDialog}>
        <AlertDialogHeader>
          <AlertDialogTitle>{props.action?.title ?? "确认操作"}</AlertDialogTitle>
          <AlertDialogDescription>{props.action?.description ?? "请确认是否继续。"}</AlertDialogDescription>
        </AlertDialogHeader>
        {props.children}
        <AlertDialogFooter>
          <AlertDialogCancel className="border-white/50 bg-white/30 hover:bg-white/50">取消</AlertDialogCancel>
          <AlertDialogAction
            className="border-0 text-white"
            onClick={() => {
              void props.action?.onConfirm()
            }}
            style={{ background: primaryGradient, boxShadow: primaryShadow }}
          >
            {props.action?.confirmLabel ?? "确认"}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
