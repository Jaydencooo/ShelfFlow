import path from "node:path"

/** @type {import('next').NextConfig} */
const nextConfig = {
  outputFileTracingRoot: path.join(process.cwd(), "../.."),
  devIndicators: false,
  images: {
    unoptimized: true,
  },
}

export default nextConfig
