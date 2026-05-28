import { readFile } from "node:fs/promises"
import path from "node:path"

import { NextResponse } from "next/server"

const PRODUCT_IMAGE_UPLOAD_DIR = "uploads/products"
const PRODUCT_IMAGE_CONTENT_TYPES: Record<string, string> = {
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".png": "image/png",
  ".webp": "image/webp",
}

function resolveAdminUploadPath(fileName: string) {
  const normalizedFileName = path.basename(fileName)
  const extension = path.extname(normalizedFileName).toLowerCase()
  const contentType = PRODUCT_IMAGE_CONTENT_TYPES[extension]

  if (!contentType) {
    return null
  }

  return {
    contentType,
    filePath: path.resolve(process.cwd(), "../shelfflow-admin-web/public", PRODUCT_IMAGE_UPLOAD_DIR, normalizedFileName),
  }
}

export async function GET(_request: Request, context: { params: Promise<{ fileName: string }> }) {
  const { fileName } = await context.params
  const resolvedImage = resolveAdminUploadPath(fileName)

  if (!resolvedImage) {
    return new NextResponse(null, { status: 404 })
  }

  try {
    const image = await readFile(resolvedImage.filePath)
    return new NextResponse(image, {
      headers: {
        "Cache-Control": "public, max-age=31536000, immutable",
        "Content-Type": resolvedImage.contentType,
      },
    })
  } catch {
    return new NextResponse(null, { status: 404 })
  }
}
