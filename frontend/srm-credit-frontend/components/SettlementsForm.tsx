"use client";

import { useEffect, useState } from "react";

import { getReceivables } from "@/lib/receivableApi";
import { createSettlement } from "@/lib/settlementApi";

import { Currency, Receivable, Settlement } from "@/types";

export default function SettlementForm() {
  const [receivables, setReceivables] = useState<Receivable[]>([]);

  const [paymentCurrency, setPaymentCurrency] = useState<Currency>("BRL");

  const [settlement, setSettlement] = useState<Settlement | null>(null);

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [settlingId, setSettlingId] = useState<number | null>(null);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/immutability
    loadReceivables();
  }, []);

  async function loadReceivables() {
    setLoading(true);
    setError("");

    try {
      const response = await getReceivables();

      setReceivables(response.content);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Não foi possível carregar os recebíveis.",
      );
    } finally {
      setLoading(false);
    }
  }

  async function handleSettlement(receivableId: number) {
    setError("");
    setSettlement(null);
    setSettlingId(receivableId);

    try {
      const response = await createSettlement({
        receivableId,
        paymentCurrency,
      });

      setSettlement(response);

      // Atualiza a tabela localmente sem precisar
      // fazer uma nova chamada imediatamente.
      setReceivables((current) =>
        current.map((receivable) =>
          receivable.id === receivableId
            ? {
                ...receivable,
                status: "SETTLED",
              }
            : receivable,
        ),
      );
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Não foi possível liquidar o recebível.",
      );
    } finally {
      setSettlingId(null);
    }
  }

  const moneyBrl = new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  });

  const moneySettlement = new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: settlement?.paymentCurrency ?? "BRL",
  });

  return (
    <section className="mx-auto w-full max-w-6xl space-y-6">
      <div className="rounded-lg border bg-white p-6 shadow">
        <div className="mb-6 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-2xl font-bold">Liquidação de recebíveis</h1>

            <p className="text-sm text-gray-600">
              Selecione um recebível disponível para realizar a liquidação.
            </p>
          </div>

          <div className="w-full md:w-48">
            <label className="mb-1 block font-medium">Moeda de pagamento</label>

            <select
              className="w-full rounded border p-2"
              value={paymentCurrency}
              onChange={(event) =>
                setPaymentCurrency(event.target.value as Currency)
              }
            >
              <option value="BRL">BRL</option>
              <option value="USD">USD</option>
            </select>
          </div>
        </div>

        {loading && <p className="text-gray-600">Carregando recebíveis...</p>}

        {!loading && error && (
          <p className="rounded bg-red-100 p-3 text-red-700">{error}</p>
        )}

        {!loading && !error && receivables.length === 0 && (
          <p className="rounded bg-gray-100 p-4 text-gray-700">
            Nenhum recebível encontrado.
          </p>
        )}

        {!loading && receivables.length > 0 && (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[800px] border-collapse">
              <thead>
                <tr className="border-b bg-gray-100 text-left">
                  <th className="p-3">ID</th>
                  <th className="p-3">Cedente</th>
                  <th className="p-3">Tipo</th>
                  <th className="p-3">Valor de face</th>
                  <th className="p-3">Prazo</th>
                  <th className="p-3">Vencimento</th>
                  <th className="p-3">Status</th>
                  <th className="p-3">Ação</th>
                </tr>
              </thead>

              <tbody>
                {receivables.map((receivable) => {
                  const isSettling = settlingId === receivable.id;

                  const isAvailable = receivable.status === "AVAILABLE";

                  return (
                    <tr
                      key={receivable.id}
                      className="border-b hover:bg-gray-50"
                    >
                      <td className="p-3">{receivable.id}</td>

                      <td className="p-3">{receivable.assignor}</td>

                      <td className="p-3">
                        {formatReceivableType(receivable.type)}
                      </td>

                      <td className="p-3">
                        {moneyBrl.format(receivable.faceValue)}
                      </td>

                      <td className="p-3">{receivable.termInMonths} mês(es)</td>

                      <td className="p-3">{formatDate(receivable.dueDate)}</td>

                      <td className="p-3">
                        <span
                          className={
                            isAvailable
                              ? "rounded bg-green-100 px-2 py-1 text-sm text-green-700"
                              : "rounded bg-gray-200 px-2 py-1 text-sm text-gray-700"
                          }
                        >
                          {receivable.status}
                        </span>
                      </td>

                      <td className="p-3">
                        <button
                          type="button"
                          disabled={!isAvailable || isSettling}
                          onClick={() => handleSettlement(receivable.id)}
                          className="rounded bg-green-600 px-3 py-2 text-sm text-white hover:bg-green-700 disabled:cursor-not-allowed disabled:bg-gray-400"
                        >
                          {isSettling
                            ? "Liquidando..."
                            : isAvailable
                              ? "Liquidar"
                              : "Indisponível"}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {settlement && (
        <div className="rounded-lg border border-green-300 bg-green-50 p-6">
          <h2 className="mb-3 text-xl font-bold text-green-800">
            Liquidação realizada
          </h2>

          <p>
            ID da liquidação: <strong>{settlement.id}</strong>
          </p>

          <p>
            Recebível: <strong>{settlement.receivableId ?? "-"}</strong>
          </p>

          <p>
            Valor liquidado:{" "}
            <strong>{moneySettlement.format(settlement.settledAmount)}</strong>
          </p>

          <p>
            Moeda: <strong>{settlement.paymentCurrency}</strong>
          </p>

          <p>
            Data: <strong>{formatDateTime(settlement.createdAt)}</strong>
          </p>
        </div>
      )}
    </section>
  );
}

function formatReceivableType(type: Receivable["type"]): string {
  if (type === "DUPLICATE_MERCANTILE") {
    return "Duplicata mercantil";
  }

  return "Cheque pré-datado";
}

function formatDate(value: string): string {
  if (!value) {
    return "-";
  }

  return new Intl.DateTimeFormat("pt-BR").format(new Date(value));
}

function formatDateTime(value: string): string {
  if (!value) {
    return "-";
  }

  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(value));
}
