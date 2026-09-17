"use client";

import { FormEvent, useState } from "react";
import { simulateSettlement } from "@/lib/simulationApi";
import {
  Currency,
  ReceivableType,
  SimulationRequest,
  SimulationResult,
} from "@/types";

export default function SimulationForm() {
  const [form, setForm] = useState<SimulationRequest>({
    assignor: "",
    type: "DUPLICATE_MERCANTILE",
    faceValue: 0,
    termInMonths: 1,
    dueDate: "",
    paymentCurrency: "BRL",
  });

  const [result, setResult] = useState<SimulationResult | null>(null);

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  function updateField(field: keyof SimulationRequest, value: string | number) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError("");
    setResult(null);

    if (form.faceValue <= 0) {
      setError("Informe um valor de face válido.");
      return;
    }

    if (form.termInMonths < 0) {
      setError("O prazo não pode ser negativo.");
      return;
    }

    if (!form.dueDate) {
      setError("Informe a data de vencimento.");
      return;
    }

    setLoading(true);

    try {
      const response = await simulateSettlement({
        ...form,
        dueDate:
          form.dueDate.length === 16 ? `${form.dueDate}:00` : form.dueDate,
      });

      setResult(response);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Não foi possível realizar a simulação.",
      );
    } finally {
      setLoading(false);
    }
  }

  const moneyBrl = new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  });

  const moneySettlement = new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: result?.paymentCurrency ?? "BRL",
  });

  return (
    <section className="mx-auto w-full max-w-2xl space-y-6">
      <form
        onSubmit={handleSubmit}
        className="space-y-4 rounded-lg border bg-white p-6 shadow"
      >
        <h1 className="text-2xl font-bold">Simular liquidação</h1>

        <div>
          <label className="block font-medium">Cedente</label>

          <input
            className="w-full rounded border p-2"
            value={form.assignor}
            onChange={(event) => updateField("assignor", event.target.value)}
            required
          />
        </div>

        <div>
          <label className="block font-medium">Tipo de recebível</label>

          <select
            className="w-full rounded border p-2"
            value={form.type}
            onChange={(event) =>
              updateField("type", event.target.value as ReceivableType)
            }
          >
            <option value="DUPLICATE_MERCANTILE">Duplicata Mercantil</option>

            <option value="PRE_DATED_CHECK">Cheque Pré-datado</option>
          </select>
        </div>

        <div>
          <label className="block font-medium">Valor de face</label>

          <input
            type="number"
            min="0.01"
            step="0.01"
            className="w-full rounded border p-2"
            value={form.faceValue || ""}
            onChange={(event) =>
              updateField("faceValue", Number(event.target.value))
            }
            required
          />
        </div>

        <div>
          <label className="block font-medium">Prazo em meses</label>

          <input
            type="number"
            min="0"
            step="1"
            className="w-full rounded border p-2"
            value={form.termInMonths}
            onChange={(event) =>
              updateField("termInMonths", Number(event.target.value))
            }
            required
          />
        </div>

        <div>
          <label className="block font-medium">Data de vencimento</label>

          <input
            type="datetime-local"
            className="w-full rounded border p-2"
            value={form.dueDate}
            onChange={(event) => updateField("dueDate", event.target.value)}
            required
          />
        </div>

        <div>
          <label className="block font-medium">Moeda de pagamento</label>

          <select
            className="w-full rounded border p-2"
            value={form.paymentCurrency}
            onChange={(event) =>
              updateField("paymentCurrency", event.target.value as Currency)
            }
          >
            <option value="BRL">BRL</option>
            <option value="USD">USD</option>
          </select>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded bg-indigo-600 px-4 py-2 text-white disabled:opacity-50"
        >
          {loading ? "Calculando..." : "Simular liquidação"}
        </button>
      </form>

      {error && <p className="rounded bg-red-100 p-3 text-red-700">{error}</p>}

      {result && (
        <div className="space-y-2 rounded-lg border bg-indigo-50 p-6">
          <h2 className="text-xl font-bold">Resultado da simulação</h2>

          <p>
            Valor de face: <strong>{moneyBrl.format(result.faceValue)}</strong>
          </p>

          <p>
            Valor presente em BRL:{" "}
            <strong>{moneyBrl.format(result.presentValueBrl)}</strong>
          </p>

          <p>
            Valor da liquidação:{" "}
            <strong>{moneySettlement.format(result.settledAmount)}</strong>
          </p>

          <p>Taxa base: {(result.baseRateUsed * 100).toFixed(2)}%</p>

          <p>Spread: {(result.spreadUsed * 100).toFixed(2)}%</p>

          {result.exchangeRateUsed !== null && (
            <p>Taxa de câmbio: {result.exchangeRateUsed.toFixed(4)}</p>
          )}
        </div>
      )}
    </section>
  );
}
