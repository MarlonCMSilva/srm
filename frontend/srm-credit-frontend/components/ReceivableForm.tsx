"use client";

import { FormEvent, useState } from "react";
import { createReceivable } from "@/lib/receivableApi";
import { CreateReceivableRequest, ReceivableType } from "@/types";

interface Props {
  onCreated?: (id: number) => void;
}

export default function ReceivableForm({ onCreated }: Props) {
  const [form, setForm] = useState<CreateReceivableRequest>({
    assignor: "",
    type: "DUPLICATE_MERCANTILE",
    faceValue: 0,
    termInMonths: 1,
    dueDate: "",
  });

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  function updateField(
    field: keyof CreateReceivableRequest,
    value: string | number,
  ) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setMessage("");
    setError("");

    if (form.faceValue <= 0) {
      setError("O valor de face deve ser maior que zero.");
      return;
    }

    if (form.termInMonths < 0) {
      setError("O prazo não pode ser negativo.");
      return;
    }

    setLoading(true);

    try {
      const created = await createReceivable(form);

      setMessage(`Recebível criado com sucesso. ID: ${created.id}`);

      onCreated?.(created.id);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Não foi possível criar o recebível.",
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="mx-auto w-full max-w-2xl space-y-6">
      <form
        onSubmit={handleSubmit}
        className="max-w-xl space-y-4 rounded border p-6"
      >
        <h1 className="text-xl font-bold">Cadastro de Recebível</h1>

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
          <label className="block font-medium">Tipo</label>

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

        <button
          type="submit"
          disabled={loading}
          className="rounded bg-blue-600 px-4 py-2 text-white disabled:opacity-50"
        >
          {loading ? "Salvando..." : "Cadastrar recebível"}
        </button>

        {message && <p className="text-green-600">{message}</p>}

        {error && <p className="text-red-600">{error}</p>}
      </form>
    </section>
  );
}
