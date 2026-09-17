import { SimulationRequest, SimulationResult } from "@/types";

import { apiFetch } from "./api";

export function simulateSettlement(
  data: SimulationRequest,
): Promise<SimulationResult> {
  return apiFetch<SimulationResult>("/settlements/simulation", {
    method: "POST",
    body: JSON.stringify(data),
  });
}
