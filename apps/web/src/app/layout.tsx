import type { Metadata } from "next";
import { IBM_Plex_Mono, Manrope } from "next/font/google";
import Link from "next/link";

import { ThemeSwitcher } from "@/components/ThemeSwitcher";

import "./globals.css";

const manrope = Manrope({
  subsets: ["latin"],
  variable: "--font-manrope",
  display: "swap",
});

const plexMono = IBM_Plex_Mono({
  subsets: ["latin"],
  weight: ["400", "500"],
  variable: "--font-plex-mono",
  display: "swap",
});

export const metadata: Metadata = {
  title: "FinanceTarget — planos com direção",
  description: "Planejamento financeiro orientado a metas, com premissas transparentes.",
};

const themeInitializer = `
  try {
    const saved = localStorage.getItem('financetarget-theme');
    const theme = saved === 'light' || saved === 'dark'
      ? saved
      : (matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
    document.documentElement.dataset.theme = theme;
  } catch (_) {}
`;

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="pt-BR" suppressHydrationWarning>
      <head>
        <script dangerouslySetInnerHTML={{ __html: themeInitializer }} />
      </head>
      <body className={`${manrope.variable} ${plexMono.variable}`}>
        <a className="skip-link" href="#conteudo-principal">
          Ir para o conteúdo
        </a>
        <header className="site-header">
          <Link className="wordmark" href="/" aria-label="FinanceTarget, página inicial">
            FinanceTarget<span aria-hidden="true">.</span>
          </Link>
          <ThemeSwitcher />
        </header>
        {children}
      </body>
    </html>
  );
}
