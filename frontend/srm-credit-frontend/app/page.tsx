import Link from "next/link";

export default function HomePage() {
  return (
    <main className="space-y-4 p-8">
      <section className="mx-auto w-full max-w-2xl space-y-6">
        <h1 className="text-2xl font-bold">SRM Credit Engine</h1>

        <nav className="flex gap-4">
          <Link
            href="/receivables/new"
            className="rounded bg-blue-600 px-4 py-2 text-white"
          >
            Novo recebível
          </Link>

          <Link
            href="/simulations"
            className="rounded bg-indigo-600 px-4 py-2 text-white"
          >
            Simulação
          </Link>

          <Link
            href="/settlements"
            className="rounded bg-green-600 px-4 py-2 text-white"
          >
            Liquidação
          </Link>
        </nav>
      </section>
    </main>
  );
}
